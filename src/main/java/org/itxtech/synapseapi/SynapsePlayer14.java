package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockNoteblock;
import cn.nukkit.entity.data.ShortEntityData;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.event.player.*;
import cn.nukkit.level.GameRules;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.PunchBlockParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.*;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Binary;
import cn.nukkit.utils.DummyBossBar;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.TextFormat;
import com.google.gson.JsonPrimitive;
import org.itxtech.synapseapi.event.player.SynapsePlayerBroadcastLevelSoundEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerConnectEvent;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12NetEase;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12Urgency;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.*;
import org.itxtech.synapseapi.multiprotocol.utils.LevelSoundEventEnum;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Optional;

public class SynapsePlayer14 extends SynapsePlayer {

	public SynapsePlayer14(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
	}

	public void handleLoginPacket(PlayerLoginPacket packet) {
		if (!this.isSynapseLogin) {
			super.handleDataPacket(SynapseAPI.getInstance().getPacket(packet.cachedLoginPacket));
			return;
		}
		this.isFirstTimeLogin = packet.isFirstTime;
		this.cachedExtra = packet.extra;
		SynapsePlayerConnectEvent ev;
		this.server.getPluginManager().callEvent(ev = new SynapsePlayerConnectEvent(this, this.isFirstTimeLogin));
		if (!ev.isCancelled()) {
			this.protocol = packet.protocol;

			try {
				DataPacket pk = PacketRegister.getFullPacket(packet.cachedLoginPacket, packet.protocol);
				if (pk instanceof org.itxtech.synapseapi.multiprotocol.protocol14.protocol.LoginPacket14) {
					((org.itxtech.synapseapi.multiprotocol.protocol14.protocol.LoginPacket14) pk).isFirstTimeLogin = packet.isFirstTime;
					((org.itxtech.synapseapi.multiprotocol.protocol14.protocol.LoginPacket14) pk).username = packet.extra.get("username").getAsString();
					((org.itxtech.synapseapi.multiprotocol.protocol14.protocol.LoginPacket14) pk).clientUUID = packet.uuid;
					((org.itxtech.synapseapi.multiprotocol.protocol14.protocol.LoginPacket14) pk).xuid = packet.extra.get("xuid").getAsString();
					this.isNetEaseClient = Optional.ofNullable(packet.extra.get("netease")).orElseGet(() -> new JsonPrimitive(false)).getAsBoolean();
				}
				this.handleDataPacket(pk);
			} catch (Exception e) {
				MainLogger.getLogger().logException(e);
				this.close();
			}
		}
	}

	@Override
	public void handleDataPacket(DataPacket packet) {
		if (!this.isSynapseLogin) {
			super.handleDataPacket(packet);
			return;
		}
		packetswitch:
		switch (packet.pid()) {
			// 1.4协议的PID仍然相同
			case ProtocolInfo.LOGIN_PACKET:
				if (!this.callPacketReceiveEvent(((LoginPacket14) packet).toDefault())) break;

				if (this.loggedIn) {
					break;
				}

				LoginPacket14 loginPacket = (LoginPacket14) packet;

				String message;
				if (loginPacket.getProtocol() < ProtocolInfo.CURRENT_PROTOCOL) {
					if (loginPacket.getProtocol() < ProtocolInfo.CURRENT_PROTOCOL) {
						message = "disconnectionScreen.outdatedClient";

						this.sendPlayStatus(PlayStatusPacket.LOGIN_FAILED_CLIENT);
					} else {
						message = "disconnectionScreen.outdatedServer";

						this.sendPlayStatus(PlayStatusPacket.LOGIN_FAILED_SERVER);
					}
					this.close("", message, false);
					break;
				}

				this.protocol = loginPacket.protocol;
				this.username = TextFormat.clean(loginPacket.username);
				this.originName = this.username;
				this.displayName = this.username;
				this.iusername = this.username.toLowerCase();
				this.setDataProperty(new StringEntityData(DATA_NAMETAG, this.username), false);

				setLoginChainData(ClientChainData12NetEase.of(loginPacket.getBuffer()));
				if (this.loginChainData.getClientUUID() != null) { // 网易认证通过！
					this.isNetEaseClient = true;
					this.getServer().getLogger().notice(this.username + TextFormat.RED + " 中国版验证通过！");
				} else { // 国际版普通认证
					try {
						this.getServer().getLogger().notice(this.username + TextFormat.YELLOW + " 正在解析为国际版！");
						setLoginChainData(ClientChainData12.of(loginPacket.getBuffer()));
					} catch (Exception e) {
						this.getServer().getLogger()
								.notice(this.username + TextFormat.RED + " 解析时出现问题，采用紧急解析方案！" + e.getMessage());
						setLoginChainData(ClientChainData12Urgency.of(loginPacket.getBuffer()));
					}
				}
				if (this.server.getOnlinePlayers().size() >= this.server.getMaxPlayers()
						&& this.kick(PlayerKickEvent.Reason.SERVER_FULL, "disconnectionScreen.serverFull", false)) {
					break;
				}

				this.randomClientId = loginPacket.clientId;

				this.uuid = loginPacket.clientUUID;
				this.rawUUID = Binary.writeUUID(this.uuid);

				boolean valid = true;
				int len = loginPacket.username.length();
				if (len > 16 || len < 2) {
					valid = false;
				}

				/*
				 * for (int i = 0; i < len && valid; i++) { char c =
				 * loginPacket.username.charAt(i); if ((c >= 'a' && c <= 'z') || (c >= 'A' && c
				 * <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == ' ' ) { continue; }
				 *
				 * valid = false; break; }
				 */

				if (!valid || Objects.equals(this.iusername, "rcon") || Objects.equals(this.iusername, "console")) {
					this.close("", "disconnectionScreen.invalidName");

					break;
				}

				if (!loginPacket.skin.isValid()) {
					this.close("", "disconnectionScreen.invalidSkin");
					break;
				} else {
					this.setSkin(loginPacket.getSkin());
				}

				PlayerPreLoginEvent playerPreLoginEvent;
				this.server.getPluginManager()
						.callEvent(playerPreLoginEvent = new PlayerPreLoginEvent(this, "Plugin reason"));
				if (playerPreLoginEvent.isCancelled()) {
					this.close("", playerPreLoginEvent.getKickMessage());

					break;
				}

				Player playerInstance = this;
				this.preLoginEventTask = new AsyncTask() {

					private PlayerAsyncPreLoginEvent e;

					@Override
					public void onRun() {
						e = new PlayerAsyncPreLoginEvent(playerInstance, username, uuid, getAddress(), getPort());
						server.getPluginManager().callEvent(e);
					}

					@Override
					public void onCompletion(Server server) {
						playerInstance.completePreLoginEventTask(e);
					}
				};

				this.server.getScheduler().scheduleAsyncTask(this.preLoginEventTask);

				this.processLogin();
				break;

			case ProtocolInfo.PLAYER_ACTION_PACKET:
				PlayerActionPacket14 playerActionPacket = (PlayerActionPacket14) packet;

				if (!this.callPacketReceiveEvent(((PlayerActionPacket14) packet).toDefault())) break;

				if (!this.spawned || (!this.isAlive() && playerActionPacket.action != PlayerActionPacket14.ACTION_RESPAWN && playerActionPacket.action != PlayerActionPacket14.ACTION_DIMENSION_CHANGE_REQUEST)) {
					break;
				}

				playerActionPacket.entityId = this.id;
				Vector3 pos = new Vector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z);
				BlockFace face = BlockFace.fromIndex(playerActionPacket.face);

				switch (playerActionPacket.action) {
					case PlayerActionPacket14.ACTION_START_BREAK:

						if (this.lastBreak != Long.MAX_VALUE || pos.distanceSquared(this) > 100) {
							break;
						}
						Block target = this.level.getBlock(pos);
						PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(this, this.inventory.getItemInHand(), target, face, target.getId() == 0 ? PlayerInteractEvent.Action.LEFT_CLICK_AIR : PlayerInteractEvent.Action.LEFT_CLICK_BLOCK);
						this.getServer().getPluginManager().callEvent(playerInteractEvent);
						if (playerInteractEvent.isCancelled()) {
							this.inventory.sendHeldItem(this);
							break;
						}
						if (target.getId() == Block.NOTEBLOCK) {
							((BlockNoteblock) target).emitSound();
							break;
						}
						Block block = target.getSide(face);
						if (block.getId() == Block.FIRE) {
							this.level.setBlock(block, new BlockAir(), true);
							break;
						}
						if (!this.isCreative()) {
							//improved this to take stuff like swimming, ladders, enchanted tools into account, fix wrong tool break time calculations for bad tools (pmmp/PocketMine-MP#211)
							//Done by lmlstarqaq
							double breakTime = Math.ceil(target.getBreakTime(this.inventory.getItemInHand(), this) * 20);
							if (breakTime > 0) {
								LevelEventPacket pk = new LevelEventPacket();
								pk.evid = LevelEventPacket.EVENT_BLOCK_START_BREAK;
								pk.x = (float) pos.x;
								pk.y = (float) pos.y;
								pk.z = (float) pos.z;
								pk.data = (int) (65535 / breakTime);
								this.getLevel().addChunkPacket(pos.getFloorX() >> 4, pos.getFloorZ() >> 4, pk);
							}
						}

						this.breakingBlock = target;
						this.lastBreak = System.currentTimeMillis();
						break;

					case PlayerActionPacket14.ACTION_ABORT_BREAK:
						this.lastBreak = Long.MAX_VALUE;
						this.breakingBlock = null;
					case PlayerActionPacket14.ACTION_STOP_BREAK:
						LevelEventPacket pk = new LevelEventPacket();
						pk.evid = LevelEventPacket.EVENT_BLOCK_STOP_BREAK;
						pk.x = (float) pos.x;
						pk.y = (float) pos.y;
						pk.z = (float) pos.z;
						pk.data = 0;
						this.getLevel().addChunkPacket(pos.getFloorX() >> 4, pos.getFloorZ() >> 4, pk);
						this.breakingBlock = null;
						break;
					case PlayerActionPacket14.ACTION_GET_UPDATED_BLOCK:
						break; //TODO
					case PlayerActionPacket14.ACTION_DROP_ITEM:
						break; //TODO
					case PlayerActionPacket14.ACTION_STOP_SLEEPING:
						this.stopSleep();
						break;
					case PlayerActionPacket14.ACTION_RESPAWN:
						if (!this.spawned || this.isAlive() || !this.isOnline()) {
							break;
						}

						if (this.server.isHardcore()) {
							this.setBanned(true);
							break;
						}

						this.craftingType = CRAFTING_SMALL;
						this.resetCraftingGridType();

						PlayerRespawnEvent playerRespawnEvent = new PlayerRespawnEvent(this, this.getSpawn());
						this.server.getPluginManager().callEvent(playerRespawnEvent);

						Position respawnPos = playerRespawnEvent.getRespawnPosition();

						this.teleport(respawnPos, null);

						RespawnPacket respawnPacket = new RespawnPacket();
						respawnPacket.x = (float) respawnPos.x;
						respawnPacket.y = (float) respawnPos.y;
						respawnPacket.z = (float) respawnPos.z;
						this.dataPacket(respawnPacket);

						this.setSprinting(false);
						this.setSneaking(false);

						this.extinguish();
						this.setDataProperty(new ShortEntityData(Player.DATA_AIR, 400), false);
						this.deadTicks = 0;
						this.noDamageTicks = 60;

						this.removeAllEffects();
						this.setHealth(this.getMaxHealth());
						this.getFoodData().setLevel(20, 20);

						this.sendData(this);

						this.setMovementSpeed(DEFAULT_SPEED);

						this.getAdventureSettings().update();
						this.inventory.sendContents(this);
						this.inventory.sendArmorContents(this);

						this.spawnToAll();
						this.scheduleUpdate();
						break;
					case PlayerActionPacket14.ACTION_JUMP:
						break packetswitch;
					case PlayerActionPacket14.ACTION_START_SPRINT:
						PlayerToggleSprintEvent playerToggleSprintEvent = new PlayerToggleSprintEvent(this, true);
						this.server.getPluginManager().callEvent(playerToggleSprintEvent);
						if (playerToggleSprintEvent.isCancelled()) {
							this.sendData(this);
						} else {
							this.setSprinting(true);
						}
						this.formWindows.clear();
						break packetswitch;
					case PlayerActionPacket14.ACTION_STOP_SPRINT:
						playerToggleSprintEvent = new PlayerToggleSprintEvent(this, false);
						this.server.getPluginManager().callEvent(playerToggleSprintEvent);
						if (playerToggleSprintEvent.isCancelled()) {
							this.sendData(this);
						} else {
							this.setSprinting(false);
						}
						break packetswitch;
					case PlayerActionPacket14.ACTION_START_SNEAK:
						PlayerToggleSneakEvent playerToggleSneakEvent = new PlayerToggleSneakEvent(this, true);
						this.server.getPluginManager().callEvent(playerToggleSneakEvent);
						if (playerToggleSneakEvent.isCancelled()) {
							this.sendData(this);
						} else {
							this.setSneaking(true);
						}
						break packetswitch;
					case PlayerActionPacket14.ACTION_STOP_SNEAK:
						playerToggleSneakEvent = new PlayerToggleSneakEvent(this, false);
						this.server.getPluginManager().callEvent(playerToggleSneakEvent);
						if (playerToggleSneakEvent.isCancelled()) {
							this.sendData(this);
						} else {
							this.setSneaking(false);
						}
						break packetswitch;
					case PlayerActionPacket14.ACTION_DIMENSION_CHANGE_ACK:
						break; //TODO
					case PlayerActionPacket14.ACTION_START_GLIDE:
						PlayerToggleGlideEvent playerToggleGlideEvent = new PlayerToggleGlideEvent(this, true);
						this.server.getPluginManager().callEvent(playerToggleGlideEvent);
						if (playerToggleGlideEvent.isCancelled()) {
							this.sendData(this);
						} else {
							this.setGliding(true);
						}
						break packetswitch;
					case PlayerActionPacket14.ACTION_STOP_GLIDE:
						playerToggleGlideEvent = new PlayerToggleGlideEvent(this, false);
						this.server.getPluginManager().callEvent(playerToggleGlideEvent);
						if (playerToggleGlideEvent.isCancelled()) {
							this.sendData(this);
						} else {
							this.setGliding(false);
						}
						break packetswitch;
					case PlayerActionPacket14.ACTION_CONTINUE_BREAK:
						if (this.isBreakingBlock()) {
							block = this.level.getBlock(pos);
							this.level.addParticle(new PunchBlockParticle(pos, block, face));
						}
						break;
					case PlayerActionPacket14.ACTION_START_SWIMMING:
						this.setSwimming(true);
						break;
					case PlayerActionPacket14.ACTION_STOP_SWIMMING:
						this.setSwimming(false);
						break;
				}

				this.startAction = -1;
				this.setDataFlag(Player.DATA_FLAGS, Player.DATA_FLAG_ACTION, false);
				break;
            case ProtocolInfo.TEXT_PACKET:
				if (!this.callPacketReceiveEvent(((TextPacket14) packet).toDefault())) break;
                if (!this.spawned || !this.isAlive()) {
                    break;
                }

                TextPacket14 textPacket = (TextPacket14) packet;

                if (textPacket.type == TextPacket.TYPE_CHAT) {
                    this.chat(textPacket.message);
                }
                break;
			case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				LevelSoundEventPacket levelSoundEventPacket = (LevelSoundEventPacket) packet;
				SynapsePlayerBroadcastLevelSoundEvent event = new SynapsePlayerBroadcastLevelSoundEvent(this,
						LevelSoundEventEnum.fromV14(levelSoundEventPacket.sound),
						new Vector3(levelSoundEventPacket.x, levelSoundEventPacket.y, levelSoundEventPacket.z),
						levelSoundEventPacket.extraData,
						levelSoundEventPacket.pitch,
						"minecraft:player",
						levelSoundEventPacket.isBabyMob,
						levelSoundEventPacket.isGlobal);
				this.getServer().getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
					this.getLevel().getChunkPlayers(this.getFloorX() >> 4, this.getFloorZ() >> 4).values().stream()
							.filter(p -> p instanceof SynapsePlayer)
							.forEach(p -> ((SynapsePlayer) p).sendLevelSoundEvent(
									event.getLevelSound(),
									event.getPos(),
									event.getExtraData(),
									event.getPitch(),
									event.getEntityIdentifier(),
									event.isBabyMob(),
									event.isGlobal()
							));
				}
				break;
			default:
				super.handleDataPacket(packet);
		}

	}

	@Override
	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket14 startGamePacket = new StartGamePacket14();
		startGamePacket.entityUniqueId = Long.MAX_VALUE;
		startGamePacket.entityRuntimeId = Long.MAX_VALUE;
		startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
		startGamePacket.x = (float) this.x;
		startGamePacket.y = (float) this.y;
		startGamePacket.z = (float) this.z;
		startGamePacket.yaw = (float) this.yaw;
		startGamePacket.pitch = (float) this.pitch;
		startGamePacket.seed = -1;
		startGamePacket.dimension = (byte) (this.level.getDimension() & 0xff);
		startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
		startGamePacket.difficulty = this.server.getDifficulty();
		startGamePacket.spawnX = (int) spawnPosition.x;
		startGamePacket.spawnY = (int) spawnPosition.y;
		startGamePacket.spawnZ = (int) spawnPosition.z;
		startGamePacket.hasAchievementsDisabled = true;
		startGamePacket.dayCycleStopTime = -1;
		startGamePacket.eduMode = false;
		startGamePacket.rainLevel = 0;
		startGamePacket.lightningLevel = 0;
		startGamePacket.commandsEnabled = this.isEnableClientCommand();
		startGamePacket.levelId = "";
		startGamePacket.worldName = this.getServer().getNetwork().getName();
		startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
		startGamePacket.gameRules = getSupportedRules();

		return startGamePacket;
	}

	@Override
	public void sendPopup(String message, String sendersXUID) {
		TextPacket14 pk = new TextPacket14();
		pk.type = TextPacket14.TYPE_POPUP;
		pk.sendersXUID = sendersXUID;
		pk.message = message;
		this.dataPacket(pk);
	}

	@Override
	public void sendChat(String source, String message) {
		TextPacket14 pk = new TextPacket14();
		pk.type = TextPacket14.TYPE_CHAT;
		pk.sendersXUID = source;
		pk.message = this.server.getLanguage().translateString(message);
		this.dataPacket(pk);
	}

	@Override
	public void sendTip(String message) {
		TextPacket14 pk = new TextPacket14();
		pk.type = TextPacket14.TYPE_TIP;
		pk.message = message;
		this.dataPacket(pk);
	}
	
	public void sendTranslation(String message, String[] parameters) {
        TextPacket14 pk = new TextPacket14();
        if (!this.server.isLanguageForced()) {
            pk.type = TextPacket14.TYPE_TRANSLATION;
			pk.isLocalized = true;
            pk.message = this.server.getLanguage().translateString(message, parameters, "nukkit.");
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = this.server.getLanguage().translateString(parameters[i], parameters, "nukkit.");

            }
            pk.parameters = parameters;
        } else {
            pk.type = TextPacket14.TYPE_RAW;
            pk.message = this.server.getLanguage().translateString(message, parameters);
        }
        this.dataPacket(pk);
    }
	
	@Override
    public void sendMessage(String message) {
        TextPacket14 pk = new TextPacket14();
        pk.type = TextPacket14.TYPE_RAW;
        pk.message = this.server.getLanguage().translateString(message);
        this.dataPacket(pk);
    }

	protected GameRules getSupportedRules() {
		return this.level.gameRules;
	}

	/**
	 * Returns a client-friendly gamemode of the specified real gamemode This
	 * function takes care of handling gamemodes known to MCPE (as of 1.1.0.3, that
	 * includes Survival, Creative and Adventure)
	 * <p>
	 * TODO: remove this when Spectator Mode gets added properly to MCPE
	 */
	private static int getClientFriendlyGamemode(int gamemode) {
		gamemode &= 0x03;
		if (gamemode == Player.SPECTATOR) {
			return Player.CREATIVE;
		}
		return gamemode;
	}

	@Override
	protected DataPacket generateResourcePackInfoPacket() {
		ResourcePacksInfoPacket14 resoucePacket = new ResourcePacksInfoPacket14();
		resoucePacket.resourcePackEntries = this.resourcePacks.values().toArray(new ResourcePack[0]);
		resoucePacket.behaviourPackEntries = this.behaviourPacks.values().toArray(new ResourcePack[0]);
		resoucePacket.mustAccept = this.forceResources;
		return resoucePacket;
	}

	public void forceReteleport() {
		this.teleportPosition = this.getPosition();
        this.getDummyBossBars().values().forEach(DummyBossBar::destroy);  //游戏崩溃问题
        this.usedChunks.clear();
        this.isLevelChange = true;
        //this.nextAllowSendTop = System.currentTimeMillis() + 2000;  //2秒后才运行发送Top
        this.nextChunkOrderRun = 10000;

        ChangeDimensionPacket changeDimensionPacket1 = new ChangeDimensionPacket();
        changeDimensionPacket1.dimension = 2;
        changeDimensionPacket1.x = (float) this.getX();
        changeDimensionPacket1.y = (float) this.getY() + this.getEyeHeight();
        changeDimensionPacket1.z = (float) this.getZ();
        this.dataPacket(changeDimensionPacket1);

        this.forceSendEmptyChunks();
        this.getServer().getScheduler().scheduleDelayedTask(() -> {
            PlayStatusPacket statusPacket0 = new PlayStatusPacket();
            statusPacket0.status = PlayStatusPacket.PLAYER_SPAWN;
            dataPacket(statusPacket0);
        }, 10);

        this.getServer().getScheduler().scheduleDelayedTask(() -> {
            ChangeDimensionPacket changeDimensionPacket = new ChangeDimensionPacket();
            changeDimensionPacket.dimension = 0;
            changeDimensionPacket.x = (float) this.getX();
            changeDimensionPacket.y = (float) this.getY() + this.getEyeHeight();
            changeDimensionPacket.z = (float) this.getZ();
            dataPacket(changeDimensionPacket);
            nextChunkOrderRun = 0;
            sendPosition(getPosition(), yaw, pitch, MovePlayerPacket.MODE_RESET);
        }, 20);
    }

    @Override
	public void sendLevelSoundEvent(LevelSoundEventEnum levelSound, Vector3 pos, int extraData, int pitch, String entityIdentifier, boolean isBabyMob, boolean isGlobal) {
		if (levelSound == null || levelSound.getV14() == -1) return;
		LevelSoundEventPacket pk = new LevelSoundEventPacket();
		pk.sound = levelSound.getV14();
		pk.x = (float) pos.x;
		pk.y = (float) pos.y;
		pk.z = (float) pos.z;
		pk.extraData = levelSound.translateTo14ExtraData(extraData);
		pk.pitch = pitch;
		pk.isBabyMob = isBabyMob;
		pk.isGlobal = isGlobal;
		this.dataPacket(pk);
	}

}

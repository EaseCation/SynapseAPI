package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockNoteblock;
import cn.nukkit.entity.data.ShortEntityData;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.event.player.*;
import cn.nukkit.item.Item;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.level.GameRules;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.PunchBlockParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Mth;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.PacketViolationReason;
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
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.common.BuildPlatform;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.PlayerActionPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12NetEase;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12Urgency;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.MoveEntityAbsolutePacket15;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;
import org.itxtech.synapseapi.utils.ClientChainDataXbox;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class SynapsePlayer14 extends SynapsePlayer {
	private static final boolean DEBUG_ENVIRONMENT = Boolean.getBoolean("easecation.debugging");

	public SynapsePlayer14(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
	}

	public void handleLoginPacket(PlayerLoginPacket packet) {
		if (!this.isSynapseLogin) {
			super.handleDataPacket(SynapseAPI.getPacket(packet.cachedLoginPacket));
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
					if (packet.extra.has("titleId")) ((LoginPacket14) pk).titleId = packet.extra.get("titleId").getAsString();
					if (packet.extra.has("sandboxId")) ((LoginPacket14) pk).sandboxId = packet.extra.get("sandboxId").getAsString();
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
					onPacketViolation(PacketViolationReason.ALREADY_LOGGED_IN);
					break;
				}

				LoginPacket14 loginPacket = (LoginPacket14) packet;

				this.protocol = loginPacket.protocol;

				if (loginPacket.username != null) {
					this.username = TextFormat.clean(loginPacket.username);
					this.originName = this.username;
					this.displayName = this.username;
					this.iusername = this.username.toLowerCase();
				}

				if (loginPacket.getProtocol() < AbstractProtocol.FIRST_ALLOW_LOGIN_PROTOCOL.getProtocolStart()) {
					this.sendPlayStatus(PlayStatusPacket.LOGIN_FAILED_CLIENT);
					this.close("", "disconnectionScreen.outdatedClient");
					break;
				}
				if (loginPacket.getProtocol() > AbstractProtocol.LAST_ALLOW_LOGIN_PROTOCOL.getProtocolStart()) {
					this.sendPlayStatus(PlayStatusPacket.LOGIN_FAILED_SERVER);
					this.close("", "disconnectionScreen.outdatedServer");
					break;
				}

				this.setDataProperty(new StringEntityData(DATA_NAMETAG, this.username), false);

				setLoginChainData(ClientChainData12NetEase.of(loginPacket.getBuffer()));
				if (this.loginChainData.getClientUUID() != null) { // 网易认证通过！
					this.isNetEaseClient = true;
					this.getServer().getLogger().notice(this.username + TextFormat.RED + " 中国版验证通过！");
				} else if (DEBUG_ENVIRONMENT && !ClientChainDataXbox.of(loginPacket.getBuffer()).isXboxAuthed() && username.startsWith("netease")) { // 国际版验证失败, 特定前缀玩家名解析为中国版 (仅限调试环境)
					this.isNetEaseClient = true;
					this.getServer().getLogger().notice(this.username + TextFormat.RED + " Xbox验证未通过！");
				} else { // 国际版普通认证
					try {
						this.getServer().getLogger().notice(this.username + TextFormat.YELLOW + " 正在解析为国际版！");
						setLoginChainData(ClientChainData12.of(loginPacket.getBuffer()));
					} catch (Exception e) {
						this.getServer().getLogger()
								.notice(this.username + TextFormat.RED + " 解析时出现问题，采用紧急解析方案！", e);
						setLoginChainData(ClientChainData12Urgency.of(loginPacket.getBuffer()));
					}
				}

				if (isNetEaseClient()) {
					int buildPlatform = loginChainData.getDeviceOS();
					switch (buildPlatform) {
						case BuildPlatform.GOOGLE:
						case BuildPlatform.IOS:
							break;
						case BuildPlatform.WIN32:
							// ModPC
							break;
						default:
							onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "ce_os" + buildPlatform);
							return;
					}

					if ("Nintendo Switch".equals(loginChainData.getDeviceModel())) {
						onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "ce_dm_nx");
						return;
					}
				} else {
					int buildPlatform = loginChainData.getDeviceOS();
					switch (buildPlatform) {
						case BuildPlatform.DEDICATED:
						case BuildPlatform.OSX:
							onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "me_os" + buildPlatform);
							return;
						default:
							if (buildPlatform <= 0) {
								onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "me_os" + buildPlatform);
								return;
							}
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

				this.server.getScheduler().scheduleAsyncTask(SynapseAPI.getInstance(), this.preLoginEventTask);

				this.processLogin();
				break;

			case ProtocolInfo.PLAYER_ACTION_PACKET:
				PlayerActionPacket14 playerActionPacket = (PlayerActionPacket14) packet;

				if (!this.callPacketReceiveEvent(((PlayerActionPacket14) packet).toDefault())) break;

				if (!this.spawned || (!this.isAlive() && playerActionPacket.action != PlayerActionPacket14.ACTION_RESPAWN /*&& playerActionPacket.action != PlayerActionPacket14.ACTION_DIMENSION_CHANGE_REQUEST*/)) {
					break;
				}

				playerActionPacket.entityId = this.id;
				Vector3 pos = new Vector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z);

				switch (playerActionPacket.action) {
					case PlayerActionPacket14.ACTION_START_BREAK:
						if (isServerAuthoritativeBlockBreakingEnabled()) {
							onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action0");
							return;
						}

						if (this.isSpectator()) {
							break;
						}

						long currentBreak = System.currentTimeMillis();
						BlockVector3 currentBreakPosition = new BlockVector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z);
						// HACK: Client spams multiple left clicks so we need to skip them.
						if ((lastBreakPosition.equalsVec(currentBreakPosition) && (currentBreak - this.lastBreak) < 10) || pos.distanceSquared(this) > 100) {
							break;
						}

						Block target = this.level.getBlock(pos, false);
						BlockFace face = BlockFace.fromIndex(playerActionPacket.data);

						PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(this, this.inventory.getItemInHand(), target, face, target.getId() == BlockID.AIR ? PlayerInteractEvent.Action.LEFT_CLICK_AIR : PlayerInteractEvent.Action.LEFT_CLICK_BLOCK);
						this.getServer().getPluginManager().callEvent(playerInteractEvent);
						if (playerInteractEvent.isCancelled()) {
							this.inventory.sendHeldItem(this);
							break;
						}

						if (/*!isAdventure() &&*/ target.getId() == Block.NOTEBLOCK) {
							((BlockNoteblock) target).emitSound();
							break;
						}

						Block block = target.getSide(face);
						if (block.isFire()) {
							this.level.setBlock(block, Block.get(Block.AIR), true);
							this.level.addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_EXTINGUISH_FIRE);
							break;
						}

						if (!this.isCreative()) {
							//improved this to take stuff like swimming, ladders, enchanted tools into account, fix wrong tool break time calculations for bad tools (pmmp/PocketMine-MP#211)
							//Done by lmlstarqaq
							double breakTime = Mth.ceil(target.getBreakTime(this.inventory.getItemInHand(), this) * 20);
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
						this.lastBreak = currentBreak;
						this.lastBreakPosition = currentBreakPosition;
						break;
					case PlayerActionPacket14.ACTION_STOP_BREAK:
					case PlayerActionPacket14.ACTION_ABORT_BREAK:
						if (isServerAuthoritativeBlockBreakingEnabled()) {
							onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action2");
							return;
						}

						if (pos.distanceSquared(this) < 100) { // same as with ACTION_START_BREAK
							LevelEventPacket pk = new LevelEventPacket();
							pk.evid = LevelEventPacket.EVENT_BLOCK_STOP_BREAK;
							pk.x = (float) pos.x;
							pk.y = (float) pos.y;
							pk.z = (float) pos.z;
							pk.data = 0;
							this.getLevel().addChunkPacket(pos.getChunkX(), pos.getChunkZ(), pk);
						}
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
						if (isSneaking()) {
							this.setSneaking(false);
						}
						if (isGliding()) {
							this.setGliding(false);
						}
						if (isSwimming()) {
							this.setSwimming(false);
						}
						if (isCrawling()) {
							this.setCrawling(false);
						}

						this.setDataProperty(new ShortEntityData(Player.DATA_AIR, 300), false);
						this.deadTicks = 0;
						this.noDamageTicks = 60;

						this.removeAllEffects();

						SetHealthPacket healthPacket = new SetHealthPacket();
						healthPacket.health = getMaxHealth();
						this.dataPacket(healthPacket);

						this.setHealth(this.getMaxHealth());
						this.getFoodData().setLevel(20, 20);

						this.sendData(this);
						this.sendData(this.getViewers().values().toArray(new Player[0]));

						this.setMovementSpeed(DEFAULT_SPEED);

						this.getAdventureSettings().update();
						this.inventory.sendContents(this);
						this.inventory.sendArmorContents(this);
						this.offhandInventory.sendContents(this);

						this.spawnToAll();
						this.scheduleUpdate();
						break;
					case PlayerActionPacket14.ACTION_JUMP:
						if (isServerAuthoritativeMovementEnabled()) {
							onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action8");
							return;
						}

						PlayerJumpEvent playerJumpEvent = new PlayerJumpEvent(this);
						this.server.getPluginManager().callEvent(playerJumpEvent);
						break packetswitch;
					case PlayerActionPacket14.ACTION_START_SPRINT:
						if (isServerAuthoritativeMovementEnabled()) {
							onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action9");
							return;
						}

						if (isSprinting()) {
							break packetswitch;
						}

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
						if (isServerAuthoritativeMovementEnabled()) {
							onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action10");
							return;
						}

						if (!isSprinting()) {
							break packetswitch;
						}

						playerToggleSprintEvent = new PlayerToggleSprintEvent(this, false);
						this.server.getPluginManager().callEvent(playerToggleSprintEvent);
						if (playerToggleSprintEvent.isCancelled()) {
							this.sendData(this);
						} else {
							this.setSprinting(false);
						}
						break packetswitch;
					case PlayerActionPacket14.ACTION_START_SNEAK:
						if (isServerAuthoritativeMovementEnabled()) {
							onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action11");
							return;
						}

						if (isSneaking()) {
							break packetswitch;
						}

						PlayerToggleSneakEvent playerToggleSneakEvent = new PlayerToggleSneakEvent(this, true);
						this.server.getPluginManager().callEvent(playerToggleSneakEvent);
						if (playerToggleSneakEvent.isCancelled()) {
							this.sendData(this);
						} else {
							this.setSneaking(true);
						}
						break packetswitch;
					case PlayerActionPacket14.ACTION_STOP_SNEAK:
						if (isServerAuthoritativeMovementEnabled()) {
							onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action12");
							return;
						}

						if (!isSneaking()) {
							break packetswitch;
						}

						playerToggleSneakEvent = new PlayerToggleSneakEvent(this, false);
						this.server.getPluginManager().callEvent(playerToggleSneakEvent);
						if (playerToggleSneakEvent.isCancelled()) {
							this.sendData(this);
						} else {
							this.setSneaking(false);
						}
						break packetswitch;
					case PlayerActionPacket14.ACTION_DIMENSION_CHANGE_ACK:
						this.onDimensionChangeSuccess();
						break;
					case PlayerActionPacket14.ACTION_START_GLIDE:
						if (isServerAuthoritativeMovementEnabled()) {
							onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action15");
							return;
						}

						if (isGliding()) {
							break packetswitch;
						}

						PlayerToggleGlideEvent playerToggleGlideEvent = new PlayerToggleGlideEvent(this, true);
						Item chestplate = getInventory().getChestplate();
						if (chestplate.getId() != Item.ELYTRA || chestplate.getDamage() >= chestplate.getMaxDurability() - 1) {
							playerToggleGlideEvent.setCancelled();
						}
						this.server.getPluginManager().callEvent(playerToggleGlideEvent);
						if (playerToggleGlideEvent.isCancelled()) {
							this.sendData(this);
						} else {
							this.setGliding(true);
						}
						break packetswitch;
					case PlayerActionPacket14.ACTION_STOP_GLIDE:
						if (isServerAuthoritativeMovementEnabled()) {
							onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action16");
							return;
						}

						if (!isGliding()) {
							break packetswitch;
						}

						playerToggleGlideEvent = new PlayerToggleGlideEvent(this, false);
						this.server.getPluginManager().callEvent(playerToggleGlideEvent);
						if (playerToggleGlideEvent.isCancelled()) {
							this.sendData(this);
						} else {
							this.setGliding(false);
						}
						break packetswitch;
					case PlayerActionPacket14.ACTION_CONTINUE_BREAK:
						if (isServerAuthoritativeBlockBreakingEnabled()) {
							onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action18");
							return;
						}

						if (this.isBreakingBlock()) {
							block = this.level.getBlock(pos, false);
							face = BlockFace.fromIndex(playerActionPacket.data);
							this.level.addParticle(new PunchBlockParticle(pos, block, face));
						}
						break;
					case PlayerActionPacket14.ACTION_START_SWIMMING:
						if (isServerAuthoritativeMovementEnabled()) {
							onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action21");
							return;
						}

						if (isSwimming()) {
							break;
						}

						PlayerToggleSwimEvent playerToggleSwimEvent = new PlayerToggleSwimEvent(this, true);
						this.server.getPluginManager().callEvent(playerToggleSwimEvent);
						if (playerToggleSwimEvent.isCancelled()) {
							this.sendData(this);
						} else {
							this.setSwimming(true);
						}
						break;
					case PlayerActionPacket14.ACTION_STOP_SWIMMING:
						if (isServerAuthoritativeMovementEnabled()) {
							onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action22");
							return;
						}

						if (!isSwimming()) {
							break;
						}

						playerToggleSwimEvent = new PlayerToggleSwimEvent(this, false);
						this.server.getPluginManager().callEvent(playerToggleSwimEvent);
						if (playerToggleSwimEvent.isCancelled()) {
							this.sendData(this);
						} else {
							this.setSwimming(false);
						}
						break;
				}

				this.setUsingItem(false);
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
			/*case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				LevelSoundEventPacket levelSoundEventPacket = (LevelSoundEventPacket) packet;
				LevelSoundEventEnum sound = LevelSoundEventEnum.fromV14(levelSoundEventPacket.sound);
				SynapsePlayerBroadcastLevelSoundEvent event = new SynapsePlayerBroadcastLevelSoundEvent(this,
						sound,
						new Vector3(levelSoundEventPacket.x, levelSoundEventPacket.y, levelSoundEventPacket.z),
						sound.translateExtraDataFromClient(levelSoundEventPacket.extraData, AbstractProtocol.fromRealProtocol(getProtocol()), isNetEaseClient()),
						levelSoundEventPacket.pitch,
						EntityFullNames.PLAYER,
						levelSoundEventPacket.isBabyMob,
						levelSoundEventPacket.isGlobal);
				if (this.isSpectator()) {
					event.setCancelled();
				}
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
				break;*/
			case ProtocolInfo.MOVE_ACTOR_ABSOLUTE_PACKET:
				if (this.getProtocol() >= AbstractProtocol.PROTOCOL_15.getProtocolStart()) {
					MoveEntityAbsolutePacket15 moveEntityAbsolutePacket = (MoveEntityAbsolutePacket15) packet;
					if (!validateCoordinate((float) moveEntityAbsolutePacket.x) || !validateCoordinate((float) moveEntityAbsolutePacket.y) || !validateCoordinate((float) moveEntityAbsolutePacket.z)
							|| !validateFloat((float) moveEntityAbsolutePacket.pitch) || !validateFloat((float) moveEntityAbsolutePacket.yaw) || !validateFloat((float) moveEntityAbsolutePacket.headYaw)) {
						this.getServer().getLogger().warning("Invalid vehicle movement received: " + this.getName());
						this.close("", "Invalid vehicle movement");
						return;
					}

					if (this.riding == null || this.riding.getId() != moveEntityAbsolutePacket.eid) {
						break;
					}

					if (this.riding instanceof EntityBoat) {
						if (this.temporalVector.setComponents(moveEntityAbsolutePacket.x, moveEntityAbsolutePacket.y, moveEntityAbsolutePacket.z).distanceSquared(this.riding) < 1000) {
							((EntityBoat) this.riding).onInput(moveEntityAbsolutePacket.x, moveEntityAbsolutePacket.y, moveEntityAbsolutePacket.z, moveEntityAbsolutePacket.yaw);
						}
					}
				}
				break;
			default:
				super.handleDataPacket(packet);
		}

	}

	@Override
	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket14 startGamePacket = new StartGamePacket14();
		startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
		startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
		startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
		startGamePacket.x = (float) this.x;
		startGamePacket.y = (float) this.y;
		startGamePacket.z = (float) this.z;
		startGamePacket.yaw = (float) this.yaw;
		startGamePacket.pitch = (float) this.pitch;
		startGamePacket.seed = -1;
		startGamePacket.dimension = (byte) (this.level.getDimension().ordinal() & 0xff);
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
		startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
		return startGamePacket;
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
        changeDimensionPacket1.y = (float) this.getY() + this.getBaseOffset();
        changeDimensionPacket1.z = (float) this.getZ();
        this.dataPacket(changeDimensionPacket1);

        this.forceSendEmptyChunks();

		if (getProtocol() >= AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
			PlayerActionPacket119 ackPacket = new PlayerActionPacket119();
			ackPacket.action = PlayerActionPacket.ACTION_DIMENSION_CHANGE_ACK;
			ackPacket.entityId = getId();
			ackPacket.x = getFloorX();
			ackPacket.y = getFloorY();
			ackPacket.z = getFloorZ();
			ackPacket.resultX = ackPacket.x;
			ackPacket.resultY = ackPacket.y;
			ackPacket.resultZ = ackPacket.z;
			dataPacket(ackPacket);
		}

        this.getServer().getScheduler().scheduleDelayedTask(SynapseAPI.getInstance(), () -> {
            PlayStatusPacket statusPacket0 = new PlayStatusPacket();
            statusPacket0.status = PlayStatusPacket.PLAYER_SPAWN;
            dataPacket(statusPacket0);
        }, 10);

        this.getServer().getScheduler().scheduleDelayedTask(SynapseAPI.getInstance(), () -> {
            ChangeDimensionPacket changeDimensionPacket = new ChangeDimensionPacket();
            changeDimensionPacket.dimension = 0;
            changeDimensionPacket.x = (float) this.getX();
            changeDimensionPacket.y = (float) this.getY() + this.getBaseOffset();
            changeDimensionPacket.z = (float) this.getZ();
            dataPacket(changeDimensionPacket);

            nextChunkOrderRun = 0;

            sendPosition(getPosition(), yaw, pitch, MovePlayerPacket.MODE_RESPAWN);

			if (getProtocol() >= AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
				PlayerActionPacket119 ackPacket = new PlayerActionPacket119();
				ackPacket.action = PlayerActionPacket.ACTION_DIMENSION_CHANGE_ACK;
				ackPacket.entityId = getId();
				ackPacket.x = getFloorX();
				ackPacket.y = getFloorY();
				ackPacket.z = getFloorZ();
				ackPacket.resultX = ackPacket.x;
				ackPacket.resultY = ackPacket.y;
				ackPacket.resultZ = ackPacket.z;
				dataPacket(ackPacket);
			}
        }, 20);
    }

    /*@Override
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
	}*/

	@Override
	public void sendJukeboxPopup(TranslationContainer message) {
		TextPacket14 pk = new TextPacket14();
		pk.type = TextPacket14.JUKE_BOX_POPUP;
		pk.isLocalized = true;
		pk.message = message.getText();
		pk.parameters = Arrays.stream(message.getParameters()).map(String::valueOf).toArray(String[]::new);
		this.dataPacket(pk);
	}
}

package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.PlayerFood;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerRespawnEvent;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.level.GameRules;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.*;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.TextFormat;
import com.google.gson.*;
import org.itxtech.synapseapi.event.player.SynapsePlayerBroadcastLevelSoundEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerConnectEvent;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.LoginPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.*;
import org.itxtech.synapseapi.network.protocol.mod.EncryptedPacket;
import org.itxtech.synapseapi.network.protocol.mod.ServerSubPacketHandler;
import org.itxtech.synapseapi.network.protocol.mod.SubPacket;
import org.itxtech.synapseapi.network.protocol.mod.SubPacketHandler;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static cn.nukkit.SharedConstants.RESOURCE_PACK_CHUNK_SIZE;
import static org.itxtech.synapseapi.SynapseSharedConstants.NETWORK_STACK_LATENCY_TELEMETRY;

public class SynapsePlayer16 extends SynapsePlayer14 {

	protected boolean spawnStatusSent;

	protected long pingNs;
	protected long latencyNs;

	private ServerSubPacketHandler subPacketHandler;

	public SynapsePlayer16(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
	}

	@Override
	public void handleLoginPacket(PlayerLoginPacket packet) {
		if (!this.isSynapseLogin) {
			super.handleDataPacket(SynapseAPI.getPacket(packet.cachedLoginPacket));
			return;
		}
		this.isFirstTimeLogin = packet.isFirstTime;
		this.cachedExtra = packet.extra;
		// 从上一个服务器传递过来的dummyDimension，用于发送子区块的时候使用
		if (this.cachedExtra != null && this.cachedExtra.has("dummyDimension")) {
			this.dummyDimension = this.cachedExtra.get("dummyDimension").getAsInt();
			this.getServer().getLogger().debug("[DummyDimension] 从上一服务端收到玩家 " + Optional.ofNullable(packet.extra).map(extra -> extra.get("username")).map(JsonElement::getAsString).orElse("null") + " 的dummyDimension: " + this.dummyDimension);
		}
		SynapsePlayerConnectEvent ev;
		this.server.getPluginManager().callEvent(ev = new SynapsePlayerConnectEvent(this, this.isFirstTimeLogin));
		if (!ev.isCancelled()) {
			this.protocol = packet.protocol;

			try {
				DataPacket pk = packet.decodedLoginPacket;
				if (pk instanceof LoginPacket14) {
					((LoginPacket14) pk).isFirstTimeLogin = packet.isFirstTime;
					((LoginPacket14) pk).username = packet.extra.get("username").getAsString();
					((LoginPacket14) pk).clientUUID = packet.uuid;
					((LoginPacket14) pk).xuid = packet.extra.get("xuid").getAsString();
					if (packet.extra.has("titleId")) ((LoginPacket14) pk).titleId = packet.extra.get("titleId").getAsString();
					if (packet.extra.has("sandboxId")) ((LoginPacket14) pk).sandboxId = packet.extra.get("sandboxId").getAsString();
					this.isNetEaseClient = Optional.ofNullable(packet.extra.get("netease")).orElseGet(() -> new JsonPrimitive(false)).getAsBoolean();
				}
				if (pk == null) {
					close();
				} else {
					this.handleDataPacket(pk);
				}
			} catch (Exception e) {
				MainLogger.getLogger().logException(e);
				this.close();
			}
		}

		if (isNetEaseClient() && subPacketHandler == null) {
			subPacketHandler = new BaseSubPacketHandler(this);
		}
	}

	@Override
	public void handleDataPacket(DataPacket packet) {
		if (!this.isSynapseLogin) {
			super.handleDataPacket(packet);
			return;
		}
		switch (packet.pid()) {
			case ProtocolInfo.RESOURCE_PACK_CLIENT_RESPONSE_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				ResourcePackClientResponsePacket16 responsePacket = (ResourcePackClientResponsePacket16) packet;
				switch (responsePacket.responseStatus) {
					case ResourcePackClientResponsePacket.STATUS_REFUSED:
						this.close("", "disconnectionScreen.noReason");
						break;
					case ResourcePackClientResponsePacket.STATUS_SEND_PACKS:
						for (ResourcePackClientResponsePacket16.Entry entry : responsePacket.packEntries) {
							ResourcePack resourcePack = this.server.getResourcePackManager().getPackById(entry.uuid);
							if (resourcePack == null) {
								this.close("", "disconnectionScreen.resourcePack");
								break;
							}

							ResourcePackDataInfoPacket dataInfoPacket = new ResourcePackDataInfoPacket();
							dataInfoPacket.packId = resourcePack.getPackId();
							dataInfoPacket.maxChunkSize = RESOURCE_PACK_CHUNK_SIZE;
							dataInfoPacket.chunkCount = resourcePack.getChunkCount();
							dataInfoPacket.compressedPackSize = resourcePack.getPackSize();
							dataInfoPacket.sha256 = resourcePack.getSha256();
							if (resourcePack.getPackType().equals("resources")) {
								dataInfoPacket.type = ResourcePackDataInfoPacket.TYPE_RESOURCE;
							} else if (resourcePack.getPackType().equals("data")) {
								dataInfoPacket.type = ResourcePackDataInfoPacket.TYPE_BEHAVIOR;
							}
							this.dataPacket(dataInfoPacket);
						}
						break;
					case ResourcePackClientResponsePacket.STATUS_HAVE_ALL_PACKS:
						ResourcePackStackPacket16 stackPacket = new ResourcePackStackPacket16();
						stackPacket.mustAccept = this.forceResources;
						stackPacket.resourcePackStack = this.server.getResourcePackManager().getResourceStack();
						this.dataPacket(stackPacket);
						break;
					case ResourcePackClientResponsePacket.STATUS_COMPLETED:
						if (this.preLoginEventTask.isFinished()) {
							this.completeLoginSequence();
						} else {
							this.shouldLogin = true;
						}
						break;
				}
				break;
			/*case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				LevelSoundEventPacket16 levelSoundEventPacket = (LevelSoundEventPacket16) packet;
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
			case ProtocolInfo.PACKET_PY_RPC:
				if (!callPacketReceiveEvent(packet)) break;
				NEPyRpcPacket16 pyRpcPacket = (NEPyRpcPacket16) packet;
				if (subPacketHandler == null) {
					break;
				}
				for (SubPacket<SubPacketHandler> subPacket : pyRpcPacket.subPackets) {
					try {
						subPacket.handle(subPacketHandler);
					} catch (Exception e) {
						getServer().getLogger().error("Unable to handle netease rpc sub packet: " + getName(), e);
					}
				}
				/*
				NetEasePlayerPyRpcReceiveEvent pyRpcReceiveEvent = new NetEasePlayerPyRpcReceiveEvent(this, pyRpcPacket.data);
				Server.getInstance().getPluginManager().callEvent(pyRpcReceiveEvent);
				//Try decode ModEventC2S
				Value data = pyRpcReceiveEvent.getData();
				try {
					if (data.isMapValue()) {
						String json = data.toJson();
						JsonObject obj = GSON.fromJson(json, JsonObject.class);
						if (obj.has("value") && obj.get("value").isJsonArray()) {
							JsonArray value0 = obj.get("value").getAsJsonArray();
							if ("ModEventC2S".equals(value0.get(0).getAsString()) && value0.get(1).isJsonObject()) {
								JsonObject obj1 = value0.get(1).getAsJsonObject();
								if (obj1.has("value") && obj1.get("value").isJsonArray()) {
									JsonArray value1 = obj1.get("value").getAsJsonArray();
									String modName = value1.get(0).getAsString();
									String systemName = value1.get(1).getAsString();
									String eventName = value1.get(2).getAsString();
									JsonObject eventData = value1.get(3).getAsJsonObject();
									NetEasePlayerModEventC2SEvent modEventC2SEvent = new NetEasePlayerModEventC2SEvent(
											this,
											modName,
											systemName,
											eventName,
											eventData
									);
									Server.getInstance().getPluginManager().callEvent(modEventC2SEvent);
								}
							} else if ("StoreBuySuccServerEvent".equals(value0.get(0).getAsString())) {
								SynapsePlayerNetEaseStoreBuySuccEvent ev = new SynapsePlayerNetEaseStoreBuySuccEvent(this);
								Server.getInstance().getPluginManager().callEvent(ev);
							}
						}
					} else if (data.isArrayValue()) {
						String json = data.toJson();
						JsonArray array = GSON.fromJson(json, JsonArray.class);
						if (array.size() >= 1 && array.get(0).isJsonPrimitive()) {
							String type = array.get(0).getAsString();
							if ("ModEventC2S".equals(type) && array.size() >= 2 && array.get(1).isJsonArray()) {
								JsonArray value0 = array.get(1).getAsJsonArray();
								String modName = value0.get(0).getAsString();
								String systemName = value0.get(1).getAsString();
								String eventName = value0.get(2).getAsString();
								JsonObject eventData = value0.get(3).getAsJsonObject();
								NetEasePlayerModEventC2SEvent modEventC2SEvent = new NetEasePlayerModEventC2SEvent(
										this,
										modName,
										systemName,
										eventName,
										eventData
								);
								Server.getInstance().getPluginManager().callEvent(modEventC2SEvent);
							} else if ("StoreBuySuccServerEvent".equals(type)) {
								SynapsePlayerNetEaseStoreBuySuccEvent ev = new SynapsePlayerNetEaseStoreBuySuccEvent(this);
								Server.getInstance().getPluginManager().callEvent(ev);
							}
						}
					}
				} catch (Exception e) {
					//ignore
				}*/
				break;
			case ProtocolInfo.SET_LOCAL_PLAYER_AS_INITIALIZED_PACKET:
				if (!callPacketReceiveEvent(packet)) {
					break;
				}
				this.locallyInitialized = true;
				break;
			case ProtocolInfo.NETWORK_STACK_LATENCY_PACKET:
				if (!callPacketReceiveEvent(packet)) {
					break;
				}
				if (NETWORK_STACK_LATENCY_TELEMETRY) {
					latencyNs = System.nanoTime() - pingNs;
					ping();
				}
				break;
			default:
				super.handleDataPacket(packet);
				break;
		}

	}

	@Override
	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket16 startGamePacket = new StartGamePacket16();
		startGamePacket.netease = this.isNetEaseClient();
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

	@Override
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
		ResourcePacksInfoPacket16 resoucePacket = new ResourcePacksInfoPacket16();
		resoucePacket.resourcePackEntries = this.resourcePacks.values().toArray(new ResourcePack[0]);
		resoucePacket.behaviourPackEntries = this.behaviourPacks.values().toArray(new ResourcePack[0]);
		resoucePacket.mustAccept = this.forceResources;
		return resoucePacket;
	}

	@Override
	protected void doFirstSpawn() {
		if (this.spawnStatusSent) {
			return;
		}
		this.spawnStatusSent = true;
		this.spawned = true;

		this.inventory.sendContents(this);
		this.inventory.sendArmorContents(this);
		this.offhandInventory.sendContents(this);

		this.sendPotionEffects(this);
		this.sendData(this);

		Position pos = this.level.getSafeSpawn(this);

		PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(this, pos);

		this.server.getPluginManager().callEvent(respawnEvent);

		pos = respawnEvent.getRespawnPosition();

		if (this.getHealth() < 1) {
			pos = this.getSpawn();
		}

		this.firstRespawn(pos);

		this.sendPlayStatus(PlayStatusPacket.PLAYER_SPAWN);

		PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(this,
				new TranslationContainer(TextFormat.YELLOW + "%multiplayer.player.joined", this.getDisplayName())
		);

		this.server.getPluginManager().callEvent(playerJoinEvent);

		if (!playerJoinEvent.getJoinMessage().toString().trim().isEmpty()) {
			this.server.broadcastMessage(playerJoinEvent.getJoinMessage());
		}

		this.noDamageTicks = 60;

		this.sendRecipeList();
		this.sendTrimRecipes();

		this.sendCreativeContents();

		for (long index : this.usedChunks.keySet()) {
			int chunkX = Level.getHashX(index);
			int chunkZ = Level.getHashZ(index);
			for (Entity entity : this.level.getChunkEntities(chunkX, chunkZ).values()) {
				if (this != entity && !entity.closed && entity.isAlive()) {
					entity.spawnTo(this);
				}
			}
		}

		int experience = this.getExperience();
		if (experience != 0) {
			this.sendExperience(experience);
		}

		int level = this.getExperienceLevel();
		if (level != 0) {
			this.sendExperienceLevel(this.getExperienceLevel());
		}

		this.teleport(pos, null); // Prevent PlayerTeleportEvent during player spawn

		if (!this.isSpectator()) {
			this.spawnToAll();
		}

		//Weather
		this.getLevel().sendWeather(this);

		//FoodLevel
		PlayerFood food = this.getFoodData();
		if (food.getLevel() != food.getMaxLevel()) {
			food.sendFoodLevel();
		}
	}

	/*@Override
	public void sendLevelSoundEvent(LevelSoundEventEnum levelSound, Vector3 pos, int extraData, int pitch, String entityIdentifier, boolean isBabyMob, boolean isGlobal) {
		if (levelSound == null || levelSound.getV14() == -1) return;
		LevelSoundEventPacket pk = new LevelSoundEventPacket();
		pk.sound = levelSound.getV14();
		pk.x = (float) pos.x;
		pk.y = (float) pos.y;
		pk.z = (float) pos.z;
		pk.extraData = levelSound.translateTo16ExtraData(extraData, AbstractProtocol.fromRealProtocol(this.protocol), this.isNetEaseClient);
		pk.pitch = pitch;
		pk.isBabyMob = isBabyMob;
		pk.isGlobal = isGlobal;
		this.dataPacket(pk);
	}*/

	@Override
	public void sendPyRpcData(Value data) {
		NEPyRpcPacket16 pk = new NEPyRpcPacket16();
		pk.data = data;
		this.dataPacket(pk);
	}

	@Override
	public void modNotifyToClient(String modName, String systemName, String eventName, MapValue eventData) {
		ArrayValue data = ValueFactory.newArray(
				ValueFactory.newBinary("ModEventS2C".getBytes(StandardCharsets.UTF_8)),
				ValueFactory.newArray(
						ValueFactory.newBinary(modName.getBytes(StandardCharsets.UTF_8)),
						ValueFactory.newBinary(systemName.getBytes(StandardCharsets.UTF_8)),
						ValueFactory.newBinary(eventName.getBytes(StandardCharsets.UTF_8)),
						eventData
				),
				ValueFactory.newNil()
		);
		this.sendPyRpcData(data);
	}

	@Override
	public void modNotifyToClientEncrypted(String modName, String systemName, String eventName, String data, Function<String, String> encMethod) {
		NEPyRpcPacket16 pk = new NEPyRpcPacket16();
		pk.subPackets = new SubPacket[]{new EncryptedPacket(modName, systemName, eventName, data, encMethod)};
		pk.encrypt = true;
		this.dataPacket(pk);
	}

	@Override
	public void ping() {
		long time = System.nanoTime();
		pingNs = time;

		NetworkStackLatencyPacket16 packet = new NetworkStackLatencyPacket16();
		packet.timestamp = time;
		dataPacket(packet);
	}

	@Override
	public long getLatency() {
		return latencyNs;
	}

	protected void sendTrimRecipes() {
		// 1.20+
	}

	@Override
	public void setSubPacketHandler(ServerSubPacketHandler handler) {
		subPacketHandler = handler;
	}
}

package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.PlayerFood;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerRespawnEvent;
import cn.nukkit.inventory.FurnaceRecipe;
import cn.nukkit.inventory.Recipe;
import cn.nukkit.inventory.ShapedRecipe;
import cn.nukkit.inventory.ShapelessRecipe;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.level.GameRules;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.TextFormat;
import org.itxtech.synapseapi.event.player.SynapsePlayerBroadcastLevelSoundEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerConnectEvent;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.*;
import org.itxtech.synapseapi.multiprotocol.utils.LevelSoundEventEnum;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;

import java.util.zip.Deflater;

public class SynapsePlayer16 extends SynapsePlayer14 {

	private final static BatchPacket craftingDataPacket;

	static {
		CraftingDataPacket16 pk = new CraftingDataPacket16();
		pk.cleanRecipes = true;

		for (Recipe recipe : Server.getInstance().getCraftingManager().getRecipes()) {
			if (recipe instanceof ShapedRecipe) {
				pk.addShapedRecipe((ShapedRecipe) recipe);
			} else if (recipe instanceof ShapelessRecipe) {
				pk.addShapelessRecipe((ShapelessRecipe) recipe);
			}
		}

		for (FurnaceRecipe recipe : Server.getInstance().getCraftingManager().getFurnaceRecipes().values()) {
			pk.addFurnaceRecipe(recipe);
		}
		pk.encode();

		craftingDataPacket = pk.compress(Deflater.BEST_COMPRESSION);
	}

	public SynapsePlayer16(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, String ip, int port) {
		super(interfaz, synapseEntry, clientID, ip, port);
	}

	public void handleLoginPacket(PlayerLoginPacket packet) {
		if (!this.isSynapseLogin) {
			super.handleDataPacket(SynapseAPI.getInstance().getPacket(packet.cachedLoginPacket));
			return;
		}
		this.isFirstTimeLogin = packet.isFirstTime;
		SynapsePlayerConnectEvent ev;
		this.server.getPluginManager().callEvent(ev = new SynapsePlayerConnectEvent(this, this.isFirstTimeLogin));
		if (!ev.isCancelled()) {
			this.protocol = packet.protocol;

			try {
				DataPacket pk = PacketRegister.getFullPacket(packet.cachedLoginPacket, packet.protocol);
				if (pk instanceof LoginPacket14) {
					((LoginPacket14) pk).isFirstTimeLogin = packet.isFirstTime;
					((LoginPacket14) pk).username = packet.username;
					((LoginPacket14) pk).clientUUID = packet.uuid;
					((LoginPacket14) pk).xuid = packet.xuid;
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
			case ProtocolInfo.RESOURCE_PACK_CLIENT_RESPONSE_PACKET:
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
							dataInfoPacket.maxChunkSize = 1048576; //megabyte
							dataInfoPacket.chunkCount = resourcePack.getPackSize() / dataInfoPacket.maxChunkSize;
							dataInfoPacket.compressedPackSize = resourcePack.getPackSize();
							dataInfoPacket.sha256 = resourcePack.getSha256();
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
						this.completeLoginSequence();
						break;
				}
				break;
			case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET:
				LevelSoundEventPacket16 levelSoundEventPacket = (LevelSoundEventPacket16) packet;
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
		StartGamePacket16 startGamePacket = new StartGamePacket16();
		startGamePacket.netease = this.isNetEaseClient();
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

	protected void doFirstSpawn() {

		this.setEnableClientCommand(true);
		this.getAdventureSettings().update();

		this.sendPotionEffects(this);
		this.sendData(this);

		SetTimePacket setTimePacket = new SetTimePacket();
		setTimePacket.time = this.level.getTime();
		this.dataPacket(setTimePacket);

		Position pos = this.level.getSafeSpawn(this);

		PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(this, pos);

		this.server.getPluginManager().callEvent(respawnEvent);

		pos = respawnEvent.getRespawnPosition();

		if (this.getHealth() <= 0) {
			RespawnPacket respawnPacket = new RespawnPacket();
			pos = this.getSpawn();
			respawnPacket.x = (float) pos.x;
			respawnPacket.y = (float) pos.y + this.getEyeHeight();
			respawnPacket.z = (float) pos.z;
			this.dataPacket(respawnPacket);
		} else {
			RespawnPacket respawnPacket = new RespawnPacket();
			respawnPacket.x = (float) pos.x;
			respawnPacket.y = (float) pos.y + this.getEyeHeight();
			respawnPacket.z = (float) pos.z;
			this.dataPacket(respawnPacket);
		}

		this.sendPlayStatus(PlayStatusPacket.PLAYER_SPAWN);

		PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(this,
				new TranslationContainer(TextFormat.YELLOW + "%multiplayer.player.joined", new String[]{
						this.getDisplayName()
				})
		);

		this.server.getPluginManager().callEvent(playerJoinEvent);

		this.spawned = true;

		if (playerJoinEvent.getJoinMessage().toString().trim().length() > 0) {
			this.server.broadcastMessage(playerJoinEvent.getJoinMessage());
		}

		this.inventory.sendContents(this);
		this.inventory.sendArmorContents(this);

		this.noDamageTicks = 60;

		this.dataPacket(craftingDataPacket);

		if (this.gamemode == Player.SPECTATOR) {
			InventoryContentPacket inventoryContentPacket = new InventoryContentPacket();
			inventoryContentPacket.inventoryId = ContainerIds.CREATIVE;
			this.dataPacket(inventoryContentPacket);
		} else {
			inventory.sendCreativeContents();
		}

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

		//todo Updater

		//Weather
		if (this.level.isRaining() || this.level.isThundering()) {
			this.getLevel().sendWeather(this);
		}
		this.getLevel().sendWeather(this);

		//FoodLevel
		PlayerFood food = this.getFoodData();
		if (food.getLevel() != food.getMaxLevel()) {
			food.sendFoodLevel();
		}
	}

	@Override
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
	}

}

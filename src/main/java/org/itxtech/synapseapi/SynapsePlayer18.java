package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.GameRules;
import cn.nukkit.level.Position;
import cn.nukkit.math.MathHelper;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.*;
import cn.nukkit.resourcepacks.ResourcePack;
import org.itxtech.synapseapi.event.player.SynapsePlayerBroadcastLevelSoundEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerRequestAvailableEntityIdentifiersPaletteEvent;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.ResourcePackClientResponsePacket16;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.*;
import org.itxtech.synapseapi.multiprotocol.utils.AvailableEntityIdentifiersPalette;
import org.itxtech.synapseapi.multiprotocol.utils.LevelSoundEventEnum;

import java.net.InetSocketAddress;

public class SynapsePlayer18 extends SynapsePlayer17 {

	public SynapsePlayer18(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
		this.levelChangeLoadScreen = true;
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
				if (!callPacketReceiveEvent(packet)) break;
				ResourcePackClientResponsePacket16 responsePacket = (ResourcePackClientResponsePacket16) packet;
				switch (responsePacket.responseStatus) {
					case ResourcePackClientResponsePacket.STATUS_REFUSED:
						this.close("", "disconnectionScreen.noReason");
						break;
					case ResourcePackClientResponsePacket.STATUS_SEND_PACKS:
						for (ResourcePackClientResponsePacket16.Entry entry : responsePacket.packEntries) {
							ResourcePack resourcePack = this.resourcePacks.getOrDefault(entry.uuid, this.behaviourPacks.get(entry.uuid));
							if (resourcePack == null) {
								this.close("", "disconnectionScreen.resourcePack");
								break;
							}

							ResourcePackDataInfoPacket dataInfoPacket = new ResourcePackDataInfoPacket();
							dataInfoPacket.packId = resourcePack.getPackId();
							dataInfoPacket.maxChunkSize = RESOURCE_PACK_CHUNK_SIZE;
							dataInfoPacket.chunkCount = MathHelper.ceil(resourcePack.getPackSize() / (float) RESOURCE_PACK_CHUNK_SIZE);
							dataInfoPacket.compressedPackSize = resourcePack.getPackSize();
							dataInfoPacket.sha256 = resourcePack.getSha256();
							if (resourcePack.getPackType().equals("resources")) {
								dataInfoPacket.type = ResourcePackDataInfoPacket.TYPE_RESOURCE;
							}
							else if (resourcePack.getPackType().equals("data")) {
								dataInfoPacket.type = ResourcePackDataInfoPacket.TYPE_BEHAVIOR;
							}
							this.dataPacket(dataInfoPacket);
						}
						break;
					case ResourcePackClientResponsePacket.STATUS_HAVE_ALL_PACKS:
						ResourcePackStackPacket18 stackPacket = new ResourcePackStackPacket18();
						stackPacket.mustAccept = this.forceResources;
						stackPacket.resourcePackStack = this.resourcePacks.values().toArray(new ResourcePack[0]);
						stackPacket.behaviourPackStack = this.behaviourPacks.values().toArray(new ResourcePack[0]);
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
			case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V2:
				if (!callPacketReceiveEvent(packet)) break;
				LevelSoundEventPacketV218 levelSoundEventPacket = (LevelSoundEventPacketV218) packet;
				SynapsePlayerBroadcastLevelSoundEvent event = new SynapsePlayerBroadcastLevelSoundEvent(this,
						LevelSoundEventEnum.fromV18(levelSoundEventPacket.sound),
						new Vector3(levelSoundEventPacket.x, levelSoundEventPacket.y, levelSoundEventPacket.z),
						levelSoundEventPacket.extraData,
						0,
						levelSoundEventPacket.entityIdentifier,
						levelSoundEventPacket.isBabyMob,
						levelSoundEventPacket.isGlobal);
				if (this.isSpectator() && (event.getLevelSound() == LevelSoundEventEnum.SOUND_HIT || event.getLevelSound() == LevelSoundEventEnum.SOUND_ATTACK_NODAMAGE))
					event.setCancelled();
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
				break;
		}

	}

	@Override
	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket18 startGamePacket = new StartGamePacket18();
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

	@Override
	protected void completeLoginSequence() {
		super.completeLoginSequence();
		if (this.loggedIn) {
			AvailableEntityIdentifiersPacket18 pk = new AvailableEntityIdentifiersPacket18();
			SynapsePlayerRequestAvailableEntityIdentifiersPaletteEvent event = new SynapsePlayerRequestAvailableEntityIdentifiersPaletteEvent(this, AvailableEntityIdentifiersPalette.getData(AbstractProtocol.fromRealProtocol(this.getProtocol())));
			this.getServer().getPluginManager().callEvent(event);
			pk.tag = event.getData();
			if (pk.tag != null) {
				this.dataPacket(pk);
			} else {
				Server.getInstance().getLogger().warning("Null AvailableEntityIdentifiersPacket data to player " + this.getName() + "!");
			}
		}
	}

	protected GameRules getSupportedRules() {
		return this.level.gameRules;
	}

	@Override
	protected boolean orderChunks() {
		if (super.orderChunks()) {
			if (!loadQueue.isEmpty()) {
				this.noticeChunkPublisherUpdate();
			}
			return true;
		}
		return false;
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
	public void sendLevelSoundEvent(LevelSoundEventEnum levelSound, Vector3 pos, int extraData, int pitch, String entityIdentifier, boolean isBabyMob, boolean isGlobal) {
		if (levelSound == null || levelSound.getV18() == -1) return;
		LevelSoundEventPacketV218 pk = new LevelSoundEventPacketV218();
		pk.sound = levelSound.getV18();
		pk.x = (float) pos.x;
		pk.y = (float) pos.y;
		pk.z = (float) pos.z;
		pk.extraData = levelSound.translateTo18ExtraData(extraData, pitch, AbstractProtocol.fromRealProtocol(this.protocol), this.isNetEaseClient);
		pk.entityIdentifier = entityIdentifier;
		pk.isBabyMob = isBabyMob;
		pk.isGlobal = isGlobal;
		this.dataPacket(pk);
	}

	protected void noticeChunkPublisherUpdate() {
		NetworkChunkPublisherUpdatePacket18 packet = new NetworkChunkPublisherUpdatePacket18();
		packet.position = this.asBlockVector3();
		packet.radius = viewDistance << 4;
		this.dataPacket(packet);
	}
}

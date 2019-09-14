package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LevelChunkPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.resourcepacks.ResourcePack;
import org.itxtech.synapseapi.event.player.SynapsePlayerBroadcastLevelSoundEvent;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.StartGamePacket112;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.AvailableEntityIdentifiersPacket18;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.BiomeDefinitionListPacket18;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.LevelSoundEventPacketV319;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.ResourcePacksInfoPacket19;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.StartGamePacket19;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.AvailableEntityIdentifiersPalette;
import org.itxtech.synapseapi.multiprotocol.utils.LevelSoundEventEnum;

import java.net.InetSocketAddress;

public class SynapsePlayer112 extends SynapsePlayer19 {

	public SynapsePlayer112(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
		this.levelChangeLoadScreen = true;
	}

	@Override
	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket112 startGamePacket = new StartGamePacket112();
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
			this.dataPacket(new BiomeDefinitionListPacket18());

			AvailableEntityIdentifiersPacket18 pk = new AvailableEntityIdentifiersPacket18();
			pk.tag = AvailableEntityIdentifiersPalette.getData(AbstractProtocol.fromRealProtocol(this.getProtocol()));
			if (pk.tag != null) {
				this.dataPacket(new AvailableEntityIdentifiersPacket18());
			} else {
				Server.getInstance().getLogger().warning("Null AvailableEntityIdentifiersPacket data to player " + this.getName() + "!");
			}
		}
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
	public void sendChunk(int x, int z, int subChunkCount, byte[] payload) {
		if (!this.connected) {
			return;
		}

		this.usedChunks.put(Level.chunkHash(x, z), true);
		this.chunkLoadCount++;

		LevelChunkPacket pk = new LevelChunkPacket();
		pk.chunkX = x;
		pk.chunkZ = z;
		pk.subChunkCount = subChunkCount;
		pk.data = payload;

		this.batchDataPacket(pk);

		if (this.spawned) {
			for (Entity entity : this.level.getChunkEntities(x, z).values()) {
				if (this != entity && !entity.closed && entity.isAlive()) {
					entity.spawnTo(this);
				}
			}
		}
	}

	@Override
	protected void forceSendEmptyChunks(int chunkRadius) {
		int chunkPositionX = this.getFloorX() >> 4;
		int chunkPositionZ = this.getFloorZ() >> 4;
		for (int x = -chunkRadius; x < chunkRadius; x++) {
			for (int z = -chunkRadius; z < chunkRadius; z++) {
				LevelChunkPacket chunk = new LevelChunkPacket();
				chunk.chunkX = chunkPositionX + x;
				chunk.chunkZ = chunkPositionZ + z;
				chunk.data = new byte[0];
				this.dataPacket(chunk);
			}
		}
	}
}

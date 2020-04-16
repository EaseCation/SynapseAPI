package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.generic.ChunkBlobCache;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LevelChunkPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import com.nukkitx.network.raknet.RakNetReliability;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.ClientCacheBlobStatusPacket112;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.ClientCacheMissResponsePacket112;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.ClientCacheStatusPacket112;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.StartGamePacket112;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.BiomeDefinitionListPacket18;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;

import java.net.InetSocketAddress;

public class SynapsePlayer112 extends SynapsePlayer19 {

	public Long2ObjectMap<byte[]> clientCacheTrack = null; //为null表示客户端未支持 TODO 代理跨服怎么办？

	public SynapsePlayer112(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
		this.levelChangeLoadScreen = false;
	}

	@Override
	public void handleLoginPacket(PlayerLoginPacket packet) {
		super.handleLoginPacket(packet);
		//客户端已经发了ClientCacheStatusPacket，等以后出bug的时候用
		//if (packet.extra.has("blob_cache") && packet.extra.get("blob_cache").getAsBoolean())
		//	initClientBlobCache();
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
			//this.initClientBlobCache();
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
	public void sendChunk(int x, int z, int subChunkCount, ChunkBlobCache blobCache, DataPacket packet) {
		if (this.clientCacheTrack == null || blobCache == null) super.sendChunk(x, z, subChunkCount, blobCache, packet);
		else if (this.getChunkX() == x && this.getChunkZ() == z) {
			super.sendChunk(x, z, subChunkCount, blobCache, packet);
			//this.getServer().getLogger().debug("Send self chunk " + x + ":" + z + " pos=" + this.x + "," + this.y + "," + this.z + " teleportPos=" + teleportPosition);
		} else {
			if (!this.connected) {
				return;
			}

			this.usedChunks.put(Level.chunkHash(x, z), true);
			this.chunkLoadCount++;

			LevelChunkPacket pk = new LevelChunkPacket();

			clientCacheTrack.putAll(blobCache.getClientBlobs());

			/*
			List<String> dump = new ArrayList<>();
			for (long blobId : blobIds) {
				dump.add(Binary.bytesToHexString(Binary.writeLLong(blobId)));
			}
			this.getServer().getLogger().warning("[ClientCache] put=" + Arrays.toString(dump.toArray()));
			*/

			pk.chunkX = x;
			pk.chunkZ = z;
			pk.subChunkCount = subChunkCount;
			pk.cacheEnabled = true;
			pk.blobIds = blobCache.getBlobIds();
			pk.data = blobCache.getClientBlobCachedPayload();
			pk.setReliability(RakNetReliability.RELIABLE);

			this.dataPacket(pk);

			//chunkDebug = true;

			if (this.spawned) {
				for (Entity entity : this.level.getChunkEntities(x, z).values()) {
					if (this != entity && !entity.closed && entity.isAlive()) {
						entity.spawnTo(this);
					}
				}
			}
		}
	}

	@Override
	public void sendChunk(int x, int z, int subChunkCount, ChunkBlobCache blobCache, byte[] payload) {
		if (!this.connected) {
			return;
		}

		this.usedChunks.put(Level.chunkHash(x, z), true);
		this.chunkLoadCount++;

		LevelChunkPacket pk = new LevelChunkPacket();

		if (this.getChunkX() == x && this.getChunkZ() == z) {
			pk.chunkX = x;
			pk.chunkZ = z;
			pk.subChunkCount = subChunkCount;
			pk.data = payload;
			pk.setReliability(RakNetReliability.RELIABLE);
			//this.getServer().getLogger().debug("Send self chunk (payload) " + x + ":" + z + " pos=" + this.x + "," + this.y + "," + this.z + " teleportPos=" + teleportPosition);
		}
		if (this.clientCacheTrack != null && blobCache != null) {
			clientCacheTrack.putAll(blobCache.getClientBlobs());
			pk.chunkX = x;
			pk.chunkZ = z;
			pk.subChunkCount = subChunkCount;
			pk.cacheEnabled = true;
			pk.blobIds = blobCache.getBlobIds();
			pk.data = blobCache.getClientBlobCachedPayload();
			pk.setReliability(RakNetReliability.RELIABLE);
		} else {
			pk.chunkX = x;
			pk.chunkZ = z;
			pk.subChunkCount = subChunkCount;
			pk.data = payload;
			pk.setReliability(RakNetReliability.RELIABLE);
		}

		this.dataPacket(pk);

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

	private void initClientBlobCache() {
		if (this.clientCacheTrack == null) {
			this.clientCacheTrack = new Long2ObjectOpenHashMap<>();
			this.chunksPerTick = SynapseAPI.getInstance().getConfig().getInt("blob-cache-chunk-send-pre-tick", this.chunksPerTick * 2);
			getServer().getLogger().info(this.getName() + "已启用客户端区块缓存, 每tick发送区块被设为" + this.chunksPerTick);
		}
	}

	@Override
	public Long2ObjectMap<byte[]> getClientCacheTrack() {
		return this.clientCacheTrack;
	}

	@Override
	public void handleDataPacket(DataPacket packet) {
		if (!this.isSynapseLogin) {
			super.handleDataPacket(packet);
			return;
		}
		switch (packet.pid()) {
			case ProtocolInfo.CLIENT_CACHE_STATUS_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				if (((ClientCacheStatusPacket112) packet).supported) this.initClientBlobCache();
				//this.getServer().getLogger().warning("[ClientCache] supported=" + ((ClientCacheStatusPacket112) packet).supported);
				break;
			case ProtocolInfo.CLIENT_CACHE_BLOB_STATUS_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				if (clientCacheTrack != null) {
					ClientCacheBlobStatusPacket112 pk = (ClientCacheBlobStatusPacket112) packet;

					//this.getServer().getLogger().warning("[ClientCache] recv ClientCacheBlobStatusPacket");
					//List<String> dump = new ArrayList<>();
					//for (long blobId : pk.missHashes) {
					//	dump.add(Binary.bytesToHexString(Binary.writeLLong(blobId)));
					//}
					//this.getServer().getLogger().warning("miss=" + Arrays.toString(dump.toArray()));
					//this.getServer().getLogger().warning("hit=" + Arrays.toString(pk.hitHashes));

					ClientCacheMissResponsePacket112 responsePk = new ClientCacheMissResponsePacket112();
					for (long id : pk.missHashes) {
						byte[] blob = clientCacheTrack.get(id);
						if (blob != null) {
							responsePk.blobs.put(id, blob);
							clientCacheTrack.remove(id);
							//this.getServer().getLogger().warning("[ClientCache] del " + id);
						}
					}
					responsePk.setReliability(RakNetReliability.RELIABLE);
					this.dataPacket(responsePk);

					//this.getServer().getLogger().warning("[ClientCache] sent ClientCacheMissResponsePacket");
					//this.getServer().getLogger().warning("blobs=" + Arrays.toString(responsePk.blobs.keySet().toArray()));

					for (long id : pk.hitHashes) {
						clientCacheTrack.remove(id);
						//this.getServer().getLogger().warning("[ClientCache] del " + id);
					}
				}
				break;
			default:
				super.handleDataPacket(packet);
				break;
		}
	}
}

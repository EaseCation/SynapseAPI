package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
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
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.ClientCacheBlobStatusPacket112;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.ClientCacheMissResponsePacket112;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.ClientCacheStatusPacket112;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.StartGamePacket112;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.BiomeDefinitionListPacket18;
import org.itxtech.synapseapi.multiprotocol.utils.BiomeDefinitions;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;
import org.itxtech.synapseapi.utils.BlobTrack;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadLocalRandom;

import static org.itxtech.synapseapi.SynapseSharedConstants.*;

public class SynapsePlayer112 extends SynapsePlayer19 {

	public Long2ObjectMap<BlobTrack> clientCacheTrack = null; //为null表示客户端未支持 TODO 代理跨服怎么办？
	protected int clientChunkLoadCount = 0;
	protected boolean sendQueuedChunk = true;
	protected long teleportChunkBlobHash;

	public SynapsePlayer112(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
		// this.levelChangeLoadScreen = false;
	}

	/*@Override
	public void handleLoginPacket(PlayerLoginPacket packet) {
		super.handleLoginPacket(packet);
		//客户端已经发了ClientCacheStatusPacket，等以后出bug的时候用
		//if (packet.extra.has("blob_cache") && packet.extra.get("blob_cache").getAsBoolean())
		//	initClientBlobCache();
	}*/

	@Override
	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket112 startGamePacket = new StartGamePacket112();
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
	protected void sendBiomeDefinitionList() {
		BiomeDefinitionListPacket18 pk = new BiomeDefinitionListPacket18();
		if ((pk.tag = BiomeDefinitions.getData(AbstractProtocol.fromRealProtocol(this.getProtocol()))) != null) {
			this.dataPacket(pk);
		} else {
			Server.getInstance().getLogger().warning("null BiomeDefinitionListPacket data to player " + this.getName());
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
		if (!this.isBlobCacheAvailable() || blobCache == null || this.isBlobCacheDisabled()) {
			super.sendChunk(x, z, subChunkCount, blobCache, packet);
		} else {
			if (!this.connected) {
				return;
			}
			this.noticeChunkPublisherUpdate();
			long chunkHash = Level.chunkHash(x, z);

			long[] blobIds;
			Long2ObjectMap<byte[]> blobs;
			if (this.isExtendedLevel()) {
//				subChunkCount += Anvil.PADDING_SUB_CHUNK_COUNT;
				if (this.getProtocol() >= AbstractProtocol.PROTOCOL_118_30.getProtocolStart()) {
					blobIds = blobCache.getExtendedBlobIdsNew();
					blobs = blobCache.getExtendedClientBlobsNew();
				} else {
					blobIds = blobCache.getExtendedBlobIds();
					blobs = blobCache.getExtendedClientBlobs();
				}
			} else {
				blobIds = blobCache.getBlobIds();
				blobs = blobCache.getClientBlobs();
			}

			this.usedChunks.put(chunkHash, true);
			this.chunkLoadCount++;

			LevelChunkPacket pk = new LevelChunkPacket();

			ObjectIterator<Long2ObjectMap.Entry<byte[]>> iter = Long2ObjectMaps.fastIterator(blobs);
			while (iter.hasNext()) {
				Long2ObjectMap.Entry<? extends byte[]> entry = iter.next();
				long hash = entry.getLongKey();
				clientCacheTrack.put(hash, new BlobTrack(hash, entry.getValue()));
			}

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
			pk.blobIds = blobIds;
			pk.data = blobCache.getFullChunkCachedPayload();
			pk.setReliability(RakNetReliability.RELIABLE_ORDERED);

			this.dataPacket(pk);

			this.sendQueuedChunk = false;

			//chunkDebug = true;

			for (BlockEntity blockEntity : this.level.getChunkBlockEntities(x, z).values()) {
				if (!(blockEntity instanceof BlockEntitySpawnable)) {
					continue;
				}
				((BlockEntitySpawnable) blockEntity).spawnTo(this);
			}

			if (this.spawned) {
				for (Entity entity : this.level.getChunkEntities(x, z).values()) {
					if (this != entity && !entity.closed && entity.isAlive()) {
						entity.spawnTo(this);
					}
				}
			}

			if (chunkHash == this.teleportChunkIndex) {
				if (blobIds.length > 1) {
					this.teleportChunkBlobHash = blobIds[0];
					//this.teleportChunkLoaded = false;
				} else {
					this.teleportChunkLoaded = true;
				}
			}
		}
	}

	@Override
	public void sendChunk(int x, int z, int subChunkCount, ChunkBlobCache blobCache, byte[] payload, byte[] subModePayload) {
		if (!this.connected) {
			return;
		}
		this.noticeChunkPublisherUpdate();
		long chunkHash = Level.chunkHash(x, z);

		this.usedChunks.put(chunkHash, true);
		this.chunkLoadCount++;

		LevelChunkPacket pk = new LevelChunkPacket();

		boolean centerChunk = this.getChunkX() == x && this.getChunkZ() == z;
		if (centerChunk) {
			//this.getServer().getLogger().debug("Send self chunk (payload) " + x + ":" + z + " pos=" + this.x + "," + this.y + "," + this.z + " teleportPos=" + teleportPosition);
			this.teleportChunkLoaded = true;
		}

		if (this.isBlobCacheAvailable() && blobCache != null && (!centerChunk || !CENTER_CHUNK_WITHOUT_CACHE) && !this.isBlobCacheDisabled()) {
			long[] blobIds;
			Long2ObjectMap<byte[]> blobs;
			if (this.isExtendedLevel()) {
//				subChunkCount += Anvil.PADDING_SUB_CHUNK_COUNT;
				if (this.getProtocol() >= AbstractProtocol.PROTOCOL_118_30.getProtocolStart()) {
					blobIds = blobCache.getExtendedBlobIdsNew();
					blobs = blobCache.getExtendedClientBlobsNew();
				} else {
					blobIds = blobCache.getExtendedBlobIds();
					blobs = blobCache.getExtendedClientBlobs();
				}
			} else {
				blobIds = blobCache.getBlobIds();
				blobs = blobCache.getClientBlobs();
			}

			ObjectIterator<Long2ObjectMap.Entry<byte[]>> iter = Long2ObjectMaps.fastIterator(blobs);
			while (iter.hasNext()) {
				Long2ObjectMap.Entry<? extends byte[]> entry = iter.next();
				long hash = entry.getLongKey();
				clientCacheTrack.put(hash, new BlobTrack(hash, entry.getValue()));
			}

			pk.chunkX = x;
			pk.chunkZ = z;
			pk.subChunkCount = subChunkCount;
			pk.cacheEnabled = true;
			pk.blobIds = blobIds;
			pk.data = blobCache.getFullChunkCachedPayload();
			pk.setReliability(RakNetReliability.RELIABLE_ORDERED);

			this.sendQueuedChunk = false;

			if (chunkHash == this.teleportChunkIndex) {
				if (blobIds.length > 1) {
					this.teleportChunkBlobHash = blobIds[0];
					//this.teleportChunkLoaded = false;
				} else {
					this.teleportChunkLoaded = true;
				}
			}
		} else {
			pk.chunkX = x;
			pk.chunkZ = z;
			pk.subChunkCount = subChunkCount;
			pk.data = payload;
			pk.setReliability(RakNetReliability.RELIABLE_ORDERED);
		}

		this.dataPacket(pk);

		for (BlockEntity blockEntity : this.level.getChunkBlockEntities(x, z).values()) {
			if (!(blockEntity instanceof BlockEntitySpawnable)) {
				continue;
			}
			((BlockEntitySpawnable) blockEntity).spawnTo(this);
		}

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

				if (this.teleportChunkIndex == Level.chunkHash(x, z)) {
					this.teleportChunkLoaded = true;
				}
			}
		}
	}

	protected void initClientBlobCache() {
		if (USE_CLIENT_BLOB_CACHE && this.clientCacheTrack == null) {
			this.clientCacheTrack = new Long2ObjectOpenHashMap<BlobTrack>() {
				@Override
				public BlobTrack put(long hash, BlobTrack track) {
					BlobTrack oldValue = super.put(hash, track);
					if (oldValue != null) {
						track.retain(oldValue.refCnt());
					}
					return oldValue;
				}
			};
			this.chunksPerTick = SynapseAPI.getInstance().getConfig().getInt("blob-cache-chunk-send-pre-tick", this.chunksPerTick * 2);
			getServer().getLogger().info(this.getName() + "已启用客户端区块缓存, 每tick发送区块被设为" + this.chunksPerTick);
		}
	}

	@Override
	public Long2ObjectMap<BlobTrack> getClientCacheTrack() {
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
				if (this.isBlobCacheAvailable()) {
					ClientCacheBlobStatusPacket112 pk = (ClientCacheBlobStatusPacket112) packet;

					//this.getServer().getLogger().warning("[ClientCache] recv ClientCacheBlobStatusPacket");
					//List<String> dump = new ArrayList<>();
					//for (long blobId : pk.missHashes) {
					//	dump.add(Binary.bytesToHexString(Binary.writeLLong(blobId)));
					//}
					//this.getServer().getLogger().warning("miss=" + Arrays.toString(dump.toArray()));
					//this.getServer().getLogger().warning("hit=" + Arrays.toString(pk.hitHashes));

					ClientCacheMissResponsePacket112 responsePk = new ClientCacheMissResponsePacket112();
					for (long id : pk.missSet) {
						BlobTrack track = clientCacheTrack.get(id);
						if (track != null) {
							responsePk.blobs.put(id, track.getBlob());
							this.clientChunkLoadCount += track.refCnt();
							clientCacheTrack.remove(id);
							//this.getServer().getLogger().warning("[ClientCache] del " + id);
						}

						if (id == teleportChunkBlobHash) {
							this.teleportChunkLoaded = true;
						}
					}
					responsePk.setReliability(RakNetReliability.RELIABLE_ORDERED);
					this.dataPacket(responsePk);

					//this.getServer().getLogger().warning("[ClientCache] sent ClientCacheMissResponsePacket");
					//this.getServer().getLogger().warning("blobs=" + Arrays.toString(responsePk.blobs.keySet().toArray()));

					for (long id : pk.hitSet) {
						BlobTrack track = clientCacheTrack.remove(id);
						if (track != null) {
							this.clientChunkLoadCount += track.refCnt();
						}
						//this.getServer().getLogger().warning("[ClientCache] del " + id);

						if (id == teleportChunkBlobHash) {
							this.teleportChunkLoaded = true;
						}
					}

					this.sendQueuedChunk = true;
				}
				break;
			default:
				super.handleDataPacket(packet);
				break;
		}
	}

/*
	@Override
	public boolean onUpdate(int currentTick) {
		int tickDiff = currentTick - this.lastUpdate;
		if (tickDiff > 0) {
			this.updateSynapsePlayerTiming.startTiming();
			if (this.isBlobCacheAvailable() && !this.isBlobCacheDisabled()) {
				if (this.teleportChunkLoaded) {
					if (this.isImmobile() && !this.lastImmobile) {
						//this.setImmobile(false);
					}
				} else if (!this.isImmobile()) {
					//this.setImmobile();
				}
			}
			this.updateSynapsePlayerTiming.stopTiming();
		}
		return super.onUpdate(currentTick);
	}
*/

	@Override
	protected boolean canDoFirstSpawn() {
		return this.isBlobCacheAvailable() && !this.isBlobCacheDisabled() ? this.clientChunkLoadCount >= this.spawnThreshold : super.canDoFirstSpawn();
	}

	@Override
	protected boolean canSendQueuedChunk() {
		return this.sendQueuedChunk;
	}

	/*@Override
	protected boolean sendQueuedChunk() {
		boolean success = super.sendQueuedChunk();
		if (success) {
			this.sendQueuedChunk = false;
		}
		return success;
	}*/

	public boolean isBlobCacheAvailable() {
		return USE_CLIENT_BLOB_CACHE && this.clientCacheTrack != null;
	}

	public boolean isBlobCacheDisabled() {
		return false;
	}

	protected boolean isExtendedLevel() {
		return false;
	}
}

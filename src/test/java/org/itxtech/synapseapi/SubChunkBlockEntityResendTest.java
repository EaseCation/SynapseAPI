package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.level.GlobalBlockPaletteInterface.StaticVersion;
import cn.nukkit.level.HeightRange;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.format.generic.ChunkBlobCache;
import cn.nukkit.level.format.generic.ChunkCachedData;
import cn.nukkit.level.format.generic.ChunkPacketCache;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.SubChunkPacket;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证子区块响应后的方块实体同步：无论缓存是否过期，都补发本次响应覆盖的实体，
 * 确保按玩家生成的数据在原始、预压缩和 Blob 响应之后下发。
 */
class SubChunkBlockEntityResendTest {
    private static final int CHUNK_X = 3;
    private static final int CHUNK_Z = 4;
    private static final int REQUESTED_SUB_CHUNK_Y = 2;
    private static final int SECOND_SUB_CHUNK_Y = 4;
    private static final int OUTSIDE_SUB_CHUNK_Y = 5;
    private static final int SUB_CHUNK_COUNT = 6;
    private static final StaticVersion BLOCK_VERSION = StaticVersion.V1_21_90;

    @BeforeAll
    static void initializeProtocols() {
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> servers = mockStatic(Server.class)) {
            servers.when(Server::getInstance).thenReturn(server);
            AbstractProtocol.PROTOCOL_121_90.getProtocolStart();
        }
    }

    @Test
    void currentPayloadIsFollowedByBlockEntitiesOfTheSentSubChunk() throws Exception {
        Fixture fixture = new Fixture(7, 7);

        fixture.sendPayloads();

        SubChunkPacket response = assertInstanceOf(SubChunkPacket.class, fixture.verifyResponseOrder(1, false).getFirst());
        assertSame(fixture.payloads[REQUESTED_SUB_CHUNK_Y], response.data);
        assertFalse(response.cacheEnabled);
    }

    @Test
    void stalePayloadIsFollowedByBlockEntitiesOfTheSentSubChunk() throws Exception {
        Fixture fixture = new Fixture(7, 8);

        fixture.sendPayloads();

        fixture.verifyResponseOrder(1, false);
    }

    @Test
    void currentCachedPacketIsFollowedByBlockEntitiesOfTheSentSubChunk() throws Exception {
        Fixture fixture = new Fixture(7, 7);

        fixture.sendCachedPackets();

        assertSame(fixture.packets[REQUESTED_SUB_CHUNK_Y], fixture.verifyResponseOrder(1, false).getFirst());
    }

    @Test
    void staleCachedPacketIsFollowedByBlockEntitiesOfTheSentSubChunk() throws Exception {
        Fixture fixture = new Fixture(7, 8);

        fixture.sendCachedPackets();

        assertSame(fixture.packets[REQUESTED_SUB_CHUNK_Y], fixture.verifyResponseOrder(1, false).getFirst());
    }

    @Test
    void payloadBlobResponseIsFollowedByBlockEntitiesOfTheSentSubChunk() throws Exception {
        Fixture fixture = new Fixture(7, 7);
        fixture.enableBlobCache();

        fixture.sendPayloads();

        fixture.verifyBlobResponse();
    }

    @Test
    void cachedBlobResponseIsFollowedByBlockEntitiesOfTheSentSubChunk() throws Exception {
        Fixture fixture = new Fixture(7, 7);
        fixture.enableBlobCache();

        fixture.sendCachedPackets();

        fixture.verifyBlobResponse();
    }

    @Test
    void allPayloadResponsesPrecedeBlockEntitiesIncludingNewRequests() throws Exception {
        Fixture fixture = new Fixture(7, 7);
        fixture.addPendingRequest();

        fixture.sendPayloads();

        List<DataPacket> responses = fixture.verifyResponseOrder(2, true);
        assertEquals(REQUESTED_SUB_CHUNK_Y, assertInstanceOf(SubChunkPacket.class, responses.get(0)).subChunkY);
        assertEquals(SECOND_SUB_CHUNK_Y, assertInstanceOf(SubChunkPacket.class, responses.get(1)).subChunkY);
    }

    @Test
    void allCachedResponsesPrecedeBlockEntitiesIncludingNewRequests() throws Exception {
        Fixture fixture = new Fixture(7, 7);
        fixture.addPendingRequest();

        fixture.sendCachedPackets();

        List<DataPacket> responses = fixture.verifyResponseOrder(2, true);
        assertSame(fixture.packets[REQUESTED_SUB_CHUNK_Y], responses.get(0));
        assertSame(fixture.packets[SECOND_SUB_CHUNK_Y], responses.get(1));
    }

    @Test
    void cachedResponseDuringLevelChangePrecedesBlockEntities() throws Exception {
        Fixture fixture = new Fixture(7, 7);
        doReturn(true).when(fixture.player).isNeedLevelChangeLoadScreen();

        fixture.sendCachedPackets();

        SubChunkPacket response = assertInstanceOf(SubChunkPacket.class, fixture.verifyResponseOrder(1, false).getFirst());
        assertEquals(REQUESTED_SUB_CHUNK_Y, response.subChunkY);
        assertSame(fixture.payloads[REQUESTED_SUB_CHUNK_Y], response.data);
    }

    @Test
    void emptyPayloadRequestDoesNotVisitBlockEntities() throws Exception {
        Fixture fixture = new Fixture(7, 7);
        fixture.clearRequests();

        fixture.sendPayloads();

        verify(fixture.level, never()).getChunkBlockEntities(anyInt(), anyInt());
        verify(fixture.player, never()).dataPacket(any(DataPacket.class));
    }

    @Test
    void emptyCachedRequestDoesNotVisitBlockEntities() throws Exception {
        Fixture fixture = new Fixture(7, 7);
        fixture.clearRequests();

        fixture.sendCachedPackets();

        verify(fixture.level, never()).getChunkBlockEntities(anyInt(), anyInt());
        verify(fixture.player, never()).dataPacket(any(DataPacket.class));
    }

    private static final class Fixture {
        private final SynapsePlayer116100 player = mock(SynapsePlayer116100.class, CALLS_REAL_METHODS);
        private final Level level = mock(Level.class);
        private final BlockEntitySpawnable inside = mock(BlockEntitySpawnable.class);
        private final BlockEntitySpawnable second = mock(BlockEntitySpawnable.class);
        private final BlockEntitySpawnable outside = mock(BlockEntitySpawnable.class);
        private final Long2ObjectMap<IntSet> sendQueue = new Long2ObjectOpenHashMap<>();
        private final Long2ObjectMap<IntSet> requestQueue = new Long2ObjectOpenHashMap<>();
        private final byte[][] payloads = new byte[SUB_CHUNK_COUNT][];
        private final byte[][] blockEntityPayloads = new byte[SUB_CHUNK_COUNT][];
        private final long[] blobIds = new long[SUB_CHUNK_COUNT];
        private final BatchPacket[] packets = new BatchPacket[SUB_CHUNK_COUNT];
        private final Map<StaticVersion, byte[][]> versionedPayloads = new EnumMap<>(StaticVersion.class);
        private final ChunkCachedData cachedData;

        private Fixture(long dataRevision, long chunkRevision) throws Exception {
            Long2ObjectMap<byte[]> blobs = new Long2ObjectOpenHashMap<>();
            SubChunkPacket[] uncompressedPackets = new SubChunkPacket[SUB_CHUNK_COUNT];
            for (int y = 0; y < SUB_CHUNK_COUNT; y++) {
                this.payloads[y] = new byte[]{(byte) y, 1};
                this.blockEntityPayloads[y] = new byte[]{(byte) y, 2};
                this.blobIds[y] = y + 10;
                blobs.put(this.blobIds[y], new byte[]{(byte) y, 3});
                this.packets[y] = new BatchPacket();
                SubChunkPacket packet = new SubChunkPacket();
                packet.subChunkX = CHUNK_X;
                packet.subChunkY = y;
                packet.subChunkZ = CHUNK_Z;
                packet.data = this.payloads[y];
                uncompressedPackets[y] = packet;
            }
            this.versionedPayloads.put(BLOCK_VERSION, this.payloads);
            ChunkBlobCache blobCache = new ChunkBlobCache(this.blobIds, blobs, this.blobIds, blobs,
                    new byte[0], new byte[0], this.blockEntityPayloads);
            ChunkPacketCache packetCache = mock(ChunkPacketCache.class);
            when(packetCache.getSubChunkPackets(BLOCK_VERSION)).thenReturn(this.packets);
            when(packetCache.getSubChunkPacketsUncompressed(BLOCK_VERSION)).thenReturn(uncompressedPackets);
            this.cachedData = new ChunkCachedData(SUB_CHUNK_COUNT, new byte[SUB_CHUNK_COUNT], new byte[SUB_CHUNK_COUNT][],
                    new boolean[SUB_CHUNK_COUNT], blobCache, packetCache, dataRevision);

            this.sendQueue.put(Level.chunkHash(CHUNK_X, CHUNK_Z), new IntLinkedOpenHashSet(new int[]{REQUESTED_SUB_CHUNK_Y}));
            setField(SynapsePlayer116100.class, "subChunkSendQueue", this.sendQueue);
            setField(SynapsePlayer116100.class, "subChunkRequestQueue", this.requestQueue);
            setField(Player.class, "connected", true);
            setField(Player.class, "blockVersion", BLOCK_VERSION);
            this.player.level = this.level;

            when(this.level.getHeightRange()).thenReturn(mock(HeightRange.class));
            BaseFullChunk chunk = mock(BaseFullChunk.class);
            when(chunk.getNetworkRevision()).thenReturn(chunkRevision);
            when(this.level.getChunkIfLoaded(CHUNK_X, CHUNK_Z)).thenReturn(chunk);

            when(this.inside.getSubChunkY()).thenReturn(REQUESTED_SUB_CHUNK_Y);
            when(this.second.getSubChunkY()).thenReturn(SECOND_SUB_CHUNK_Y);
            when(this.outside.getSubChunkY()).thenReturn(OUTSIDE_SUB_CHUNK_Y);
            Map<Long, BlockEntity> blockEntities = new LinkedHashMap<>();
            blockEntities.put(1L, this.inside);
            blockEntities.put(2L, this.second);
            blockEntities.put(3L, this.outside);
            when(this.level.getChunkBlockEntities(CHUNK_X, CHUNK_Z)).thenReturn(blockEntities);

            doReturn(false).when(this.player).isMainThreadInputEnabled();
            doReturn(false).when(this.player).isNeedLevelChangeLoadScreen();
            doReturn(false).when(this.player).isBlobCacheAvailable();
            doReturn(true).when(this.player).isSubChunkBlobCacheEnabled();
            doReturn(BLOCK_VERSION.getProtocol()).when(this.player).getProtocol();
            doAnswer(invocation -> new SubChunkPacket()).when(this.player).createSubChunkPacket();
            doReturn(true).when(this.player).dataPacket(any(DataPacket.class));
        }

        private void enableBlobCache() {
            this.player.clientCacheTrack = new Long2ObjectOpenHashMap<>();
            doReturn(true).when(this.player).isBlobCacheAvailable();
        }

        private void addPendingRequest() {
            this.requestQueue.put(Level.chunkHash(CHUNK_X, CHUNK_Z), new IntLinkedOpenHashSet(new int[]{SECOND_SUB_CHUNK_Y}));
        }

        private void clearRequests() {
            this.sendQueue.get(Level.chunkHash(CHUNK_X, CHUNK_Z)).clear();
        }

        private void sendPayloads() {
            this.player.sendSubChunks(0, CHUNK_X, CHUNK_Z, SUB_CHUNK_COUNT, this.cachedData, this.versionedPayloads,
                    this.cachedData.getHeightMapType(), this.cachedData.getHeightMapData());
        }

        private void sendCachedPackets() {
            this.player.sendSubChunks(0, CHUNK_X, CHUNK_Z, SUB_CHUNK_COUNT, this.cachedData,
                    this.cachedData.getHeightMapType(), this.cachedData.getHeightMapData());
        }

        private void verifyBlobResponse() {
            SubChunkPacket response = assertInstanceOf(SubChunkPacket.class, verifyResponseOrder(1, false).getFirst());
            assertTrue(response.cacheEnabled);
            assertEquals(this.blobIds[REQUESTED_SUB_CHUNK_Y], response.blobId);
            assertSame(this.blockEntityPayloads[REQUESTED_SUB_CHUNK_Y], response.data);
            assertTrue(this.player.clientCacheTrack.containsKey(response.blobId));
        }

        private List<DataPacket> verifyResponseOrder(int responseCount, boolean includesSecond) {
            ArgumentCaptor<DataPacket> responses = ArgumentCaptor.forClass(DataPacket.class);
            InOrder order = inOrder(this.player, this.inside, this.second);
            order.verify(this.player, times(responseCount)).dataPacket(responses.capture());
            order.verify(this.inside).spawnTo(this.player);
            if (includesSecond) {
                order.verify(this.second).spawnTo(this.player);
            } else {
                verify(this.second, never()).spawnTo(any(Player.class));
            }
            verify(this.outside, never()).spawnTo(any(Player.class));
            return responses.getAllValues();
        }

        private void setField(Class<?> owner, String name, Object value) throws Exception {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            field.set(this.player, value);
        }
    }
}

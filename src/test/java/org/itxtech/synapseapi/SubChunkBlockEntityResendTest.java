package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.level.GlobalBlockPaletteInterface.StaticVersion;
import cn.nukkit.level.HeightRange;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.format.generic.ChunkBlobCache;
import cn.nukkit.level.format.generic.ChunkCachedData;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.SubChunkPacket;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证子区块响应之后的方块实体补发：发出的数据是区块当前版本时不补发，
 * 已经过期时只补发本次发出的子区块里的方块实体，并且排在子区块数据之后。
 */
class SubChunkBlockEntityResendTest {
    private static final int CHUNK_X = 3;
    private static final int CHUNK_Z = 4;
    private static final int REQUESTED_SUB_CHUNK_Y = 2;

    @Test
    void currentSubChunkDataIsNotFollowedByBlockEntityPackets() throws Exception {
        Fixture fixture = new Fixture(7, 7);

        fixture.sendSubChunks();

        verify(fixture.player).dataPacket(any(SubChunkPacket.class));
        verify(fixture.level, never()).getChunkBlockEntities(anyInt(), anyInt());
        verify(fixture.inside, never()).spawnTo(any(Player.class));
    }

    @Test
    void staleSubChunkDataIsFollowedByBlockEntitiesOfTheSentSubChunksOnly() throws Exception {
        Fixture fixture = new Fixture(7, 8);

        fixture.sendSubChunks();

        InOrder order = inOrder(fixture.player, fixture.inside);
        order.verify(fixture.player).dataPacket(any(SubChunkPacket.class));
        order.verify(fixture.inside).spawnTo(fixture.player);
        verify(fixture.outside, never()).spawnTo(any(Player.class));
    }

    private static final class Fixture {
        private final SynapsePlayer116100 player = mock(SynapsePlayer116100.class, CALLS_REAL_METHODS);
        private final Level level = mock(Level.class);
        private final BlockEntitySpawnable inside = mock(BlockEntitySpawnable.class);
        private final BlockEntitySpawnable outside = mock(BlockEntitySpawnable.class);
        private final ChunkCachedData cachedData;

        private Fixture(long dataRevision, long chunkRevision) throws Exception {
            this.cachedData = new ChunkCachedData(0, new byte[0], new byte[0][], new boolean[0], mock(ChunkBlobCache.class), null, dataRevision);

            Long2ObjectMap<IntSet> sendQueue = new Long2ObjectOpenHashMap<>();
            sendQueue.put(Level.chunkHash(CHUNK_X, CHUNK_Z), new IntOpenHashSet(new int[]{REQUESTED_SUB_CHUNK_Y}));
            setField(SynapsePlayer116100.class, "subChunkSendQueue", sendQueue);
            setField(SynapsePlayer116100.class, "subChunkRequestQueue", new Long2ObjectOpenHashMap<IntSet>());
            setField(Player.class, "connected", true);
            this.player.level = this.level;

            HeightRange heightRange = mock(HeightRange.class);
            when(this.level.getHeightRange()).thenReturn(heightRange);
            BaseFullChunk chunk = mock(BaseFullChunk.class);
            when(chunk.getNetworkRevision()).thenReturn(chunkRevision);
            when(this.level.getChunkIfLoaded(CHUNK_X, CHUNK_Z)).thenReturn(chunk);

            when(this.inside.getSubChunkY()).thenReturn(REQUESTED_SUB_CHUNK_Y);
            when(this.outside.getSubChunkY()).thenReturn(REQUESTED_SUB_CHUNK_Y + 3);
            Map<Long, BlockEntity> blockEntities = new LinkedHashMap<>();
            blockEntities.put(1L, this.inside);
            blockEntities.put(2L, this.outside);
            when(this.level.getChunkBlockEntities(CHUNK_X, CHUNK_Z)).thenReturn(blockEntities);

            doReturn(false).when(this.player).isMainThreadInputEnabled();
            doReturn(false).when(this.player).isNeedLevelChangeLoadScreen();
            doReturn(mock(SubChunkPacket.class)).when(this.player).createSubChunkPacket();
            doReturn(true).when(this.player).dataPacket(any(DataPacket.class));
        }

        private void sendSubChunks() {
            // 区块没有任何非空子区块时，每个请求都会得到一个“全是空气”的响应，足以覆盖补发逻辑
            this.player.sendSubChunks(0, CHUNK_X, CHUNK_Z, 0, this.cachedData, new EnumMap<StaticVersion, byte[][]>(StaticVersion.class), new byte[0], new byte[0][]);
        }

        private void setField(Class<?> owner, String name, Object value) throws Exception {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            field.set(this.player, value);
        }
    }
}

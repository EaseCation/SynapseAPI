package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.plugin.PluginManager;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.ClientCacheBlobStatusPacket112;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.ClientCacheMissResponsePacket112;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.SubChunkRequestPacket11810;
import org.itxtech.synapseapi.utils.BlobTrack;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChunkRequestAdmissionTest {
    @Test
    void subchunksAndSmallCacheConfirmationsPreserveWholePacketOrdering() {
        for (boolean blob : new boolean[]{false, true}) {
            for (int count : new int[]{0, 1, 64, 65}) {
                DataPacket packet = blob ? status(count, 0) : subchunks(count);
                SynapsePlayer player = mock(SynapsePlayer.class);
                ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 8, 1024, 8, 1024,
                        exception -> fail(exception));
                ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
                List<String> consumed = new ArrayList<>();
                dispatcher.offer(session, new ServerInputTask() {
                    public boolean canRunBetweenTicks() { return BetweenTickPackets.canRunBetweenTicks(player, packet); }
                    public void run(InboundContext context) { consumed.add("terrain"); }
                }, 1, BetweenTickPackets.supports(packet));
                dispatcher.offer(session, context -> consumed.add("input"), 1, true);
                assertEquals(!blob || count <= 64 ? 2 : 0, dispatcher.drain(true, 7, Long.MAX_VALUE));
                dispatcher.drainWorldTick(8);
                assertEquals(List.of("terrain", "input"), consumed);
                verify(player, never()).canProcessInputStateBetweenTicks();
            }
        }
        assertFalse(BetweenTickPackets.supports(status(33, 32)));
        assertFalse(BetweenTickPackets.supports(new ClientCacheBlobStatusPacket112()));
        assertFalse(BetweenTickPackets.supports(new SubChunkRequestPacket11810()));
    }

    @Test
    void cancelledBlobStatusDoesNotRemoveTrackingOrProduceAReply() {
        Fixture fixture = new Fixture(true);
        fixture.player.clientCacheTrack.put(1, new BlobTrack(1, new byte[]{7}, 2));
        doAnswer(call -> { ((DataPacketReceiveEvent) call.getArgument(0)).setCancelled(); return null; })
                .when(fixture.plugins).callEvent(any(DataPacketReceiveEvent.class));
        fixture.player.handleInputDataPacket(status(0, 1), 0);
        assertEquals(1, fixture.player.clientCacheTrack.size());
        assertEquals(0, fixture.player.loaded());
        verify(fixture.player, never()).dataPacket(any());
        verify(fixture.plugins, times(1)).callEvent(any(DataPacketReceiveEvent.class));
    }

    @Test
    void hitMissAndDuplicateAcknowledgementsKeepOriginalAccountingInBothModes() {
        for (boolean enabled : new boolean[]{false, true}) {
            Fixture fixture = new Fixture(enabled);
            byte[] blob = new byte[]{7, 8};
            fixture.player.clientCacheTrack.put(1, new BlobTrack(1, blob, 2));
            fixture.player.clientCacheTrack.put(101, new BlobTrack(101, new byte[]{9}, 3));
            List<DataPacket> sent = new ArrayList<>();
            doAnswer(call -> { sent.add(call.getArgument(0)); return true; }).when(fixture.player).dataPacket(any());
            ClientCacheBlobStatusPacket112 packet = status(1, 1);
            fixture.player.handleInputDataPacket(packet, 0);
            assertEquals(5, fixture.player.loaded());
            assertTrue(fixture.player.clientCacheTrack.isEmpty());
            assertSame(blob, ((ClientCacheMissResponsePacket112) sent.getFirst()).blobs.get(1));
            assertTrue(fixture.player.ready());
            fixture.player.handleInputDataPacket(packet, 0);
            assertEquals(5, fixture.player.loaded());
            assertTrue(((ClientCacheMissResponsePacket112) sent.getLast()).blobs.isEmpty());
        }
    }

    @Test
    void retiredSessionDoesNotAcceptTerrainAcknowledgement() {
        Fixture fixture = new Fixture(true);
        doReturn(false).when(fixture.player).isAcceptingInputPackets();
        fixture.player.handleInputDataPacket(status(1, 1), 0);
        verify(fixture.plugins, never()).callEvent(any(DataPacketReceiveEvent.class));
        verify(fixture.player, never()).dataPacket(any());
    }

    @Test
    void originalReplyCallbackRemainsBetweenMissAndHitAccounting() {
        Fixture fixture = new Fixture(true);
        fixture.player.clientCacheTrack.put(1, new BlobTrack(1, new byte[]{7}, 2));
        fixture.player.clientCacheTrack.put(101, new BlobTrack(101, new byte[]{8}, 3));
        doAnswer(call -> {
            assertEquals(2, fixture.player.loaded());
            assertFalse(fixture.player.clientCacheTrack.containsKey(1));
            assertTrue(fixture.player.clientCacheTrack.containsKey(101));
            return false;
        }).when(fixture.player).dataPacket(any());
        fixture.player.handleInputDataPacket(status(1, 1), 0);
        assertEquals(5, fixture.player.loaded());
    }

    private static ClientCacheBlobStatusPacket112 status(int hits, int misses) {
        ClientCacheBlobStatusPacket112 packet = new ClientCacheBlobStatusPacket112();
        packet.hitSet = new LongOpenHashSet(); packet.missSet = new LongOpenHashSet();
        for (int i = 0; i < hits; i++) packet.hitSet.add(101 + i);
        for (int i = 0; i < misses; i++) packet.missSet.add(1 + i);
        return packet;
    }

    private static SubChunkRequestPacket11810 subchunks(int count) {
        SubChunkRequestPacket11810 packet = new SubChunkRequestPacket11810();
        packet.positionOffsets = new BlockVector3[count];
        for (int i = 0; i < count; i++) packet.positionOffsets[i] = new BlockVector3(i, 0, 0);
        return packet;
    }

    private static final class Fixture {
        final Probe player = mock(Probe.class, CALLS_REAL_METHODS);
        final PluginManager plugins = mock(PluginManager.class);
        Fixture(boolean enabled) {
            Server server = mock(Server.class);
            player.configure(server);
            when(server.isPrimaryThread()).thenReturn(true);
            when(server.getPluginManager()).thenReturn(plugins);
            doReturn(enabled).when(player).isMainThreadInputEnabled();
            doReturn(true).when(player).isAcceptingInputPackets();
            doReturn(true).when(player).isBlobCacheAvailable();
            doReturn(false).when(player).isClosed();
        }
    }

    static class Probe extends SynapsePlayer112 {
        Probe() { super(null, null, 0L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server) {
            this.server = server; this.isSynapseLogin = true;
            this.clientCacheTrack = new Long2ObjectOpenHashMap<>();
            this.teleportChunkBlobHash = 1;
        }
        int loaded() { return clientChunkLoadCount; }
        boolean ready() { return teleportChunkLoaded && sendQueuedChunk; }
    }
}

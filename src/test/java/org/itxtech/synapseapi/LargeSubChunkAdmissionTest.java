package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.data.ServerConfiguration;
import cn.nukkit.entity.attribute.Attribute;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.HeightRange;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.BinaryStream;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import cn.nukkit.network.protocol.SubChunkPacket;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.SubChunkRequestPacket11810;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LargeSubChunkAdmissionTest {
    @BeforeAll
    static void initialize() {
        Block.init(); Item.init(); Attribute.init();
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            global.when(Server::getInstance).thenReturn(server);
            AbstractProtocol.PROTOCOL_121_20.getProtocolStart();
        }
    }

    @Test
    void largeRegistrationPrecedesFollowerAndKeepsWorldServiceQuota() {
        Fixture fixture = new Fixture(true);
        SubChunkRequestPacket11810 packet = packet(160);
        fixture.dispatch(packet);
        assertEquals(160, fixture.player.waiting());
        assertEquals(160, fixture.player.entries());
        verify(fixture.level, never()).requestSubChunks(anyInt(), anyInt(), any(), anyInt());
        fixture.player.consume();
        assertEquals(152, fixture.player.waiting());
        assertEquals(8, fixture.player.inFlight());
        verify(fixture.level, times(8)).requestSubChunks(anyInt(), anyInt(), any(), anyInt());
    }

    @Test
    void duplicateLargeRequestsKeepOriginalWaitingAndInFlightSets() {
        Fixture fixture = new Fixture(true);
        SubChunkRequestPacket11810 packet = packet(1024);
        fixture.dispatch(packet);
        fixture.player.consume();
        fixture.dispatch(packet);
        assertEquals(248, fixture.player.waiting());
        assertEquals(8, fixture.player.inFlight());
        assertEquals(1024, fixture.player.entries());
    }

    @Test
    void invalidLargeRequestRepliesInOriginalOrderBeforeFollower() {
        Fixture fixture = new Fixture(true);
        SubChunkRequestPacket11810 packet = packet(160);
        packet.subChunkY = 100;
        fixture.dispatch(packet);
        assertEquals(0, fixture.player.waiting());
        assertEquals(160, fixture.player.sent.size());
        for (DataPacket reply : fixture.player.sent) {
            assertEquals(SubChunkPacket.REQUEST_RESULT_Y_INDEX_OUT_OF_BOUNDS, ((SubChunkPacket) reply).requestResult);
        }
        verify(fixture.level, never()).requestSubChunks(anyInt(), anyInt(), any(), anyInt());
    }

    @Test
    void cancelledLargeRequestNeitherRegistersNorReplies() {
        Fixture fixture = new Fixture(true);
        doAnswer(call -> { ((DataPacketReceiveEvent) call.getArgument(0)).setCancelled(); return null; })
                .when(fixture.plugins).callEvent(any(DataPacketReceiveEvent.class));
        fixture.dispatch(packet(160));
        assertEquals(0, fixture.player.waiting());
        assertTrue(fixture.player.sent.isEmpty());
    }

    @Test
    void disabledOriginalPathKeepsRegistrationAndWorldQuota() {
        Fixture fixture = new Fixture(false);
        fixture.player.handleDataPacket(packet(160));
        assertEquals(160, fixture.player.waiting());
        fixture.player.consume();
        assertEquals(152, fixture.player.waiting());
        assertEquals(8, fixture.player.inFlight());
    }

    @Test
    void realDecoderKeepsOriginalLimitAndSignedOffsets() {
        BinaryStream wire = header();
        wire.putLInt(65);
        for (int i = 0; i < 65; i++) {
            wire.putByte((byte) -1); wire.putByte((byte) 0); wire.putByte((byte) 1);
        }
        SubChunkRequestPacket11810 packet = new SubChunkRequestPacket11810();
        packet.setBuffer(wire.getBuffer(), 0);
        packet.decode();
        assertEquals(65, packet.positionOffsets.length);
        assertEquals(-1, packet.positionOffsets[64].x);
        assertTrue(BetweenTickPackets.supports(packet));
        BinaryStream oversized = header();
        oversized.putLInt(96 * 96 * 64 + 1);
        SubChunkRequestPacket11810 invalid = new SubChunkRequestPacket11810();
        invalid.setBuffer(oversized.getBuffer(), 0);
        assertThrows(IndexOutOfBoundsException.class, invalid::decode);
    }

    private static BinaryStream header() {
        BinaryStream wire = new BinaryStream();
        for (int i = 0; i < 4; i++) wire.putVarInt(0);
        return wire;
    }

    private static SubChunkRequestPacket11810 packet(int count) {
        SubChunkRequestPacket11810 packet = new SubChunkRequestPacket11810();
        packet.positionOffsets = new BlockVector3[count];
        for (int i = 0; i < count; i++) packet.positionOffsets[i] = new BlockVector3(i % 16 - 8, i / 256, i / 16 % 16 - 8);
        return packet;
    }

    private static final class Fixture {
        final Probe player;
        final Level level = mock(Level.class);
        final PluginManager plugins = mock(PluginManager.class);

        Fixture(boolean enabled) {
            Server server = mock(Server.class);
            when(server.getConfiguration()).thenReturn(mock(ServerConfiguration.class));
            when(server.getDefaultLevel()).thenReturn(level);
            when(server.getViewDistance()).thenReturn(8);
            when(server.getPluginManager()).thenReturn(plugins);
            when(server.isPrimaryThread()).thenReturn(true);
            when(level.getHeightRange()).thenReturn(HeightRange.MINIMUM);
            when(level.requestSubChunks(anyInt(), anyInt(), any(), anyInt())).thenReturn(true);
            try (MockedStatic<Server> global = mockStatic(Server.class)) {
                global.when(Server::getInstance).thenReturn(server);
                player = new Probe(mock(SourceInterface.class), enabled);
            }
        }

        void dispatch(SubChunkRequestPacket11810 packet) {
            assertTrue(BetweenTickPackets.supports(packet));
            ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 8, 1024, 8, 1024, error -> fail(error));
            ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
            player.bindInputSession(session);
            List<String> order = new ArrayList<>();
            dispatcher.offer(session, new ServerInputTask() {
                @Override public boolean canRunBetweenTicks() { return BetweenTickPackets.canRunBetweenTicks(player, packet); }
                @Override public void run(InboundContext context) {
                    assertTrue(context.betweenTicks());
                    player.handleInputDataPacket(packet, 0);
                    order.add("whole-request");
                }
            }, 1, true);
            dispatcher.offer(session, context -> order.add("follower"), 1, true);
            assertEquals(2, dispatcher.drain(true, 1, Long.MAX_VALUE));
            assertEquals(List.of("whole-request", "follower"), order);
        }
    }

    static class Probe extends SynapsePlayer116100 {
        private final boolean enabled;
        final List<DataPacket> sent = new ArrayList<>();

        Probe(SourceInterface source, boolean enabled) {
            super(source, null, 0L, new InetSocketAddress("127.0.0.1", 19132));
            this.enabled = enabled;
            this.isSynapseLogin = true;
            this.sessionId = UUID.randomUUID();
            this.chunksPerTick = 8;
            this.protocol = AbstractProtocol.PROTOCOL_121_20.getProtocolStart();
        }
        @Override public boolean isMainThreadInputEnabled() { return enabled; }
        @Override public boolean isInitialized() { return true; }
        @Override public boolean isSubChunkRequestAvailable() { return true; }
        @Override public int getLoaderId() { return 1; }
        @Override public int getViewDistance() { return 8; }
        @Override public int getDummyDimension() { return 0; }
        @Override public boolean dataPacket(DataPacket packet) { sent.add(packet); return true; }
        int waiting() { return subChunkRequestQueue.size(); }
        int inFlight() { return subChunkSendQueue.size(); }
        int entries() {
            return subChunkRequestQueue.values().stream().mapToInt(IntSet::size).sum()
                    + subChunkSendQueue.values().stream().mapToInt(IntSet::size).sum();
        }
        void consume() { processSubChunkRequest(); }
    }
}

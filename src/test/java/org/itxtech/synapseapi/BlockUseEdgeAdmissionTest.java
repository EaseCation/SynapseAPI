package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.ServerInputDispatcher;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.PlayerActionPacket119;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlockUseEdgeAdmissionTest {
    @BeforeAll
    static void initializeProtocol() {
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> servers = mockStatic(Server.class)) {
            servers.when(Server::getInstance).thenReturn(server);
            AbstractProtocol.PROTOCOL_119.getProtocolStart();
        }
    }

    @Test
    void nativeStartKeepsOriginalStateBeforeFollowingInput() {
        check(true, false, false, true);
    }

    @Test
    void nativeStopKeepsOriginalStateBeforeFollowingInput() {
        check(false, false, false, true);
    }

    @Test
    void javaStartDoesNotInventNativeUsingState() {
        check(true, true, false, true);
    }

    @Test
    void cancelledStartCannotSetState() {
        check(true, false, true, true);
    }

    @Test
    void disabledModeKeepsOriginalStartAndStop() {
        check(true, false, false, false);
        check(false, false, false, false);
    }

    private void check(boolean start, boolean javaClient, boolean cancelled, boolean enabled) {
        BlockPlacementInventoryResyncTest.TestPlayer player = mock(BlockPlacementInventoryResyncTest.TestPlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        int protocol = AbstractProtocol.PROTOCOL_119.getProtocolStart();
        player.configure(server, null, null, protocol);
        when(server.getPlayerInputProcessor()).thenReturn(mock(PlayerInputProcessor.class));
        doReturn(protocol).when(player).getProtocol();
        doReturn(true).when(player).isInitialized();
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).supportsPacketSequences();
        doReturn(javaClient).when(player).isJavaClient();
        List<String> order = new ArrayList<>();
        doAnswer(call -> { order.add("event"); return !cancelled; }).when(player).callPacketReceiveEvent(any());
        // 验证原handler调用状态入口及顺序，不用夹具时间替代生产时钟。
        doAnswer(call -> { order.add("using:" + call.getArgument(0)); return null; }).when(player).setUsingItem(anyBoolean());
        PlayerActionPacket119 packet = new PlayerActionPacket119();
        packet.action = start ? PlayerActionPacket119.ACTION_ITEM_USE_ON_START : PlayerActionPacket119.ACTION_ITEM_USE_ON_STOP;
        if (enabled) {
            assertTrue(BetweenTickPackets.supports(packet));
            assertTrue(BetweenTickPackets.canRunBetweenTicks(player, packet));
            ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 1024, 16, 1024, error -> fail(error));
            ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
            dispatcher.offer(session, context -> {
                assertTrue(context.betweenTicks());
                player.handleDataPacket(packet);
            }, 1, true);
            dispatcher.offer(session, context -> order.add("follower"), 1, true);
            assertEquals(2, dispatcher.drain(true, 42, Long.MAX_VALUE));
        } else {
            player.handleDataPacket(packet);
            order.add("follower");
        }
        assertEquals(cancelled ? List.of("event", "follower")
                : List.of("event", "using:" + (start && !javaClient), "follower"), order);
        verify(player, never()).onUpdate(anyInt());
    }
}

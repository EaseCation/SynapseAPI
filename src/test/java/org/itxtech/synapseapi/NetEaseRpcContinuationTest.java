package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.data.ServerConfiguration;
import cn.nukkit.entity.attribute.Attribute;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.MainLogger;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.NEPyRpcPacket16;
import org.itxtech.synapseapi.network.protocol.mod.ServerSubPacketHandler;
import org.itxtech.synapseapi.network.protocol.mod.StoreBuySuccessPacket;
import org.itxtech.synapseapi.network.protocol.mod.SubPacket;
import org.itxtech.synapseapi.network.protocol.mod.SubPacketHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NetEaseRpcContinuationTest {
    @BeforeAll
    static void initializePlayerItems() {
        Block.init();
        Item.init();
        Attribute.init();
    }

    @Test
    void retirementStopsLaterHandlerAndNextSubPacket() {
        verifyCalls(true, true, false, List.of("first"));
    }

    @Test
    void disabledModePreservesHandlerAndSubPacketOrderAfterRetirement() {
        verifyCalls(false, true, false, List.of("first", "second", "first", "second"));
    }

    @Test
    void normalInputsPreserveHandlerAndSubPacketOrder() {
        verifyCalls(true, false, false, List.of("first", "second", "first", "second"));
    }

    @Test
    void cancelledPacketDoesNotInvokeExtensions() {
        verifyCalls(true, false, true, List.of());
    }

    @Test
    void callbackInPacketReceiveRetiringSourceStopsExtensions() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.addHandlers(false);
            doAnswer(call -> {
                doReturn(false).when(fixture.player).isAcceptingInputPackets();
                return true;
            }).when(fixture.player).callPacketReceiveEvent(any());
            fixture.player.handleDataPacket(fixture.packet());
            assertTrue(fixture.calls.isEmpty());
        }
    }

    @Test
    void emptyHandlerAndEmptyNormalizedInputRetainNoOpBehavior() {
        try (Fixture fixture = new Fixture(true)) {
            assertDoesNotThrow(() -> fixture.player.handleDataPacket(fixture.packet()));
            fixture.addHandlers(false);
            NEPyRpcPacket16 empty = new NEPyRpcPacket16();
            empty.subPackets = List.of();
            assertDoesNotThrow(() -> fixture.player.handleDataPacket(empty));
            assertTrue(fixture.calls.isEmpty());
        }
    }

    @Test
    void existingExceptionIsolationSkipsRemainingHandlerOnlyForCurrentSubPacket() {
        try (Fixture fixture = new Fixture(true)) {
            StoreBuySuccessPacket first = new StoreBuySuccessPacket();
            fixture.player.addSubPacketHandler(new ServerSubPacketHandler<StoreBuySuccessPacket>() {
                @Override
                public void dispatch(SubPacket<? extends SubPacketHandler<?>> packet) {
                    fixture.calls.add("first");
                    if (packet == first) throw new IllegalStateException("Owned RPC callback failure");
                }
            });
            fixture.addSecondHandler();
            NEPyRpcPacket16 packet = new NEPyRpcPacket16();
            packet.subPackets = List.of(first, new StoreBuySuccessPacket());
            fixture.player.handleDataPacket(packet);
            assertEquals(List.of("first", "first", "second"), fixture.calls);
        }
    }

    @Test
    void retiredSourceAfterThrowCannotContinueAtNextSubPacket() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.player.addSubPacketHandler(new ServerSubPacketHandler<StoreBuySuccessPacket>() {
                @Override
                public void dispatch(SubPacket<? extends SubPacketHandler<?>> packet) {
                    fixture.calls.add("first");
                    doReturn(false).when(fixture.player).isAcceptingInputPackets();
                    throw new IllegalStateException("Owned retired RPC callback failure");
                }
            });
            fixture.addSecondHandler();
            fixture.player.handleDataPacket(fixture.packet());
            assertEquals(List.of("first"), fixture.calls);
        }
    }

    private void verifyCalls(boolean enabled, boolean retire, boolean cancelled, List<String> expected) {
        try (Fixture fixture = new Fixture(enabled)) {
            fixture.addHandlers(retire);
            if (cancelled) doReturn(false).when(fixture.player).callPacketReceiveEvent(any());
            fixture.player.handleDataPacket(fixture.packet());
            assertEquals(expected, fixture.calls);
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final MockedStatic<Server> servers = mockStatic(Server.class);
        private final ProbePlayer player;
        private final List<String> calls = new ArrayList<>();

        private Fixture(boolean enabled) {
            Server server = mock(Server.class);
            when(server.getConfiguration()).thenReturn(mock(ServerConfiguration.class));
            when(server.getDefaultLevel()).thenReturn(mock(Level.class));
            when(server.getLogger()).thenReturn(mock(MainLogger.class));
            servers.when(Server::getInstance).thenReturn(server);
            try {
                player = spy(new ProbePlayer());
            } catch (RuntimeException | Error failure) {
                servers.close();
                throw failure;
            }
            doReturn(enabled).when(player).isMainThreadInputEnabled();
            doReturn(true).when(player).isAcceptingInputPackets();
            doReturn(true).when(player).callPacketReceiveEvent(any());
        }

        private NEPyRpcPacket16 packet() {
            NEPyRpcPacket16 packet = new NEPyRpcPacket16();
            packet.subPackets = List.of(new StoreBuySuccessPacket(), new StoreBuySuccessPacket());
            return packet;
        }

        private void addHandlers(boolean retire) {
            player.addSubPacketHandler(new ServerSubPacketHandler<StoreBuySuccessPacket>() {
                @Override
                public void dispatch(SubPacket<? extends SubPacketHandler<?>> packet) {
                    calls.add("first");
                    if (retire) doReturn(false).when(player).isAcceptingInputPackets();
                }
            });
            addSecondHandler();
        }

        private void addSecondHandler() {
            player.addSubPacketHandler(new ServerSubPacketHandler<StoreBuySuccessPacket>() {
                @Override
                public void dispatch(SubPacket<? extends SubPacketHandler<?>> packet) {
                    calls.add("second");
                }
            });
        }

        @Override
        public void close() {
            servers.close();
        }
    }

    static class ProbePlayer extends SynapsePlayer16 {
        ProbePlayer() {
            super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132));
            this.isSynapseLogin = true;
        }
    }
}

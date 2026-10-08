package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.player.PlayerAnimationEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol121120.protocol.AnimatePacket121120;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.AnimatePacket121130;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;
import java.net.InetSocketAddress;

import static org.mockito.Mockito.*;

class AnimationInputLifecycleTest {
    @BeforeAll
    static void initializeProtocol() {
        Block.init();
        Item.init();
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            global.when(Server::getInstance).thenReturn(server);
            AbstractProtocol.PROTOCOL_126_20.getProtocolStart();
        }
    }

    @Test void normalSwingIsPublishedOnce() { check("normal", true, 1); }
    @Test void cancelledSwingIsNotPublished() { check("cancel", true, 0); }
    @Test void teleportInAnimationEventInvalidatesPublication() { check("teleport", true, 0); }
    @Test void deathInAnimationEventInvalidatesPublication() { check("death", true, 0); }
    @Test void closeInAnimationEventInvalidatesPublication() { check("close", true, 0); }
    @Test void disabledModeRetainsTheExistingContinuation() { check("teleport", false, 1); }

    private void check(String scenario, boolean enabled, int published) {
        for (int layout = 0; layout < 3; layout++) {
            TestPlayer player = mock(TestPlayer.class, CALLS_REAL_METHODS);
            Server server = mock(Server.class);
            PluginManager plugins = mock(PluginManager.class);
            when(server.getPluginManager()).thenReturn(plugins);
            int protocol = switch (layout) {
                case 0 -> AbstractProtocol.PROTOCOL_121_20.getProtocolStart();
                case 1 -> AbstractProtocol.PROTOCOL_121_120.getProtocolStart();
                default -> AbstractProtocol.PROTOCOL_126_20.getProtocolStart();
            };
            Level level = mock(Level.class);
            player.configure(server, level, mock(PlayerInventory.class), protocol);
            player.connectForTest();
            doReturn(enabled).when(player).isMainThreadInputEnabled();
            doReturn(true).when(player).isInitialized();
            doReturn(true).when(player).isOnline();
            doReturn(true).when(player).isAlive();
            doReturn(true).when(player).isAcceptingInputPackets();
            doReturn(false).when(player).isClosed();
            doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
            doReturn(0L).when(player).getMovementEpoch();
            doReturn(Map.of()).when(player).getViewers();
            doAnswer(call -> {
                PlayerAnimationEvent event = call.getArgument(0);
                switch (scenario) {
                    case "cancel" -> event.setCancelled();
                    case "teleport" -> doReturn(1L).when(player).getMovementEpoch();
                    case "death" -> doReturn(false).when(player).isAlive();
                    case "close" -> doReturn(false).when(player).isOnline();
                    default -> { }
                }
                return null;
            }).when(plugins).callEvent(any(PlayerAnimationEvent.class));
            DataPacket packet;
            if (layout == 0) {
                AnimatePacket animation = new AnimatePacket();
                animation.action = AnimatePacket.Action.SWING_ARM;
                packet = animation;
            } else if (layout == 1) {
                AnimatePacket121120 animation = new AnimatePacket121120();
                animation.action = AnimatePacket.Action.SWING_ARM;
                packet = animation;
            } else {
                AnimatePacket121130 animation = new AnimatePacket121130();
                animation.action = AnimatePacket.Action.SWING_ARM;
                packet = animation;
            }
            try (MockedStatic<Server> global = mockStatic(Server.class)) {
                player.handleDataPacket(packet);
                global.verify(() -> Server.broadcastPacket(anyCollection(), any(AnimatePacket.class)), times(published));
            }
            verify(plugins).callEvent(any(PlayerAnimationEvent.class));
        }
    }

    static class TestPlayer extends BlockPlacementInventoryResyncTest.TestPlayer {
        TestPlayer(SourceInterface source, SynapseEntry entry, Long id, InetSocketAddress address) {
            super(source, entry, id, address);
        }

        void connectForTest() {
            this.connected = true;
            this.loggedIn = true;
        }
    }
}

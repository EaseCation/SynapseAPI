package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.SettingsCommandPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol11960.protocol.CommandRequestPacket11960;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.CommandRequestPacket121130;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;

import static org.mockito.Mockito.*;

class CommandCallbackLifecycleTest {
    @BeforeAll
    static void initializeProtocols() {
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> servers = mockStatic(Server.class)) {
            servers.when(Server::getInstance).thenReturn(server);
            AbstractProtocol.PROTOCOL_126_20.getProtocolStart();
        }
    }

    @Test
    void callbackCommittedTransferStopsAllTargetCommandBranches() {
        verifyScenario(true, "transfer", false);
    }

    @Test
    void callbackClosedPlayerStopsAllTargetCommandBranches() {
        verifyScenario(true, "close", false);
    }

    @Test
    void disabledModePreservesOriginalDispatchAfterCommittedTransfer() {
        verifyScenario(false, "transfer", true);
    }

    @Test
    void normalCommandDispatchesExactlyOnce() {
        verifyScenario(true, "normal", true);
    }

    @Test
    void pluginCancellationStillStopsDispatch() {
        verifyScenario(true, "cancel", false);
    }

    @Test
    void legalTeleportAndModifiedCommandUseCurrentServerState() {
        verifyScenario(true, "teleport", true);
    }

    @Test
    void pluginSelectedCommandSenderIsPreservedWhileOriginalSessionRemainsActive() {
        verifyScenario(true, "sender", true);
    }

    private void verifyScenario(boolean inputMode, String variant, boolean execute) {
        for (String path : new String[]{"native860", "native898", "java975", "settings860", "settings975"}) {
            Server server = mock(Server.class);
            PluginManager plugins = mock(PluginManager.class);
            when(server.getPluginManager()).thenReturn(plugins);
            ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
            player.configure(server);
            player.spawned = true;
            doReturn(true).when(player).isAlive();
            doReturn(true).when(player).isInitialized();
            doReturn(true).when(player).isOnline();
            doReturn(true).when(player).isInputSessionActive();
            doReturn(false).when(player).isClosed();
            doReturn(inputMode).when(player).isMainThreadInputEnabled();
            doReturn(true).when(player).callPacketReceiveEvent(any(DataPacket.class));
            doReturn(1L).when(player).getMovementEpoch();
            doNothing().when(player).resetCraftingGridType();
            doNothing().when(player).addViolationLevel(anyInt(), anyString());
            int protocol = path.contains("975") ? AbstractProtocol.PROTOCOL_126_20.getProtocolStart()
                    : path.contains("898") ? AbstractProtocol.PROTOCOL_121_130.getProtocolStart()
                    : AbstractProtocol.PROTOCOL_121_124.getProtocolStart();
            doReturn(protocol).when(player).getProtocol();
            Player replacement = mock(Player.class);
            doAnswer(call -> {
                PlayerCommandPreprocessEvent event = call.getArgument(0);
                switch (variant) {
                    case "transfer" -> doReturn(false).when(player).isAcceptingInputPackets();
                    case "close" -> {
                        doReturn(true).when(player).isClosed();
                        doReturn(false).when(player).isInputSessionActive();
                    }
                    case "cancel" -> event.setCancelled();
                    case "teleport" -> {
                        doReturn(2L).when(player).getMovementEpoch();
                        event.setMessage("/modified");
                    }
                    case "sender" -> event.setPlayer(replacement);
                    default -> {}
                }
                return null;
            }).when(plugins).callEvent(any(PlayerCommandPreprocessEvent.class));
            DataPacket packet;
            if (path.startsWith("settings")) {
                SettingsCommandPacket113 settings = new SettingsCommandPacket113();
                settings.command = "/niqa";
                packet = settings;
            } else if (protocol >= AbstractProtocol.PROTOCOL_121_130.getProtocolStart()) {
                CommandRequestPacket121130 command = new CommandRequestPacket121130();
                command.command = "/niqa";
                packet = command;
            } else {
                CommandRequestPacket11960 command = new CommandRequestPacket11960();
                command.command = "/niqa";
                packet = command;
            }
            try (MockedStatic<Server> servers = mockStatic(Server.class)) {
                servers.when(Server::getInstance).thenReturn(server);
                player.handleDataPacket(packet);
            }
            verify(plugins, times(1)).callEvent(any(PlayerCommandPreprocessEvent.class));
            if (execute) {
                verify(server).dispatchCommand(variant.equals("sender") ? replacement : player,
                        variant.equals("teleport") ? "modified" : "niqa");
            } else {
                verify(server, never()).dispatchCommand(any(), anyString());
            }
            if (variant.equals("teleport")) verify(player, never()).getMovementEpoch();
        }
    }

    static class ProbePlayer extends SynapsePlayer116100 {
        ProbePlayer() {
            super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132));
        }

        void configure(Server server) {
            this.server = server;
            this.isSynapseLogin = true;
            this.messageCounter = 2;
        }
    }
}

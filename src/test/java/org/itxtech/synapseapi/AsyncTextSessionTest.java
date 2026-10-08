package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.event.player.SynapsePlayerPreChatEvent;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AsyncTextSessionTest {
    @Test
    void retiredPlayerDoesNotPublishPreChatEvenWhenNewPlayerSharesNetworkSession() {
        try (Fixture fixture = new Fixture()) {
            SynapsePlayer old = fixture.player(true, false);
            SynapsePlayer current = fixture.player(true, true);
            UUID session = UUID.randomUUID();
            doReturn(session).when(old).getSessionId();
            doReturn(session).when(current).getSessionId();
            doReturn(true).when(old).isClosed();

            old.preChat("old");
            current.preChat("new");

            verify(fixture.plugins, times(1)).callEvent(any(SynapsePlayerPreChatEvent.class));
            verify(old, never()).chat(anyString());
            verify(current).chat("new");
        }
    }

    @Test
    void committedTransferStopsCallbackBeforeLogoutClosesThePlayer() {
        try (Fixture fixture = new Fixture()) {
            SynapsePlayer player = fixture.player(true, true);
            doReturn(false).when(player).isAcceptingInputPackets();
            doReturn(false).when(player).isClosed();
            player.spawned = true;

            player.preChat("retired");

            verify(fixture.plugins, never()).callEvent(any(SynapsePlayerPreChatEvent.class));
            verify(player, never()).chat(anyString());
        }
    }

    @Test
    void preChatCallbackRetiringInputStopsFollowingChat() {
        try (Fixture fixture = new Fixture()) {
            SynapsePlayer player = fixture.player(true, true);
            doAnswer(call -> {
                doReturn(false).when(player).isInputSessionActive();
                return null;
            }).when(fixture.plugins).callEvent(any(SynapsePlayerPreChatEvent.class));

            player.preChat("callback");

            verify(fixture.plugins, times(1)).callEvent(any(SynapsePlayerPreChatEvent.class));
            verify(player, never()).chat(anyString());
        }
    }

    @Test
    void normalWorldChangePreservesModifiedTextAndExactlyOneChat() {
        try (Fixture fixture = new Fixture()) {
            SynapsePlayer player = fixture.player(true, true);
            doReturn(1L).when(player).getMovementEpoch();
            doAnswer(call -> {
                doReturn(2L).when(player).getMovementEpoch();
                SynapsePlayerPreChatEvent event = call.getArgument(0);
                event.setMessage("modified");
                return null;
            }).when(fixture.plugins).callEvent(any(SynapsePlayerPreChatEvent.class));

            player.preChat("original");

            verify(fixture.plugins, times(1)).callEvent(any(SynapsePlayerPreChatEvent.class));
            verify(player).chat("modified");
            verify(player, never()).getMovementEpoch();
        }
    }

    @Test
    void pluginCancellationStillStopsChat() {
        try (Fixture fixture = new Fixture()) {
            SynapsePlayer player = fixture.player(true, true);
            doAnswer(call -> {
                SynapsePlayerPreChatEvent event = call.getArgument(0);
                event.setCancelled();
                return null;
            }).when(fixture.plugins).callEvent(any(SynapsePlayerPreChatEvent.class));

            player.preChat("cancelled");

            verify(player, never()).chat(anyString());
        }
    }

    @Test
    void disabledModePreservesOriginalRetiredPlayerExtensionAndChatCalls() {
        try (Fixture fixture = new Fixture()) {
            SynapsePlayer player = fixture.player(false, false);

            player.preChat("legacy");

            verify(fixture.plugins).callEvent(any(SynapsePlayerPreChatEvent.class));
            verify(player).chat("legacy");
            verify(player, never()).isAcceptingInputPackets();
        }
    }

    @Test
    void disabledModePreservesContinuationAfterExtensionRetiresInput() {
        try (Fixture fixture = new Fixture()) {
            SynapsePlayer player = fixture.player(false, true);
            doAnswer(call -> {
                doReturn(false).when(player).isInputSessionActive();
                return null;
            }).when(fixture.plugins).callEvent(any(SynapsePlayerPreChatEvent.class));

            player.preChat("legacy-callback");

            verify(player).chat("legacy-callback");
            verify(player, never()).isAcceptingInputPackets();
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final PluginManager plugins = mock(PluginManager.class);
        private final MockedStatic<Server> servers = mockStatic(Server.class);

        private Fixture() {
            Server server = mock(Server.class);
            when(server.getPluginManager()).thenReturn(plugins);
            servers.when(Server::getInstance).thenReturn(server);
        }

        private SynapsePlayer player(boolean enabled, boolean active) {
            SynapsePlayer player = mock(SynapsePlayer.class, CALLS_REAL_METHODS);
            doReturn(enabled).when(player).isMainThreadInputEnabled();
            doReturn(active).when(player).isInputSessionActive();
            doReturn(true).when(player).chat(anyString());
            return player;
        }

        @Override
        public void close() {
            servers.close();
        }
    }
}

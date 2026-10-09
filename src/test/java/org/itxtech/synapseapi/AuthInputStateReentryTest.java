package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerToggleSneakEvent;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.LoginChainData;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthInputStateReentryTest {
    @Test
    void stateContinuationRequiresLiveCurrentInputOwner() {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        doReturn(true).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).isCurrentInputPosition();
        assertTrue(player.canContinueInputState());
        doReturn(false).when(player).isAlive();
        assertFalse(player.canContinueInputState());
        doReturn(true).when(player).isAlive();
        doReturn(false).when(player).isAcceptingInputPackets();
        assertFalse(player.canContinueInputState());
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(false).when(player).isCurrentInputPosition();
        assertFalse(player.canContinueInputState());
        doReturn(true).when(player).isCurrentInputPosition();
        doReturn(false).when(player).isOnline();
        assertFalse(player.canContinueInputState());
    }

    @Test
    void disabledModeDoesNotApplyTheNewStateAdmissionRules() {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        doReturn(false).when(player).isMainThreadInputEnabled();
        assertTrue(player.canContinueInputState());
        verify(player, never()).isCurrentInputPosition();
        verify(player, never()).isAcceptingInputPackets();
    }

    @Test
    void teleportDuringSneakEventCannotWriteOldState() {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        player.configure(server);
        when(server.getPluginManager()).thenReturn(plugins);
        doReturn(true).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(false).when(player).isSneaking();
        doReturn(true).when(player).setSneaking(anyBoolean());
        doReturn(true).when(player).callPacketReceiveEvent(any());
        doNothing().when(player).onClientTickUpdated(anyLong());
        doReturn(mock(LoginChainData.class)).when(player).getLoginChainData();
        AtomicBoolean current = new AtomicBoolean(true);
        doAnswer(call -> current.get()).when(player).isCurrentInputPosition();
        doAnswer(call -> {
            if (call.getArgument(0) instanceof PlayerToggleSneakEvent) current.set(false);
            return null;
        }).when(plugins).callEvent(any());
        PlayerAuthInputPacket116220 packet = mock(PlayerAuthInputPacket116220.class);
        when(packet.pid()).thenReturn(ProtocolInfo.PLAYER_AUTH_INPUT_PACKET);
        when(packet.hasFlag(PlayerAuthInputFlags.START_SNEAKING)).thenReturn(true);
        player.handleDataPacket(packet);
        verify(plugins).callEvent(any(PlayerToggleSneakEvent.class));
        verify(player, never()).setSneaking(true);
    }

    static class ProbePlayer extends SynapsePlayer116 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server) { this.server = server; this.isSynapseLogin = true; }
    }
}

package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.LoginChainData;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.mockito.Mockito.*;

class EmbeddedUseMovementBoundaryTest {
    @Test
    void rejectedMovementStopsBeforeReadingEmbeddedUse() {
        ProbePlayer player = player(true);
        PlayerAuthInputPacket116220 packet = packet();
        player.handleDataPacket(packet);
        verify(player).commitReceivedMovement();
        verify(packet, never()).getUseItemData();
        verify(player, never()).prepareItemUseSlot(anyInt(), any(), any());
    }

    @Test
    void cancelledWholeInputCannotCommitMovementOrStartEmbeddedUse() {
        ProbePlayer player = player(false);
        PlayerAuthInputPacket116220 packet = packet();
        player.handleDataPacket(packet);
        verify(player, never()).commitReceivedMovement();
        verify(packet, never()).getUseItemData();
        verify(player, never()).prepareItemUseSlot(anyInt(), any(), any());
    }

    private static PlayerAuthInputPacket116220 packet() {
        PlayerAuthInputPacket116220 packet = mock(PlayerAuthInputPacket116220.class);
        when(packet.pid()).thenReturn(ProtocolInfo.PLAYER_AUTH_INPUT_PACKET);
        return packet;
    }

    private static ProbePlayer player(boolean packetAccepted) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        player.configure(server);
        doReturn(true).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).isCurrentInputPosition();
        doReturn(packetAccepted).when(player).callPacketReceiveEvent(any());
        doReturn(false).when(player).commitReceivedMovement();
        doReturn(mock(LoginChainData.class)).when(player).getLoginChainData();
        doNothing().when(player).onClientTickUpdated(anyLong());
        return player;
    }

    static class ProbePlayer extends SynapsePlayer116 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server) { this.server = server; this.isSynapseLogin = true; this.spawned = true; }
        @Override protected float getBaseOffset() { return 0; }
    }
}

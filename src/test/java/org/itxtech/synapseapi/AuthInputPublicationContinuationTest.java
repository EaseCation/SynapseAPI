package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerJumpEvent;
import cn.nukkit.event.player.PlayerToggleSprintEvent;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.LoginChainData;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.*;

class AuthInputPublicationContinuationTest {
    @Test void sprintPublicationCannotContinueTheOldInputAfterTeleport() { check("sprint", true, true); }
    @Test void cancellationCorrectionCannotContinueTheOldInputAfterTeleport() { check("correction", true, true); }
    @Test void swimmingPublicationCannotContinueAfterDeath() { check("swim", true, true); }
    @Test void stopSneakingPublicationCannotContinueAfterRetirement() { check("stop-sneak", true, true); }
    @Test void sameOwnerSprintPublicationStillAllowsJump() { check("sprint", true, false); }
    @Test void disabledModeKeepsTheOriginalFollowupEvent() { check("sprint", false, true); }

    private void check(String variant, boolean enabled, boolean invalidate) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        player.configure(server);
        when(server.getPluginManager()).thenReturn(plugins);
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(false).when(player).isSprinting();
        doReturn(false).when(player).isSwimming();
        doReturn(false).when(player).hasEffect(anyInt());
        doReturn(variant.equals("stop-sneak")).when(player).isSneaking();
        doReturn(true).when(player).callPacketReceiveEvent(any());
        doNothing().when(player).onClientTickUpdated(anyLong());
        doReturn(mock(LoginChainData.class)).when(player).getLoginChainData();
        doReturn(false).when(player).commitReceivedMovement();
        AtomicBoolean current = new AtomicBoolean(true);
        doAnswer(call -> current.get()).when(player).isCurrentInputPosition();
        doAnswer(call -> {
            if (invalidate) current.set(false);
            return true;
        }).when(player).setSprinting(true);
        doAnswer(call -> {
            if (invalidate) doReturn(false).when(player).isAlive();
            return true;
        }).when(player).setSwimming(true);
        doAnswer(call -> {
            if (invalidate) doReturn(false).when(player).isAcceptingInputPackets();
            return true;
        }).when(player).setSneaking(false);
        doAnswer(call -> {
            if (invalidate) current.set(false);
            return null;
        }).when(player).sendData(player);
        doAnswer(call -> {
            if (variant.equals("correction") && call.getArgument(0) instanceof PlayerToggleSprintEvent event) event.setCancelled();
            return null;
        }).when(plugins).callEvent(any());
        PlayerAuthInputPacket116220 packet = mock(PlayerAuthInputPacket116220.class);
        when(packet.pid()).thenReturn(ProtocolInfo.PLAYER_AUTH_INPUT_PACKET);
        int flag = switch (variant) {
            case "swim" -> PlayerAuthInputFlags.START_SWIMMING;
            case "stop-sneak" -> PlayerAuthInputFlags.STOP_SNEAKING;
            default -> PlayerAuthInputFlags.START_SPRINTING;
        };
        when(packet.hasFlag(flag)).thenReturn(true);
        when(packet.hasFlag(PlayerAuthInputFlags.START_JUMPING)).thenReturn(true);
        player.handleDataPacket(packet);
        if (enabled && invalidate) verify(plugins, never()).callEvent(any(PlayerJumpEvent.class));
        else verify(plugins).callEvent(any(PlayerJumpEvent.class));
        if (variant.equals("correction")) verify(player).sendData(player);
        else if (variant.equals("swim")) verify(player).setSwimming(true);
        else if (variant.equals("stop-sneak")) verify(player).setSneaking(false);
        else verify(player).setSprinting(true);
    }

    static class ProbePlayer extends SynapsePlayer116 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server) { this.server = server; this.isSynapseLogin = true; }
        @Override protected float getBaseOffset() { return 0; }
    }
}

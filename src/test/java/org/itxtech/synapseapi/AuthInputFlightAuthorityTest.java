package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerToggleFlightEvent;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.LoginChainData;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthInputFlightAuthorityTest {
    @Test void revokedPermissionCannotStartFlight() { check("revoke", true, true, true, false, false); }
    @Test void authorizedRequestStartsFlight() { check("normal", true, true, true, false, true); }
    @Test void unauthorizedStartIsRejectedBeforeEvent() { check("normal", true, true, false, false, false); }
    @Test void stoppingDoesNotRequirePermission() { check("normal", true, false, false, true, false); }
    @Test void cancelledStartDoesNotFly() { check("cancel", true, true, true, false, false); }
    @Test void cancelledStopKeepsCurrentState() { check("cancel", true, false, false, true, true); }
    @Test void callbackDeathDoesNotWriteFlight() { check("death", true, true, true, false, false); }
    @Test void callbackRetirementDoesNotWriteFlight() { check("retire", true, true, true, false, false); }
    @Test void callbackEpochChangeDoesNotWriteFlight() { check("epoch", true, true, true, false, false); }
    @Test void callbackSpectatorCannotOverwriteModeState() { check("spectator", true, false, true, true, true); }
    @Test void initialSpectatorCannotStartFlight() { check("initial-spectator", true, true, true, false, false); }
    @Test void disabledModeRetainsGlobalAllowPath() { check("revoke", false, true, true, false, true); }

    private void check(String variant, boolean enabled, boolean start, boolean allowed, boolean initiallyFlying, boolean expectedFlying) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        AdventureSettings settings = new AdventureSettings(player)
                .set(AdventureSettings.Type.ALLOW_FLIGHT, allowed)
                .set(AdventureSettings.Type.FLYING, initiallyFlying);
        player.configure(server);
        when(server.getPluginManager()).thenReturn(plugins);
        when(server.getAllowFlight()).thenReturn(true);
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).isCurrentInputPosition();
        doReturn(variant.equals("initial-spectator")).when(player).isSpectator();
        doReturn(settings).when(player).getAdventureSettings();
        doReturn(true).when(player).callPacketReceiveEvent(any());
        doReturn(mock(LoginChainData.class)).when(player).getLoginChainData();
        doNothing().when(player).onClientTickUpdated(anyLong());
        doNothing().when(player).sendAbilities(any(), any());
        // 飞行断言截止于状态消费，不借用后续位置提交验证本分支。
        doReturn(false).when(player).commitReceivedMovement();
        doAnswer(call -> {
            if (call.getArgument(0) instanceof PlayerToggleFlightEvent event) {
                switch (variant) {
                    case "revoke" -> settings.set(AdventureSettings.Type.ALLOW_FLIGHT, false);
                    case "cancel" -> event.setCancelled();
                    case "death" -> doReturn(false).when(player).isAlive();
                    case "retire" -> doReturn(false).when(player).isAcceptingInputPackets();
                    case "epoch" -> doReturn(false).when(player).isCurrentInputPosition();
                    case "spectator" -> doReturn(true).when(player).isSpectator();
                    default -> { }
                }
            }
            return null;
        }).when(plugins).callEvent(any());
        PlayerAuthInputPacket116220 packet = mock(PlayerAuthInputPacket116220.class);
        when(packet.pid()).thenReturn(ProtocolInfo.PLAYER_AUTH_INPUT_PACKET);
        when(packet.hasFlag(start ? PlayerAuthInputFlags.START_FLYING : PlayerAuthInputFlags.STOP_FLYING)).thenReturn(true);
        player.handleDataPacket(packet);
        assertEquals(expectedFlying, settings.get(AdventureSettings.Type.FLYING));
        if (enabled && start && (!allowed || variant.equals("initial-spectator"))) {
            verify(plugins, never()).callEvent(any(PlayerToggleFlightEvent.class));
            verify(player).sendAbilities(player, settings);
        } else {
            verify(plugins).callEvent(any(PlayerToggleFlightEvent.class));
        }
    }

    static class ProbePlayer extends SynapsePlayer116 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server) { this.server = server; this.isSynapseLogin = true; }
        @Override protected float getBaseOffset() { return 0; }
    }
}

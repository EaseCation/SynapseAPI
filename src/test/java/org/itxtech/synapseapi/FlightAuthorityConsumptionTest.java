package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerToggleFlightEvent;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.network.protocol.types.PlayerAbility;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.RequestAbilityPacket119;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightAuthorityConsumptionTest {
    @Test void revokedPermissionCannotStartFlight() { check("revoke", true); }
    @Test void livePermissionStillStartsFlight() { check("normal", true); }
    @Test void cancelledEventDoesNotStartFlight() { check("cancel", true); }
    @Test void permissionWithoutOwnerCannotStartFlight() { check("retire", true); }
    @Test void callbackDeathCannotStartFlight() { check("death", true); }
    @Test void callbackTeleportCannotStartFlight() { check("epoch", true); }
    @Test void spectatorAfterCallbackCannotOverwriteModeFlightState() { check("spectator", true); }
    @Test void disabledModeKeepsOriginalGlobalAllowPath() { check("revoke", false); }

    private void check(String variant, boolean enabled) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        AdventureSettings settings = new AdventureSettings(player).set(AdventureSettings.Type.ALLOW_FLIGHT, true);
        player.configure(server);
        when(server.getPluginManager()).thenReturn(plugins);
        when(server.getAllowFlight()).thenReturn(true);
        doReturn(true).when(player).isInitialized();
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).isCurrentInputPosition();
        doReturn(false).when(player).isSpectator();
        doReturn(settings).when(player).getAdventureSettings();
        doReturn(true).when(player).callPacketReceiveEvent(any());
        doNothing().when(player).sendAbilities(any(), any());
        doAnswer(call -> {
            if (call.getArgument(0) instanceof PlayerToggleFlightEvent event) {
                if (variant.equals("revoke")) settings.set(AdventureSettings.Type.ALLOW_FLIGHT, false);
                if (variant.equals("cancel")) event.setCancelled();
                if (variant.equals("retire")) doReturn(false).when(player).isAcceptingInputPackets();
                if (variant.equals("death")) doReturn(false).when(player).isAlive();
                if (variant.equals("epoch")) doReturn(false).when(player).isCurrentInputPosition();
                if (variant.equals("spectator")) doReturn(true).when(player).isSpectator();
            }
            return null;
        }).when(plugins).callEvent(any());
        RequestAbilityPacket119 packet = new RequestAbilityPacket119();
        packet.ability = PlayerAbility.FLYING;
        packet.type = RequestAbilityPacket119.TYPE_BOOL;
        packet.boolValue = true;
        player.handleDataPacket(packet);
        assertEquals(variant.equals("normal") || !enabled && variant.equals("revoke"), settings.get(AdventureSettings.Type.FLYING));
    }

    static class ProbePlayer extends SynapsePlayer116100 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server) { this.server = server; this.isSynapseLogin = true; }
    }
}

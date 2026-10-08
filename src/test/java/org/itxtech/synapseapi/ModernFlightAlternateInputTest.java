package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerToggleFlightEvent;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.protocol.AdventureSettingsPacket;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.PacketViolationReason;
import cn.nukkit.network.protocol.types.PlayerAbility;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.PlayerActionPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.RequestAbilityPacket119;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ModernFlightAlternateInputTest {
    @BeforeAll
    static void initializeProtocols() {
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> servers = mockStatic(Server.class)) {
            servers.when(Server::getInstance).thenReturn(server);
            AbstractProtocol.PROTOCOL_126_30.getProtocolStart();
        }
    }

    @Test void clientMayFlyRequestCannotGrantPermission() {
        Fixture fixture = new Fixture();
        fixture.request(PlayerAbility.MAY_FLY, RequestAbilityPacket119.TYPE_BOOL, true);
        fixture.assertNoFlightChange(false);
        assertFalse(fixture.settings.get(AdventureSettings.Type.ALLOW_FLIGHT));
    }

    @Test void floatRequestCannotSetFlying() {
        Fixture fixture = new Fixture();
        fixture.request(PlayerAbility.FLYING, RequestAbilityPacket119.TYPE_FLOAT, true);
        fixture.assertNoFlightChange(false);
    }

    @Test void unauthorizedBooleanStartIsRejectedBeforeEvent() {
        Fixture fixture = new Fixture();
        fixture.request(PlayerAbility.FLYING, RequestAbilityPacket119.TYPE_BOOL, true);
        fixture.assertNoFlightChange(false);
        verify(fixture.player).sendAbilities(fixture.player, fixture.settings);
    }

    @Test void booleanStopDoesNotRequireStartPermission() {
        Fixture fixture = new Fixture();
        fixture.settings.set(AdventureSettings.Type.FLYING, true);
        fixture.request(PlayerAbility.FLYING, RequestAbilityPacket119.TYPE_BOOL, false);
        assertFalse(fixture.settings.get(AdventureSettings.Type.FLYING));
        verify(fixture.plugins).callEvent(any(PlayerToggleFlightEvent.class));
    }

    @Test void legacyPlayerActionStartCannotBypassAuthoritativeMovement() {
        Fixture fixture = new Fixture();
        fixture.action(PlayerActionPacket.ACTION_START_FLYING);
        fixture.assertNoFlightChange(false);
        verify(fixture.player).onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action34");
    }

    @Test void legacyPlayerActionStopCannotBypassAuthoritativeMovement() {
        Fixture fixture = new Fixture();
        fixture.settings.set(AdventureSettings.Type.FLYING, true);
        fixture.action(PlayerActionPacket.ACTION_STOP_FLYING);
        fixture.assertNoFlightChange(true);
        verify(fixture.player).onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action35");
    }

    @Test void legacyAdventureSettingsCannotGrantModernFlight() {
        Fixture fixture = new Fixture();
        AdventureSettingsPacket packet = new AdventureSettingsPacket();
        packet.setFlag(AdventureSettingsPacket.FLYING, true);
        fixture.player.handleDataPacket(packet);
        fixture.assertNoFlightChange(false);
    }

    private static class Fixture {
        final ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        final PluginManager plugins = mock(PluginManager.class);
        final AdventureSettings settings = new AdventureSettings(player);

        Fixture() {
            Server server = mock(Server.class);
            player.configure(server);
            when(server.getPluginManager()).thenReturn(plugins);
            when(server.getAllowFlight()).thenReturn(true);
            doReturn(true).when(player).isInitialized();
            doReturn(true).when(player).isMainThreadInputEnabled();
            doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
            doReturn(true).when(player).isOnline();
            doReturn(true).when(player).isAlive();
            doReturn(true).when(player).isAcceptingInputPackets();
            doReturn(true).when(player).isCurrentInputPosition();
            doReturn(false).when(player).isSpectator();
            doReturn(settings).when(player).getAdventureSettings();
            doReturn(true).when(player).callPacketReceiveEvent(any());
            doReturn(975).when(player).getProtocol();
            doNothing().when(player).sendAbilities(any(), any());
            doNothing().when(player).onPacketViolation(any(), anyString());
        }

        void request(PlayerAbility ability, int type, boolean value) {
            RequestAbilityPacket119 packet = new RequestAbilityPacket119();
            packet.ability = ability;
            packet.type = type;
            packet.boolValue = value;
            player.handleDataPacket(packet);
        }

        void action(int action) {
            PlayerActionPacket119 packet = new PlayerActionPacket119();
            packet.action = action;
            player.handleDataPacket(packet);
        }

        void assertNoFlightChange(boolean initiallyFlying) {
            if (initiallyFlying) assertTrue(settings.get(AdventureSettings.Type.FLYING));
            else assertFalse(settings.get(AdventureSettings.Type.FLYING));
            verify(plugins, never()).callEvent(any(PlayerToggleFlightEvent.class));
        }
    }

    static class ProbePlayer extends SynapsePlayer116100 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server) { this.server = server; this.isSynapseLogin = true; this.spawned = true; }
    }
}

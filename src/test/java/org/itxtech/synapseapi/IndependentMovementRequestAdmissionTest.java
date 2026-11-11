package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerKickEvent;
import cn.nukkit.event.player.PlayerPassengerJumpEvent;
import cn.nukkit.event.player.PlayerToggleFlightEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.Network;
import cn.nukkit.network.input.InputValidationResult;
import cn.nukkit.network.protocol.AdventureSettingsPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.RiderJumpPacket;
import cn.nukkit.network.protocol.types.PlayerAbility;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.RequestAbilityPacket119;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IndependentMovementRequestAdmissionTest {
    @BeforeAll static void initialize() { LegacyMovementAdmissionTest.protocols(); }

    @Test void authorizedFlightAndFollowerUseOriginalEventAndValidation() { flight("normal", true); }
    @Test void packetAndToggleCancellationCannotReopenFlight() {
        flight("cancel-packet", true);flight("cancel-toggle", true);flight("reject", true);
    }
    @Test void callbackPermissionAndOwnershipAreRechecked() {
        for (String variant : List.of("deny", "revoke", "retire", "death", "epoch", "spectator")) flight(variant, true);
    }
    @Test void stopAndDuplicateRequestKeepOriginalSemantics() {
        Fixture f = new Fixture(true);f.settings.set(AdventureSettings.Type.FLYING, true);
        f.deliver(request(PlayerAbility.FLYING, RequestAbilityPacket119.TYPE_BOOL, false), true);
        assertFalse(f.settings.get(AdventureSettings.Type.FLYING));
        f.deliver(request(PlayerAbility.FLYING, RequestAbilityPacket119.TYPE_BOOL, false), true);
        verify(f.plugins).callEvent(any(PlayerToggleFlightEvent.class));
        verify(f.processor, times(2)).validateReceivedPacket(any(), any(), eq(false));
    }
    @Test void otherCapabilitiesAndFloatCannotGrantFlight() {
        for (RequestAbilityPacket119 p : List.of(request(PlayerAbility.MAY_FLY, RequestAbilityPacket119.TYPE_BOOL, true),
                request(PlayerAbility.FLYING, RequestAbilityPacket119.TYPE_FLOAT, true))) {
            Fixture f = new Fixture(true);p.floatValue = Float.NaN;f.deliver(p, true);
            assertFalse(f.settings.get(AdventureSettings.Type.ALLOW_FLIGHT));assertFalse(f.settings.get(AdventureSettings.Type.FLYING));
            verify(f.plugins, never()).callEvent(any(PlayerToggleFlightEvent.class));
        }
    }
    @Test void allUninitializedOrRetiredRequestsStopAtOriginalEntry() {
        for (boolean retired : new boolean[]{false, true}) for (DataPacket p : List.of(request(PlayerAbility.FLYING, 1, true), new RiderJumpPacket(), new AdventureSettingsPacket())) {
            Fixture f = new Fixture(true);
            if (retired) doReturn(false).when(f.player).isAcceptingInputPackets();else doReturn(false).when(f.player).isInitialized();
            f.deliver(p, true);verifyNoInteractions(f.plugins, f.processor);assertFalse(f.settings.get(AdventureSettings.Type.FLYING));
        }
    }
    @Test void riderJumpKeepsOriginalStrengthAndExactlyOneEvent() { jump(true, false); }
    @Test void cancelledRiderPacketDoesNotDispatchJumpEvent() { jump(true, true); }
    @Test void modernAdventureSettingsRemainIgnored() { adventure(true); }
    @Test void disabledFlightKeepsOriginalAllowAndCancellationRules() {
        for (String variant : List.of("normal", "revoke", "cancel-packet", "cancel-toggle")) flight(variant, false);
    }
    @Test void disabledJumpAndAdventureKeepOriginalEvents() { jump(false, false);jump(false, true);adventure(false); }
    @Test void legacyAdventureSettingsKeepIdentityAndCancellation() {
        for (boolean enabled : new boolean[]{true, false}) for (String variant : List.of("normal", "wrong-id", "cancel")) {
            Fixture f = new Fixture(enabled);doReturn(AbstractProtocol.PROTOCOL_118.getProtocolStart()).when(f.player).getProtocol();
            AdventureSettingsPacket p = new AdventureSettingsPacket();p.entityUniqueId = variant.equals("wrong-id") ? 2 : f.player.getLocalEntityId();p.setFlag(AdventureSettingsPacket.FLYING, true);
            if (variant.equals("cancel")) doAnswer(call -> {if (call.getArgument(0) instanceof PlayerToggleFlightEvent e) e.setCancelled();return null;}).when(f.plugins).callEvent(any());
            f.deliver(p, enabled);assertEquals(variant.equals("normal"), f.settings.get(AdventureSettings.Type.FLYING));
            assertFalse(f.settings.get(AdventureSettings.Type.ALLOW_FLIGHT));
        }
    }
    @Test void globalFlightDisabledKeepsEachModesOriginalRejection() {
        for (boolean enabled : new boolean[]{true, false}) {
            Fixture f = new Fixture(enabled);when(f.server.getAllowFlight()).thenReturn(false);
            f.deliver(request(PlayerAbility.FLYING, RequestAbilityPacket119.TYPE_BOOL, true), enabled);
            assertFalse(f.settings.get(AdventureSettings.Type.FLYING));
            verify(f.player, times(enabled ? 0 : 1)).kick(PlayerKickEvent.Reason.FLYING_DISABLED, "Flying is not enabled on this server", false);
            verify(f.player, times(enabled ? 1 : 0)).sendAbilities(f.player, f.settings);
            verify(f.plugins, never()).callEvent(any(PlayerToggleFlightEvent.class));
        }
    }

    @Test void actualTargetWireKeepsTheOriginalRequestValues() {
        Server server = mock(Server.class);Network network = mock(Network.class);when(server.getNetwork()).thenReturn(network);
        when(network.getPacket(ProtocolInfo.PASSENGER_JUMP_PACKET)).thenAnswer(call -> new RiderJumpPacket());
        when(network.getPacket(ProtocolInfo.ADVENTURE_SETTINGS_PACKET)).thenAnswer(call -> new AdventureSettingsPacket());
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            global.when(Server::getInstance).thenReturn(server);
            for (int protocol : new int[]{859, 860, 898, 975}) for (int kind = 0; kind < 3; kind++) {
                BinaryStream wire = new BinaryStream();
                if (kind == 0) {wire.putUnsignedVarInt(ProtocolInfo.REQUEST_ABILITY_PACKET);wire.putVarInt(PlayerAbility.FLYING.ordinal());wire.putByte((byte) RequestAbilityPacket119.TYPE_BOOL);wire.putBoolean(true);wire.putLFloat(0);}
                else if (kind == 1) {wire.putUnsignedVarInt(ProtocolInfo.PASSENGER_JUMP_PACKET);wire.putVarInt(100);}
                else {wire.putUnsignedVarInt(ProtocolInfo.ADVENTURE_SETTINGS_PACKET);wire.putUnsignedVarInt(AdventureSettingsPacket.FLYING);for (int i = 0; i < 4; i++) wire.putUnsignedVarInt(0);wire.putLLong(1);}
                DataPacket packet = PacketRegister.getFullPacket(wire.getBuffer(), protocol, false);assertNotNull(packet);assertEquals(wire.getCount(), packet.getOffset());assertTrue(BetweenTickPackets.supports(packet));
                if (packet instanceof RequestAbilityPacket119 p) {assertEquals(PlayerAbility.FLYING, p.ability);assertTrue(p.boolValue);}
                else if (packet instanceof RiderJumpPacket p) assertEquals(100, p.strength);
                else assertTrue(((AdventureSettingsPacket) packet).getFlag(AdventureSettingsPacket.FLYING));
            }
        }
    }

    private void flight(String variant, boolean enabled) {
        Fixture f = new Fixture(enabled);f.settings.set(AdventureSettings.Type.ALLOW_FLIGHT, !variant.equals("deny"));f.cancel = variant.equals("cancel-packet");
        if (variant.equals("reject")) when(f.processor.validateReceivedPacket(any(), any(), anyBoolean())).thenReturn(InputValidationResult.REJECTED);
        doAnswer(call -> {
            if (call.getArgument(0) instanceof DataPacketReceiveEvent e && f.cancel) e.setCancelled();
            if (call.getArgument(0) instanceof PlayerToggleFlightEvent e) {
                if (variant.equals("cancel-toggle")) e.setCancelled();
                if (variant.equals("revoke")) f.settings.set(AdventureSettings.Type.ALLOW_FLIGHT, false);
                if (variant.equals("retire")) doReturn(false).when(f.player).isAcceptingInputPackets();
                if (variant.equals("death")) doReturn(false).when(f.player).isAlive();
                if (variant.equals("epoch")) doReturn(false).when(f.player).isCurrentInputPosition();
                if (variant.equals("spectator")) doReturn(true).when(f.player).isSpectator();
            }
            return null;
        }).when(f.plugins).callEvent(any());
        f.deliver(request(PlayerAbility.FLYING, RequestAbilityPacket119.TYPE_BOOL, true), enabled);
        assertEquals(variant.equals("normal") || !enabled && variant.equals("revoke"), f.settings.get(AdventureSettings.Type.FLYING));
        verify(f.processor, times(enabled ? 1 : 0)).validateReceivedPacket(any(), any(), eq(f.cancel));
        verify(f.player, never()).onUpdate(anyInt());verify(f.processor, never()).onMovementCommitted(any(), any());assertNull(f.player.pending());
    }
    private void jump(boolean enabled, boolean cancelled) {
        Fixture f = new Fixture(enabled);List<Float> strengths = new ArrayList<>();
        doAnswer(call -> {if (call.getArgument(0) instanceof DataPacketReceiveEvent e && cancelled) e.setCancelled();if (call.getArgument(0) instanceof PlayerPassengerJumpEvent e) strengths.add(e.getStrength());return null;}).when(f.plugins).callEvent(any());
        for (int value : new int[]{0, 100, -1, Integer.MAX_VALUE}) {RiderJumpPacket p = new RiderJumpPacket();p.strength = value;f.deliver(p, enabled);}
        assertEquals(cancelled ? List.of() : List.of(0f, 100f, -1f, (float) Integer.MAX_VALUE), strengths);
        verifyNoInteractions(f.processor);verify(f.player, never()).onUpdate(anyInt());assertNull(f.player.pending());
    }
    private void adventure(boolean enabled) {
        Fixture f = new Fixture(enabled);AdventureSettingsPacket p = new AdventureSettingsPacket();p.setFlag(AdventureSettingsPacket.ALLOW_FLIGHT, true);p.setFlag(AdventureSettingsPacket.FLYING, true);p.entityUniqueId = f.player.getLocalEntityId();f.deliver(p, enabled);
        assertFalse(f.settings.get(AdventureSettings.Type.ALLOW_FLIGHT));assertFalse(f.settings.get(AdventureSettings.Type.FLYING));verifyNoInteractions(f.plugins, f.processor);
    }
    private static RequestAbilityPacket119 request(PlayerAbility ability, int type, boolean flying) {
        RequestAbilityPacket119 p = new RequestAbilityPacket119();p.ability = ability;p.type = type;p.boolValue = flying;return p;
    }
    private static class Fixture extends LegacyMovementAdmissionTest.Fixture {
        final AdventureSettings settings;
        Fixture(boolean enabled) {
            super(enabled, true);settings = new AdventureSettings(player);doReturn(settings).when(player).getAdventureSettings();
            when(server.getAllowFlight()).thenReturn(true);doReturn(false).when(player).isSpectator();doNothing().when(player).sendAbilities(any(), any());
            doReturn(true).when(player).kick(any(PlayerKickEvent.Reason.class), anyString(), anyBoolean());
        }
    }
}

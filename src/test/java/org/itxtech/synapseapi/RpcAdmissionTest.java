package org.itxtech.synapseapi;

import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.input.InputValidationResult;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.NEPyRpcPacket16;
import org.itxtech.synapseapi.network.protocol.mod.ServerSubPacketHandler;
import org.itxtech.synapseapi.network.protocol.mod.StoreBuySuccessPacket;
import org.itxtech.synapseapi.network.protocol.mod.SubPacket;
import org.itxtech.synapseapi.network.protocol.mod.SubPacketHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RpcAdmissionTest {
    @BeforeAll
    static void initialize() { LegacyMovementAdmissionTest.protocols(); }

    @Test
    void deadNonEmptyRpcKeepsOriginalCallbackOrder() { callbacks(true, "dead", false); }

    @Test
    void disabledDeadRpcKeepsOriginalCallbackOrder() { callbacks(false, "dead", false); }

    @Test
    void originalCallbackDoesNotRequirePhysicalReadiness() {
        for (String state : List.of("unspawned", "offline", "no-sequence", "no-processor")) callbacks(true, state, false);
    }

    @Test
    void originalDeathInsideCallbackDoesNotInventAnExtraStop() { callbacks(true, "callback-death", false); }

    @Test
    void callbackRetirementStopsRemainingCallbacksOnlyInInputMode() {
        callbacks(true, "dead", true);
        callbacks(false, "dead", true);
    }

    private void callbacks(boolean enabled, String state, boolean retire) {
        try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(enabled, true)) {
            doReturn(true).when(f.player).supportsPacketSequences();
            NEPyRpcPacket16 packet = pair();
            List<String> calls = new ArrayList<>();
            if (state.equals("dead")) f.player.makeDead();
            if (state.equals("unspawned")) f.player.spawned = false;
            if (state.equals("offline")) doReturn(false).when(f.player).isOnline();
            if (state.equals("no-sequence")) doReturn(false).when(f.player).supportsPacketSequences();
            if (state.equals("no-processor")) when(f.server.getPlayerInputProcessor()).thenReturn(null);
            for (int i = 0; i < 2; i++) {
                int index = i;
                f.player.addSubPacketHandler(new ServerSubPacketHandler<StoreBuySuccessPacket>() {
                    @Override
                    public void dispatch(SubPacket<? extends SubPacketHandler<?>> sub) {
                        calls.add(index + ":" + (sub == packet.subPackets.getFirst() ? 0 : 1));
                        if (retire && index == 0) doReturn(false).when(f.player).isAcceptingInputPackets();
                        if (state.equals("callback-death") && index == 0) f.player.makeDead();
                    }
                });
            }
            f.deliver(packet);
            assertEquals(enabled && retire ? List.of("0:0") : List.of("0:0", "1:0", "0:1", "1:1"), calls);
            verify(f.plugins).callEvent(any(DataPacketReceiveEvent.class));
            verify(f.processor, times(enabled && !state.equals("no-processor") ? 1 : 0)).validateReceivedPacket(f.player, packet, false);
            verify(f.player, never()).onUpdate(anyInt());
            assertEquals(0, f.player.respawns);
        }
    }

    @Test
    void emptyRpcStillRunsOriginalEventAndValidation() {
        try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
            f.player.makeDead();
            NEPyRpcPacket16 packet = new NEPyRpcPacket16();
            f.deliver(packet);
            verify(f.plugins).callEvent(any(DataPacketReceiveEvent.class));
            verify(f.processor).validateReceivedPacket(f.player, packet, false);
            assertFalse(f.player.isAlive());
        }
    }

    @Test
    void cancellationValidationAndEntryRejectionCannotReopenRpc() {
        for (String state : List.of("cancel", "validation", "uninitialized", "retired")) {
            try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
                f.player.makeDead();
                List<String> calls = new ArrayList<>();
                f.player.addSubPacketHandler(new ServerSubPacketHandler<StoreBuySuccessPacket>() {
                    @Override
                    public void dispatch(SubPacket<? extends SubPacketHandler<?>> sub) { calls.add("called"); }
                });
                if (state.equals("cancel")) f.cancel = true;
                if (state.equals("validation")) when(f.processor.validateReceivedPacket(any(), any(), anyBoolean())).thenReturn(InputValidationResult.REJECTED);
                if (state.equals("uninitialized")) doReturn(false).when(f.player).isInitialized();
                if (state.equals("retired")) doReturn(false).when(f.player).isAcceptingInputPackets();
                f.deliver(pair());
                assertTrue(calls.isEmpty());
                if (state.equals("uninitialized") || state.equals("retired")) verifyNoInteractions(f.plugins, f.processor);
            }
        }
    }

    @Test
    void nullNormalizedListDoesNotGainStaticAdmission() {
        NEPyRpcPacket16 packet = new NEPyRpcPacket16();
        packet.subPackets = null;
        assertFalse(BetweenTickPackets.supports(packet));
    }

    private static NEPyRpcPacket16 pair() {
        NEPyRpcPacket16 packet = new NEPyRpcPacket16();
        packet.subPackets = List.of(new StoreBuySuccessPacket(), new StoreBuySuccessPacket());
        return packet;
    }
}

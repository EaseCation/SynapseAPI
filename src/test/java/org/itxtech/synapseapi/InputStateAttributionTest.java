package org.itxtech.synapseapi;

import cn.nukkit.entity.Entity;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.PlayerHotbarPacket;
import cn.nukkit.network.protocol.ResourcePackChunkRequestPacket;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.NEPyRpcPacket16;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.NetworkStackLatencyPacket19;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InputStateAttributionTest {
    private record Gate(InputBlockReason reason, Consumer<ItemActionAdmissionTest.TestPlayer> setup) { }

    private static List<Gate> stateGates() {
        return List.of(
                new Gate(InputBlockReason.NOT_SPAWNED, p -> p.spawned(false)),
                new Gate(InputBlockReason.OFFLINE, p -> doReturn(false).when(p).isOnline()),
                new Gate(InputBlockReason.DEAD, p -> doReturn(false).when(p).isAlive()),
                new Gate(InputBlockReason.INPUT_NOT_ACCEPTING, p -> doReturn(false).when(p).isAcceptingInputPackets()),
                new Gate(InputBlockReason.PACKET_SEQUENCE_UNSUPPORTED, p -> doReturn(false).when(p).supportsPacketSequences()),
                new Gate(InputBlockReason.INPUT_PROCESSOR_MISSING, p -> when(p.testServer().getPlayerInputProcessor()).thenReturn(null)));
    }

    @Test
    void remainingStatePacketsKeepEachOriginalGate() {
        InventoryTransactionPacket116 mismatch = new InventoryTransactionPacket116();
        mismatch.transactionType = InventoryTransactionPacket116.TYPE_MISMATCH;
        List<DataPacket> packets = List.of(new AnimatePacket());
        for (DataPacket packet : packets) {
            assertGate(ItemActionAdmissionTest.player(), packet, InputBlockReason.NONE);
            for (Gate gate : stateGates()) {
                ItemActionAdmissionTest.TestPlayer player = ItemActionAdmissionTest.player();
                gate.setup.accept(player);
                assertGate(player, packet, gate.reason);
                assertGate(player, mismatch, InputBlockReason.NONE);
            }
        }
    }

    @Test
    void firstFailureOrderAndIndependentMovementRemainUnchanged() {
        List<Gate> gates = stateGates();
        for (int start = 0; start < gates.size(); start++) {
            ItemActionAdmissionTest.TestPlayer player = ItemActionAdmissionTest.player();
            for (int i = start; i < gates.size(); i++) gates.get(i).setup.accept(player);
            assertGate(player, new AnimatePacket(), gates.get(start).reason);
            assertGate(player, new MobEquipmentPacket(), InputBlockReason.NONE);
            assertGate(player, new PlayerHotbarPacket(), InputBlockReason.NONE);
            assertGate(player, new NEPyRpcPacket16(), InputBlockReason.NONE);
            assertGate(player, new PlayerAuthInputPacket116220(), InputBlockReason.NONE);
            assertGate(player, new ResourcePackChunkRequestPacket(), InputBlockReason.NONE);
            assertGate(player, new NetworkStackLatencyPacket19(), InputBlockReason.NONE);
        }
    }

    @Test
    void itemDependencyDoesNotBecomeAnRpcGate() {
        List<Gate> gates = List.of(
                new Gate(InputBlockReason.TELEPORT_PENDING, ItemActionAdmissionTest.TestPlayer::awaitTeleport),
                new Gate(InputBlockReason.RIDING, p -> p.ride(mock(Entity.class))),
                new Gate(InputBlockReason.GLIDING, p -> doReturn(true).when(p).isGliding()));
        InventoryTransactionPacket116 use = new InventoryTransactionPacket116();
        use.transactionType = InventoryTransactionPacket116.TYPE_USE_ITEM;
        for (Gate gate : gates) {
            ItemActionAdmissionTest.TestPlayer player = ItemActionAdmissionTest.player();
            gate.setup.accept(player);
            assertGate(player, use, gate.reason);
            assertGate(player, new NEPyRpcPacket16(), InputBlockReason.NONE);
            doReturn(false).when(player).isAlive();
            assertGate(player, use, InputBlockReason.DEAD);
        }
    }

    private static void assertGate(ItemActionAdmissionTest.TestPlayer player, DataPacket packet, InputBlockReason expected) {
        assertEquals(expected == InputBlockReason.NONE, BetweenTickPackets.canRunBetweenTicks(player, packet));
        assertEquals(expected, BetweenTickPackets.blockReason(player, packet));
    }
}

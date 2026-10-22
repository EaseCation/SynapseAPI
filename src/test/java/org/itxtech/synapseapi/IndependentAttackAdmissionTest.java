package org.itxtech.synapseapi;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.knockback.KnockbackProfile;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.inventory.InventorySlotReference;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.network.input.InputValidationResult;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IndependentAttackAdmissionTest {
    @BeforeAll static void initialize() { LegacyMovementAdmissionTest.protocols(); }

    @Test void originalEntryAndVerifierRejectCancelledOrRetiredInput() {
        for (String state : List.of("normal", "cancel", "retired", "uninitialized", "dead", "stale-position", "out-of-reach", "validator-reject")) {
            try (EquipmentAdmissionTest.Fixture f = new EquipmentAdmissionTest.Fixture(true)) {
                Entity target = configure(f);
                if (state.equals("cancel")) f.cancel = true;
                if (state.equals("retired")) doReturn(false).when(f.player).isAcceptingInputPackets();
                if (state.equals("uninitialized")) doReturn(false).when(f.player).isInitialized();
                if (state.equals("dead")) f.player.makeDead();
                if (state.equals("stale-position")) doReturn(false).when(f.player).isCurrentInputPosition();
                if (state.equals("out-of-reach")) doReturn(false).when(f.player).canInteract(any(Vector3.class), nullable(AxisAlignedBB.class), anyDouble());
                if (state.equals("validator-reject")) when(f.processor.validateReceivedPacket(any(), any(), anyBoolean())).thenReturn(InputValidationResult.REJECTED);
                InventoryTransactionPacket116 packet = attack(f);
                f.deliver(packet);
                int entry = state.equals("uninitialized") || state.equals("retired") ? 0 : 1;
                verify(f.plugins, times(entry)).callEvent(any(DataPacketReceiveEvent.class));
                verify(f.processor, times(entry)).validateReceivedPacket(f.player, packet, state.equals("cancel"));
                verify(target, times(state.equals("normal") ? 1 : 0)).attack(any(EntityDamageEvent.class));
                verify(f.player, never()).onUpdate(anyInt());
            }
        }
    }

    @Test void repeatedAttacksKeepOriginalAttemptLimit() {
        for (boolean enabled : new boolean[]{true, false}) {
            try (EquipmentAdmissionTest.Fixture f = new EquipmentAdmissionTest.Fixture(enabled)) {
                Entity target = configure(f);
                f.deliver(attack(f));
                f.deliver(attack(f));
                verify(target).attack(any(EntityDamageEvent.class));
                assertEquals(2, f.player.getAttackAttemptsInCurrentWindow());
            }
        }
    }

    @Test void interactionAndComplexTransactionPartsAreNotNewlyAdmitted() {
        try (EquipmentAdmissionTest.Fixture f = new EquipmentAdmissionTest.Fixture(true)) {
            InventoryTransactionPacket116 p = attack(f);
            assertTrue(BetweenTickPackets.supports(p));
            ((UseItemOnEntityData) p.transactionData).actionType = InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_INTERACT;
            assertFalse(BetweenTickPackets.supports(p));
            ((UseItemOnEntityData) p.transactionData).actionType = InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_ATTACK;
            p.isCraftingPart = true;assertFalse(BetweenTickPackets.supports(p));
            p.isCraftingPart = false;p.isEnchantingPart = true;assertFalse(BetweenTickPackets.supports(p));
            p.isEnchantingPart = false;p.isRepairItemPart = true;assertFalse(BetweenTickPackets.supports(p));
        }
    }

    private static Entity configure(EquipmentAdmissionTest.Fixture f) {
        Entity target = mock(Entity.class);
        when(target.getId()).thenReturn(42L);
        when(target.getLevel()).thenReturn(f.level);
        when(target.isAlive()).thenReturn(true);
        when(f.level.getEntity(42)).thenReturn(target);
        when(target.attack(any(EntityDamageEvent.class))).thenReturn(true);
        doReturn(true).when(f.player).canInteract(any(Vector3.class), nullable(AxisAlignedBB.class), anyDouble());
        doReturn(new KnockbackProfile("test")).when(f.player).getKnockbackProfile();
        InventorySlotReference source = mock(InventorySlotReference.class);
        when(source.isCurrent()).thenReturn(true);
        when(source.isSelectedBy(f.slots)).thenReturn(true);
        doReturn(source).when(f.slots).captureHeldItem();
        return target;
    }

    private static InventoryTransactionPacket116 attack(EquipmentAdmissionTest.Fixture f) {
        UseItemOnEntityData data = new UseItemOnEntityData();
        data.actionType = InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_ATTACK;
        data.entityRuntimeId = 42;
        data.itemInHand = f.slots.getItemInHand();
        InventoryTransactionPacket116 packet = new InventoryTransactionPacket116();
        packet.transactionType = InventoryTransactionPacket116.TYPE_USE_ITEM_ON_ENTITY;
        packet.transactionData = data;
        packet.actions = new NetworkInventoryAction[0];
        return packet;
    }
}

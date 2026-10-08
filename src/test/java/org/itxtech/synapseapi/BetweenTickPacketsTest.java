package org.itxtech.synapseapi;

import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.types.ContainerIds;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol121120.protocol.AnimatePacket121120;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.AnimatePacket121130;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.NEPyRpcPacket16;
import org.itxtech.synapseapi.network.protocol.mod.StoreBuySuccessPacket;

import java.util.List;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.types.ItemStackRequest;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.multiprotocol.common.inventory.LegacySetItemSlotData;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket.PlayerBlockAction;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.NetworkStackLatencyPacket19;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BetweenTickPacketsTest {
    @Test
    void normalizedRpcKeepsItsSubpacketOrderAheadOfTheFollowingAction() {
        NEPyRpcPacket16 packet = new NEPyRpcPacket16();
        StoreBuySuccessPacket first = new StoreBuySuccessPacket();
        StoreBuySuccessPacket second = new StoreBuySuccessPacket();
        packet.subPackets = List.of(first, second);
        assertTrue(BetweenTickPackets.supports(packet));
        assertSame(first, packet.subPackets.getFirst());
        assertSame(second, packet.subPackets.getLast());
        SynapsePlayer player = mock(SynapsePlayer.class);
        when(player.canProcessInputStateBetweenTicks()).thenReturn(true);
        assertTrue(BetweenTickPackets.canRunBetweenTicks(player, packet));
        when(player.canProcessInputStateBetweenTicks()).thenReturn(false);
        assertFalse(BetweenTickPackets.canRunBetweenTicks(player, packet));
        packet.subPackets = null;
        assertFalse(BetweenTickPackets.supports(packet));
    }
    @Test
    void stateAndReleaseCanProgressWhileMovementWaitsForItemUse() {
        SynapsePlayer player = mock(SynapsePlayer.class);
        when(player.canProcessInputStateBetweenTicks()).thenReturn(true);
        when(player.canProcessItemActionBetweenTicks()).thenReturn(true);
        when(player.canProcessMovementBetweenTicks()).thenReturn(false);
        MobEquipmentPacket equipment = new MobEquipmentPacket();
        assertTrue(BetweenTickPackets.canRunBetweenTicks(player, equipment));
        assertTrue(BetweenTickPackets.canRunBetweenTicks(player, release()));
        assertFalse(BetweenTickPackets.canRunBetweenTicks(player, movement()));
    }

    @Test
    void aLifecycleChangeBeforeConsumptionKeepsItemActionsBehindTheHead() {
        SynapsePlayer player = mock(SynapsePlayer.class);
        InventoryTransactionPacket116 packet = release();
        when(player.canProcessItemActionBetweenTicks()).thenReturn(true);
        assertTrue(BetweenTickPackets.canRunBetweenTicks(player, packet));
        when(player.canProcessItemActionBetweenTicks()).thenReturn(false);
        assertFalse(BetweenTickPackets.canRunBetweenTicks(player, packet));
        assertTrue(BetweenTickPackets.canRunBetweenTicks(player, new NetworkStackLatencyPacket19()));
    }

    @Test
    void onlyReviewedSwingBranchesAreAdmittedAcrossAnimationLayouts() {
        AnimatePacket121120 old = new AnimatePacket121120();
        AnimatePacket121130 current = new AnimatePacket121130();
        for (AnimatePacket.Action action : AnimatePacket.Action.values()) {
            old.action = action;
            current.action = action;
            assertEquals(action == AnimatePacket.Action.SWING_ARM, BetweenTickPackets.supports(old));
            assertEquals(action == AnimatePacket.Action.SWING_ARM, BetweenTickPackets.supports(current));
        }
        old.action = current.action = AnimatePacket.Action.SWING_ARM;
        old.data = Float.NaN;
        current.data = Float.POSITIVE_INFINITY;
        assertFalse(BetweenTickPackets.supports(old));
        assertFalse(BetweenTickPackets.supports(current));
    }

    @Test
    void inventoryAndCraftingWorkRemainWholePacketBarriers() {
        InventoryTransactionPacket116 packet = release();
        assertTrue(BetweenTickPackets.supports(packet));
        for (int source : new int[]{NetworkInventoryAction.SOURCE_WORLD,
                NetworkInventoryAction.SOURCE_CREATIVE, NetworkInventoryAction.SOURCE_TODO}) {
            NetworkInventoryAction action = new NetworkInventoryAction();
            action.sourceType = source;
            packet.actions = new NetworkInventoryAction[]{action};
            assertFalse(BetweenTickPackets.supports(packet));
        }
        packet.actions = new NetworkInventoryAction[0];
        packet.legacyRequestId = 1;
        assertTrue(BetweenTickPackets.supports(packet));
        packet.legacyRequestId = 0;
        packet.isCraftingPart = true;
        assertFalse(BetweenTickPackets.supports(packet));
        packet.isCraftingPart = false;
        packet.transactionType = InventoryTransactionPacket116.TYPE_NORMAL;
        assertFalse(BetweenTickPackets.supports(packet));
    }

    @Test
    void legacySlotSynchronizationStaysAttachedToTheWholeStandaloneUse() {
        InventoryTransactionPacket116 packet = release();
        packet.legacyRequestId = -32;
        LegacySetItemSlotData slots = new LegacySetItemSlotData();
        // 真实基岩 3.9 的旧版同步编号，与动作窗口编号不同。
        slots.containerId = 30;
        slots.changedSlotIndexes = new byte[]{1};
        packet.requestChangedSlots = new LegacySetItemSlotData[]{slots};
        NetworkInventoryAction action = new NetworkInventoryAction();
        action.sourceType = NetworkInventoryAction.SOURCE_CONTAINER;
        action.windowId = ContainerIds.INVENTORY;
        action.inventorySlot = 1;
        packet.actions = new NetworkInventoryAction[]{action};

        assertTrue(BetweenTickPackets.supports(packet));
        assertEquals(-32, packet.legacyRequestId);
        assertSame(slots, packet.requestChangedSlots[0]);
        assertSame(action, packet.actions[0]);

        slots.containerId = ContainerIds.UI;
        assertTrue(BetweenTickPackets.supports(packet));

        // 同步元数据不能让真实的 UI 容器动作越过屏障。
        action.windowId = ContainerIds.UI;
        slots.containerId = ContainerIds.INVENTORY;
        assertFalse(BetweenTickPackets.supports(packet));
        action.windowId = ContainerIds.INVENTORY;
        action.sourceType = NetworkInventoryAction.SOURCE_CREATIVE;
        assertFalse(BetweenTickPackets.supports(packet));
    }

    @Test
    void meleeAndAirUsePreserveTheirOriginalPayloadsAndRestrictOtherSubactions() {
        InventoryTransactionPacket116 packet = new InventoryTransactionPacket116();
        packet.actions = new NetworkInventoryAction[0];
        UseItemOnEntityData entity = new UseItemOnEntityData();
        entity.itemInHand = mock(Item.class);
        entity.actionType = InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_ATTACK;
        packet.transactionType = InventoryTransactionPacket116.TYPE_USE_ITEM_ON_ENTITY;
        packet.transactionData = entity;
        assertTrue(BetweenTickPackets.supports(packet));
        entity.actionType = InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_INTERACT;
        assertFalse(BetweenTickPackets.supports(packet));
        UseItemData use = new UseItemData();
        use.itemInHand = mock(Item.class);
        use.actionType = InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR;
        packet.transactionType = InventoryTransactionPacket116.TYPE_USE_ITEM;
        packet.transactionData = use;
        assertTrue(BetweenTickPackets.supports(packet));
        use.actionType = InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_BLOCK;
        assertFalse(BetweenTickPackets.supports(packet));
    }

    private static InventoryTransactionPacket116 release() {
        InventoryTransactionPacket116 packet = new InventoryTransactionPacket116();
        packet.actions = new NetworkInventoryAction[0];
        packet.transactionType = InventoryTransactionPacket116.TYPE_RELEASE_ITEM;
        ReleaseItemData release = new ReleaseItemData();
        release.itemInHand = mock(Item.class);
        release.actionType = InventoryTransactionPacket116.RELEASE_ITEM_ACTION_RELEASE;
        packet.transactionData = release;
        return packet;
    }
    private static DataPacket movement() {
        return mock(DataPacket.class, withSettings().extraInterfaces(IPlayerAuthInputPacket.class));
    }

    private static void flags(IPlayerAuthInputPacket input, int layout, int... flags) {
        long lower = 0;
        long upper = 0;
        for (int flag : flags) {
            int bit = flag >= PlayerAuthInputFlags.ACK_ENTITY_DATA ? flag + layout : flag;
            if (bit < Long.SIZE) {
                lower |= 1L << bit;
            } else {
                upper |= 1L << (bit - Long.SIZE);
            }
        }
        when(input.getNeteaseFlagsVersion()).thenReturn(layout);
        when(input.getInputFlags()).thenReturn(lower);
        when(input.getInputFlags2()).thenReturn(upper);
    }

    @Test
    void movementAndCrossWordRawInputAreAcceptedInEachImplementedLayout() {
        DataPacket packet = movement();
        IPlayerAuthInputPacket input = (IPlayerAuthInputPacket) packet;
        for (int layout = 0; layout <= 2; layout++) {
            flags(input, layout, PlayerAuthInputFlags.UP, PlayerAuthInputFlags.START_SPRINTING,
                    PlayerAuthInputFlags.START_JUMPING, PlayerAuthInputFlags.HANDLED_TELEPORT, PlayerAuthInputFlags.MISSED_SWING,
                    PlayerAuthInputFlags.ACK_ENTITY_DATA, PlayerAuthInputFlags.HORIZONTAL_COLLISION,
                    PlayerAuthInputFlags.SNEAK_PRESSED_RAW, PlayerAuthInputFlags.SNEAK_CURRENT_RAW);
            assertTrue(BetweenTickPackets.supports(packet));
        }
    }

    @Test
    void embeddedAirUseKeepsItsPayloadAndUsesTheOriginalLayoutFlag() {
        DataPacket packet = movement();
        IPlayerAuthInputPacket input = (IPlayerAuthInputPacket) packet;
        when(input.hasFlag(anyInt())).thenCallRealMethod();
        UseItemData use = new UseItemData();
        use.itemInHand = mock(Item.class);
        use.actionType = InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR;
        when(input.getUseItemData()).thenReturn(use);
        NetworkInventoryAction action = new NetworkInventoryAction();
        action.sourceType = NetworkInventoryAction.SOURCE_CONTAINER;
        action.windowId = ContainerIds.INVENTORY;
        when(input.getInventoryActions()).thenReturn(new NetworkInventoryAction[]{action});
        when(input.getLegacyRequestId()).thenReturn(-32);
        LegacySetItemSlotData sync = new LegacySetItemSlotData();
        when(input.getRequestChangedSlots()).thenReturn(new LegacySetItemSlotData[]{sync});
        for (int layout = 0; layout <= 2; layout++) {
            flags(input, layout, PlayerAuthInputFlags.UP, PlayerAuthInputFlags.MISSED_SWING, PlayerAuthInputFlags.PERFORM_ITEM_INTERACTION);
            assertTrue(BetweenTickPackets.supports(packet));
            assertSame(use, input.getUseItemData());
            assertSame(action, input.getInventoryActions()[0]);
            assertSame(sync, input.getRequestChangedSlots()[0]);
        }
        flags(input, 2, PlayerAuthInputFlags.UP);
        assertFalse(BetweenTickPackets.supports(packet));
    }

    @Test
    void embeddedUseCannotAdmitContainerCraftingOrMixedWorldActions() {
        DataPacket packet = movement();
        IPlayerAuthInputPacket input = (IPlayerAuthInputPacket) packet;
        when(input.hasFlag(anyInt())).thenCallRealMethod();
        flags(input, 2, PlayerAuthInputFlags.PERFORM_ITEM_INTERACTION);
        UseItemData use = new UseItemData();
        use.actionType = InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR;
        use.itemInHand = mock(Item.class);
        when(input.getUseItemData()).thenReturn(use);
        NetworkInventoryAction action = new NetworkInventoryAction();
        action.sourceType = NetworkInventoryAction.SOURCE_CONTAINER;
        action.windowId = ContainerIds.UI;
        when(input.getInventoryActions()).thenReturn(new NetworkInventoryAction[]{action});
        assertFalse(BetweenTickPackets.supports(packet));
        action.windowId = ContainerIds.INVENTORY;
        assertTrue(BetweenTickPackets.supports(packet));
        action.sourceType = NetworkInventoryAction.SOURCE_CREATIVE;
        assertFalse(BetweenTickPackets.supports(packet));
        action.sourceType = NetworkInventoryAction.SOURCE_CONTAINER;
        when(input.isCraftingPart()).thenReturn(true);
        assertFalse(BetweenTickPackets.supports(packet));
        when(input.isCraftingPart()).thenReturn(false);
        when(input.getBlockActions()).thenReturn(new PlayerBlockAction[1]);
        assertFalse(BetweenTickPackets.supports(packet));
        when(input.getBlockActions()).thenReturn(null);
        use.actionType = InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_BLOCK;
        assertFalse(BetweenTickPackets.supports(packet));
    }

    @Test
    void unreviewedActionsAndVehicleHintsRemainWholePacketBarriers() {
        DataPacket packet = movement();
        IPlayerAuthInputPacket input = (IPlayerAuthInputPacket) packet;
        int[] barriers = {PlayerAuthInputFlags.START_SWIMMING, PlayerAuthInputFlags.START_GLIDING,
                PlayerAuthInputFlags.START_CRAWLING, PlayerAuthInputFlags.START_FLYING,
                PlayerAuthInputFlags.START_USING_ITEM, PlayerAuthInputFlags.START_SPIN_ATTACK,
                PlayerAuthInputFlags.IN_CLIENT_PREDICTED_IN_VEHICLE, PlayerAuthInputFlags.PADDLE_LEFT,
                PlayerAuthInputFlags.PERFORM_ITEM_INTERACTION, PlayerAuthInputFlags.PERFORM_BLOCK_ACTIONS,
                PlayerAuthInputFlags.PERFORM_ITEM_STACK_REQUEST};
        for (int layout = 0; layout <= 2; layout++) {
            for (int flag : barriers) {
                flags(input, layout, PlayerAuthInputFlags.UP, flag);
                assertFalse(BetweenTickPackets.supports(packet));
            }
        }
    }

    @Test
    void payloadWithoutItsExpectedFlagStillRemainsABarrier() {
        DataPacket packet = movement();
        IPlayerAuthInputPacket input = (IPlayerAuthInputPacket) packet;
        when(input.getUseItemData()).thenReturn(mock(UseItemData.class));
        assertFalse(BetweenTickPackets.supports(packet));
        when(input.getUseItemData()).thenReturn(null);
        when(input.getItemStackRequest()).thenReturn(mock(ItemStackRequest.class));
        assertFalse(BetweenTickPackets.supports(packet));
        when(input.getItemStackRequest()).thenReturn(null);
        when(input.getInventoryActions()).thenReturn(new NetworkInventoryAction[1]);
        assertFalse(BetweenTickPackets.supports(packet));
        when(input.getInventoryActions()).thenReturn(null);
        when(input.getBlockActions()).thenReturn(new PlayerBlockAction[1]);
        assertFalse(BetweenTickPackets.supports(packet));
        when(input.getBlockActions()).thenReturn(null);
        when(input.getRequestChangedSlots()).thenReturn(new LegacySetItemSlotData[1]);
        assertFalse(BetweenTickPackets.supports(packet));
    }

    @Test
    void oldInventoryTransactionsRemainBarriers() {
        DataPacket packet = movement();
        IPlayerAuthInputPacket input = (IPlayerAuthInputPacket) packet;
        when(input.getLegacyRequestId()).thenReturn(1);
        assertFalse(BetweenTickPackets.supports(packet));
        when(input.getLegacyRequestId()).thenReturn(0);
        when(input.isCraftingPart()).thenReturn(true);
        assertFalse(BetweenTickPackets.supports(packet));
        when(input.isCraftingPart()).thenReturn(false);
        when(input.isEnchantingPart()).thenReturn(true);
        assertFalse(BetweenTickPackets.supports(packet));
        when(input.isEnchantingPart()).thenReturn(false);
        when(input.isRepairItemPart()).thenReturn(true);
        assertFalse(BetweenTickPackets.supports(packet));
    }

    @Test
    void unknownBitsAndUnknownLayoutsDoNotAcquireIdleEligibility() {
        DataPacket packet = movement();
        IPlayerAuthInputPacket input = (IPlayerAuthInputPacket) packet;
        when(input.getInputFlags2()).thenReturn(1L << 20);
        assertFalse(BetweenTickPackets.supports(packet));
        when(input.getInputFlags2()).thenReturn(0L);
        when(input.getNeteaseFlagsVersion()).thenReturn(3);
        assertFalse(BetweenTickPackets.supports(packet));
        when(input.getNeteaseFlagsVersion()).thenReturn(-1);
        assertFalse(BetweenTickPackets.supports(packet));
    }

    @Test
    void emptyPayloadContainersDoNotTurnPureMovementIntoInventoryWork() {
        DataPacket packet = movement();
        IPlayerAuthInputPacket input = (IPlayerAuthInputPacket) packet;
        when(input.getInventoryActions()).thenReturn(new NetworkInventoryAction[0]);
        when(input.getBlockActions()).thenReturn(new PlayerBlockAction[0]);
        when(input.getRequestChangedSlots()).thenReturn(new LegacySetItemSlotData[0]);
        assertTrue(BetweenTickPackets.supports(packet));
    }

    @Test
    void businessLatencyUsesTheSameEligibilityButOtherPacketsStayBarriers() {
        assertTrue(BetweenTickPackets.supports(new NetworkStackLatencyPacket19()));
        assertFalse(BetweenTickPackets.supports(mock(DataPacket.class)));
    }
}

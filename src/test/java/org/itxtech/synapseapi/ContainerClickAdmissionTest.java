package org.itxtech.synapseapi;

import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * 全包准入 M4/M5：普通事务（任意窗口容器动作、创造栏、丢弃、关窗溢出与信标）与实体交互整包放行；
 * 合成/附魔/修复分片及其他虚拟来源保留屏障。队列语义与包类型无关，用代表包验证突发与前序屏障。
 */
class ContainerClickAdmissionTest {
    @Test
    void normalTransactionsFromContainersCreativeAndDropsAreAdmitted() {
        assertTrue(BetweenTickPackets.supports(normal(container(7), container(ContainerIds.INVENTORY))));
        assertTrue(BetweenTickPackets.supports(normal(container(ContainerIds.ARMOR), container(ContainerIds.OFFHAND))));
        assertTrue(BetweenTickPackets.supports(normal(action(NetworkInventoryAction.SOURCE_CREATIVE, 0), container(ContainerIds.INVENTORY))));
        assertTrue(BetweenTickPackets.supports(normal(container(ContainerIds.INVENTORY), action(NetworkInventoryAction.SOURCE_WORLD, 0))));
        assertTrue(BetweenTickPackets.supports(normal(todo(NetworkInventoryAction.SOURCE_TYPE_CONTAINER_DROP_CONTENTS), action(NetworkInventoryAction.SOURCE_WORLD, 0))));
        assertTrue(BetweenTickPackets.supports(normal(todo(NetworkInventoryAction.SOURCE_TYPE_BEACON), container(ContainerIds.INVENTORY))));
        assertTrue(BetweenTickPackets.supports(normal()));
    }

    @Test
    void craftingTradingAndOtherVirtualSourcesRemainBarriers() {
        for (int window : new int[]{NetworkInventoryAction.SOURCE_TYPE_CRAFTING_ADD_INGREDIENT,
                NetworkInventoryAction.SOURCE_TYPE_CRAFTING_RESULT, NetworkInventoryAction.SOURCE_TYPE_ANVIL_INPUT,
                NetworkInventoryAction.SOURCE_TYPE_ENCHANT_INPUT, NetworkInventoryAction.SOURCE_TYPE_TRADING_INPUT_1}) {
            assertFalse(BetweenTickPackets.supports(normal(todo(window), container(ContainerIds.INVENTORY))), "todo " + window);
        }
        assertFalse(BetweenTickPackets.supports(normal(action(NetworkInventoryAction.SOURCE_CRAFT_SLOT, 0), container(0))));
        assertFalse(BetweenTickPackets.supports(normal(action(NetworkInventoryAction.SOURCE_GLOBAL, 0))));
        assertFalse(BetweenTickPackets.supports(normal(container(0), null)));
        InventoryTransactionPacket116 parts = normal(container(7), container(0));
        parts.isCraftingPart = true;
        assertFalse(BetweenTickPackets.supports(parts));
        parts.isCraftingPart = false;
        parts.isEnchantingPart = true;
        assertFalse(BetweenTickPackets.supports(parts));
        parts.isEnchantingPart = false;
        parts.isRepairItemPart = true;
        assertFalse(BetweenTickPackets.supports(parts));
        parts.isRepairItemPart = false;
        parts.actions = null;
        assertFalse(BetweenTickPackets.supports(parts));
    }

    @Test
    void entityInteractionIsAdmittedOnlyWithInventoryOrOffhandActions() {
        for (int type : new int[]{InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_INTERACT,
                InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_ITEM_INTERACT}) {
            InventoryTransactionPacket116 packet = entity(type);
            assertTrue(BetweenTickPackets.supports(packet));
            packet.actions = new NetworkInventoryAction[]{container(7)};
            assertFalse(BetweenTickPackets.supports(packet));
            packet.actions = new NetworkInventoryAction[0];
            ((UseItemOnEntityData) packet.transactionData).itemInHand = null;
            assertFalse(BetweenTickPackets.supports(packet));
        }
    }

    @Test
    void clickBurstDrainsBetweenTicksAndAnEarlierCraftingPartStillHoldsIt() {
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 64, 4096, 64, 4096,
                error -> fail(error));
        ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
        List<String> order = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            int index = i;
            dispatcher.offer(session, context -> {
                assertTrue(context.betweenTicks());
                order.add("click" + index);
            }, 1, BetweenTickPackets.supports(normal(container(7), container(ContainerIds.INVENTORY))));
            dispatcher.offer(session, context -> order.add("follower" + index), 1, true);
        }
        assertEquals(40, dispatcher.drain(true, 7, Long.MAX_VALUE));
        assertEquals(0, dispatcher.snapshot().queuedTasks());
        assertEquals("click0", order.getFirst());
        assertEquals("follower19", order.getLast());

        order.clear();
        InventoryTransactionPacket116 craft = normal(container(0));
        craft.isCraftingPart = true;
        dispatcher.offer(session, context -> order.add("craft"), 1, BetweenTickPackets.supports(craft));
        dispatcher.offer(session, context -> order.add("click"), 1,
                BetweenTickPackets.supports(normal(container(7), container(ContainerIds.INVENTORY))));
        assertEquals(0, dispatcher.drain(true, 8, Long.MAX_VALUE));
        assertEquals(2, dispatcher.drainWorldTick(9));
        assertEquals(List.of("craft", "click"), order);
    }

    private static InventoryTransactionPacket116 normal(NetworkInventoryAction... actions) {
        InventoryTransactionPacket116 packet = new InventoryTransactionPacket116();
        packet.transactionType = InventoryTransactionPacket116.TYPE_NORMAL;
        packet.actions = actions;
        return packet;
    }

    private static InventoryTransactionPacket116 entity(int type) {
        InventoryTransactionPacket116 packet = new InventoryTransactionPacket116();
        packet.transactionType = InventoryTransactionPacket116.TYPE_USE_ITEM_ON_ENTITY;
        packet.actions = new NetworkInventoryAction[0];
        UseItemOnEntityData data = new UseItemOnEntityData();
        data.actionType = type;
        data.itemInHand = mock(Item.class);
        packet.transactionData = data;
        return packet;
    }

    private static NetworkInventoryAction container(int window) {
        return action(NetworkInventoryAction.SOURCE_CONTAINER, window);
    }

    private static NetworkInventoryAction todo(int window) {
        return action(NetworkInventoryAction.SOURCE_TODO, window);
    }

    private static NetworkInventoryAction action(int source, int window) {
        NetworkInventoryAction action = new NetworkInventoryAction();
        action.sourceType = source;
        action.windowId = window;
        return action;
    }
}

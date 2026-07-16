package org.itxtech.synapseapi;

import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemUseHandResolverTest {

    @Test
    void acceptsPrivateOffhandMarkerOnlyWhenRuntimeCapabilityAllowsIt() {
        assertEquals(ItemUseHand.OFF_HAND, JavaItemUseRouting.resolve(true, -1));
        assertEquals(ItemUseHand.MAIN_HAND, JavaItemUseRouting.resolve(true, 4));
        assertEquals(ItemUseHand.MAIN_HAND, JavaItemUseRouting.resolve(false, -1));
        assertEquals(ItemUseHand.MAIN_HAND, JavaItemUseRouting.resolve(false, 0));
        assertEquals(ItemUseHand.MAIN_HAND, JavaItemUseRouting.resolve(false, 8));
        assertEquals(ItemUseHand.MAIN_HAND, JavaItemUseRouting.resolve(true, 8));
    }

    @Test
    void restrictsOffhandToRightClickAndEntityInteract() {
        assertTrue(JavaItemUseRouting.supportsUseItemAction(InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK));
        assertTrue(JavaItemUseRouting.supportsUseItemAction(InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR));
        assertFalse(JavaItemUseRouting.supportsUseItemAction(InventoryTransactionPacket.USE_ITEM_ACTION_BREAK_BLOCK));
        assertTrue(JavaItemUseRouting.supportsEntityAction(InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_INTERACT));
        assertFalse(JavaItemUseRouting.supportsEntityAction(InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_ATTACK));
    }

    @Test
    void scopesOnlySupportedEmbeddedUsesToRuntimeAllowedOffhand() {
        assertEquals(ItemUseHand.OFF_HAND, JavaItemUseRouting.resolveEmbeddedUse(
                true, -1, InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR));
        assertEquals(ItemUseHand.OFF_HAND, JavaItemUseRouting.resolveEmbeddedUse(
                true, -1, InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK));
        assertNull(JavaItemUseRouting.resolveEmbeddedUse(
                true, -1, InventoryTransactionPacket.USE_ITEM_ACTION_BREAK_BLOCK));

        assertEquals(ItemUseHand.MAIN_HAND, JavaItemUseRouting.resolveEmbeddedUse(
                false, -1, InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR));
        assertEquals(ItemUseHand.MAIN_HAND, JavaItemUseRouting.resolveEmbeddedUse(
                false, -1, InventoryTransactionPacket.USE_ITEM_ACTION_BREAK_BLOCK));
        assertEquals(ItemUseHand.MAIN_HAND, JavaItemUseRouting.resolveEmbeddedUse(
                true, 4, InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR));
    }

    @Test
    void enablesExplicitShieldUseOnlyWhenRuntimeCapabilityAllowsIt() {
        assertTrue(JavaItemUseRouting.supportsExplicitShieldUse(true, Item.SHIELD));
        assertFalse(JavaItemUseRouting.supportsExplicitShieldUse(false, Item.SHIELD));
        assertFalse(JavaItemUseRouting.supportsExplicitShieldUse(true, Item.IRON_SWORD));
    }

    @Test
    void rebindsOnlyAnActiveRuntimeAllowedOffhandUseRecordedUnderAnotherHand() {
        assertTrue(JavaItemUseRouting.shouldRebindStartedUse(
                true, ItemUseHand.OFF_HAND, true, ItemUseHand.MAIN_HAND));
        assertFalse(JavaItemUseRouting.shouldRebindStartedUse(
                false, ItemUseHand.OFF_HAND, true, ItemUseHand.MAIN_HAND));
        assertFalse(JavaItemUseRouting.shouldRebindStartedUse(
                true, ItemUseHand.MAIN_HAND, true, ItemUseHand.MAIN_HAND));
        assertFalse(JavaItemUseRouting.shouldRebindStartedUse(
                true, ItemUseHand.OFF_HAND, false, ItemUseHand.MAIN_HAND));
        assertFalse(JavaItemUseRouting.shouldRebindStartedUse(
                true, ItemUseHand.OFF_HAND, true, ItemUseHand.OFF_HAND));
    }

    @Test
    void protocol775UsesTheLegacyInventoryHandler() {
        assertTrue(JavaItemUseRouting.usesLegacyInventoryHandler(775));
        assertFalse(JavaItemUseRouting.usesLegacyInventoryHandler(
                JavaItemUseRouting.MODERN_INVENTORY_HANDLER_PROTOCOL));
    }
}

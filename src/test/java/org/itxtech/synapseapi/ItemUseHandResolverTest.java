package org.itxtech.synapseapi;

import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.inventory.transaction.data.TransactionData;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemUseHandResolverTest {

    @Test
    void routesPrivateOffhandMarkerUsingJavaIdentityAndRuntimeCapability() {
        assertEquals(JavaItemUseRouting.Route.OFF_HAND, JavaItemUseRouting.resolve(true, true, -1));
        assertEquals(JavaItemUseRouting.Route.REJECT, JavaItemUseRouting.resolve(true, false, -1));
        assertEquals(JavaItemUseRouting.Route.MAIN_HAND, JavaItemUseRouting.resolve(false, false, -1));
        assertEquals(JavaItemUseRouting.Route.MAIN_HAND, JavaItemUseRouting.resolve(false, true, -1));
        assertEquals(JavaItemUseRouting.Route.MAIN_HAND, JavaItemUseRouting.resolve(true, false, 4));
        assertEquals(JavaItemUseRouting.Route.MAIN_HAND, JavaItemUseRouting.resolve(false, false, 8));
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
        assertEquals(JavaItemUseRouting.Route.OFF_HAND, JavaItemUseRouting.resolveEmbeddedUse(
                true, true, -1, InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR));
        assertEquals(JavaItemUseRouting.Route.OFF_HAND, JavaItemUseRouting.resolveEmbeddedUse(
                true, true, -1, InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK));
        assertEquals(JavaItemUseRouting.Route.REJECT, JavaItemUseRouting.resolveEmbeddedUse(
                true, true, -1, InventoryTransactionPacket.USE_ITEM_ACTION_BREAK_BLOCK));

        assertEquals(JavaItemUseRouting.Route.REJECT, JavaItemUseRouting.resolveEmbeddedUse(
                true, false, -1, InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR));
        assertEquals(JavaItemUseRouting.Route.MAIN_HAND, JavaItemUseRouting.resolveEmbeddedUse(
                false, false, -1, InventoryTransactionPacket.USE_ITEM_ACTION_BREAK_BLOCK));
        assertEquals(JavaItemUseRouting.Route.MAIN_HAND, JavaItemUseRouting.resolveEmbeddedUse(
                true, false, 4, InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR));
    }

    @Test
    void rejectsEveryDisabledJavaTransactionBeforeHandlerDispatch() {
        UseItemData useItem = new UseItemData();
        useItem.hotbarSlot = -1;
        UseItemOnEntityData entityInteract = new UseItemOnEntityData();
        entityInteract.hotbarSlot = -1;
        ReleaseItemData release = new ReleaseItemData();
        release.hotbarSlot = -1;

        assertEquals(JavaItemUseRouting.Route.REJECT, JavaItemUseRouting.resolveTransaction(
                true, false, InventoryTransactionPacket.TYPE_USE_ITEM, useItem));
        assertEquals(JavaItemUseRouting.Route.REJECT, JavaItemUseRouting.resolveTransaction(
                true, false, InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY, entityInteract));
        assertEquals(JavaItemUseRouting.Route.REJECT, JavaItemUseRouting.resolveTransaction(
                true, false, InventoryTransactionPacket.TYPE_RELEASE_ITEM, release));
    }

    @Test
    void preservesNativeBedrockTransactionRoutingForPrivateMarkerValue() {
        UseItemData useItem = new UseItemData();
        useItem.hotbarSlot = -1;
        UseItemOnEntityData entityInteract = new UseItemOnEntityData();
        entityInteract.hotbarSlot = -1;
        ReleaseItemData release = new ReleaseItemData();
        release.hotbarSlot = -1;

        assertEquals(JavaItemUseRouting.Route.MAIN_HAND, JavaItemUseRouting.resolveTransaction(
                false, false, InventoryTransactionPacket.TYPE_USE_ITEM, useItem));
        assertEquals(JavaItemUseRouting.Route.MAIN_HAND, JavaItemUseRouting.resolveTransaction(
                false, false, InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY, entityInteract));
        assertEquals(JavaItemUseRouting.Route.MAIN_HAND, JavaItemUseRouting.resolveTransaction(
                false, false, InventoryTransactionPacket.TYPE_RELEASE_ITEM, release));
    }

    @Test
    void leavesNativeBedrockTransactionDataUntouchedForTheOriginalHandler() {
        TransactionData unexpectedData = new TransactionData() {
        };

        assertEquals(JavaItemUseRouting.Route.MAIN_HAND, JavaItemUseRouting.resolveTransaction(
                false, false, InventoryTransactionPacket.TYPE_USE_ITEM, unexpectedData));
        assertEquals(JavaItemUseRouting.Route.MAIN_HAND, JavaItemUseRouting.resolveTransaction(
                false, false, InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY, unexpectedData));
        assertEquals(JavaItemUseRouting.Route.MAIN_HAND, JavaItemUseRouting.resolveTransaction(
                false, false, InventoryTransactionPacket.TYPE_RELEASE_ITEM, unexpectedData));
        assertEquals(JavaItemUseRouting.Route.MAIN_HAND, JavaItemUseRouting.resolveTransaction(
                false, false, InventoryTransactionPacket.TYPE_USE_ITEM, null));
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
    void cancelsOnlyAnActiveOffhandUseForARejectedRelease() {
        assertTrue(JavaItemUseRouting.shouldCancelRejectedRelease(
                InventoryTransactionPacket.TYPE_RELEASE_ITEM,
                JavaItemUseRouting.Route.REJECT, true, ItemUseHand.OFF_HAND));
        assertFalse(JavaItemUseRouting.shouldCancelRejectedRelease(
                InventoryTransactionPacket.TYPE_USE_ITEM,
                JavaItemUseRouting.Route.REJECT, true, ItemUseHand.OFF_HAND));
        assertFalse(JavaItemUseRouting.shouldCancelRejectedRelease(
                InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY,
                JavaItemUseRouting.Route.REJECT, true, ItemUseHand.OFF_HAND));
        assertFalse(JavaItemUseRouting.shouldCancelRejectedRelease(
                InventoryTransactionPacket.TYPE_RELEASE_ITEM,
                JavaItemUseRouting.Route.REJECT, true, ItemUseHand.MAIN_HAND));
        assertFalse(JavaItemUseRouting.shouldCancelRejectedRelease(
                InventoryTransactionPacket.TYPE_RELEASE_ITEM,
                JavaItemUseRouting.Route.REJECT, false, ItemUseHand.OFF_HAND));
        assertFalse(JavaItemUseRouting.shouldCancelRejectedRelease(
                InventoryTransactionPacket.TYPE_RELEASE_ITEM,
                JavaItemUseRouting.Route.MAIN_HAND, true, ItemUseHand.OFF_HAND));
    }

    @Test
    void protocol775UsesTheLegacyInventoryHandler() {
        assertTrue(JavaItemUseRouting.usesLegacyInventoryHandler(775));
        assertFalse(JavaItemUseRouting.usesLegacyInventoryHandler(
                JavaItemUseRouting.MODERN_INVENTORY_HANDLER_PROTOCOL));
    }
}

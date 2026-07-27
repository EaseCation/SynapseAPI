package org.itxtech.synapseapi;

import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.inventory.transaction.data.TransactionData;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.PlayerActionPacket;

final class JavaItemUseRouting {

    static final int OFFHAND_HOTBAR_SLOT = -1;
    static final int MODERN_INVENTORY_HANDLER_PROTOCOL = 1001; // Bedrock 1.26.30

    private JavaItemUseRouting() {
    }

    enum Route {
        MAIN_HAND(ItemUseHand.MAIN_HAND),
        OFF_HAND(ItemUseHand.OFF_HAND),
        REJECT(null);

        private final ItemUseHand hand;

        Route(ItemUseHand hand) {
            this.hand = hand;
        }

        ItemUseHand hand() {
            if (this.hand == null) {
                throw new IllegalStateException("Rejected item use has no interaction hand");
            }
            return this.hand;
        }
    }

    static Route resolve(boolean javaClient, boolean offhandAllowed, int hotbarSlot) {
        if (hotbarSlot != OFFHAND_HOTBAR_SLOT || !javaClient) {
            return Route.MAIN_HAND;
        }
        return offhandAllowed ? Route.OFF_HAND : Route.REJECT;
    }

    static Route resolveTransaction(boolean javaClient,
                                    boolean offhandAllowed,
                                    int transactionType,
                                    TransactionData transactionData) {
        if (!javaClient || transactionData == null) {
            return Route.MAIN_HAND;
        }
        return switch (transactionType) {
            case InventoryTransactionPacket.TYPE_USE_ITEM -> resolve(
                    javaClient, offhandAllowed, ((UseItemData) transactionData).hotbarSlot);
            case InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY -> resolve(
                    javaClient, offhandAllowed, ((UseItemOnEntityData) transactionData).hotbarSlot);
            case InventoryTransactionPacket.TYPE_RELEASE_ITEM -> resolve(
                    javaClient, offhandAllowed, ((ReleaseItemData) transactionData).hotbarSlot);
            default -> Route.MAIN_HAND;
        };
    }

    static boolean supportsUseItemAction(int actionType) {
        return actionType == InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK
                || actionType == InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR;
    }

    static Route resolveEmbeddedUse(boolean javaClient, boolean offhandAllowed, int hotbarSlot, int actionType) {
        Route route = resolve(javaClient, offhandAllowed, hotbarSlot);
        return route != Route.OFF_HAND || supportsUseItemAction(actionType) ? route : Route.REJECT;
    }

    static boolean supportsEntityAction(int actionType) {
        return actionType == InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_INTERACT;
    }

    static boolean supportsExplicitShieldUse(boolean offhandAllowed, int itemId) {
        return offhandAllowed && itemId == Item.SHIELD;
    }

    static boolean startsServerManagedItemUseOn(boolean javaClient, int actionType) {
        return !javaClient && actionType == PlayerActionPacket.ACTION_ITEM_USE_ON_START;
    }

    static boolean shouldRebindStartedUse(boolean offhandAllowed,
                                          ItemUseHand interactionHand,
                                          boolean usingItem,
                                          ItemUseHand usingHand) {
        return offhandAllowed
                && interactionHand == ItemUseHand.OFF_HAND
                && usingItem
                && usingHand != ItemUseHand.OFF_HAND;
    }

    static boolean shouldCancelRejectedRelease(int transactionType,
                                               Route route,
                                               boolean usingItem,
                                               ItemUseHand usingHand) {
        return transactionType == InventoryTransactionPacket.TYPE_RELEASE_ITEM
                && route == Route.REJECT
                && usingItem
                && usingHand == ItemUseHand.OFF_HAND;
    }

    static boolean usesLegacyInventoryHandler(int protocol) {
        return protocol < MODERN_INVENTORY_HANDLER_PROTOCOL;
    }
}

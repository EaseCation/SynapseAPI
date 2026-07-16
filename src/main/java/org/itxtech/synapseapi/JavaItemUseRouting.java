package org.itxtech.synapseapi;

import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.InventoryTransactionPacket;

final class JavaItemUseRouting {

    static final int OFFHAND_HOTBAR_SLOT = -1;
    static final int MODERN_INVENTORY_HANDLER_PROTOCOL = 1001; // Bedrock 1.26.30

    private JavaItemUseRouting() {
    }

    static ItemUseHand resolve(boolean offhandAllowed, int hotbarSlot) {
        return offhandAllowed && hotbarSlot == OFFHAND_HOTBAR_SLOT
                ? ItemUseHand.OFF_HAND
                : ItemUseHand.MAIN_HAND;
    }

    static boolean supportsUseItemAction(int actionType) {
        return actionType == InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK
                || actionType == InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR;
    }

    static ItemUseHand resolveEmbeddedUse(boolean offhandAllowed, int hotbarSlot, int actionType) {
        ItemUseHand hand = resolve(offhandAllowed, hotbarSlot);
        return hand != ItemUseHand.OFF_HAND || supportsUseItemAction(actionType) ? hand : null;
    }

    static boolean supportsEntityAction(int actionType) {
        return actionType == InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_INTERACT;
    }

    static boolean supportsExplicitShieldUse(boolean offhandAllowed, int itemId) {
        return offhandAllowed && itemId == Item.SHIELD;
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

    static boolean usesLegacyInventoryHandler(int protocol) {
        return protocol < MODERN_INVENTORY_HANDLER_PROTOCOL;
    }
}

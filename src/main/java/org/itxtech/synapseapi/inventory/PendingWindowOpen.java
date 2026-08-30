package org.itxtech.synapseapi.inventory;

import cn.nukkit.inventory.Inventory;

public record PendingWindowOpen(Inventory inventory, int windowId, boolean permanent, boolean alwaysOpen) {
}

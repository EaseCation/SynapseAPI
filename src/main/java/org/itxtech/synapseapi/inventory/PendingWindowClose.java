package org.itxtech.synapseapi.inventory;

import cn.nukkit.inventory.Inventory;

public record PendingWindowClose(Inventory inventory, int windowId, boolean closeEventFired) {
}

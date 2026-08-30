package org.itxtech.synapseapi.inventory;

import cn.nukkit.inventory.Inventory;

import javax.annotation.Nullable;

public final class PendingWindowCloseTracker {
    @Nullable
    private PendingWindowClose pending;

    public boolean begin(int windowId, Inventory inventory, boolean closeEventFired) {
        if (this.pending != null) {
            return false;
        }
        this.pending = new PendingWindowClose(inventory, windowId, closeEventFired);
        return true;
    }

    @Nullable
    public PendingWindowClose acknowledge(int windowId) {
        PendingWindowClose current = this.pending;
        if (current == null || current.windowId() != windowId) {
            return null;
        }
        this.pending = null;
        return current;
    }

    public boolean hasPending() {
        return this.pending != null;
    }

    @Nullable
    public PendingWindowClose getPending() {
        return this.pending;
    }

    public void clear() {
        this.pending = null;
    }
}

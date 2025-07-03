package org.itxtech.synapseapi.event.server;

import cn.nukkit.event.HandlerList;
import cn.nukkit.event.server.ServerEvent;

public class ItemRegistryChecksumChangedEvent extends ServerEvent {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private final long checksum;

    public ItemRegistryChecksumChangedEvent(long checksum) {
        this.checksum = checksum;
    }

    public long getChecksum() {
        return checksum;
    }
}

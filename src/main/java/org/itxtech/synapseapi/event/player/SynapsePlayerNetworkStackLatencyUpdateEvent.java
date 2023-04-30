package org.itxtech.synapseapi.event.player;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import org.itxtech.synapseapi.SynapsePlayer;

public class SynapsePlayerNetworkStackLatencyUpdateEvent extends SynapsePlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final long originTimestamp;
    private final long legacy;

    public SynapsePlayerNetworkStackLatencyUpdateEvent(SynapsePlayer player, long originTimestamp, long legacy) {
        super(player);
        this.originTimestamp = originTimestamp;
        this.legacy = legacy;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public long getOriginTimestamp() {
        return originTimestamp;
    }

    public long getLegacy() {
        return legacy;
    }
}

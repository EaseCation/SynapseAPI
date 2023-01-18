package org.itxtech.synapseapi.event.player;

import cn.nukkit.event.HandlerList;
import org.itxtech.synapseapi.SynapsePlayer;

public class SynapsePlayerTooManyBatchPacketsEvent extends SynapsePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public final int count;

    public SynapsePlayerTooManyBatchPacketsEvent(SynapsePlayer player, int count) {
        super(player);
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}

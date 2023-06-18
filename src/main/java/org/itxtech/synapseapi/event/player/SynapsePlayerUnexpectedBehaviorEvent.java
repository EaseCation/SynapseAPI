package org.itxtech.synapseapi.event.player;

import cn.nukkit.event.HandlerList;
import org.itxtech.synapseapi.SynapsePlayer;

public class SynapsePlayerUnexpectedBehaviorEvent extends SynapsePlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private final String tag;

    public SynapsePlayerUnexpectedBehaviorEvent(SynapsePlayer player, String tag) {
        super(player);
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}

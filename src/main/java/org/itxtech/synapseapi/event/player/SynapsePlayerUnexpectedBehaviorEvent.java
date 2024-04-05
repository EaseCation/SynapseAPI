package org.itxtech.synapseapi.event.player;

import cn.nukkit.event.HandlerList;
import org.itxtech.synapseapi.SynapsePlayer;

public class SynapsePlayerUnexpectedBehaviorEvent extends SynapsePlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private final String tag;
    private final String context;

    public SynapsePlayerUnexpectedBehaviorEvent(SynapsePlayer player, String tag) {
        this(player, tag, "");
    }

    public SynapsePlayerUnexpectedBehaviorEvent(SynapsePlayer player, String tag, String context) {
        super(player);
        this.tag = tag;
        this.context = context;
    }

    public String getTag() {
        return tag;
    }

    public String getContext() {
        return context;
    }
}

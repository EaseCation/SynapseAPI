package org.itxtech.synapseapi.event.player;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import org.itxtech.synapseapi.SynapsePlayer;

public class SynapsePlayerPreChatEvent extends SynapsePlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private String message;

    public SynapsePlayerPreChatEvent(SynapsePlayer player, String message) {
        super(player);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

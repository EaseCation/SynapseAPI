package org.itxtech.synapseapi.event.player.netease;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.event.player.SynapsePlayerEvent;

/**
 * Created by boybook on 16/6/25.
 */
public class SynapsePlayerNetEaseStoreBuySuccEvent extends SynapsePlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public SynapsePlayerNetEaseStoreBuySuccEvent(SynapsePlayer player) {
        super(player);
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

}

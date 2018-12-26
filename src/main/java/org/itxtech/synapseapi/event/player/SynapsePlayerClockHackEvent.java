package org.itxtech.synapseapi.event.player;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import org.itxtech.synapseapi.SynapsePlayer;

/**
 * Created by boybook on 16/6/25.
 */
public class SynapsePlayerClockHackEvent extends SynapsePlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final int movePlayerPacketCount;

    public SynapsePlayerClockHackEvent(SynapsePlayer player, int movePlayerPacketCount) {
        super(player);
        this.movePlayerPacketCount = movePlayerPacketCount;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public int getMovePlayerPacketCount() {
        return movePlayerPacketCount;
    }
}

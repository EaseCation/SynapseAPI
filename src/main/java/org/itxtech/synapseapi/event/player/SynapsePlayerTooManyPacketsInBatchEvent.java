package org.itxtech.synapseapi.event.player;

import cn.nukkit.event.HandlerList;
import org.itxtech.synapseapi.SynapsePlayer;

public class SynapsePlayerTooManyPacketsInBatchEvent extends SynapsePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public final short[] packetCount;

    public SynapsePlayerTooManyPacketsInBatchEvent(SynapsePlayer player, short[] packetCount) {
        super(player);
        this.packetCount = packetCount;
    }

    public short[] getPacketCount() {
        return packetCount;
    }
}

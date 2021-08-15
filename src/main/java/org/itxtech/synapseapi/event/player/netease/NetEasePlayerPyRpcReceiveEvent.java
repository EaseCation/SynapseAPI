package org.itxtech.synapseapi.event.player.netease;

import cn.nukkit.event.HandlerList;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.event.player.SynapsePlayerEvent;
import org.msgpack.value.Value;

public class NetEasePlayerPyRpcReceiveEvent extends SynapsePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Value data;

    public NetEasePlayerPyRpcReceiveEvent(SynapsePlayer player, Value data) {
        super(player);
        this.data = data;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public Value getData() {
        return data;
    }

}

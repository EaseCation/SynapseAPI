package org.itxtech.synapseapi;

import org.itxtech.synapseapi.network.protocol.mod.ServerSubPacketHandler;
import org.itxtech.synapseapi.network.protocol.mod.ServerboundSubPacket;

public abstract class ServerboundAbstractSubPacketHandler<P extends ServerboundSubPacket<?>> implements ServerSubPacketHandler<P> {
    protected final SynapsePlayer player;

    public ServerboundAbstractSubPacketHandler(SynapsePlayer player) {
        this.player = player;
    }
}

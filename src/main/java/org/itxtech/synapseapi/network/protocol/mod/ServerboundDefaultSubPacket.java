package org.itxtech.synapseapi.network.protocol.mod;

import org.itxtech.synapseapi.ServerboundDefaultSubPacketHandler;

public interface ServerboundDefaultSubPacket extends ServerboundSubPacket<ServerboundDefaultSubPacketHandler> {
    @Override
    default Class<ServerboundDefaultSubPacketHandler> getHandlerClass() {
        return ServerboundDefaultSubPacketHandler.class;
    }
}

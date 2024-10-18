package org.itxtech.synapseapi.network.protocol.mod;

import org.itxtech.synapseapi.ClientboundDefaultSubPacketHandler;

public interface ClientboundDefaultSubPacket extends ClientboundSubPacket<ClientboundDefaultSubPacketHandler> {
    @Override
    default Class<ClientboundDefaultSubPacketHandler> getHandlerClass() {
        return ClientboundDefaultSubPacketHandler.class;
    }
}

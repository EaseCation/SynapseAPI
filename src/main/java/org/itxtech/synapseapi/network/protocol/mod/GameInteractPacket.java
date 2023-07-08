package org.itxtech.synapseapi.network.protocol.mod;

import org.itxtech.synapseapi.network.protocol.mod.data.InteractType;

public record GameInteractPacket(InteractType type) implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

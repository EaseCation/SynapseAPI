package org.itxtech.synapseapi.network.protocol.mod;

import org.itxtech.synapseapi.network.protocol.mod.data.ClientScreenInfo;

public record DisplayEnvironmentPacket(ClientScreenInfo info) implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

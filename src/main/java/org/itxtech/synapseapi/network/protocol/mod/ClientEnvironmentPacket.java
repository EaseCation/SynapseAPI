package org.itxtech.synapseapi.network.protocol.mod;

import org.itxtech.synapseapi.network.protocol.mod.data.ECModClientInfo;

public record ClientEnvironmentPacket(ECModClientInfo info) implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

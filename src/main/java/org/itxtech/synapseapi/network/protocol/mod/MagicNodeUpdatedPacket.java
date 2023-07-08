package org.itxtech.synapseapi.network.protocol.mod;

public record MagicNodeUpdatedPacket() implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

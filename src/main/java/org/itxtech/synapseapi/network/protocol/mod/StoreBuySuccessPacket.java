package org.itxtech.synapseapi.network.protocol.mod;

public record StoreBuySuccessPacket() implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

package org.itxtech.synapseapi.network.protocol.mod;

public record StoreBuySuccessPacket() implements ServerboundSubPacket<ServerSubPacketHandler> {
    @Override
    public void handle(ServerSubPacketHandler handler) {
        handler.handle(this);
    }
}

package org.itxtech.synapseapi.network.protocol.mod;

public record AnimationEmotePacket(String emoteName) implements ServerboundSubPacket<ServerSubPacketHandler> {
    @Override
    public void handle(ServerSubPacketHandler handler) {
        handler.handle(this);
    }
}

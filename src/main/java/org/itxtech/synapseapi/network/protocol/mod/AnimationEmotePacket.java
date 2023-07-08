package org.itxtech.synapseapi.network.protocol.mod;

public record AnimationEmotePacket(String emoteName) implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

package org.itxtech.synapseapi.network.protocol.mod;

public record HudInteractPacket(String button) implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

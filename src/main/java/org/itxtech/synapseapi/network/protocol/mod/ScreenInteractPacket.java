package org.itxtech.synapseapi.network.protocol.mod;

public record ScreenInteractPacket(int touchEvent, String buttonName) implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

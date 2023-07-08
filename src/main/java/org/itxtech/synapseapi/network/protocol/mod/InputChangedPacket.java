package org.itxtech.synapseapi.network.protocol.mod;

public record InputChangedPacket(int inputMode) implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

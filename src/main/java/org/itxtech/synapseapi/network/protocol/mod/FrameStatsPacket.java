package org.itxtech.synapseapi.network.protocol.mod;

public record FrameStatsPacket(int requestId, float fps) implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

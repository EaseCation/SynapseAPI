package org.itxtech.synapseapi.network.protocol.mod;

public record ErrorReportPacket(String playerId, String exception, String data, String stackTrace, String report) implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

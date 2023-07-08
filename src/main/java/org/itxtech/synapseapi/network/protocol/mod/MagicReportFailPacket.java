package org.itxtech.synapseapi.network.protocol.mod;

import java.time.Instant;

public record MagicReportFailPacket(Instant time, String error) implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

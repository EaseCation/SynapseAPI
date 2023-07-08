package org.itxtech.synapseapi.network.protocol.mod;

import java.time.Instant;
import java.util.List;

public record MagicReportPacket(Instant time, List<String> result, Integer gameType, Boolean selinux) implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

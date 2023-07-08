package org.itxtech.synapseapi.network.protocol.mod;

import java.time.Instant;
import java.util.List;

public record MagicDirectoryResultPacket(Instant time, List<String> result) implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

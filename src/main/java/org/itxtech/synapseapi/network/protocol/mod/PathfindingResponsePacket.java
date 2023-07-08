package org.itxtech.synapseapi.network.protocol.mod;

import org.itxtech.synapseapi.network.protocol.mod.data.NavPathResult;

public record PathfindingResponsePacket(int requestId, NavPathResult result) implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

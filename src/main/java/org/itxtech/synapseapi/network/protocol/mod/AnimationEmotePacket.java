package org.itxtech.synapseapi.network.protocol.mod;

import org.itxtech.synapseapi.ServerboundDefaultSubPacketHandler;

public record AnimationEmotePacket(String emoteName) implements ServerboundDefaultSubPacket {
    @Override
    public void handle(ServerboundDefaultSubPacketHandler handler) {
        handler.handle(this);
    }
}

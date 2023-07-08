package org.itxtech.synapseapi.network.protocol.mod;

import cn.nukkit.math.Vector3;

public record DimensionChangedPacket(String playerId, int fromDimensionId, int toDimensionId, Vector3 toPos) implements ServerboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }
}

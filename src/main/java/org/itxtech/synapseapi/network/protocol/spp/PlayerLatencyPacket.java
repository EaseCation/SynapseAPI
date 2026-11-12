package org.itxtech.synapseapi.network.protocol.spp;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;

import java.util.UUID;

public class PlayerLatencyPacket extends SynapseDataPacket {
    public static final int NETWORK_ID = SynapseInfo.PLAYER_LATENCY_PACKET;

    public ObjectIntPair<UUID>[] pings;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void encode() {
        reset();
        putUnsignedVarInt(pings.length);
        for (ObjectIntPair<UUID> data : pings) {
            putUUID(data.left());
            putVarInt(data.rightInt());
        }
    }

    @Override
    public void decode() {
        long count = getUnsignedVarInt();
        int maxCount = (getCount() - getOffset()) / (Long.BYTES * 2 + 1);
        this.pings = getArray(checkLength(count, maxCount), new ObjectIntPair[0],
                stream -> ObjectIntPair.of(stream.getUUID(), stream.getVarInt()), maxCount);
    }
}

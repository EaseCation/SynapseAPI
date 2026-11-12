package org.itxtech.synapseapi.network.protocol.spp;

import cn.nukkit.utils.BinaryStream;
import java.util.UUID;

/**
 * Author: PeratX
 * SynapseAPI Project
 */
public class BroadcastPacket extends SynapseDataPacket {
    public static final int NETWORK_ID = SynapseInfo.BROADCAST_PACKET;

    public UUID[] sessionIds;
    public boolean direct;
    public byte[] payload;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void encode() {
        this.reset();
        this.putBoolean(this.direct);
        this.putUnsignedVarInt(this.sessionIds.length);
        for (UUID sessionId : this.sessionIds) {
            this.putUUID(sessionId);
        }
        this.putUnsignedVarInt(this.payload.length);
        this.put(this.payload);
    }

    @Override
    public void decode() {
        this.direct = this.getBoolean();
        long count = this.getUnsignedVarInt();
        int remaining = this.getCount() - this.getOffset();
        if (remaining < 1) {
            throw new IndexOutOfBoundsException("Missing broadcast payload length");
        }
        int maxCount = (remaining - 1) / (Long.BYTES * 2);
        this.sessionIds = this.getArray(checkLength(count, maxCount), new UUID[0], BinaryStream::getUUID, maxCount);
        this.payload = this.getByteArray();
    }
}

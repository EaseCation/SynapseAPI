package org.itxtech.synapseapi.network.protocol.spp;

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
        int len = (int) this.getUnsignedVarInt();
        this.sessionIds = new UUID[len];
        for (int i = 0; i < len; i++) {
            this.sessionIds[i] = this.getUUID();
        }
        this.payload = this.get((int) this.getUnsignedVarInt());
    }
}

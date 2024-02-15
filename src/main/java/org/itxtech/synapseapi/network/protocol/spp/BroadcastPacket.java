package org.itxtech.synapseapi.network.protocol.spp;

import java.util.UUID;

/**
 * Author: PeratX
 * SynapseAPI Project
 */
public class BroadcastPacket extends SynapseDataPacket {
    public static final int NETWORK_ID = SynapseInfo.BROADCAST_PACKET;

    public UUID[] entries;
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
        this.putUnsignedVarInt(this.entries.length);
        for (UUID uniqueId : this.entries) {
            this.putUUID(uniqueId);
        }
        this.putUnsignedVarInt(this.payload.length);
        this.put(this.payload);
    }

    @Override
    public void decode() {
        this.direct = this.getBoolean();
        int len = (int) this.getUnsignedVarInt();
        this.entries = new UUID[len];
        for (int i = 0; i < len; i++) {
            this.entries[i] = this.getUUID();
        }
        this.payload = this.get((int) this.getUnsignedVarInt());
    }
}

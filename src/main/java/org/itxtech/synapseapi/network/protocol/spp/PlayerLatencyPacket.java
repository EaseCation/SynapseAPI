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
        int length = (int) getUnsignedVarInt();
        this.pings = new  ObjectIntPair[length];
        for (int i = 0; i < length; i++) {
            this.pings[i] = ObjectIntPair.of(getUUID(), getVarInt());
        }
    }
}

package org.itxtech.synapseapi.multiprotocol.protocol117.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class SyncEntityPropertyPacket117 extends Packet117 {
    public static final int NETWORK_ID = ProtocolInfo.SYNC_ACTOR_PROPERTY_PACKET;

    /**
     * CompoundTag.
     */
    public byte[] nbt;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();
        put(nbt);
    }
}

package org.itxtech.synapseapi.multiprotocol.protocol117.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class AddVolumeEntityPacket117 extends Packet117 {
    public static final int NETWORK_ID = ProtocolInfo.ADD_VOLUME_ENTITY_PACKET;

    public int netId;
    /**
     * CompoundTag.
     */
    public byte[] components;

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
        putUnsignedVarInt(netId);
        put(components);
    }
}

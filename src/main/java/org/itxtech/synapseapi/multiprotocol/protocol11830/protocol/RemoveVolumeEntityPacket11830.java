package org.itxtech.synapseapi.multiprotocol.protocol11830.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class RemoveVolumeEntityPacket11830 extends Packet11830 {
    public static final int NETWORK_ID = ProtocolInfo.REMOVE_VOLUME_ENTITY_PACKET;

    public int netId;
    public int dimension;

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
        putVarInt(dimension);
    }
}

package org.itxtech.synapseapi.multiprotocol.protocol11950.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class UpdateClientInputLocksPacket11950 extends Packet11950 {
    public static final int NETWORK_ID = ProtocolInfo.UPDATE_CLIENT_INPUT_LOCKS_PACKET;

    public int flags;
    public float x;
    public float y;
    public float z;

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
        putUnsignedVarInt(flags);
        putVector3f(x, y, z);
    }
}

package org.itxtech.synapseapi.multiprotocol.protocol11980.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class OpenSignPacket11980 extends Packet11980 {
    public static final int NETWORK_ID = ProtocolInfo.OPEN_SIGN_PACKET;

    public int x;
    public int y;
    public int z;
    public boolean front;

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
        putBlockVector3(x, y, z);
        putBoolean(front);
    }
}

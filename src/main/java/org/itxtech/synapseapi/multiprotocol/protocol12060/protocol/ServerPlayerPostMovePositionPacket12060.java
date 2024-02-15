package org.itxtech.synapseapi.multiprotocol.protocol12060.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;

public class ServerPlayerPostMovePositionPacket12060 extends Packet12060 {
    public static final int NETWORK_ID = ProtocolInfo.SERVER_PLAYER_POST_MOVE_POSITION_PACKET;

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

        putVector3f(x, y, z);
    }
}

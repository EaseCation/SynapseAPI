package org.itxtech.synapseapi.multiprotocol.protocol12180.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class PlayerLocationPacket12180 extends Packet12180 {
    public static final int NETWORK_ID = ProtocolInfo.PLAYER_LOCATION_PACKET;

    public boolean hide;
    public long entityUniqueId;
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
        putLInt(hide ? 1 : 0);
        putEntityUniqueId(entityUniqueId);
        if (!hide) {
            putVector3f(x, y, z);
        }
    }
}

package org.itxtech.synapseapi.multiprotocol.protocol12140.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class SetMovementAuthorityPacket12140 extends Packet12140 {
    public static final int NETWORK_ID = ProtocolInfo.SET_MOVEMENT_AUTHORITY_PACKET;

    public static final int MODE_CLIENT_AUTHORITATIVE_V2 = 0;
    public static final int MODE_SERVER_AUTHORITATIVE_V3 = 1;

    public int mode = MODE_CLIENT_AUTHORITATIVE_V2;

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
        putByte(mode);
    }
}

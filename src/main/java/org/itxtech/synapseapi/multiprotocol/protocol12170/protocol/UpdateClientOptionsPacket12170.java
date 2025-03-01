package org.itxtech.synapseapi.multiprotocol.protocol12170.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class UpdateClientOptionsPacket12170 extends Packet12170 {
    public static final int NETWORK_ID = ProtocolInfo.UPDATE_CLIENT_OPTIONS_PACKET;

    public static final int GRAPHICS_MODE_SIMPLE = 0;
    public static final int GRAPHICS_MODE_FANCY = 1;
    public static final int GRAPHICS_MODE_ADVANCED = 2;
    public static final int GRAPHICS_MODE_RAY_TRACED = 3;

    public Integer newGraphicsMode;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        if (getBoolean()) {
            newGraphicsMode = getByte();
        }
    }

    @Override
    public void encode() {
    }
}

package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;

@ToString
public class UpdateClientOptionsPacket12620 extends Packet12620 {
    public static final int NETWORK_ID = ProtocolInfo.UPDATE_CLIENT_OPTIONS_PACKET;

    public static final int GRAPHICS_MODE_SIMPLE = 0;
    public static final int GRAPHICS_MODE_FANCY = 1;
    public static final int GRAPHICS_MODE_ADVANCED = 2;
    public static final int GRAPHICS_MODE_RAY_TRACED = 3;

    public Integer newGraphicsMode;
    public Boolean filterProfanityChange;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        newGraphicsMode = getOptional(BinaryStream::getByte);
        filterProfanityChange = getOptional(BinaryStream::getBoolean);
    }

    @Override
    public void encode() {
    }
}

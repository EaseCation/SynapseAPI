package org.itxtech.synapseapi.multiprotocol.protocol12170.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class PlayerVideoCapturePacket12170 extends Packet12170 {
    public static final int NETWORK_ID = ProtocolInfo.PLAYER_VIDEO_CAPTURE_PACKET;

    public boolean action;
    public int frameRate;
    public String filePrefix = "";

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
        putBoolean(action);
        if (action) {
            putLInt(frameRate);
            putString(filePrefix);
        }
    }
}

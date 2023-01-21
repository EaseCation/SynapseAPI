package org.itxtech.synapseapi.multiprotocol.protocol110.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class VideoStreamConnectPacket110 extends Packet110 {

    public static final byte NETWORK_ID = ProtocolInfo.VIDEO_STREAM_CONNECT_PACKET;

    public static final byte ACTION_OPEN = 0;
    public static final byte ACTION_CLOSE = 1;

    public String address;
    public float screenshotFrequency;
    public byte action;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putString(address);
        this.putLFloat(screenshotFrequency);
        this.putByte(action);
    }

}

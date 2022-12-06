package org.itxtech.synapseapi.multiprotocol.protocol119.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class ToastRequestPacket119 extends Packet119 {
    public static final int NETWORK_ID = ProtocolInfo.TOAST_REQUEST_PACKET;

    public String title = "";
    public String content = "";

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
        this.putString(this.title);
        this.putString(this.content);
    }
}

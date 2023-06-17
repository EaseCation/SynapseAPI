package org.itxtech.synapseapi.multiprotocol.protocol11930.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class RequestNetworkSettingsPacket11930 extends Packet11930 {
    public static final int NETWORK_ID = ProtocolInfo.REQUEST_NETWORK_SETTINGS_PACKET;

    public int protocol;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.protocol = this.getInt();
    }

    @Override
    public void encode() {
    }
}

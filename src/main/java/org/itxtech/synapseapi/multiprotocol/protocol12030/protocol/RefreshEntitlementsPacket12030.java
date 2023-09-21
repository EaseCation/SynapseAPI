package org.itxtech.synapseapi.multiprotocol.protocol12030.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class RefreshEntitlementsPacket12030 extends Packet12030 {
    public static final int NETWORK_ID = ProtocolInfo.REFRESH_ENTITLEMENTS_PACKET;

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
    }
}

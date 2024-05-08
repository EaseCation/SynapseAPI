package org.itxtech.synapseapi.multiprotocol.protocol15.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class SetLocalPlayerAsInitializedPacket15 extends Packet15 {
    public static final int NETWORK_ID = ProtocolInfo.SET_LOCAL_PLAYER_AS_INITIALIZED_PACKET;

    public long eid;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        eid = this.getEntityRuntimeId();
    }

    @Override
    public void encode() {
    }
}

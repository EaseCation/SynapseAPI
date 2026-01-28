package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class DebugInfoPacket116 extends Packet116 {
    public static final int NETWORK_ID = ProtocolInfo.DEBUG_INFO_PACKET;

    public long entityUniqueId;
    public String data = "";

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        entityUniqueId = getEntityUniqueId();
        data = getString();
    }

    @Override
    public void encode() {
        reset();
        putEntityUniqueId(entityUniqueId);
        putString(data);
    }
}

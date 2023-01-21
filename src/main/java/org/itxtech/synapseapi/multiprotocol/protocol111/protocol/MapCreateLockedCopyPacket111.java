package org.itxtech.synapseapi.multiprotocol.protocol111.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class MapCreateLockedCopyPacket111 extends Packet111 {

    public long originalMapId;
    public long newMapId;

    @Override
    public int pid() {
        return ProtocolInfo.MAP_CREATE_LOCKED_COPY_PACKET;
    }

    @Override
    public void decode() {
        this.originalMapId = this.getVarLong();
        this.newMapId = this.getVarLong();
    }

    @Override
    public void encode() {
    }
}

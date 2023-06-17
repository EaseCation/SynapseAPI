package org.itxtech.synapseapi.multiprotocol.protocol11830.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class TickingAreasLoadStatusPacket11830 extends Packet11830 {
    public static final int NETWORK_ID = ProtocolInfo.TICKING_AREAS_LOAD_STATUS_PACKET;

    public boolean waitingForPreload;

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
        putBoolean(waitingForPreload);
    }
}


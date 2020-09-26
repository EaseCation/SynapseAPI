package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;

public class TickSyncPacket113 extends Packet113 {

    public static final int NETWORK_ID = ProtocolInfo.TICK_SYNC_PACKET;

    public long requestTimestamp;
    public long responseTimestamp;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.requestTimestamp = this.getLLong();
        this.responseTimestamp = this.getLLong();
    }

    @Override
    public void encode() {
        this.reset();
        this.putLLong(this.requestTimestamp);
        this.putLLong(this.responseTimestamp);
    }
}

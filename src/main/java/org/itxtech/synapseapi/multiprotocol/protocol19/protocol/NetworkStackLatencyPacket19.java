package org.itxtech.synapseapi.multiprotocol.protocol19.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;

public class NetworkStackLatencyPacket19 extends Packet19 {

    public long timestamp;
    public boolean isFromServer;

    @Override
    public int pid() {
        return ProtocolInfo.NETWORK_STACK_LATENCY_PACKET;
    }

    @Override
    public void decode() {
        timestamp = this.getLLong();
        isFromServer = this.getBoolean();
    }

    @Override
    public void encode() {
        this.reset();
        this.putLLong(timestamp);
        this.putBoolean(isFromServer);
    }
}

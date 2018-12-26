package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;

public class NetworkStackLatencyPacket16 extends Packet16 {

    public long timestamp;

    @Override
    public int pid() {
        return ProtocolInfo.NETWORK_STACK_LATENCY_PACKET;
    }

    @Override
    public void decode() {
        timestamp = this.getLLong();
    }

    @Override
    public void encode() {
        this.reset();
        this.putLLong(timestamp);
    }
}

package org.itxtech.synapseapi.multiprotocol.protocol11930.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class ServerStatsPacket11930 extends Packet11930 {
    public static final int NETWORK_ID = ProtocolInfo.SERVER_STATS_PACKET;

    public float serverTime;
    public float networkTime;

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
        this.putLFloat(this.serverTime);
        this.putLFloat(this.networkTime);
    }
}

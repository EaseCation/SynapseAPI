package org.itxtech.synapseapi.multiprotocol.protocol11980.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class RequestChunkRadiusPacket11980 extends Packet11980 {
    public static final int NETWORK_ID = ProtocolInfo.REQUEST_CHUNK_RADIUS_PACKET;

    public int radius;
    public int maxRadius;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.radius = this.getVarInt();
        this.maxRadius = this.getByte();
    }

    @Override
    public void encode() {
    }
}

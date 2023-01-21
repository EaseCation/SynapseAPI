package org.itxtech.synapseapi.multiprotocol.protocol118.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class SubChunkRequestPacket118 extends Packet118 {

    public static final int NETWORK_ID = ProtocolInfo.SUB_CHUNK_REQUEST_PACKET;

    public int dimension;
    public int subChunkX;
    public int subChunkY;
    public int subChunkZ;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.dimension = this.getVarInt();
        this.subChunkX = this.getVarInt();
        this.subChunkY = this.getVarInt();
        this.subChunkZ = this.getVarInt();
    }

    @Override
    public void encode() {

    }
}

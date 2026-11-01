package org.itxtech.synapseapi.multiprotocol.protocol12630.protocol;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol118.protocol.SubChunkRequestPacket118;

@ToString
public class SubChunkRequestPacket12630 extends Packet12630 {
    public static final int NETWORK_ID = ProtocolInfo.SUB_CHUNK_REQUEST_PACKET;

    public int dimension;
    public int subChunkX;
    public int subChunkY;
    public int subChunkZ;
    public BlockVector3[] positionOffsets;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.dimension = this.getVarInt();
        this.positionOffsets = this.getArray(new BlockVector3[0], stream -> new BlockVector3(stream.getSignedByte(), stream.getSignedByte(), stream.getSignedByte()), 8192);
        this.subChunkX = this.getLInt();
        this.subChunkY = this.getLInt();
        this.subChunkZ = this.getLInt();
    }

    @Override
    public void encode() {
    }

    @Override
    public DataPacket toDefault() {
        SubChunkRequestPacket118 pk = new SubChunkRequestPacket118();
        pk.dimension = this.dimension;
        pk.subChunkX = this.subChunkX;
        pk.subChunkY = this.subChunkY;
        pk.subChunkZ = this.subChunkZ;
        return pk;
    }
}

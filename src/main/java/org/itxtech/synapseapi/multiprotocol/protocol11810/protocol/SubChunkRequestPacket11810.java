package org.itxtech.synapseapi.multiprotocol.protocol11810.protocol;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.multiprotocol.protocol118.protocol.SubChunkRequestPacket118;

public class SubChunkRequestPacket11810 extends Packet11810 {

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
        this.subChunkX = this.getVarInt();
        this.subChunkY = this.getVarInt();
        this.subChunkZ = this.getVarInt();
        this.positionOffsets = this.getArrayLInt(BlockVector3.class, stream -> new BlockVector3(stream.getSingedByte(), stream.getSingedByte(), stream.getSingedByte()));
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

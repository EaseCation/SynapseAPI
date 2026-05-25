package org.itxtech.synapseapi.multiprotocol.protocol11810.protocol;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol118.protocol.SubChunkRequestPacket118;

@ToString
public class SubChunkRequestPacket11810 extends Packet11810 {

    public static final int NETWORK_ID = ProtocolInfo.SUB_CHUNK_REQUEST_PACKET;

    // A limit of how many sub chunks can client request within a single packet
    // It seems that client does not have any cap on how many sub chunks it can request,
    // and in some edge cases it requests all sub chunks within the view distance
    // The limit set here is based on maximum view distance vanilla client supports (96 chunks)
    private static final int MAX_SUB_CHUNKS = 96 * 96 * 64; // 96 chunks * 64 sub chunks (1024 blocks, -512 to 512) per chunk

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
        this.positionOffsets = this.getArray(stream -> {
            int count = stream.getLInt();
            if (count > MAX_SUB_CHUNKS) {
                throw new IndexOutOfBoundsException("too many array requests");
            }
            return count;
        }, new BlockVector3[0], stream -> new BlockVector3(stream.getSignedByte(), stream.getSignedByte(), stream.getSignedByte()));
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

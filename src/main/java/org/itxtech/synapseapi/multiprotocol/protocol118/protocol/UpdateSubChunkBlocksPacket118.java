package org.itxtech.synapseapi.multiprotocol.protocol118.protocol;

import cn.nukkit.block.Block;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.UpdateSubChunkBlocksPacket;
import cn.nukkit.network.protocol.types.BlockChangeEntry;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;

@ToString
public class UpdateSubChunkBlocksPacket118 extends Packet118 {

    public static final int NETWORK_ID = ProtocolInfo.UPDATE_SUB_CHUNK_BLOCKS_PACKET;

    public int subChunkBlockX;
    public int subChunkBlockY;
    public int subChunkBlockZ;
    public BlockChangeEntry[] layer0 = new BlockChangeEntry[0];
    public BlockChangeEntry[] layer1 = new BlockChangeEntry[0];

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
        this.putBlockVector3(this.subChunkBlockX, this.subChunkBlockY, this.subChunkBlockZ);

        this.putUnsignedVarInt(this.layer0.length);
        for (BlockChangeEntry entry : this.layer0) {
            writeEntry(entry);
        }

        this.putUnsignedVarInt(this.layer1.length);
        for (BlockChangeEntry entry : this.layer1) {
            writeEntry(entry);
        }
    }

    private void writeEntry(BlockChangeEntry entry) {
        this.putBlockVector3(entry.x(), entry.y(), entry.z());
        this.putUnsignedVarInt(entry.block());
        this.putUnsignedVarInt(entry.flags());
        this.putUnsignedVarLong(entry.syncedUpdateEntityUniqueId());
        this.putUnsignedVarInt(entry.syncedUpdateType());
    }

    public static BlockChangeEntry[] convertBlocks(BlockChangeEntry[] entries, AbstractProtocol protocol, boolean netease) {
        if (entries.length == 0) {
            return entries;
        }

        BlockChangeEntry[] result = new BlockChangeEntry[entries.length];
        for (int i = 0; i < entries.length; i++) {
            BlockChangeEntry entry = entries[i];
            int blockFullId = entry.block();
            result[i] = new BlockChangeEntry(
                    entry.x(),
                    entry.y(),
                    entry.z(),
                    AdvancedGlobalBlockPalette.getOrCreateRuntimeId(protocol, netease, Block.getIdFromFullId(blockFullId), Block.getDamageFromFullId(blockFullId)),
                    entry.flags(),
                    entry.syncedUpdateEntityUniqueId(),
                    entry.syncedUpdateType()
            );
        }
        return result;
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        UpdateSubChunkBlocksPacket packet = (UpdateSubChunkBlocksPacket) pk;
        this.subChunkBlockX = packet.subChunkBlockX;
        this.subChunkBlockY = packet.subChunkBlockY;
        this.subChunkBlockZ = packet.subChunkBlockZ;
        this.layer0 = convertBlocks(packet.layer0, protocol, netease);
        this.layer1 = convertBlocks(packet.layer1, protocol, netease);
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return UpdateSubChunkBlocksPacket.class;
    }
}

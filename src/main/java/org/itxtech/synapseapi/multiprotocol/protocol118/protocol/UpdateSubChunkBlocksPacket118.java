package org.itxtech.synapseapi.multiprotocol.protocol118.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;

@ToString
public class UpdateSubChunkBlocksPacket118 extends Packet118 {

    public static final int NETWORK_ID = ProtocolInfo.UPDATE_SUB_CHUNK_BLOCKS_PACKET;

    public int subChunkX;
    public int subChunkY;
    public int subChunkZ;
    public Entry[] layer0 = new Entry[0];
    public Entry[] layer1 = new Entry[0];

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
        this.putBlockVector3(this.subChunkX, this.subChunkY, this.subChunkZ);
        this.putUnsignedVarInt(this.layer0.length);
        for (Entry update : this.layer0) {
            update.write(this);
        }
        this.putUnsignedVarInt(this.layer1.length);
        for (Entry update : this.layer1) {
            update.write(this);
        }
    }

    @ToString
    public static class Entry {
        public final int x;
        public final int y;
        public final int z;
        public final int blockRuntimeId;
        public final int flags;
        // These two fields are useless 99.9% of the time; they are here to allow this packet to provide UpdateBlockSyncedPacket functionality.
        public final long syncedUpdateEntityUniqueId;
        public final int syncedUpdateType;

        public Entry(int x, int y, int z, int blockRuntimeId) {
            this(x, y, z, blockRuntimeId, UpdateBlockPacket.FLAG_ALL_PRIORITY);
        }

        public Entry(int x, int y, int z, int blockRuntimeId, int flags) {
            this(x, y, z, blockRuntimeId, flags, 0, 0);
        }

        public Entry(int x, int y, int z, int blockRuntimeId, int flags, long syncedUpdateEntityUniqueId, int syncedUpdateType) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockRuntimeId = blockRuntimeId;
            this.flags = flags;
            this.syncedUpdateEntityUniqueId = syncedUpdateEntityUniqueId;
            this.syncedUpdateType = syncedUpdateType;
        }

        private void write(BinaryStream stream) {
            stream.putBlockVector3(this.x, this.y, this.z);
            stream.putUnsignedVarInt(this.blockRuntimeId);
            stream.putUnsignedVarInt(this.flags);
            stream.putUnsignedVarLong(this.syncedUpdateEntityUniqueId);
            stream.putUnsignedVarInt(this.syncedUpdateType);
        }
    }
}

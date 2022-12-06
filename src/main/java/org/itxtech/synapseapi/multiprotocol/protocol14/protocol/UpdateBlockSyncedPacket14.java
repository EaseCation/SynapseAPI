package org.itxtech.synapseapi.multiprotocol.protocol14.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class UpdateBlockSyncedPacket14 extends Packet14 {
    public static final int NETWORK_ID = ProtocolInfo.UPDATE_BLOCK_SYNCED_PACKET;

    public static final int TYPE_NONE = 0;
    public static final int TYPE_CREATE = 1;
    public static final int TYPE_DESTROY = 2;

    public int x;
    public int z;
    public int y;
    public int blockRuntimeId;
    public int flags;
    public int dataLayer = 0;

    public long entityUniqueId;
    public int type = TYPE_NONE;

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
        this.putBlockVector3(this.x, this.y, this.z);
        this.putUnsignedVarInt(this.blockRuntimeId);
        this.putUnsignedVarInt(this.flags);
        this.putUnsignedVarInt(this.dataLayer);
        this.putUnsignedVarLong(this.entityUniqueId);
        this.putUnsignedVarLong(this.type);
    }
}

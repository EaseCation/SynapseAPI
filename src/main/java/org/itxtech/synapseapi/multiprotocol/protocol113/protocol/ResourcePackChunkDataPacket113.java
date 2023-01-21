package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.UUID;

@ToString(exclude = "data")
public class ResourcePackChunkDataPacket113 extends Packet113 {

    public static final int NETWORK_ID = ProtocolInfo.RESOURCE_PACK_CHUNK_DATA_PACKET;

    public UUID packId;
    public int chunkIndex;
    public long progress;
    public byte[] data;

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putString(this.packId.toString());
        this.putLInt(this.chunkIndex);
        this.putLLong(this.progress);
        this.putByteArray(this.data);
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.ResourcePackChunkDataPacket.class);
        cn.nukkit.network.protocol.ResourcePackChunkDataPacket packet = (cn.nukkit.network.protocol.ResourcePackChunkDataPacket) pk;

        this.packId = UUID.fromString(packet.packId);
        this.chunkIndex = packet.chunkIndex;
        this.progress = packet.progress;
        this.data = packet.data;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.ResourcePackChunkDataPacket.class;
    }
}

package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.UUID;

public class ResourcePackDataInfoPacket113 extends Packet113 {

    public static final int NETWORK_ID = ProtocolInfo.RESOURCE_PACK_DATA_INFO_PACKET;

    public static final int TYPE_INVALID = 0;
    public static final int TYPE_ADDON = 1;
    public static final int TYPE_CACHED = 2;
    public static final int TYPE_COPY_PROTECTED = 3;
    public static final int TYPE_BEHAVIOR = 4;
    public static final int TYPE_PERSONA_PIECE = 5;
    public static final int TYPE_RESOURCE = 6;
    public static final int TYPE_SKINS = 7;
    public static final int TYPE_WORLD_TEMPLATE = 8;
    public static final int TYPE_COUNT = 9;

    public UUID packId;
    public int maxChunkSize;
    public int chunkCount;
    public long compressedPackSize;
    public byte[] sha256;
    public boolean premium;
    public int type = TYPE_RESOURCE;

    @Override
    public void decode() {
        this.packId = UUID.fromString(this.getString());
        this.maxChunkSize = this.getLInt();
        this.chunkCount = this.getLInt();
        this.compressedPackSize = this.getLLong();
        this.sha256 = this.getByteArray();
        this.premium = this.getBoolean();
        this.type = this.getByte();
    }

    @Override
    public void encode() {
        this.reset();
        this.putString(this.packId.toString());
        this.putLInt(this.maxChunkSize);
        this.putLInt(this.chunkCount);
        this.putLLong(this.compressedPackSize);
        this.putByteArray(this.sha256);
        this.putBoolean(this.premium);
        this.putByte((byte) this.type);
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.ResourcePackDataInfoPacket.class);
        cn.nukkit.network.protocol.ResourcePackDataInfoPacket packet = (cn.nukkit.network.protocol.ResourcePackDataInfoPacket) pk;

        this.packId = UUID.fromString(packet.packId);
        this.maxChunkSize = packet.maxChunkSize;
        this.chunkCount = packet.chunkCount;
        this.compressedPackSize = packet.compressedPackSize;
        this.sha256 = packet.sha256;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.ResourcePackDataInfoPacket.class;
    }

}

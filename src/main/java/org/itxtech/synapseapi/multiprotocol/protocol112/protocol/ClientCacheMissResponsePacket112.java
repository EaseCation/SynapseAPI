package org.itxtech.synapseapi.multiprotocol.protocol112.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class ClientCacheMissResponsePacket112 extends Packet112 {

    public static final int NETWORK_ID = ProtocolInfo.CLIENT_CACHE_MISS_RESPONSE_PACKET;

    public Long2ObjectMap<byte[]> blobs = new Long2ObjectOpenHashMap<>();

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
        this.putUnsignedVarInt(this.blobs.size());
        this.blobs.forEach((id, blob) -> {
            this.putLLong(id);
            this.putByteArray(blob);
        });
    }
}

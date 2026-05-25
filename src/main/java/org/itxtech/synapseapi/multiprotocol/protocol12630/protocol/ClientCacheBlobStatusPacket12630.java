package org.itxtech.synapseapi.multiprotocol.protocol12630.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.ToString;

@ToString
public class ClientCacheBlobStatusPacket12630 extends Packet12630 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENT_CACHE_BLOB_STATUS_PACKET;

    public LongSet missSet = new LongOpenHashSet();
    public LongSet hitSet = new LongOpenHashSet();

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        int missCount = (int) this.getUnsignedVarInt();
        if (missCount > 0xfff) {
            throw new IndexOutOfBoundsException("Too many BlobIDs");
        }
        if (!isReadable(missCount * 8)) {
            throw new ArrayIndexOutOfBoundsException("Insufficient data");
        }
        for (int i = 0; i < missCount; ++i) {
            this.missSet.add(this.getLLong());
        }

        int hitCount = (int) this.getUnsignedVarInt();
        if (hitCount > 0xfff) {
            throw new IndexOutOfBoundsException("Too many BlobIDs");
        }
        if (!isReadable(hitCount * 8)) {
            throw new ArrayIndexOutOfBoundsException("Insufficient data");
        }
        for (int i = 0; i < hitCount; ++i) {
            this.hitSet.add(this.getLLong());
        }
    }

    @Override
    public void encode() {
    }
}

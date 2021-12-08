package org.itxtech.synapseapi.multiprotocol.protocol112.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

public class ClientCacheBlobStatusPacket112 extends Packet112 {

    public static final int NETWORK_ID = ProtocolInfo.CLIENT_CACHE_BLOB_STATUS_PACKET;

    public long[] missHashes;
    public long[] hitHashes;

    /**
     * missHashes 的无重复集合.
     */
    public LongSet missSet;
    /**
     * hitHashes 的无重复集合.
     */
    public LongSet hitSet;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        int missCount = (int) this.getUnsignedVarInt();
        int hitCount = (int) this.getUnsignedVarInt();

        if (missCount + hitCount > 0xfff) {
            throw new IndexOutOfBoundsException("Too many BlobIDs");
        }

        this.missHashes = new long[missCount];
        for (int i = 0; i < missCount; ++i) {
            this.missHashes[i] = this.getLLong();
        }

        this.hitHashes = new long[hitCount];
        for (int i = 0; i < hitCount; ++i) {
            this.hitHashes[i] = this.getLLong();
        }

        // 1.18客户端会发送重复的hash...
        this.missSet = new LongOpenHashSet(this.missHashes);
        this.hitSet = new LongOpenHashSet(this.hitHashes);
    }

    @Override
    public void encode() {

    }
}

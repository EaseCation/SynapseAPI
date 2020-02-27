package org.itxtech.synapseapi.multiprotocol.protocol112.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;

public class ClientCacheBlobStatusPacket112 extends Packet112 {

    public static final int NETWORK_ID = ProtocolInfo.CLIENT_CACHE_BLOB_STATUS_PACKET;

    public long[] missHashes;
    public long[] hitHashes;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        int missCount = (int) this.getUnsignedVarInt();
        int hitCount = (int) this.getUnsignedVarInt();

        if (missCount + hitCount > 0xfff) {
            throw new ArrayIndexOutOfBoundsException("Too many BlobIDs");
        }

        this.missHashes = new long[missCount];
        for (int i = 0; i < missCount; ++i) {
            this.missHashes[i] = this.getLLong();
        }

        this.hitHashes = new long[hitCount];
        for (int i = 0; i < hitCount; ++i) {
            this.hitHashes[i] = this.getLLong();
        }
    }

    @Override
    public void encode() {

    }
}

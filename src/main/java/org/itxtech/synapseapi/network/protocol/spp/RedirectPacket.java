package org.itxtech.synapseapi.network.protocol.spp;

import cn.nukkit.network.CompressionAlgorithm;

import java.util.UUID;

/**
 * Created by boybook on 16/6/24.
 */
public class RedirectPacket extends SynapseDataPacket {

    public static final int NETWORK_ID = SynapseInfo.REDIRECT_PACKET;

    public UUID sessionId;
    public byte[] mcpeBuffer;
    public int protocol;
    public byte compressionAlgorithm = CompressionAlgorithm.ZLIB;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void encode() {
        this.reset();
        this.putUUID(this.sessionId);
        this.putInt(this.protocol);
        putByte(compressionAlgorithm);
        this.putUnsignedVarInt(this.mcpeBuffer.length);
        this.put(this.mcpeBuffer);
    }

    @Override
    public void decode() {
        this.sessionId = this.getUUID();
        this.protocol = this.getInt();
        compressionAlgorithm = getSingedByte();
        this.mcpeBuffer = this.get((int) this.getUnsignedVarInt());
    }
}

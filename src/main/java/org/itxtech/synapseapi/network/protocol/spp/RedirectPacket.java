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
    public RedirectTraceData traceData;

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
        if (this.traceData != null) {
            this.putInt(RedirectTraceData.MAGIC);
            this.putByte(RedirectTraceData.VERSION);
            this.putLong(this.traceData.traceId);
            this.putLong(this.traceData.clientTimestamp);
            this.putUnsignedVarInt(RedirectTraceData.FIELD_COUNT);
            for (int i = 0; i < RedirectTraceData.FIELD_COUNT; i++) {
                this.putLong(this.traceData.times[i]);
            }
            for (int i = 0; i < RedirectTraceData.FIELD_COUNT; i++) {
                this.putLong(this.traceData.wallTimes[i]);
            }
        }
    }

    @Override
    public void decode() {
        this.sessionId = this.getUUID();
        this.protocol = this.getInt();
        compressionAlgorithm = getSingedByte();
        this.mcpeBuffer = this.get((int) this.getUnsignedVarInt());
        if (this.isReadable(4)) {
            int oldOffset = this.offset;
            int magic = this.getInt();
            if (magic == RedirectTraceData.MAGIC && this.isReadable(1 + 8 + 8 + 1)) {
                int version = this.getByte();
                if (version == RedirectTraceData.VERSION) {
                    RedirectTraceData traceData = new RedirectTraceData();
                    traceData.traceId = this.getLong();
                    traceData.clientTimestamp = this.getLong();
                    int count = (int) this.getUnsignedVarInt();
                    if (count >= 0 && count <= RedirectTraceData.FIELD_COUNT && this.isReadable(count * 8)) {
                        for (int i = 0; i < count; i++) {
                            traceData.times[i] = this.getLong();
                        }
                        if (this.isReadable(count * 8)) {
                            for (int i = 0; i < count; i++) {
                                traceData.wallTimes[i] = this.getLong();
                            }
                        }
                        this.traceData = traceData;
                    }
                }
            } else {
                this.offset = oldOffset;
            }
        }
    }
}

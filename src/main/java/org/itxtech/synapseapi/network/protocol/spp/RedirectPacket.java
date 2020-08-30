package org.itxtech.synapseapi.network.protocol.spp;

import java.util.UUID;

/**
 * Created by boybook on 16/6/24.
 */
public class RedirectPacket extends SynapseDataPacket {

    public static final int NETWORK_ID = SynapseInfo.REDIRECT_PACKET;
    public UUID uuid;
    public boolean direct;
    public int reliability;
    public int channel;
    public byte[] mcpeBuffer;
    public int protocol;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void encode() {
        this.reset();
        this.putInt(this.protocol);
        this.putUUID(this.uuid);
        this.putByte(this.direct ? (byte) 1 : (byte) 0);
        this.putByte((byte) reliability);
        this.putByte((byte) channel);
        this.putUnsignedVarInt(this.mcpeBuffer.length);
        this.put(this.mcpeBuffer);
    }

    @Override
    public void decode() {
        this.protocol = this.getInt();
        this.uuid = this.getUUID();
        this.direct = this.getByte() == 1;
        this.reliability = this.getByte();
        this.channel = this.getByte();
        this.mcpeBuffer = this.get((int) this.getUnsignedVarInt());
    }
}

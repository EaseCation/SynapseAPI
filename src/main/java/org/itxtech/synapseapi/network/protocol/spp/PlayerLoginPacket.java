package org.itxtech.synapseapi.network.protocol.spp;

import java.util.UUID;

/**
 * Created by boybook on 16/6/24.
 */
public class PlayerLoginPacket extends SynapseDataPacket {

    public static final int NETWORK_ID = SynapseInfo.PLAYER_LOGIN_PACKET;

    public int protocol;
    public UUID uuid;
    public String address;
    public int port;
    public boolean isFirstTime;
    public byte[] cachedLoginPacket;

    public String username;
    public String xuid;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void encode() {
        this.reset();
        this.putInt(this.protocol);
        this.putUUID(this.uuid);
        this.putString(this.address);
        this.putInt(this.port);
        this.putByte(this.isFirstTime ? (byte) 1 : (byte) 0);
        this.putUnsignedVarInt(this.cachedLoginPacket.length);
        this.put(this.cachedLoginPacket);
        if (this.xuid != null) this.putString(this.xuid);
    }

    @Override
    public void decode() {
        this.protocol = this.getInt();
        this.uuid = this.getUUID();
        this.address = this.getString();
        this.port = this.getInt();
        this.isFirstTime = this.getByte() == 1;
        this.cachedLoginPacket = this.get((int) this.getUnsignedVarInt());
        this.username = this.getString();
        if (!this.feof()) this.xuid = this.getString();
    }
}

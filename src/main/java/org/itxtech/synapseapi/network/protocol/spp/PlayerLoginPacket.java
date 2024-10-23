package org.itxtech.synapseapi.network.protocol.spp;

import cn.nukkit.network.protocol.DataPacket;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;

import java.util.UUID;

/**
 * Created by boybook on 16/6/24.
 */
@Log4j2
public class PlayerLoginPacket extends SynapseDataPacket {
    private static final Gson GSON = new Gson();

    public static final int NETWORK_ID = SynapseInfo.PLAYER_LOGIN_PACKET;

    public int protocol;
    public UUID uuid;
    public UUID sessionId;
    public String address;
    public int port;
    public boolean isFirstTime;
    public byte[] cachedLoginPacket;
    public JsonObject extra = new JsonObject();

    public DataPacket decodedLoginPacket;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void encode() {
        this.reset();
        this.putInt(this.protocol);
        this.putUUID(this.uuid);
        putUUID(sessionId);
        this.putString(this.address);
        this.putShort(this.port);
        this.putBoolean(this.isFirstTime);
        this.putUnsignedVarInt(this.cachedLoginPacket.length);
        this.put(this.cachedLoginPacket);
        this.putString(GSON.toJson(this.extra));
    }

    @Override
    public void decode() {
        this.protocol = this.getInt();
        this.uuid = this.getUUID();
        sessionId = getUUID();
        this.address = this.getString();
        this.port = this.getShort() & 0xffff;
        this.isFirstTime = this.getBoolean();
        this.cachedLoginPacket = this.get((int) this.getUnsignedVarInt());
        this.extra = GSON.fromJson(this.getString(), JsonObject.class);

        tryDecodeLoginPacket();
    }

    private void tryDecodeLoginPacket() {
        try {
            decodedLoginPacket = PacketRegister.getFullPacket(cachedLoginPacket, protocol, false);
        } catch (Exception e) {
            log.throwing(e);
        }
    }
}

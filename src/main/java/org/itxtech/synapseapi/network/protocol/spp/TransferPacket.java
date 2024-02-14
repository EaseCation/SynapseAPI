package org.itxtech.synapseapi.network.protocol.spp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.UUID;

/**
 * Created by boybook on 16/6/24.
 */
public class TransferPacket extends SynapseDataPacket {
    private static final Gson GSON = new Gson();

    public static final int NETWORK_ID = SynapseInfo.TRANSFER_PACKET;

    public UUID uuid;
    public String clientHash;
    public JsonObject extra = new JsonObject();

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void encode() {
        this.reset();
        this.putUUID(this.uuid);
        this.putString(this.clientHash);
        this.putString(GSON.toJson(this.extra));
    }

    @Override
    public void decode() {
        this.uuid = this.getUUID();
        this.clientHash = this.getString();
        this.extra = GSON.fromJson(this.getString(), JsonObject.class);
    }
}

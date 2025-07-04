package org.itxtech.synapseapi.network.protocol.spp;

import java.util.UUID;

/**
 * Created by boybook on 16/6/24.
 */
public class PlayerLogoutPacket extends SynapseDataPacket {

    public static final int NETWORK_ID = SynapseInfo.PLAYER_LOGOUT_PACKET;

    public UUID sessionId;
    public String reason;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void encode() {
        this.reset();
        this.putUUID(sessionId);
        this.putString(this.reason);
    }

    @Override
    public void decode() {
        this.sessionId = getUUID();
        this.reason = this.getString();
    }
}

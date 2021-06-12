package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;

public class EmotePacket116 extends Packet116 {

    public static final int NETWORK_ID = ProtocolInfo.EMOTE_PACKET;

    /**
     * C2S.
     */
    public static final byte FLAG_CLIENT = 0;
    /**
     * S2C.
     */
    public static final byte FLAG_SERVER = 1 << 0;

    public long runtimeId;
    public String emoteID;
    public byte flags;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.runtimeId = this.getEntityRuntimeId();
        this.emoteID = this.getString();
        this.flags = (byte) this.getByte();
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityRuntimeId(this.runtimeId);
        this.putString(this.emoteID);
        this.putByte(flags);
    }
}

package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class EmotePacket116 extends Packet116 {

    public static final int NETWORK_ID = ProtocolInfo.EMOTE_PACKET;

    /**
     * S2C.
     */
    public static final int FLAG_SERVER = 1 << 0;
    /**
     * @since 1.19.60
     */
    public static final int FLAG_MUTE_ANNOUNCEMENT = 1 << 1;

    public long runtimeId;
    public String emoteID;
    public int flags;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.runtimeId = this.getEntityRuntimeId();
        this.emoteID = this.getString();
        this.flags = this.getByte();
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityRuntimeId(this.runtimeId);
        this.putString(this.emoteID);
        this.putByte((byte) flags);
    }
}

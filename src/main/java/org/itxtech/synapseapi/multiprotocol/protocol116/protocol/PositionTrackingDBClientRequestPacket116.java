package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class PositionTrackingDBClientRequestPacket116 extends Packet116 {
    public static final int NETWORK_ID = ProtocolInfo.POSITION_TRACKING_DB_CLIENT_REQUEST_PACKET;

    public static final int ACTION_QUERY = 0;

    public int action = ACTION_QUERY;
    public int trackingId;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.action = this.getByte();
        this.trackingId = this.getVarInt();
    }

    @Override
    public void encode() {
    }
}

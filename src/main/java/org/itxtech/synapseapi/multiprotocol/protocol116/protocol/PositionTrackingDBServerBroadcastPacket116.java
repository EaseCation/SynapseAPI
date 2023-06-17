package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class PositionTrackingDBServerBroadcastPacket116 extends Packet116 {
    public static final int NETWORK_ID = ProtocolInfo.POSITION_TRACKING_DB_SERVER_BROADCAST_PACKET;

    public static final int ACTION_UPDATE = 0;
    public static final int ACTION_DESTROY = 1;
    public static final int ACTION_NOT_FOUND = 2;

    public int action = ACTION_UPDATE;
    public int trackingId;
    /**
     * CompoundTag.
     */
    public byte[] nbt;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();
        putByte((byte) action);
        putVarInt(trackingId);
        put(nbt);
    }
}

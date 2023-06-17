package org.itxtech.synapseapi.multiprotocol.protocol116100.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class MotionPredictionHintsPacket116100 extends Packet116100 {
    public static final int NETWORK_ID = ProtocolInfo.MOTION_PREDICTION_HINTS_PACKET;

    public long entityRuntimeId;
    public float x;
    public float y;
    public float z;
    public boolean onGround;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityRuntimeId(this.entityRuntimeId);
        this.putVector3f(this.x, this.y, this.z);
        this.putBoolean(this.onGround);
    }
}

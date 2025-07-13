package org.itxtech.synapseapi.multiprotocol.protocol121100.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class CorrectPlayerMovePredictionPacket121100 extends Packet121100 {
    public static final int NETWORK_ID = ProtocolInfo.CORRECT_PLAYER_MOVE_PREDICTION_PACKET;

    public static final int TYPE_PLAYER = 0;
    public static final int TYPE_VEHICLE = 1;

    public int type = TYPE_PLAYER;
    public float x;
    public float y;
    public float z;
    public float deltaX;
    public float deltaY;
    public float deltaZ;
    public float vehiclePitch;
    public float vehicleYaw;
    public float vehicleAngularVelocity;
    public boolean onGround;
    public long tick;

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
        this.putByte((byte) this.type);
        this.putVector3f(this.x, this.y, this.z);
        this.putVector3f(this.deltaX, this.deltaY, this.deltaZ);
        this.putVector2f(this.vehiclePitch, this.vehicleYaw);
        this.putLFloat(this.vehicleAngularVelocity);
        this.putBoolean(this.onGround);
        this.putUnsignedVarLong(this.tick);
    }
}

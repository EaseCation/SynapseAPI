package org.itxtech.synapseapi.multiprotocol.protocol12120.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;

import javax.annotation.Nullable;

@ToString
public class CorrectPlayerMovePredictionPacket12120 extends Packet12120 {
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
    @Nullable
    public Float vehicleAngularVelocity;
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
        if (this.type == TYPE_VEHICLE) {
            this.putVector2f(this.vehiclePitch, this.vehicleYaw);
            this.putOptional(this.vehicleAngularVelocity, BinaryStream::putLFloat);
        }
        this.putBoolean(this.onGround);
        this.putUnsignedVarLong(this.tick);
    }
}

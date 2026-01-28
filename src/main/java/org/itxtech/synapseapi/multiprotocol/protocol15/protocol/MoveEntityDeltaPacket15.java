package org.itxtech.synapseapi.multiprotocol.protocol15.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MoveEntityDeltaPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class MoveEntityDeltaPacket15 extends Packet15 {
    public static final int NETWORK_ID = ProtocolInfo.MOVE_ACTOR_DELTA_PACKET;

    public static final int FLAG_HAS_X = 0b1;
    public static final int FLAG_HAS_Y = 0b10;
    public static final int FLAG_HAS_Z = 0b100;
    public static final int FLAG_HAS_PITCH = 0b1000;
    public static final int FLAG_HAS_YAW = 0b10000;
    public static final int FLAG_HAS_HEAD_YAW = 0b100000;

    public long entityRuntimeId;
    public int flags = 0;
    public int xDelta = 0;
    public int yDelta = 0;
    public int zDelta = 0;
    public float pitchDelta = 0;
    public float yawDelta = 0;
    public float headYawDelta = 0;

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
        this.putEntityRuntimeId(entityRuntimeId);
        this.putByte((byte) flags);
        putCoordinate(FLAG_HAS_X, this.xDelta);
        putCoordinate(FLAG_HAS_Y, this.yDelta);
        putCoordinate(FLAG_HAS_Z, this.zDelta);
        putRotation(FLAG_HAS_PITCH, this.pitchDelta);
        putRotation(FLAG_HAS_YAW, this.yawDelta);
        putRotation(FLAG_HAS_HEAD_YAW, this.headYawDelta);
    }

    private void putCoordinate(int flag, int value) {
        if ((flags & flag) != 0) {
            this.putVarInt(value);
        }
    }

    private void putRotation(int flag, float value) {
        if ((flags & flag) != 0) {
            this.putByte((byte) (value / (360f / 256f)));
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        MoveEntityDeltaPacket packet = (MoveEntityDeltaPacket) pk;
        this.flags = packet.flags;
        this.xDelta = (int) packet.x;
        this.yDelta = (int) packet.y;
        this.zDelta = (int) packet.z;
        this.pitchDelta = packet.pitch;
        this.yawDelta = packet.yaw;
        this.headYawDelta = packet.headYaw;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return MoveEntityDeltaPacket.class;
    }
}

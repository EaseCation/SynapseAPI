package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MoveEntityDeltaPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class MoveEntityDeltaPacket113 extends Packet113 {

    public static final int NETWORK_ID = ProtocolInfo.MOVE_ACTOR_DELTA_PACKET;

    public static final int FLAG_HAS_X = 0b1;
    public static final int FLAG_HAS_Y = 0b10;
    public static final int FLAG_HAS_Z = 0b100;
    public static final int FLAG_HAS_PITCH = 0b1000;
    public static final int FLAG_HAS_YAW = 0b10000;
    public static final int FLAG_HAS_HEAD_YAW = 0b100000;

    public long entityRuntimeId;
    public int flags;
    public int x;
    public int y;
    public int z;
    public float pitchDelta;
    public float yawDelta;
    public float headYawDelta;

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
        this.putLShort(flags);
        putCoordinate(FLAG_HAS_X, this.x);
        putCoordinate(FLAG_HAS_Y, this.y);
        putCoordinate(FLAG_HAS_Z, this.z);
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
        this.x = (int) packet.x;
        this.y = (int) packet.y;
        this.z = (int) packet.z;
        this.pitchDelta = packet.pitch;
        this.yawDelta = packet.yaw;
        this.headYawDelta = packet.headYaw;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return MoveEntityDeltaPacket.class;
    }
}

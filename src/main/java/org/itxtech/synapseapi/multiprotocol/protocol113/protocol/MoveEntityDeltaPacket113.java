package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.MoveEntityDeltaPacket;
import org.itxtech.synapseapi.utils.ClassUtils;

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
    public double pitchDelta;
    public double yawDelta;
    public double headYawDelta;

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

    private int getCoordinate(int flag) {
        if ((flags & flag) != 0) {
            return this.getVarInt();
        }
        return 0;
    }

    private double getRotation(int flag) {
        if ((flags & flag) != 0) {
            return this.getByte() * (360d / 256d);
        }
        return 0d;
    }

    private void putCoordinate(int flag, int value) {
        if ((flags & flag) != 0) {
            this.putVarInt(value);
        }
    }

    private void putRotation(int flag, double value) {
        if ((flags & flag) != 0) {
            this.putByte((byte) (value / (360d / 256d)));
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, MoveEntityDeltaPacket.class);

        MoveEntityDeltaPacket packet = (MoveEntityDeltaPacket) pk;
        this.flags = packet.flags;
        this.x = packet.xDelta;
        this.y = packet.yDelta;
        this.z = packet.zDelta;
        this.pitchDelta = packet.pitchDelta;
        this.yawDelta = packet.yawDelta;
        this.headYawDelta = packet.headYawDelta;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return MoveEntityDeltaPacket.class;
    }
}

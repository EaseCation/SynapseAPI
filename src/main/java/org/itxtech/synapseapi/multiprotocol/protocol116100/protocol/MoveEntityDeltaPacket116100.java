package org.itxtech.synapseapi.multiprotocol.protocol116100.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.MoveEntityDeltaPacket;
import org.itxtech.synapseapi.utils.ClassUtils;

public class MoveEntityDeltaPacket116100 extends Packet116100 {

    public static final int NETWORK_ID = ProtocolInfo.MOVE_ENTITY_DELTA_PACKET;

    public static final int FLAG_HAS_X = 0b1;
    public static final int FLAG_HAS_Y = 0b10;
    public static final int FLAG_HAS_Z = 0b100;
    public static final int FLAG_HAS_YAW = 0b1000;
    public static final int FLAG_HAS_HEAD_YAW = 0b10000;
    public static final int FLAG_HAS_PITCH = 0b100000;

    public int flags = 0;
    public float xDelta = 0;
    public float yDelta = 0;
    public float zDelta = 0;
    public double yawDelta = 0;
    public double headYawDelta = 0;
    public double pitchDelta = 0;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.flags = this.getByte();
        this.xDelta = getCoordinate(FLAG_HAS_X);
        this.yDelta = getCoordinate(FLAG_HAS_Y);
        this.zDelta = getCoordinate(FLAG_HAS_Z);
        this.yawDelta = getRotation(FLAG_HAS_YAW);
        this.headYawDelta = getRotation(FLAG_HAS_HEAD_YAW);
        this.pitchDelta = getRotation(FLAG_HAS_PITCH);
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityRuntimeId(Long.MAX_VALUE);
        this.putByte((byte) flags);
        putCoordinate(FLAG_HAS_X, this.xDelta);
        putCoordinate(FLAG_HAS_Y, this.yDelta);
        putCoordinate(FLAG_HAS_Z, this.zDelta);
        putRotation(FLAG_HAS_YAW, this.yawDelta);
        putRotation(FLAG_HAS_HEAD_YAW, this.headYawDelta);
        putRotation(FLAG_HAS_PITCH, this.pitchDelta);
    }

    private float getCoordinate(int flag) {
        if ((flags & flag) != 0) {
            return this.getLFloat();
        }
        return 0;
    }

    private double getRotation(int flag) {
        if ((flags & flag) != 0) {
            return this.getByte() * (360d / 256d);
        }
        return 0d;
    }

    private void putCoordinate(int flag, float value) {
        if ((flags & flag) != 0) {
            this.putLFloat(value);
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
        this.xDelta = packet.xDelta;
        this.yDelta = packet.yDelta;
        this.zDelta = packet.zDelta;
        this.yawDelta = packet.yawDelta;
        this.headYawDelta = packet.headYawDelta;
        this.pitchDelta = packet.pitchDelta;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return MoveEntityDeltaPacket.class;
    }
}

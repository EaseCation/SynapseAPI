package org.itxtech.synapseapi.multiprotocol.protocol116100.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.utils.ClassUtils;

public class MovePlayerPacket116100 extends Packet116100 {

    public static final int NETWORK_ID = ProtocolInfo.MOVE_PLAYER_PACKET;

    public static final int MODE_NORMAL = 0;
    public static final int MODE_RESET = 1;
    public static final int MODE_TELEPORT = 2;
    public static final int MODE_PITCH = 3; //facepalm Mojang

    public long eid;
    public float x;
    public float y;
    public float z;
    public float yaw;
    public float headYaw;
    public float pitch;
    public int mode = MODE_NORMAL;
    public boolean onGround;
    public long ridingEid;
    public int int1 = 0;
    public int int2 = 0;
    public long frame;

    @Override
    public void decode() {
        this.eid = this.getEntityRuntimeId();
        Vector3f v = this.getVector3f();
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.pitch = this.getLFloat();
        this.headYaw = this.getLFloat();
        this.yaw = this.getLFloat();
        this.mode = this.getByte();
        this.onGround = this.getBoolean();
        this.ridingEid = this.getEntityRuntimeId();
        if (this.mode == MODE_TELEPORT) {
            this.int1 = this.getLInt();
            this.int2 = this.getLInt();
        }
        this.frame = this.getUnsignedVarLong();
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityRuntimeId(this.eid);
        this.putVector3f(this.x, this.y, this.z);
        this.putLFloat(this.pitch);
        this.putLFloat(this.yaw);
        this.putLFloat(this.headYaw);
        this.putByte((byte) this.mode);
        this.putBoolean(this.onGround);
        this.putEntityRuntimeId(this.ridingEid);
        if (this.mode == MODE_TELEPORT) {
            this.putLInt(this.int1);
            this.putLInt(this.int2);
        }
        this.putUnsignedVarLong(this.frame);
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, MovePlayerPacket.class);

        MovePlayerPacket packet = (MovePlayerPacket) pk;
        this.eid = packet.eid;
        this.x = packet.x;
        this.y = packet.y;
        this.z = packet.z;
        this.yaw = packet.yaw;
        this.headYaw = packet.headYaw;
        this.pitch = packet.pitch;
        this.mode = packet.mode;
        this.onGround = packet.onGround;
        this.ridingEid = packet.ridingEid;
        this.int1 = packet.int1;
        this.int2 = packet.int2;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return MovePlayerPacket.class;
    }
}

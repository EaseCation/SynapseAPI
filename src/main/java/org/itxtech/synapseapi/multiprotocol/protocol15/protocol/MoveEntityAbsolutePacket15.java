package org.itxtech.synapseapi.multiprotocol.protocol15.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.utils.ClassUtils;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class MoveEntityAbsolutePacket15 extends Packet15 {
    public static final int NETWORK_ID = ProtocolInfo.MOVE_ENTITY_PACKET;

    public long eid;
    public double x;
    public double y;
    public double z;
    public double yaw;
    public double headYaw;
    public double pitch;
    public boolean onGround;
    public boolean teleport;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.eid = this.getEntityRuntimeId();
        int flags = this.getByte();
        teleport = (flags & 0x01) != 0;
        onGround = (flags & 0x02) != 0;
        Vector3f v = this.getVector3f();
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.pitch = this.getByte() * (360d / 256d);
        this.headYaw = this.getByte() * (360d / 256d);
        this.yaw = this.getByte() * (360d / 256d);
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityRuntimeId(this.eid);
        byte flags = 0;
        if (teleport) {
            flags |= 0x01;
        }
        if (onGround) {
            flags |= 0x02;
        }
        this.putByte(flags);
        this.putVector3f((float) this.x, (float) this.y, (float) this.z);
        this.putByte((byte) (this.pitch / (360d / 256d)));
        this.putByte((byte) (this.headYaw / (360d / 256d)));
        this.putByte((byte) (this.yaw / (360d / 256d)));
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.MoveEntityPacket.class);
        cn.nukkit.network.protocol.MoveEntityPacket packet = (cn.nukkit.network.protocol.MoveEntityPacket) pk;

        this.eid = packet.eid;
        this.teleport = packet.teleport;
        this.onGround = packet.onGround;
        this.x = packet.x;
        this.y = packet.y;
        this.z = packet.z;
        this.pitch = packet.pitch;
        this.headYaw = packet.headYaw;
        this.yaw = packet.yaw;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.MoveEntityPacket.class;
    }
}

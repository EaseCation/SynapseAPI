package org.itxtech.synapseapi.multiprotocol.protocol12070.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.SetEntityMotionPacket;
import lombok.ToString;

@ToString
public class SetEntityMotionPacket12070 extends Packet12070 {
    public static final int NETWORK_ID = ProtocolInfo.SET_ACTOR_MOTION_PACKET;

    public long eid;
    public float motionX;
    public float motionY;
    public float motionZ;
    public long tick;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.eid = this.getEntityRuntimeId();
        Vector3f v = this.getVector3f();
        this.motionX = v.x;
        this.motionY = v.y;
        this.motionZ = v.z;
        this.tick = this.getUnsignedVarLong();
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityRuntimeId(this.eid);
        this.putVector3f(this.motionX, this.motionY, this.motionZ);
        this.putUnsignedVarLong(this.tick);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        SetEntityMotionPacket packet = (SetEntityMotionPacket) pk;
        this.eid = packet.eid;
        this.motionX = packet.motionX;
        this.motionY = packet.motionY;
        this.motionZ = packet.motionZ;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return SetEntityMotionPacket.class;
    }
}

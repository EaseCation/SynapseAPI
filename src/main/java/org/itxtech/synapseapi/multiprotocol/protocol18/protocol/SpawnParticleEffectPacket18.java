package org.itxtech.synapseapi.multiprotocol.protocol18.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.ProtocolInfo;

public class SpawnParticleEffectPacket18 extends Packet18 {

    public static final int NETWORK_ID = ProtocolInfo.SPAWN_PARTICLE_EFFECT_PACKET;

    public int dimension = 0;
    public long uniqueEntityId = -1;
    public Vector3f position;
    public String identifier;

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putByte((byte) dimension);
        this.putEntityUniqueId(uniqueEntityId);
        this.putVector3f(position);
        this.putString(identifier);
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

}

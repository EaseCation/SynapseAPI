package org.itxtech.synapseapi.multiprotocol.protocol18.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.SpawnParticleEffectPacket;
import org.itxtech.synapseapi.utils.ClassUtils;

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

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, SpawnParticleEffectPacket.class);

        SpawnParticleEffectPacket packet = (SpawnParticleEffectPacket) pk;
        this.dimension = packet.dimensionId;
        this.uniqueEntityId = packet.uniqueEntityId;
        this.position = packet.position;
        this.identifier = packet.identifier;

        return this;
    }

    @Override
    public DataPacket toDefault() {
        SpawnParticleEffectPacket pk = new SpawnParticleEffectPacket();
        pk.dimensionId = this.dimension;
        pk.uniqueEntityId = this.uniqueEntityId;
        pk.position = this.position;
        pk.identifier = this.identifier;
        return pk;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return SpawnParticleEffectPacket.class;
    }

}

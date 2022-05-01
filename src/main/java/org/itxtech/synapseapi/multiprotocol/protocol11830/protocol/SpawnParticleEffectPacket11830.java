package org.itxtech.synapseapi.multiprotocol.protocol11830.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.SpawnParticleEffectPacket18;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class SpawnParticleEffectPacket11830 extends Packet11830 {
    public static final int NETWORK_ID = ProtocolInfo.SPAWN_PARTICLE_EFFECT_PACKET;

    public int dimension = 0;
    public long uniqueEntityId = -1;
    public Vector3f position;
    public String identifier;
    /**
     * JSON.
     */
    public String molangVariables = "";

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
        this.putString(this.molangVariables);
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, SpawnParticleEffectPacket18.class);

        SpawnParticleEffectPacket18 packet = (SpawnParticleEffectPacket18) pk;
        this.dimension = packet.dimension;
        this.uniqueEntityId = packet.uniqueEntityId;
        this.position = packet.position;
        this.identifier = packet.identifier;

        return this;
    }

    @Override
    public DataPacket toDefault() {
        SpawnParticleEffectPacket18 pk = new SpawnParticleEffectPacket18();
        pk.dimension = this.dimension;
        pk.uniqueEntityId = this.uniqueEntityId;
        pk.position = this.position;
        pk.identifier = this.identifier;
        return pk;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return SpawnParticleEffectPacket18.class;
    }
}

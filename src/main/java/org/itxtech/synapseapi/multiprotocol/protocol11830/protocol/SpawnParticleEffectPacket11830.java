package org.itxtech.synapseapi.multiprotocol.protocol11830.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.SpawnParticleEffectPacket;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

import javax.annotation.Nullable;

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
    @Nullable
    public String molangVariables;

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
        this.putBoolean(this.molangVariables != null);
        if (this.molangVariables != null) {
            this.putString(this.molangVariables);
        }
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
        this.molangVariables = packet.molangVariables;

        return this;
    }

    @Override
    public DataPacket toDefault() {
        SpawnParticleEffectPacket pk = new SpawnParticleEffectPacket();
        pk.dimensionId = this.dimension;
        pk.uniqueEntityId = this.uniqueEntityId;
        pk.position = this.position;
        pk.identifier = this.identifier;
        pk.molangVariables = this.molangVariables;
        return pk;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return SpawnParticleEffectPacket.class;
    }
}

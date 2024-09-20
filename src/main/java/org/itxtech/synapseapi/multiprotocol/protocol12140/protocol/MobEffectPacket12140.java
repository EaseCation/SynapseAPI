package org.itxtech.synapseapi.multiprotocol.protocol12140.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MobEffectPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class MobEffectPacket12140 extends Packet12140 {
    public static final int NETWORK_ID = ProtocolInfo.MOB_EFFECT_PACKET;

    public static final byte EVENT_ADD = 1;
    public static final byte EVENT_MODIFY = 2;
    public static final byte EVENT_REMOVE = 3;

    public long eid;
    public int eventId;
    public int effectId;
    public int amplifier;
    public boolean particles = true;
    public int duration;
    public long tick;

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
        this.putEntityRuntimeId(this.eid);
        this.putByte((byte) this.eventId);
        this.putVarInt(this.effectId);
        this.putVarInt(this.amplifier);
        this.putBoolean(this.particles);
        this.putVarInt(this.duration);
        this.putUnsignedVarLong(this.tick);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        MobEffectPacket packet = (MobEffectPacket) pk;
        this.eid = packet.eid;
        this.eventId = packet.eventId;
        this.effectId = packet.effectId;
        this.amplifier = packet.amplifier;
        this.particles = packet.particles;
        this.duration = packet.duration;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return MobEffectPacket.class;
    }
}

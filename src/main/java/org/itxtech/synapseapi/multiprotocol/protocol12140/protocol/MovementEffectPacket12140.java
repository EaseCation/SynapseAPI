package org.itxtech.synapseapi.multiprotocol.protocol12140.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class MovementEffectPacket12140 extends Packet12140 {
    public static final int NETWORK_ID = ProtocolInfo.MOVEMENT_EFFECT_PACKET;

    public long entityRuntimeId;
    public int effectId;
    public int effectDuration;
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
        reset();
        putEntityRuntimeId(entityRuntimeId);
        putUnsignedVarInt(effectId);
        putUnsignedVarInt(effectDuration);
        putUnsignedVarLong(tick);
    }
}

package org.itxtech.synapseapi.multiprotocol.protocol12140.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.MovementEffectType;
import lombok.ToString;

/**
 * These packets are sent to the client to update specific MovementEffects.
 * These MovementEffects can be client-predicted.
 * Ex: Fireworks Rockets used while gliding send this packet to the client so they know the exact duration of the GLIDE_BOOST MovementEffect.
 */
@ToString
public class MovementEffectPacket12140 extends Packet12140 {
    public static final int NETWORK_ID = ProtocolInfo.MOVEMENT_EFFECT_PACKET;

    public long entityRuntimeId;
    /**
     * @see MovementEffectType
     */
    public int effectType = -1;
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
        putUnsignedVarInt(effectType);
        putUnsignedVarInt(effectDuration);
        putUnsignedVarLong(tick);
    }
}

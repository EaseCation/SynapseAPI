package org.itxtech.synapseapi.multiprotocol.protocol121110.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import lombok.ToString;

@ToString
public class PlayerArmorDamagePacket121110 extends Packet121110 {
    public static final int NETWORK_ID = ProtocolInfo.PLAYER_ARMOR_DAMAGE_PACKET;

    public static final int SLOT_HEAD = 0;
    public static final int SLOT_CHEST = 1;
    public static final int SLOT_LEGS = 2;
    public static final int SLOT_FEET = 3;
    public static final int SLOT_BODY = 4;

    /// Pair<ArmorSlot, Damage>[]
    public IntIntPair[] entries = new IntIntPair[0];

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

        putUnsignedVarInt(entries.length);
        for (IntIntPair entry : entries) {
            putByte(entry.leftInt());
            putLShort(entry.rightInt());
        }
    }
}

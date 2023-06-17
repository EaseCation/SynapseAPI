package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class PlayerArmorDamagePacket116 extends Packet116 {
    public static final int NETWORK_ID = ProtocolInfo.PLAYER_ARMOR_DAMAGE_PACKET;

    public static final int FLAG_HEAD = 1 << 0;
    public static final int FLAG_CHEST = 1 << 1;
    public static final int FLAG_LEGS = 1 << 2;
    public static final int FLAG_FEET = 1 << 3;

    public int flags;
    public int headSlotDamage;
    public int chestSlotDamage;
    public int legsSlotDamage;
    public int feetSlotDamage;

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

        putByte((byte) flags);
        if ((flags & FLAG_HEAD) != 0) {
            putVarInt(headSlotDamage);
        }
        if ((flags & FLAG_CHEST) != 0) {
            putVarInt(chestSlotDamage);
        }
        if ((flags & FLAG_LEGS) != 0) {
            putVarInt(legsSlotDamage);
        }
        if ((flags & FLAG_FEET) != 0) {
            putVarInt(feetSlotDamage);
        }
    }
}

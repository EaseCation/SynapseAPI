package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import lombok.Value;

@ToString
public class PlayerEnchantOptionsPacket116 extends Packet116 {
    public static final int NETWORK_ID = ProtocolInfo.PLAYER_ENCHANT_OPTIONS_PACKET;

    public EnchantOption[] options = new EnchantOption[0];

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

        putUnsignedVarInt(options.length);
        for (EnchantOption option : options) {
            putUnsignedVarInt(option.cost);

            putLInt(option.slotFlags);
            putEnchantList(option.equipActivatedEnchantments);
            putEnchantList(option.heldActivatedEnchantments);
            putEnchantList(option.selfActivatedEnchantments);

            putString(option.name);
            putVarInt(option.optionId);
        }
    }

    private void putEnchantList(Enchant... enchants) {
        putUnsignedVarInt(enchants.length);
        for (Enchant enchant : enchants) {
            putByte((byte) enchant.id);
            putByte((byte) enchant.level);
        }
    }

    @Value
    public static class EnchantOption {
        int cost;
        int slotFlags;
        Enchant[] equipActivatedEnchantments;
        Enchant[] heldActivatedEnchantments;
        Enchant[] selfActivatedEnchantments;
        String name;
        int optionId;
    }

    @Value
    public static class Enchant {
        int id;
        int level;
    }
}

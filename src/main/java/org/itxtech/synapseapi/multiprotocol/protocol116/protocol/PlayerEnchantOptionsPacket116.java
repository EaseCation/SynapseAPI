package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;

@ToString
public class PlayerEnchantOptionsPacket116 extends Packet116 {
    public static final int NETWORK_ID = ProtocolInfo.PLAYER_ENCHANT_OPTIONS_PACKET;

    public static final EnchantOption[] EMPTY_OPTIONS = new EnchantOption[0];

    public EnchantOption[] options = EMPTY_OPTIONS;

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

    @RequiredArgsConstructor
    @Data
    public static class EnchantOption {
        final int cost;
        final int slotFlags;
        final Enchant[] equipActivatedEnchantments;
        final Enchant[] heldActivatedEnchantments;
        final Enchant[] selfActivatedEnchantments;
        final String name;
        int optionId;

        public EnchantOption(int cost, Enchant[] enchantments) {
            this(cost, enchantments, "");
        }

        public EnchantOption(int cost, Enchant[] enchantments, String name) {
            this(cost, 0, enchantments, Enchant.EMPTY_ENCHANTS, Enchant.EMPTY_ENCHANTS, name);
        }
    }

    @Value
    public static class Enchant {
        public static final Enchant[] EMPTY_ENCHANTS = new Enchant[0];

        int id;
        int level;
    }
}

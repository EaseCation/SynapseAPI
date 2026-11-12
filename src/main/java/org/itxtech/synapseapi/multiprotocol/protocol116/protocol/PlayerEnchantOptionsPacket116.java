package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentOption;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class PlayerEnchantOptionsPacket116 extends Packet116 {
    public static final int NETWORK_ID = ProtocolInfo.PLAYER_ENCHANT_OPTIONS_PACKET;

    public static final EnchantmentOption[] EMPTY_OPTIONS = new EnchantmentOption[0];

    public EnchantmentOption[] options = EMPTY_OPTIONS;

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
        for (EnchantmentOption option : options) {
            putUnsignedVarInt(option.cost);

            putLInt(option.slotFlags);
            putEnchantList(option.equipActivatedEnchantments);
            putEnchantList(option.heldActivatedEnchantments);
            putEnchantList(option.selfActivatedEnchantments);

            if (neteaseMode) {
                putEnchantList(Enchantment.EMPTY); // neteaseEnchantments
            }

            putString(option.name);
            putUnsignedVarInt(option.optionId);
        }
    }

    private void putEnchantList(Enchantment... enchants) {
        putUnsignedVarInt(enchants.length);
        for (Enchantment enchant : enchants) {
            putByte(enchant.getId());
            putByte(enchant.getLevel());

            if (neteaseMode) {
                putString(""); // modEnchantIdentifier
            }
        }
    }
}

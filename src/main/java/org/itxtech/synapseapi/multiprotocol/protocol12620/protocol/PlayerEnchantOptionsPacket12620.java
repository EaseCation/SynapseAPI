package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentOption;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;

@ToString
public class PlayerEnchantOptionsPacket12620 extends Packet12620 {
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

        putArray(options, (stream, option) -> {
            stream.putByte(option.cost);

            stream.putLInt(option.slotFlags);
            putEnchantList(stream, option.equipActivatedEnchantments);
            putEnchantList(stream, option.heldActivatedEnchantments);
            putEnchantList(stream, option.selfActivatedEnchantments);

            if (neteaseMode) {
                putEnchantList(stream, Enchantment.EMPTY); // neteaseEnchantments
            }

            stream.putString(option.name, 256, true);
            stream.putUnsignedVarInt(option.optionId);
        }, 3);
    }

    private static void putEnchantList(BinaryStream stream, Enchantment... enchants) {
        stream.putUnsignedVarInt(enchants.length);
        for (Enchantment enchant : enchants) {
            stream.putByte(enchant.getId());
            stream.putByte(enchant.getLevel());

            if (stream.neteaseMode) {
                stream.putString(""); // modEnchantIdentifier
            }
        }
    }
}

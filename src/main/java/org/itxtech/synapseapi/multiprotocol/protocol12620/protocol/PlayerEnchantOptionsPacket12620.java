package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.PlayerEnchantOptionsPacket116.Enchant;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.PlayerEnchantOptionsPacket116.EnchantOption;

@ToString
public class PlayerEnchantOptionsPacket12620 extends Packet12620 {
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
            putByte(option.cost);

            putLInt(option.slotFlags);
            putEnchantList(option.equipActivatedEnchantments);
            putEnchantList(option.heldActivatedEnchantments);
            putEnchantList(option.selfActivatedEnchantments);

            if (neteaseMode) {
                putEnchantList(Enchant.EMPTY_ENCHANTS); // neteaseEnchantments
            }

            putString(option.name);
            putUnsignedVarInt(option.optionId);
        }
    }

    private void putEnchantList(Enchant... enchants) {
        putUnsignedVarInt(enchants.length);
        for (Enchant enchant : enchants) {
            putByte(enchant.id);
            putByte(enchant.level);

            if (neteaseMode) {
                putString(""); // modEnchantIdentifier
            }
        }
    }
}

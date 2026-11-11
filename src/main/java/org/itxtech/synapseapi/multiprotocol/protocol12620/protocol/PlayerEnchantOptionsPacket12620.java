package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
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

        putArray(options, (stream, option) -> {
            stream.putByte(option.cost);

            stream.putLInt(option.slotFlags);
            putEnchantList(stream, option.equipActivatedEnchantments);
            putEnchantList(stream, option.heldActivatedEnchantments);
            putEnchantList(stream, option.selfActivatedEnchantments);

            if (neteaseMode) {
                putEnchantList(stream, Enchant.EMPTY_ENCHANTS); // neteaseEnchantments
            }

            stream.putString(option.name, 256, true);
            stream.putUnsignedVarInt(option.optionId);
        }, 3);
    }

    private static void putEnchantList(BinaryStream stream, Enchant... enchants) {
        stream.putUnsignedVarInt(enchants.length);
        for (Enchant enchant : enchants) {
            stream.putByte(enchant.id);
            stream.putByte(enchant.level);

            if (stream.neteaseMode) {
                stream.putString(""); // modEnchantIdentifier
            }
        }
    }
}

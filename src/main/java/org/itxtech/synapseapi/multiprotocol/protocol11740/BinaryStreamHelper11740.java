package org.itxtech.synapseapi.multiprotocol.protocol11740;

import cn.nukkit.item.Item;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol11730.BinaryStreamHelper11730;
import org.itxtech.synapseapi.multiprotocol.protocol11740.protocol.ItemStackRequestPacket11740;

public class BinaryStreamHelper11740 extends BinaryStreamHelper11730 {

    public static BinaryStreamHelper11740 create() {
        return new BinaryStreamHelper11740();
    }

    @Override
    public String getGameVersion() {
        return "1.17.40";
    }

    @Override
    protected Object getItemStackRequestAction(BinaryStream stream) {
        int type = stream.getByte();
        switch (type) {
            case ItemStackRequestPacket11740.ACTION_TAKE:
                int count = stream.getByte();
                Object source = this.getItemStackRequestSlotInfo(stream);
                Object destination = this.getItemStackRequestSlotInfo(stream);
                break;
            case ItemStackRequestPacket11740.ACTION_PLACE:
                count = stream.getByte();
                source = this.getItemStackRequestSlotInfo(stream);
                destination = this.getItemStackRequestSlotInfo(stream);
                break;
            case ItemStackRequestPacket11740.ACTION_SWAP:
                source = this.getItemStackRequestSlotInfo(stream);
                destination = this.getItemStackRequestSlotInfo(stream);
                break;
            case ItemStackRequestPacket11740.ACTION_DROP:
                count = stream.getByte();
                source = this.getItemStackRequestSlotInfo(stream);
                boolean randomly = stream.getBoolean();
                break;
            case ItemStackRequestPacket11740.ACTION_DESTROY:
                count = stream.getByte();
                source = this.getItemStackRequestSlotInfo(stream);
                break;
            case ItemStackRequestPacket11740.ACTION_CRAFTING_CONSUME_INPUT:
                count = stream.getByte();
                source = this.getItemStackRequestSlotInfo(stream);
                break;
            case ItemStackRequestPacket11740.ACTION_CRAFTING_MARK_SECONDARY_RESULT_SLOT:
                int slot = stream.getByte();
                break;
            case ItemStackRequestPacket11740.ACTION_LAB_TABLE_COMBINE:
                break;
            case ItemStackRequestPacket11740.ACTION_BEACON_PAYMENT:
                int primaryEffect = stream.getVarInt();
                int secondaryEffect = stream.getVarInt();
                break;
            case ItemStackRequestPacket11740.ACTION_MINE_BLOCK:
                int hotbarSlot = stream.getVarInt();
                int predictedDurability = stream.getVarInt();
                int stackId = stream.getVarInt();
                break;
            case ItemStackRequestPacket11740.ACTION_CRAFTING_RECIPE:
                int recipeNetworkId = stream.getVarInt();
                break;
            case ItemStackRequestPacket11740.ACTION_CRAFTING_RECIPE_AUTO:
                recipeNetworkId = stream.getVarInt();
                int repetitions = stream.getByte();
                break;
            case ItemStackRequestPacket11740.ACTION_CREATIVE_CREATE:
                int creativeItemNetworkId = stream.getVarInt();
                break;
            case ItemStackRequestPacket11740.ACTION_CRAFTING_RECIPE_OPTIONAL:
                recipeNetworkId = stream.getVarInt();
                int filteredStringIndex = stream.getLInt();
                break;
            case ItemStackRequestPacket11740.ACTION_CRAFTING_GRINDSTONE:
                recipeNetworkId = stream.getVarInt();
                int repairCost = stream.getVarInt();
                break;
            case ItemStackRequestPacket11740.ACTION_CRAFTING_LOOM:
                String patternId = stream.getString();
                break;
            case ItemStackRequestPacket11740.ACTION_CRAFTING_NON_IMPLEMENTED_DEPRECATED_ASK_TY_LAING:
                break;
            case ItemStackRequestPacket11740.ACTION_CRAFTING_RESULTS_DEPRECATED_ASK_TY_LAING:
                int length = (int) stream.getUnsignedVarInt();
                for (int i = 0; i < length; i++) {
                    Item result = stream.getItemInstance();
                }
                int iterations = stream.getByte();
                break;
            default:
                throw new UnsupportedOperationException("Unhandled item stack request action type: " + type);
        }

        return null;
    }
}

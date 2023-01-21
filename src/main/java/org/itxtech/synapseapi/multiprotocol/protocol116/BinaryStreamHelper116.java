package org.itxtech.synapseapi.multiprotocol.protocol116;

import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.EntityLink;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol11460.BinaryStreamHelper11460;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.ItemStackRequestPacket116;

public class BinaryStreamHelper116 extends BinaryStreamHelper11460 {

    public static BinaryStreamHelper116 create() {
        return new BinaryStreamHelper116();
    }

    /**
     * 从1.16开始可以使用通配符星号. (e.g. "*")
     * @return base game version
     */
    @Override
    public String getGameVersion() {
        return "1.16.0";
    }

    @Override
    public void putEntityLink(BinaryStream stream, EntityLink link) {
        stream.putEntityUniqueId(link.fromEntityUniquieId);
        stream.putEntityUniqueId(link.toEntityUniquieId);
        stream.putByte(link.type);
        stream.putBoolean(link.immediate);
        stream.putBoolean(link.riderInitiated);
    }

    @Override
    public int getItemStackRequest(BinaryStream stream) { //TODO
        int requestId = stream.getVarInt();

        int size = (int) stream.getUnsignedVarInt();
        if (size > 4096) {
            throw new IndexOutOfBoundsException("too many array elements");
        }
//        Object[] actions = new Object[size];
        for (int i = 0; i < size; i++) {
            Object action = this.getItemStackRequestAction(stream);
//            actions[i] = action;
        }

        return requestId;
    }

    protected Object getItemStackRequestAction(BinaryStream stream) { //TODO
        int type = stream.getByte();
        switch (type) {
            case ItemStackRequestPacket116.ACTION_TAKE:
                int count = stream.getByte();
                Object source = this.getItemStackRequestSlotInfo(stream);
                Object destination = this.getItemStackRequestSlotInfo(stream);
                break;
            case ItemStackRequestPacket116.ACTION_PLACE:
                count = stream.getByte();
                source = this.getItemStackRequestSlotInfo(stream);
                destination = this.getItemStackRequestSlotInfo(stream);
                break;
            case ItemStackRequestPacket116.ACTION_SWAP:
                source = this.getItemStackRequestSlotInfo(stream);
                destination = this.getItemStackRequestSlotInfo(stream);
                break;
            case ItemStackRequestPacket116.ACTION_DROP:
                count = stream.getByte();
                source = this.getItemStackRequestSlotInfo(stream);
                boolean randomly = stream.getBoolean();
                break;
            case ItemStackRequestPacket116.ACTION_DESTROY:
                count = stream.getByte();
                source = this.getItemStackRequestSlotInfo(stream);
                break;
            case ItemStackRequestPacket116.ACTION_CRAFTING_CONSUME_INPUT:
                count = stream.getByte();
                source = this.getItemStackRequestSlotInfo(stream);
                break;
            case ItemStackRequestPacket116.ACTION_CRAFTING_MARK_SECONDARY_RESULT_SLOT:
                int slot = stream.getByte();
                break;
            case ItemStackRequestPacket116.ACTION_LAB_TABLE_COMBINE:
                break;
            case ItemStackRequestPacket116.ACTION_BEACON_PAYMENT:
                int primaryEffect = stream.getVarInt();
                int secondaryEffect = stream.getVarInt();
                break;
            case ItemStackRequestPacket116.ACTION_CRAFTING_RECIPE:
                int recipeNetworkId = stream.getVarInt();
                break;
            case ItemStackRequestPacket116.ACTION_CRAFTING_RECIPE_AUTO:
                recipeNetworkId = stream.getVarInt();
                break;
            case ItemStackRequestPacket116.ACTION_CREATIVE_CREATE:
                int creativeItemNetworkId = stream.getVarInt();
                break;
            case ItemStackRequestPacket116.ACTION_CRAFTING_NON_IMPLEMENTED_DEPRECATED_ASK_TY_LAING:
                break;
            case ItemStackRequestPacket116.ACTION_CRAFTING_RESULTS_DEPRECATED_ASK_TY_LAING:
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

    protected Object getItemStackRequestSlotInfo(BinaryStream stream) { //TODO
        int containerId = stream.getByte();
        int slotId = stream.getByte();
        int stackId = stream.getVarInt();

        return null;
    }
}

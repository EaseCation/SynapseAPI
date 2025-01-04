package org.itxtech.synapseapi.multiprotocol.protocol116;

import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.*;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.ItemStackRequestSlotInfo;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.action.*;
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
    public ItemStackRequest getItemStackRequest(BinaryStream stream) {
        ItemStackRequest request = new ItemStackRequest();

        request.requestId = stream.getVarInt();

        int size = (int) stream.getUnsignedVarInt();
        if (size > 4096) {
            throw new IndexOutOfBoundsException("too many array elements");
        }
        ItemStackRequestAction[] actions = new ItemStackRequestAction[size];
        for (int i = 0; i < size; i++) {
            actions[i] = this.getItemStackRequestAction(stream);
        }
        request.actions = actions;

        return request;
    }

    protected ItemStackRequestAction getItemStackRequestAction(BinaryStream stream) {
        int type = stream.getByte();
        switch (type) {
            case ItemStackRequestPacket116.ACTION_TAKE: {
                TakeStackRequestAction action = new TakeStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.destination = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket116.ACTION_PLACE: {
                PlaceStackRequestAction action = new PlaceStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.destination = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket116.ACTION_SWAP: {
                SwapStackRequestAction action = new SwapStackRequestAction();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.destination = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket116.ACTION_DROP: {
                DropStackRequestAction action = new DropStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.randomly = stream.getBoolean();
                return action;
            }
            case ItemStackRequestPacket116.ACTION_DESTROY: {
                DestroyStackRequestAction action = new DestroyStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket116.ACTION_CRAFTING_CONSUME_INPUT: {
                CraftingConsumeInputStackRequestAction action = new CraftingConsumeInputStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket116.ACTION_CRAFTING_MARK_SECONDARY_RESULT_SLOT: {
                CraftingCreateSpecificResultStackRequestAction action = new CraftingCreateSpecificResultStackRequestAction();
                action.resultIndex = stream.getByte();
                return action;
            }
            case ItemStackRequestPacket116.ACTION_LAB_TABLE_COMBINE: {
                LabTableCombineStackRequestAction action = new LabTableCombineStackRequestAction();
                return action;
            }
            case ItemStackRequestPacket116.ACTION_BEACON_PAYMENT: {
                BeaconPaymentStackRequestAction action = new BeaconPaymentStackRequestAction();
                action.primaryEffectId = stream.getVarInt();
                action.secondaryEffectId = stream.getVarInt();
                return action;
            }
            case ItemStackRequestPacket116.ACTION_CRAFTING_RECIPE: {
                CraftRecipeStackRequestAction action = new CraftRecipeStackRequestAction();
                action.recipeId = (int) stream.getUnsignedVarInt();
                return action;
            }
            case ItemStackRequestPacket116.ACTION_CRAFTING_RECIPE_AUTO: {
                CraftRecipeAutoStackRequestAction action = new CraftRecipeAutoStackRequestAction();
                action.recipeId = (int) stream.getUnsignedVarInt();
                return action;
            }
            case ItemStackRequestPacket116.ACTION_CREATIVE_CREATE: {
                CreativeCreateStackRequestAction action = new CreativeCreateStackRequestAction();
                action.creativeItemId = (int) stream.getUnsignedVarInt();
                return action;
            }
            case ItemStackRequestPacket116.ACTION_CRAFTING_NON_IMPLEMENTED_DEPRECATED_ASK_TY_LAING: {
                DeprecatedCraftingNonImplementedStackRequestAction action = new DeprecatedCraftingNonImplementedStackRequestAction();
                return action;
            }
            case ItemStackRequestPacket116.ACTION_CRAFTING_RESULTS_DEPRECATED_ASK_TY_LAING: {
                DeprecatedCraftingResultsStackRequestAction action = new DeprecatedCraftingResultsStackRequestAction();
                int length = (int) stream.getUnsignedVarInt();
                Item[] results = new Item[length];
                for (int i = 0; i < length; i++) {
                    results[i] = stream.getItemInstance();
                }
                action.results = results;
                action.iterations = stream.getByte();
                return action;
            }
            default: {
                throw new UnsupportedOperationException("Unhandled item stack request action type: " + type);
            }
        }
    }

    protected ItemStackRequestSlotInfo getItemStackRequestSlotInfo(BinaryStream stream) {
        ItemStackRequestSlotInfo info = new ItemStackRequestSlotInfo();
        info.containerId = stream.getByte();
        info.slotId = stream.getByte();
        info.stackId = stream.getVarInt();
        return info;
    }

    @Override
    public void putItemStackResponse(BinaryStream stream, ItemStackResponse response) {
        stream.putByte(response.result);
        stream.putVarInt(response.requestId);
        for (ItemStackResponseContainerInfo info : response.containerInfos) {
            putItemStackResponseContainerInfo(stream, info);
        }
    }

    protected void putItemStackResponseContainerInfo(BinaryStream stream, ItemStackResponseContainerInfo container) {
        stream.putByte(container.containerId);
        for (ItemStackResponseSlotInfo slot : container.slots) {
            putItemStackResponseSlotInfo(stream, slot);
        }
    }

    protected void putItemStackResponseSlotInfo(BinaryStream stream, ItemStackResponseSlotInfo info) {
        stream.putByte(info.slot);
        stream.putByte(info.hotbarSlot);
        stream.putByte(info.count);
        stream.putVarInt(info.itemStackId);
    }
}

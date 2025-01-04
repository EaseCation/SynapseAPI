package org.itxtech.synapseapi.multiprotocol.protocol116200;

import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.ItemStackRequest;
import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackResponseSlotInfo;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.action.*;
import org.itxtech.synapseapi.multiprotocol.protocol116100.BinaryStreamHelper116100;
import org.itxtech.synapseapi.multiprotocol.protocol116200.protocol.ItemStackRequestPacket116200;

public class BinaryStreamHelper116200 extends BinaryStreamHelper116100 {

    public static BinaryStreamHelper116200 create() {
        return new BinaryStreamHelper116200();
    }

    @Override
    public String getGameVersion() {
        return "1.16.200";
    }

    @Override
    protected void registerCommandParameterTypes() {
        int ARG_TYPE_INT = 1;
        int ARG_TYPE_FLOAT = 2;
        int ARG_TYPE_VALUE = 3;
        int ARG_TYPE_WILDCARD_INT = 4;
        int ARG_TYPE_OPERATOR = 5;
        int ARG_TYPE_TARGET = 6;
        int ARG_TYPE_WILDCARD_TARGET = 7;
        int ARG_TYPE_FILE_PATH = 15;
        int ARG_TYPE_STRING = 31;
        int ARG_TYPE_BLOCK_POSITION = 39;
        int ARG_TYPE_POSITION = 40;
        int ARG_TYPE_MESSAGE = 43;
        int ARG_TYPE_RAWTEXT = 45;
        int ARG_TYPE_JSON = 49;
        int ARG_TYPE_COMMAND = 56;

        this.registerCommandParameterType(CommandParamType.INT, ARG_TYPE_INT);
        this.registerCommandParameterType(CommandParamType.FLOAT, ARG_TYPE_FLOAT);
        this.registerCommandParameterType(CommandParamType.VALUE, ARG_TYPE_VALUE);
        this.registerCommandParameterType(CommandParamType.WILDCARD_INT, ARG_TYPE_WILDCARD_INT);
        this.registerCommandParameterType(CommandParamType.OPERATOR, ARG_TYPE_OPERATOR);
        this.registerCommandParameterType(CommandParamType.TARGET, ARG_TYPE_TARGET);
        this.registerCommandParameterType(CommandParamType.WILDCARD_TARGET, ARG_TYPE_WILDCARD_TARGET);
        this.registerCommandParameterType(CommandParamType.STRING, ARG_TYPE_STRING);
        this.registerCommandParameterType(CommandParamType.BLOCK_POSITION, ARG_TYPE_BLOCK_POSITION);
        this.registerCommandParameterType(CommandParamType.POSITION, ARG_TYPE_POSITION);
        this.registerCommandParameterType(CommandParamType.MESSAGE, ARG_TYPE_MESSAGE);
        this.registerCommandParameterType(CommandParamType.RAWTEXT, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.JSON, ARG_TYPE_JSON);
        this.registerCommandParameterType(CommandParamType.TEXT, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.COMMAND, ARG_TYPE_COMMAND);
        this.registerCommandParameterType(CommandParamType.FILE_PATH, ARG_TYPE_FILE_PATH);

        this.registerCommandParameterType(CommandParamType.COMPARE_OPERATOR, ARG_TYPE_OPERATOR);
        this.registerCommandParameterType(CommandParamType.INTEGER_RANGE, ARG_TYPE_INT);
        this.registerCommandParameterType(CommandParamType.EQUIPMENT_SLOT, ARG_TYPE_STRING);
        this.registerCommandParameterType(CommandParamType.BLOCK_STATES, ARG_TYPE_STRING);
    }

    @Override
    public ItemStackRequest getItemStackRequest(BinaryStream stream) {
        ItemStackRequest request = super.getItemStackRequest(stream);

        int size = (int) stream.getUnsignedVarInt();
        if (size > 4096) {
            throw new IndexOutOfBoundsException("too many array elements");
        }
        String[] filterStrings = new String[size];
        for (int i = 0; i < size; i++) {
            filterStrings[i] = stream.getString();
        }
        request.filterStrings = filterStrings;

        return request;
    }

    @Override
    protected ItemStackRequestAction getItemStackRequestAction(BinaryStream stream) {
        int type = stream.getByte();
        switch (type) {
            case ItemStackRequestPacket116200.ACTION_TAKE: {
                TakeStackRequestAction action = new TakeStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.destination = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket116200.ACTION_PLACE: {
                PlaceStackRequestAction action = new PlaceStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.destination = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket116200.ACTION_SWAP: {
                SwapStackRequestAction action = new SwapStackRequestAction();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.destination = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket116200.ACTION_DROP: {
                DropStackRequestAction action = new DropStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.randomly = stream.getBoolean();
                return action;
            }
            case ItemStackRequestPacket116200.ACTION_DESTROY: {
                DestroyStackRequestAction action = new DestroyStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket116200.ACTION_CRAFTING_CONSUME_INPUT: {
                CraftingConsumeInputStackRequestAction action = new CraftingConsumeInputStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket116200.ACTION_CRAFTING_MARK_SECONDARY_RESULT_SLOT: {
                CraftingCreateSpecificResultStackRequestAction action = new CraftingCreateSpecificResultStackRequestAction();
                action.resultIndex = stream.getByte();
                return action;
            }
            case ItemStackRequestPacket116200.ACTION_LAB_TABLE_COMBINE: {
                LabTableCombineStackRequestAction action = new LabTableCombineStackRequestAction();
                return action;
            }
            case ItemStackRequestPacket116200.ACTION_BEACON_PAYMENT: {
                BeaconPaymentStackRequestAction action = new BeaconPaymentStackRequestAction();
                action.primaryEffectId = stream.getVarInt();
                action.secondaryEffectId = stream.getVarInt();
                return action;
            }
            case ItemStackRequestPacket116200.ACTION_CRAFTING_RECIPE: {
                CraftRecipeStackRequestAction action = new CraftRecipeStackRequestAction();
                action.recipeId = (int) stream.getUnsignedVarInt();
                return action;
            }
            case ItemStackRequestPacket116200.ACTION_CRAFTING_RECIPE_AUTO: {
                CraftRecipeAutoStackRequestAction action = new CraftRecipeAutoStackRequestAction();
                action.recipeId = (int) stream.getUnsignedVarInt();
                return action;
            }
            case ItemStackRequestPacket116200.ACTION_CREATIVE_CREATE: {
                CreativeCreateStackRequestAction action = new CreativeCreateStackRequestAction();
                action.creativeItemId = (int) stream.getUnsignedVarInt();
                return action;
            }
            case ItemStackRequestPacket116200.ACTION_CRAFTING_RECIPE_OPTIONAL: {
                CraftRecipeOptionalStackRequestAction action = new CraftRecipeOptionalStackRequestAction();
                action.recipeId = (int) stream.getUnsignedVarInt();
                action.filterStringIndex = stream.getLInt();
                return action;
            }
            case ItemStackRequestPacket116200.ACTION_CRAFTING_NON_IMPLEMENTED_DEPRECATED_ASK_TY_LAING: {
                DeprecatedCraftingNonImplementedStackRequestAction action = new DeprecatedCraftingNonImplementedStackRequestAction();
                return action;
            }
            case ItemStackRequestPacket116200.ACTION_CRAFTING_RESULTS_DEPRECATED_ASK_TY_LAING: {
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

    @Override
    protected void putItemStackResponseSlotInfo(BinaryStream stream, ItemStackResponseSlotInfo info) {
        super.putItemStackResponseSlotInfo(stream, info);
        stream.putString(info.customName);
    }
}

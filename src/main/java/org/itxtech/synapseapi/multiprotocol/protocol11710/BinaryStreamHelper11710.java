package org.itxtech.synapseapi.multiprotocol.protocol11710;

import cn.nukkit.command.data.CommandData;
import cn.nukkit.command.data.CommandDataVersions;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandFlag;
import cn.nukkit.command.data.CommandOverload;
import cn.nukkit.command.data.CommandParamOption;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.action.*;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.AvailableCommandsPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol116210.protocol.ItemStackRequestPacket116210;
import org.itxtech.synapseapi.multiprotocol.protocol117.BinaryStreamHelper117;

import java.util.List;
import java.util.Map;

public class BinaryStreamHelper11710 extends BinaryStreamHelper117 {

    public static BinaryStreamHelper11710 create() {
        return new BinaryStreamHelper11710();
    }

    @Override
    public String getGameVersion() {
        return "1.17.10";
    }

    @Override
    public void putCommandData(BinaryStream stream, Map<String, CommandDataVersions> commands, List<CommandEnum> enums, List<String> postFixes, List<CommandEnum> softEnums) {
        stream.putUnsignedVarInt(commands.size());

        commands.forEach((name, cmdData) -> {
            CommandData data = cmdData.versions.get(0);

            stream.putString(name);
            stream.putString(data.description);
            int flags = 0;
            for (CommandFlag flag : data.flags) {
                flags |= 1 << flag.ordinal();
            }
            stream.putLShort(flags);
            stream.putByte((byte) data.permission.ordinal());

            stream.putLInt(data.aliases == null ? -1 : enums.indexOf(data.aliases));

            stream.putUnsignedVarInt(data.overloads.size());
            for (CommandOverload overload : data.overloads.values()) {
                stream.putUnsignedVarInt(overload.input.parameters.length);

                for (CommandParameter parameter : overload.input.parameters) {
                    stream.putString(parameter.name);

                    int type = 0;
                    int translatedType = this.getCommandParameterTypeId(parameter.type, AvailableCommandsPacket113.ARG_TYPE_INT);
                    if (parameter.postFix != null) {
                        int i = postFixes.indexOf(parameter.postFix);
                        if (i < 0) {
                            throw new IllegalStateException("Postfix '" + parameter.postFix + "' isn't in postfix array");
                        }
                        type = AvailableCommandsPacket113.ARG_FLAG_POSTFIX | i;
                    } else {
                        type |= AvailableCommandsPacket113.ARG_FLAG_VALID;
                        if (parameter.enumData != null) {
                            if (parameter.enumData.isSoft()) {
                                type |= AvailableCommandsPacket113.ARG_FLAG_SOFT_ENUM | softEnums.indexOf(parameter.enumData);
                            } else {
                                type |= AvailableCommandsPacket113.ARG_FLAG_ENUM | enums.indexOf(parameter.enumData);
                            }
                        } else {
                            type |= translatedType;
                        }
                    }

                    stream.putLInt(type);
                    stream.putBoolean(parameter.optional);
                    int options = 0;
                    for (CommandParamOption option : parameter.options) {
                        options |= 1 << option.ordinal();
                    }
                    stream.putByte((byte) options);
                }
            }
        });
    }

    @Override
    protected ItemStackRequestAction getItemStackRequestAction(BinaryStream stream) {
        int type = stream.getByte();
        switch (type) {
            case ItemStackRequestPacket116210.ACTION_TAKE: {
                TakeStackRequestAction action = new TakeStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.destination = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket116210.ACTION_PLACE: {
                PlaceStackRequestAction action = new PlaceStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.destination = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket116210.ACTION_SWAP: {
                SwapStackRequestAction action = new SwapStackRequestAction();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.destination = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket116210.ACTION_DROP: {
                DropStackRequestAction action = new DropStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.randomly = stream.getBoolean();
                return action;
            }
            case ItemStackRequestPacket116210.ACTION_DESTROY: {
                DestroyStackRequestAction action = new DestroyStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket116210.ACTION_CRAFTING_CONSUME_INPUT: {
                CraftingConsumeInputStackRequestAction action = new CraftingConsumeInputStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket116210.ACTION_CRAFTING_MARK_SECONDARY_RESULT_SLOT: {
                CraftingCreateSpecificResultStackRequestAction action = new CraftingCreateSpecificResultStackRequestAction();
                action.resultIndex = stream.getByte();
                return action;
            }
            case ItemStackRequestPacket116210.ACTION_LAB_TABLE_COMBINE: {
                LabTableCombineStackRequestAction action = new LabTableCombineStackRequestAction();
                return action;
            }
            case ItemStackRequestPacket116210.ACTION_BEACON_PAYMENT: {
                BeaconPaymentStackRequestAction action = new BeaconPaymentStackRequestAction();
                action.primaryEffectId = stream.getVarInt();
                action.secondaryEffectId = stream.getVarInt();
                return action;
            }
            case ItemStackRequestPacket116210.ACTION_MINE_BLOCK: {
                MineBlockStackRequestAction action = new MineBlockStackRequestAction();
                action.hotbarSlot = stream.getVarInt();
                action.predictedDurability = stream.getVarInt();
                action.stackId = stream.getVarInt();
                return action;
            }
            case ItemStackRequestPacket116210.ACTION_CRAFTING_RECIPE: {
                CraftRecipeStackRequestAction action = new CraftRecipeStackRequestAction();
                action.recipeId = (int) stream.getUnsignedVarInt();
                return action;
            }
            case ItemStackRequestPacket116210.ACTION_CRAFTING_RECIPE_AUTO: {
                CraftRecipeAutoStackRequestAction action = new CraftRecipeAutoStackRequestAction();
                action.recipeId = (int) stream.getUnsignedVarInt();
                action.repetitions = stream.getByte();
                return action;
            }
            case ItemStackRequestPacket116210.ACTION_CREATIVE_CREATE: {
                CreativeCreateStackRequestAction action = new CreativeCreateStackRequestAction();
                action.creativeItemId = (int) stream.getUnsignedVarInt();
                return action;
            }
            case ItemStackRequestPacket116210.ACTION_CRAFTING_RECIPE_OPTIONAL: {
                CraftRecipeOptionalStackRequestAction action = new CraftRecipeOptionalStackRequestAction();
                action.recipeId = (int) stream.getUnsignedVarInt();
                action.filterStringIndex = stream.getLInt();
                return action;
            }
            case ItemStackRequestPacket116210.ACTION_CRAFTING_NON_IMPLEMENTED_DEPRECATED_ASK_TY_LAING: {
                DeprecatedCraftingNonImplementedStackRequestAction action = new DeprecatedCraftingNonImplementedStackRequestAction();
                return action;
            }
            case ItemStackRequestPacket116210.ACTION_CRAFTING_RESULTS_DEPRECATED_ASK_TY_LAING: {
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
}

package org.itxtech.synapseapi.multiprotocol.protocol12120;

import cn.nukkit.inventory.recipe.RecipeIngredient;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.EntityLink;
import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackResponseContainerInfo;
import cn.nukkit.network.protocol.types.ItemStackResponseSlotInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.action.*;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.ItemStackRequestPacket11810;
import org.itxtech.synapseapi.multiprotocol.protocol1212.BinaryStreamHelper1212;

@Log4j2
public class BinaryStreamHelper12120 extends BinaryStreamHelper1212 {
    public static BinaryStreamHelper12120 create() {
        return new BinaryStreamHelper12120();
    }

    @Override
    public String getGameVersion() {
        return "1.21.20";
    }

    @Override
    public boolean isNetEase() {
        return false;
    }

    @Override
    public void putEntityLink(BinaryStream stream, EntityLink link) {
        stream.putEntityUniqueId(link.fromEntityUniquieId);
        stream.putEntityUniqueId(link.toEntityUniquieId);
        stream.putByte(link.type);
        stream.putBoolean(link.immediate);
        stream.putBoolean(link.riderInitiated);
        stream.putLFloat(link.vehicleAngularVelocity);
    }

    @Override
    protected ItemStackRequestAction getItemStackRequestAction(BinaryStream stream) {
        int type = stream.getByte();
        switch (type) {
            case ItemStackRequestPacket11810.ACTION_TAKE: {
                TakeStackRequestAction action = new TakeStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.destination = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_PLACE: {
                PlaceStackRequestAction action = new PlaceStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.destination = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_SWAP: {
                SwapStackRequestAction action = new SwapStackRequestAction();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.destination = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_DROP: {
                DropStackRequestAction action = new DropStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                action.randomly = stream.getBoolean();
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_DESTROY: {
                DestroyStackRequestAction action = new DestroyStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_CRAFTING_CONSUME_INPUT: {
                CraftingConsumeInputStackRequestAction action = new CraftingConsumeInputStackRequestAction();
                action.count = stream.getByte();
                action.source = this.getItemStackRequestSlotInfo(stream);
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_CRAFTING_MARK_SECONDARY_RESULT_SLOT: {
                CraftingCreateSpecificResultStackRequestAction action = new CraftingCreateSpecificResultStackRequestAction();
                action.resultIndex = stream.getByte();
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_LAB_TABLE_COMBINE: {
                LabTableCombineStackRequestAction action = new LabTableCombineStackRequestAction();
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_BEACON_PAYMENT: {
                BeaconPaymentStackRequestAction action = new BeaconPaymentStackRequestAction();
                action.primaryEffectId = stream.getVarInt();
                action.secondaryEffectId = stream.getVarInt();
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_MINE_BLOCK: {
                MineBlockStackRequestAction action = new MineBlockStackRequestAction();
                action.hotbarSlot = stream.getVarInt();
                action.predictedDurability = stream.getVarInt();
                action.stackId = stream.getVarInt();
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_CRAFTING_RECIPE: {
                CraftRecipeStackRequestAction action = new CraftRecipeStackRequestAction();
                action.recipeId = (int) stream.getUnsignedVarInt();
                action.repetitions = stream.getByte();
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_CRAFTING_RECIPE_AUTO: {
                CraftRecipeAutoStackRequestAction action = new CraftRecipeAutoStackRequestAction();
                action.recipeId = (int) stream.getUnsignedVarInt();
                action.repetitions = stream.getByte();
                action.repetitions2 = stream.getByte();
                int length = stream.getByte();
                RecipeIngredient[] ingredients = new RecipeIngredient[length];
                for (int i = 0; i < length; i++) {
                    ingredients[i] = getRecipeIngredient(stream);
                }
                action.ingredients = ingredients;
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_CREATIVE_CREATE: {
                CreativeCreateStackRequestAction action = new CreativeCreateStackRequestAction();
                action.creativeItemId = (int) stream.getUnsignedVarInt();
                action.repetitions = stream.getByte();
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_CRAFTING_RECIPE_OPTIONAL: {
                CraftRecipeOptionalStackRequestAction action = new CraftRecipeOptionalStackRequestAction();
                action.recipeId = (int) stream.getUnsignedVarInt();
                action.filterStringIndex = stream.getLInt();
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_CRAFTING_GRINDSTONE: {
                GrindstoneStackRequestAction action = new GrindstoneStackRequestAction();
                action.recipeId = (int) stream.getUnsignedVarInt();
                action.repairCost = stream.getVarInt();
                action.repetitions = stream.getByte();
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_CRAFTING_LOOM: {
                LoomStackRequestAction action = new LoomStackRequestAction();
                action.patternId = stream.getString();
                action.repetitions = stream.getByte();
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_CRAFTING_NON_IMPLEMENTED_DEPRECATED_ASK_TY_LAING: {
                DeprecatedCraftingNonImplementedStackRequestAction action = new DeprecatedCraftingNonImplementedStackRequestAction();
                return action;
            }
            case ItemStackRequestPacket11810.ACTION_CRAFTING_RESULTS_DEPRECATED_ASK_TY_LAING: {
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
    protected void putItemStackResponseContainerInfo(BinaryStream stream, ItemStackResponseContainerInfo container) {
        stream.putByte(container.containerId);
        Integer dynamicContainerId = container.dynamicContainerId;
        stream.putLInt(dynamicContainerId == null ? 0 : dynamicContainerId);
        stream.putUnsignedVarInt(container.slots.length);
        for (ItemStackResponseSlotInfo slot : container.slots) {
            putItemStackResponseSlotInfo(stream, slot);
        }
    }
}

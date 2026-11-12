package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.inventory.*;
import cn.nukkit.inventory.recipe.RecipeIngredient;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.CraftingDataPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.util.List;
import java.util.stream.Collectors;

import static org.itxtech.synapseapi.SynapseSharedConstants.SERVER_AUTHORITATIVE_INVENTORY;

@ToString
public class CraftingDataPacket12620 extends Packet12620 {
    public static final int NETWORK_ID = ProtocolInfo.CRAFTING_DATA_PACKET;

    public static final int UNLOCK_NONE = 0;
    public static final int UNLOCK_ALWAYS_UNLOCKED = 1;
    public static final int UNLOCK_IN_WATER = 2;
    public static final int UNLOCK_HAS_ITEMS = 3;

    private List<Recipe> entries = new ObjectArrayList<>();
    private List<BrewingRecipe> brewingEntries = new ObjectArrayList<>();
    private List<ContainerRecipe> containerEntries = new ObjectArrayList<>();
    public List<MaterialReducerRecipe> materialReducerEntries = new ObjectArrayList<>();
    public boolean cleanRecipes;

    @Override
    public DataPacket clean() {
        entries = new ObjectArrayList<>();
        return super.clean();
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putUnsignedVarInt(entries.size());

        int recipeNetworkId = 1;

        for (Recipe recipe : entries) {
            RecipeType type = recipe.getType();
            RecipeType networkType = type == RecipeType.FURNACE || type == RecipeType.FURNACE_DATA ? RecipeType.SHAPELESS : type;
            this.putVarInt(networkType.ordinal());
            switch (type) {
                case SHAPELESS:
                case SHAPELESS_USER_DATA:
                case SHAPELESS_CHEMISTRY:
                    ShapelessRecipe shapeless = (ShapelessRecipe) recipe;
                    this.putString(shapeless.getRecipeId());
                    List<RecipeIngredient> ingredients = shapeless.getIngredientList();
                    this.putUnsignedVarInt(ingredients.size());
                    for (RecipeIngredient ingredient : ingredients) {
                        this.putRecipeIngredient(ingredient);
                    }
                    if (SERVER_AUTHORITATIVE_INVENTORY) {
                        writeShapelessResults(shapeless);
                    } else {
                        this.putUnsignedVarInt(1); // outputs.length
                        this.putItemInstance(shapeless.getResult());
                    }
                    this.putUUID(shapeless.getId());
                    this.putString(shapeless.getTag().toString());
                    this.putVarInt(shapeless.getPriority());
                    if (type == RecipeType.SHAPELESS || type == RecipeType.SHAPELESS_USER_DATA) {
                        int unlockType = /*shapeless.getUnlockingContext()*/UNLOCK_ALWAYS_UNLOCKED;
                        this.putByte(unlockType);
                        if (unlockType == UNLOCK_NONE) {
                            /*
                            List<Item> unlockingIngredients = shapeless.getUnlockingIngredientList();
                            this.putUnsignedVarInt(unlockingIngredients.size());
                            for (Item unlockingIngredient : unlockingIngredients) {
                                this.putCraftingRecipeIngredient(unlockingIngredient);
                            }
                            */this.putUnsignedVarInt(0);
                        }
                    }
                    this.putUnsignedVarInt(SERVER_AUTHORITATIVE_INVENTORY ? recipe.getNetworkId() : recipeNetworkId++);
                    break;
                case SHAPED:
                case SHAPED_CHEMISTRY:
                    ShapedRecipe shaped = (ShapedRecipe) recipe;
                    this.putString(shaped.getRecipeId());
                    this.putVarInt(shaped.getWidth());
                    this.putVarInt(shaped.getHeight());

                    for (int z = 0; z < shaped.getHeight(); ++z) {
                        for (int x = 0; x < shaped.getWidth(); ++x) {
                            this.putRecipeIngredient(shaped.getIngredient(x, z));
                        }
                    }
                    List<Item> outputs = new ObjectArrayList<>();
                    outputs.add(shaped.getResult());
                    outputs.addAll(shaped.getExtraResults());
                    this.putUnsignedVarInt(outputs.size());
                    for (Item output : outputs) {
                        this.putItemInstance(output);
                    }
                    this.putUUID(shaped.getId());
                    this.putString(shaped.getTag().toString());
                    this.putVarInt(shaped.getPriority());
                    this.putBoolean(shaped.isAssumeSymmetry());
                    if (type == RecipeType.SHAPED) {
                        int unlockType = /*shaped.getUnlockingContext()*/UNLOCK_ALWAYS_UNLOCKED;
                        this.putByte(unlockType);
                        if (unlockType == UNLOCK_NONE) {
                            /*
                            unlockingIngredients = shaped.getUnlockingIngredientList();
                            this.putUnsignedVarInt(unlockingIngredients.size());
                            for (Item unlockingIngredient : unlockingIngredients) {
                                this.putCraftingRecipeIngredient(unlockingIngredient);
                            }
                            */this.putUnsignedVarInt(0);
                        }
                    }
                    this.putUnsignedVarInt(SERVER_AUTHORITATIVE_INVENTORY ? recipe.getNetworkId() : recipeNetworkId++);
                    break;
                case FURNACE:
                case FURNACE_DATA:
                    FurnaceRecipe furnace = (FurnaceRecipe) recipe;
                    this.putString(furnace.getRecipeId());
                    this.putUnsignedVarInt(1); // inputs.length
                    this.putCraftingRecipeIngredient(furnace.getInput());
                    this.putUnsignedVarInt(1); // outputs.length
                    this.putItemInstance(furnace.getResult());
                    this.putUUID(furnace.getId());
                    this.putString(furnace.getTag().toString());
                    this.putVarInt(furnace.getPriority());
                    this.putByte(UNLOCK_ALWAYS_UNLOCKED);
                    this.putUnsignedVarInt(SERVER_AUTHORITATIVE_INVENTORY ? recipe.getNetworkId() : recipeNetworkId++);
                    break;
                case MULTI:
                    this.putUUID(((MultiRecipe) recipe).getId());
                    this.putUnsignedVarInt(SERVER_AUTHORITATIVE_INVENTORY ? recipe.getNetworkId() : recipeNetworkId++);
                    break;
                case SMITHING_TRANSFORM:
                    SmithingTransformRecipe smithing = (SmithingTransformRecipe) recipe;
                    this.putString(smithing.getRecipeId());
                    this.putRecipeIngredient(smithing.getTemplate());
                    this.putRecipeIngredient(smithing.getBase());
                    this.putRecipeIngredient(smithing.getIngredient());
                    this.putItemInstance(smithing.getResult());
                    this.putString(smithing.getTag().toString());
                    this.putUnsignedVarInt(SERVER_AUTHORITATIVE_INVENTORY ? recipe.getNetworkId() : recipeNetworkId++);
                    break;
                case SMITHING_TRIM:
                    SmithingTrimRecipe trim = (SmithingTrimRecipe) recipe;
                    this.putString(trim.getRecipeId());
                    this.putRecipeIngredient(trim.getTemplate());
                    this.putRecipeIngredient(trim.getBase());
                    this.putRecipeIngredient(trim.getIngredient());
                    this.putString(trim.getTag().toString());
                    this.putUnsignedVarInt(SERVER_AUTHORITATIVE_INVENTORY ? recipe.getNetworkId() : recipeNetworkId++);
                    break;
            }
        }

        this.putUnsignedVarInt(this.brewingEntries.size());
        for (BrewingRecipe recipe : brewingEntries) {
            this.helper.putBrewingRecipeItem(this, recipe.getInput());
            this.helper.putBrewingRecipeItem(this, recipe.getIngredient());
            this.helper.putBrewingRecipeItem(this, recipe.getResult());
        }

        this.putUnsignedVarInt(this.containerEntries.size());
        for (ContainerRecipe recipe : containerEntries) {
            this.putVarInt(this.helper.getItemNetworkId(this, recipe.getInput()));
            this.putVarInt(this.helper.getItemNetworkId(this, recipe.getIngredient()));
            this.putVarInt(this.helper.getItemNetworkId(this, recipe.getResult()));
        }

        this.putUnsignedVarInt(this.materialReducerEntries.size());
        for (MaterialReducerRecipe recipe : materialReducerEntries) {
            this.helper.putMaterialReducerRecipeIngredient(this, recipe.getInput());

            List<Item> outputs = recipe.getOutputs();
            this.putUnsignedVarInt(outputs.size());
            for (Item output : outputs) {
                this.putVarInt(this.helper.getItemNetworkId(this, output));
                this.putVarInt(output.getCount());
            }
        }

        this.putBoolean(cleanRecipes);
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    private void writeShapelessResults(ShapelessRecipe recipe) {
        List<Item> results = recipe.getAllResults();
        putUnsignedVarInt(results.size());
        for (Item result : results) {
            putItemInstance(result);
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        CraftingDataPacket packet = (CraftingDataPacket) pk;
        this.entries = packet.entries.stream().map(e -> (Recipe) e).collect(Collectors.toList());
        this.brewingEntries = packet.brewingEntries;
        this.containerEntries = packet.containerEntries;
        this.materialReducerEntries = packet.materialReducerEntries;
        this.cleanRecipes = packet.cleanRecipes;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return CraftingDataPacket.class;
    }
}

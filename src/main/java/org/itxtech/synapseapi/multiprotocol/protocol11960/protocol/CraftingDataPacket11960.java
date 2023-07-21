package org.itxtech.synapseapi.multiprotocol.protocol11960.protocol;

import cn.nukkit.inventory.BrewingRecipe;
import cn.nukkit.inventory.ContainerRecipe;
import cn.nukkit.inventory.FurnaceRecipe;
import cn.nukkit.inventory.MaterialReducerRecipe;
import cn.nukkit.inventory.MultiRecipe;
import cn.nukkit.inventory.Recipe;
import cn.nukkit.inventory.RecipeType;
import cn.nukkit.inventory.ShapedRecipe;
import cn.nukkit.inventory.ShapelessRecipe;
import cn.nukkit.inventory.SmithingTransformRecipe;
import cn.nukkit.inventory.SmithingTrimRecipe;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.CraftingDataPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.List;
import java.util.stream.Collectors;

@ToString
public class CraftingDataPacket11960 extends Packet11960 {
    public static final byte NETWORK_ID = ProtocolInfo.CRAFTING_DATA_PACKET;

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
            this.putVarInt(recipe.getType().ordinal());
            switch (recipe.getType()) {
                case SHAPELESS:
                case SHULKER_BOX:
                case SHAPELESS_CHEMISTRY:
                    ShapelessRecipe shapeless = (ShapelessRecipe) recipe;
                    this.putString(shapeless.getRecipeId());
                    List<Item> ingredients = shapeless.getIngredientList();
                    this.putUnsignedVarInt(ingredients.size());
                    for (Item ingredient : ingredients) {
                        this.putCraftingRecipeIngredient(ingredient);
                    }
                    this.putUnsignedVarInt(1);
                    this.putItemInstance(shapeless.getResult());
                    this.putUUID(shapeless.getId());
                    this.putString(shapeless.getTag().toString());
                    this.putVarInt(shapeless.getPriority());
                    this.putUnsignedVarInt(recipeNetworkId++);
                    break;
                case SHAPED:
                case SHAPED_CHEMISTRY:
                    ShapedRecipe shaped = (ShapedRecipe) recipe;
                    this.putString(shaped.getRecipeId());
                    this.putVarInt(shaped.getWidth());
                    this.putVarInt(shaped.getHeight());

                    for (int z = 0; z < shaped.getHeight(); ++z) {
                        for (int x = 0; x < shaped.getWidth(); ++x) {
                            this.putCraftingRecipeIngredient(shaped.getIngredient(x, z));
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
                    this.putUnsignedVarInt(recipeNetworkId++);
                    break;
                case FURNACE:
                case FURNACE_DATA:
                    FurnaceRecipe furnace = (FurnaceRecipe) recipe;
                    Item input = furnace.getInput();
                    this.putVarInt(this.helper.getItemNetworkId(this, input));
                    if (recipe.getType() == RecipeType.FURNACE_DATA) {
                        this.putVarInt(input.getDamage());
                    }
                    this.putItemInstance(furnace.getResult());
                    this.putString(furnace.getTag().toString());
                    break;
                case MULTI:
                    this.putUUID(((MultiRecipe) recipe).getId());
                    this.putUnsignedVarInt(recipeNetworkId++);
                    break;
                case SMITHING_TRANSFORM:
                    SmithingTransformRecipe smithing = (SmithingTransformRecipe) recipe;
                    this.putString(smithing.getRecipeId());
                    this.putCraftingRecipeIngredient(smithing.getInput());
                    this.putCraftingRecipeIngredient(smithing.getAddition());
                    this.putItemInstance(smithing.getResult());
                    this.putString(smithing.getTag().toString());
                    this.putUnsignedVarInt(recipeNetworkId++);
                    break;
            }
        }

        this.putUnsignedVarInt(this.brewingEntries.size());
        for (BrewingRecipe recipe : brewingEntries) {
            this.putVarInt(this.helper.getItemNetworkId(this, recipe.getInput()));
            this.putVarInt(recipe.getInput().getDamage());
            this.putVarInt(this.helper.getItemNetworkId(this, recipe.getIngredient()));
            this.putVarInt(recipe.getIngredient().getDamage());
            this.putVarInt(this.helper.getItemNetworkId(this, recipe.getResult()));
            this.putVarInt(recipe.getResult().getDamage());
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

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, CraftingDataPacket.class);

        CraftingDataPacket packet = (CraftingDataPacket) pk;

        this.entries = packet.entries.stream()
                .filter(recipe -> !(recipe instanceof SmithingTrimRecipe))
                .map(e -> (Recipe) e)
                .collect(Collectors.toList());
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

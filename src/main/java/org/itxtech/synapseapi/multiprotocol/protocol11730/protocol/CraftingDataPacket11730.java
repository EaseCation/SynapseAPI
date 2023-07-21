package org.itxtech.synapseapi.multiprotocol.protocol11730.protocol;

import cn.nukkit.inventory.*;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.CraftingDataPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nukkit Project Team
 */
@ToString
public class CraftingDataPacket11730 extends Packet11730 {

    public static final byte NETWORK_ID = ProtocolInfo.CRAFTING_DATA_PACKET;

    public static final String CRAFTING_TAG_CRAFTING_TABLE = "crafting_table";
    public static final String CRAFTING_TAG_CARTOGRAPHY_TABLE = "cartography_table";
    public static final String CRAFTING_TAG_STONECUTTER = "stonecutter";
    public static final String CRAFTING_TAG_FURNACE = "furnace";
    public static final String CRAFTING_TAG_CAMPFIRE = "campfire";
    public static final String CRAFTING_TAG_SOUL_CAMPFIRE = "soul_campfire";
    public static final String CRAFTING_TAG_BLAST_FURNACE = "blast_furnace";
    public static final String CRAFTING_TAG_SMOKER = "smoker";

    private List<Recipe> entries = new ObjectArrayList<>();
    private List<BrewingRecipe> brewingEntries = new ObjectArrayList<>();
    private List<ContainerRecipe> containerEntries = new ObjectArrayList<>();
    public List<MaterialReducerRecipe> materialReducerEntries = new ObjectArrayList<>();
    public boolean cleanRecipes;

    public void addShapelessRecipe(ShapelessRecipe... recipe) {
        Collections.addAll(entries, recipe);
    }

    public void addShapedRecipe(ShapedRecipe... recipe) {
        Collections.addAll(entries, recipe);
    }

    public void addFurnaceRecipe(FurnaceRecipe... recipe) {
        Collections.addAll(entries, recipe);
    }

    public void addMultiRecipe(MultiRecipe... recipe) {
        Collections.addAll(entries, recipe);
    }

    public void addBrewingRecipe(BrewingRecipe... recipe) {
        Collections.addAll(brewingEntries, recipe);
    }

    public void addContainerRecipe(ContainerRecipe... recipe) {
        Collections.addAll(containerEntries, recipe);
    }

    public void addMaterialReducerRecipe(MaterialReducerRecipe... recipe) {
        Collections.addAll(materialReducerEntries, recipe);
    }

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
            this.putVarInt(type == RecipeType.SMITHING_TRANSFORM ? RecipeType.SHAPELESS.ordinal() : type.ordinal());
            switch (type) {
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
                case SMITHING_TRANSFORM: // actually shapeless recipe
                    SmithingTransformRecipe smithing = (SmithingTransformRecipe) recipe;
                    this.putString(smithing.getRecipeId());
                    this.putUnsignedVarInt(2); // ingredients
                    this.putCraftingRecipeIngredient(smithing.getInput());
                    this.putCraftingRecipeIngredient(smithing.getAddition());
                    this.putUnsignedVarInt(1); // results
                    this.putItemInstance(smithing.getResult());
                    this.putUUID(smithing.getInternalId());
                    this.putString(smithing.getTag().toString());
                    this.putVarInt(2); // priority
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
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        ClassUtils.requireInstance(pk, CraftingDataPacket.class);

        CraftingDataPacket packet = (CraftingDataPacket) pk;

        boolean dataDrivenSmithingTransform = protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_118_10.getProtocolStart();

        this.entries = packet.entries.stream()
                .filter(recipe -> !(recipe instanceof SmithingTrimRecipe) && (!(recipe instanceof SmithingTransformRecipe) || dataDrivenSmithingTransform))
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

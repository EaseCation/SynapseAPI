package org.itxtech.synapseapi.multiprotocol.protocol12080.protocol;

import cn.nukkit.inventory.*;
import cn.nukkit.inventory.recipe.RecipeIngredient;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.CraftingDataPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.item.RecipeFlattener;
import org.itxtech.synapseapi.multiprotocol.utils.item.TodoCraftingRecipe;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@ToString
public class CraftingDataPacket12080 extends Packet12080 {
    public static final int NETWORK_ID = ProtocolInfo.CRAFTING_DATA_PACKET;

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
            if (type == null) { //TODO: ItemDescriptor
                TodoCraftingRecipe todo = (TodoCraftingRecipe) recipe;
                type = todo.getRecipeType();
                this.putVarInt(type.ordinal());
                switch (type) {
                    case SHAPELESS: {
                        this.putString(todo.getRecipeId());
                        List<RecipeIngredient> ingredients = todo.getShapelessInput();
                        this.putUnsignedVarInt(ingredients.size());
                        for (RecipeIngredient ingredient : ingredients) {
                            this.helper.putRecipeIngredient(this, ingredient);
                        }
                        this.putUnsignedVarInt(1);
                        this.putItemInstance(todo.getResult());
                        this.putUUID(todo.getId());
                        this.putString(todo.getTag().toString());
                        this.putVarInt(todo.getPriority());
                        this.putUnsignedVarInt(recipeNetworkId++);
                        break;
                    }
                    case SHAPED: {
                        this.putString(todo.getRecipeId());
                        this.putVarInt(todo.getShapedWidth());
                        this.putVarInt(todo.getShapedHeight());
                        for (int z = 0; z < todo.getShapedHeight(); ++z) {
                            for (int x = 0; x < todo.getShapedWidth(); ++x) {
                                this.helper.putRecipeIngredient(this, todo.getShapedInput(x, z));
                            }
                        }
                        List<Item> outputs = new ObjectArrayList<>();
                        outputs.add(todo.getResult());
                        outputs.addAll(todo.getExtraResults());
                        this.putUnsignedVarInt(outputs.size());
                        for (Item output : outputs) {
                            this.putItemInstance(output);
                        }
                        this.putUUID(todo.getId());
                        this.putString(todo.getTag().toString());
                        this.putVarInt(todo.getPriority());
                        this.putBoolean(todo.isAssumeSymmetry());
                        this.putUnsignedVarInt(recipeNetworkId++);
                        break;
                    }
                    default: {
                        throw new IllegalStateException("Unimplemented todo recipe type: " + type);
                    }
                }
                continue;
            }

            this.putVarInt(type.ordinal());
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
                    this.putBoolean(shaped.isAssumeSymmetry());
                    this.putUnsignedVarInt(recipeNetworkId++);
                    break;
                case FURNACE:
                case FURNACE_DATA:
                    FurnaceRecipe furnace = (FurnaceRecipe) recipe;
                    this.helper.putFurnaceRecipeIngredient(this, furnace.getInput(), type);
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
                    this.putCraftingRecipeIngredient(smithing.getTemplate());
                    this.putCraftingRecipeIngredient(smithing.getInput());
                    this.putCraftingRecipeIngredient(smithing.getAddition());
                    this.putItemInstance(smithing.getResult());
                    this.putString(smithing.getTag().toString());
                    this.putUnsignedVarInt(recipeNetworkId++);
                    break;
                case SMITHING_TRIM:
                    SmithingTrimRecipe trim = (SmithingTrimRecipe) recipe;
                    this.putString(trim.getRecipeId());
                    this.putCraftingRecipeIngredient(trim.getTemplate());
                    this.putCraftingRecipeIngredient(trim.getInput());
                    this.putCraftingRecipeIngredient(trim.getAddition());
                    this.putString(trim.getTag().toString());
                    this.putUnsignedVarInt(recipeNetworkId++);
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

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        CraftingDataPacket packet = (CraftingDataPacket) pk;

        this.entries = packet.entries.stream().map(e -> (Recipe) e).collect(Collectors.toList());
        this.brewingEntries = packet.brewingEntries;
        this.containerEntries = packet.containerEntries;
        this.materialReducerEntries = packet.materialReducerEntries;
        this.cleanRecipes = packet.cleanRecipes;

        RecipeFlattener.addFlattenedRecipes(protocol, this.entries);

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return CraftingDataPacket.class;
    }
}

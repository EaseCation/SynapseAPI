package org.itxtech.synapseapi.multiprotocol.protocol112.protocol;

import cn.nukkit.inventory.*;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nukkit Project Team
 */
@ToString
public class CraftingDataPacket112 extends Packet112 {

    public static final int NETWORK_ID = ProtocolInfo.CRAFTING_DATA_PACKET;

    public static final String CRAFTING_TAG_CRAFTING_TABLE = "crafting_table";
    public static final String CRAFTING_TAG_CARTOGRAPHY_TABLE = "cartography_table";
    public static final String CRAFTING_TAG_STONECUTTER = "stonecutter";
    public static final String CRAFTING_TAG_FURNACE = "furnace";
    public static final String CRAFTING_TAG_CAMPFIRE = "campfire";
    public static final String CRAFTING_TAG_BLAST_FURNACE = "blast_furnace";
    public static final String CRAFTING_TAG_SMOKER = "smoker";

    public List<Recipe> entries = new ObjectArrayList<>();
    public boolean cleanRecipes;

    public void addShapelessRecipe(ShapelessRecipe... recipe) {
        Collections.addAll(entries, (ShapelessRecipe[]) recipe);
    }

    public void addShapedRecipe(ShapedRecipe... recipe) {
        Collections.addAll(entries, (ShapedRecipe[]) recipe);
    }

    public void addFurnaceRecipe(FurnaceRecipe... recipe) {
        Collections.addAll(entries, (FurnaceRecipe[]) recipe);
    }

    public void addMultiRecipe(MultiRecipe... recipe) {
        Collections.addAll(entries, recipe);
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
                    this.putSlot(shapeless.getResult());
                    this.putUUID(shapeless.getId());
                    this.putString(shapeless.getTag().toString());
                    this.putVarInt(shapeless.getPriority());
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
                        this.putSlot(output);
                    }
                    this.putUUID(shaped.getId());
                    this.putString(shaped.getTag().toString());
                    this.putVarInt(shaped.getPriority());
                    break;
                case FURNACE:
                case FURNACE_DATA:
                    FurnaceRecipe furnace = (FurnaceRecipe) recipe;
                    Item input = furnace.getInput();
                    this.putVarInt(input.getId());
                    if (recipe.getType() == RecipeType.FURNACE_DATA) {
                        this.putVarInt(input.getDamage());
                    }
                    this.putSlot(furnace.getResult());
                    this.putString(furnace.getTag().toString());
                    break;
                case MULTI:
                    this.putUUID(((MultiRecipe) recipe).getId());
                    break;
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
        ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.CraftingDataPacket.class);

        cn.nukkit.network.protocol.CraftingDataPacket packet = (cn.nukkit.network.protocol.CraftingDataPacket) pk;

        this.entries = packet.entries.stream().map(e -> (Recipe) e).collect(Collectors.toList());
        this.cleanRecipes = packet.cleanRecipes;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.CraftingDataPacket.class;
    }

}

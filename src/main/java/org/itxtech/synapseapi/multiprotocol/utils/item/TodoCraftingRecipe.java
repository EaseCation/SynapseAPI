package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.inventory.CraftingManager;
import cn.nukkit.inventory.CraftingRecipe;
import cn.nukkit.inventory.RecipeTag;
import cn.nukkit.inventory.RecipeType;
import cn.nukkit.inventory.recipe.RecipeIngredient;
import cn.nukkit.item.Item;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;

import java.util.*;

public class TodoCraftingRecipe implements CraftingRecipe {
    private final RecipeType type;
    private final UUID uuid;
    private final String recipeId;

    private final Item output;

    private final List<RecipeIngredient> shapelessInput = new ArrayList<>();

    private final String[] shapedPattern;
    private final Char2ObjectMap<RecipeIngredient> shapedInput = new Char2ObjectOpenHashMap<>();
    private final boolean assumeSymmetry = true; //TODO: 1.20.80+

    private TodoCraftingRecipe(RecipeType type, UUID uuid, Item output, String[] shapedPattern) {
        this.type = type;
        this.uuid = uuid;
        this.recipeId = uuid.toString().replace("-", "");
        this.output = output;
        this.shapedPattern = shapedPattern;
    }

    public static TodoCraftingRecipe shapeless(Item output, List<RecipeIngredient> input) {
        TodoCraftingRecipe recipe = new TodoCraftingRecipe(RecipeType.SHAPELESS, UUID.randomUUID(), output, null);
        recipe.shapelessInput.addAll(input);
        return recipe;
    }

    public static TodoCraftingRecipe shaped(Item output, String[] shapedPattern, Map<Character, RecipeIngredient> input) {
        TodoCraftingRecipe recipe = new TodoCraftingRecipe(RecipeType.SHAPED, UUID.randomUUID(), output, shapedPattern);
        recipe.shapedInput.putAll(input);
        return recipe;
    }

    @Override
    public String getRecipeId() {
        return recipeId;
    }

    @Override
    public UUID getId() {
        return uuid;
    }

    @Override
    public void setId(UUID id) {
    }

    @Override
    public boolean requiresCraftingTable() {
        return false;
    }

    @Override
    public List<Item> getExtraResults() {
        return Collections.emptyList();
    }

    @Override
    public List<Item> getAllResults() {
        return Collections.singletonList(output);
    }

    @Override
    public int getPriority() {
        return -1;
    }

    @Override
    public boolean matchItems(List<Item> inputList, List<Item> extraOutputList, int multiplier) {
        return false;
    }

    @Override
    public List<Item> getIngredientsAggregate() {
        return Collections.emptyList();
    }

    @Override
    public Item getResult() {
        return output;
    }

    @Override
    public void registerToCraftingManager(CraftingManager manager) {
    }

    @Override
    public RecipeType getType() {
        return null;
    }

    @Override
    public RecipeTag getTag() {
        return RecipeTag.CRAFTING_TABLE;
    }

    public RecipeType getRecipeType() {
        return type;
    }

    public List<RecipeIngredient> getShapelessInput() {
        return shapelessInput;
    }

    public String[] getShaped() {
        return shapedPattern;
    }

    public int getShapedWidth() {
        return shapedPattern[0].length();
    }

    public int getShapedHeight() {
        return shapedPattern.length;
    }

    public Char2ObjectMap<RecipeIngredient> getShapedInput() {
        return shapedInput;
    }

    public RecipeIngredient getShapedInput(int x, int y) {
        RecipeIngredient item = shapedInput.get(shapedPattern[y].charAt(x));
        return item != null ? item : RecipeIngredient.EMPTY;
    }

    public boolean isAssumeSymmetry() {
        return assumeSymmetry;
    }
}

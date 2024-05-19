package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.inventory.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.Items;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.itxtech.synapseapi.SynapseSharedConstants.*;

@Log4j2
public class CraftingManagerLegacy extends CraftingManager {
    protected static final UnsupportedOperationException UNSUPPORTED_ITEM_EXCEPTION = new UnsupportedOperationException();

    @Override
    protected void initialize() {
        log.debug("Loading advanced (legacy) crafting manager...");

        JsonObject root;
        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(ByteStreams.toByteArray(SynapseAPI.class.getClassLoader().getResourceAsStream("recipes116100.json"))))) {
            root = new Gson().fromJson(reader, JsonObject.class);
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load recipes.json", e);
        }

        RECIPE_COUNT = 1;

        root.getAsJsonArray("shapeless").forEach(element -> loadShapeless(element, ShapelessRecipe::new, RecipeType.SHAPELESS));
//        root.getAsJsonArray("shapeless_shulker_box").forEach(element -> loadShapeless(element, ShulkerBoxRecipe::new, RecipeType.SHULKER_BOX)); //TODO: nbt
        if (ENABLE_CHEMISTRY_FEATURE) {
            root.getAsJsonArray("shapeless_chemistry").forEach(element -> loadShapeless(element, ShapelessChemistryRecipe::new, RecipeType.SHAPELESS_CHEMISTRY));
        }

        root.getAsJsonArray("shaped").forEach(element -> loadShaped(element, ShapedRecipe::new));
        if (ENABLE_CHEMISTRY_FEATURE) {
            root.getAsJsonArray("shaped_chemistry").forEach(element -> loadShaped(element, ShapedChemistryRecipe::new));
        }

        root.getAsJsonArray("smelting").forEach(this::loadSmelting);

        root.getAsJsonArray("special_hardcoded").forEach(this::loadHardcoded);

        root.getAsJsonArray("potion_type").forEach(this::loadPotionType);

        root.getAsJsonArray("potion_container_change").forEach(this::loadPotionContainer);

        root.getAsJsonArray("smithing").forEach(this::loadSmithing);

        log.info("Loaded " + this.recipes.size() + " recipes.");
    }

    protected void loadShapeless(JsonElement element, Shapeless supplier, RecipeType type) {
        JsonObject entry = element.getAsJsonObject();

        RecipeTag tag = getShapelessRecipeTag(entry.get("block").getAsString(), type);
        if (tag == null) {
            return;
        }

        JsonArray input = entry.getAsJsonArray("input");
        JsonArray output = entry.getAsJsonArray("output");

        // TODO: handle multiple result items
        if (output.size() > 1) {
            log.debug("Skip multiple result recipe: {}", entry);
            return;
        }

        List<Item> ingredients = new ObjectArrayList<>();

        try {
            input.forEach(itemEntry -> ingredients.add(deserializeItem(itemEntry.getAsJsonObject())));
            ingredients.sort(recipeComparator);

            registerRecipe(supplier.create(
                    entry.get("id").getAsString(),
                    Integer.toUnsignedString(RECIPE_COUNT, 36),
                    entry.get("priority").getAsInt(),
                    deserializeItem(output.get(0).getAsJsonObject()),
                    ingredients,
                    tag));
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported shapeless recipe: {}", entry);
        }
    }

    protected void loadShaped(JsonElement element, Shaped supplier) {
        JsonObject entry = element.getAsJsonObject();

        RecipeTag tag = getShapedRecipeTag(entry.get("block").getAsString());
        if (tag == null) {
            return;
        }

        JsonArray shape = entry.getAsJsonArray("shape");
        JsonObject input = entry.getAsJsonObject("input");
        JsonArray output = entry.getAsJsonArray("output");

        Char2ObjectMap<Item> ingredients = new Char2ObjectOpenHashMap<>();
        Item firstResult;
        List<Item> extraResults = new ObjectArrayList<>();

        try {
            input.asMap().forEach((symbol, item) -> ingredients.put(symbol.charAt(0), deserializeItem(item.getAsJsonObject())));

            if (output.size() == 1) {
                firstResult = deserializeItem(output.get(0).getAsJsonObject());
            } else {
                firstResult = deserializeItem(output.remove(0).getAsJsonObject());
                output.forEach(item -> extraResults.add(deserializeItem(item.getAsJsonObject())));
            }

            registerRecipe(supplier.create(
                    entry.get("id").getAsString(),
                    Integer.toUnsignedString(RECIPE_COUNT, 36),
                    entry.get("priority").getAsInt(),
                    firstResult,
                    shape.asList().stream().map(JsonElement::getAsString).toArray(String[]::new),
                    ingredients,
                    extraResults,
                    tag));
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported shaped recipe: {}", entry);
        }
    }

    protected void loadSmelting(JsonElement element) {
        JsonObject entry = element.getAsJsonObject();

        RecipeTag tag = getSmeltingRecipeTag(entry.get("block").getAsString());
        if (tag == null) {
            return;
        }

        try {
            registerRecipe(new FurnaceRecipe(
                    deserializeItem(entry.getAsJsonObject("output")),
                    deserializeItem(entry.getAsJsonObject("input")),
                    tag));
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported smelting recipe: {}", entry);
        }
    }

    protected void loadHardcoded(JsonElement element) {
        registerRecipe(new MultiRecipe(UUID.fromString(element.getAsString())));
    }

    protected void loadPotionType(JsonElement element) {
        JsonObject entry = element.getAsJsonObject();

        try {
            registerBrewingRecipe(new BrewingRecipe(
                    deserializeItem(entry.getAsJsonObject("input")),
                    deserializeItem(entry.getAsJsonObject("ingredient")),
                    deserializeItem(entry.getAsJsonObject("output"))));
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported potion type recipe: {}", entry);
        }
    }

    protected void loadPotionContainer(JsonElement element) {
        JsonObject entry = element.getAsJsonObject();

        try {
            registerContainerRecipe(new ContainerRecipe(
                    Item.get(entry.get("input_item_id").getAsInt()),
                    deserializeItem(entry.getAsJsonObject("ingredient")),
                    Item.get(entry.get("output_item_id").getAsInt())));
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported potion container recipe: {}", entry);
        }
    }

    protected void loadSmithing(JsonElement element) {
        JsonObject entry = element.getAsJsonObject();

        RecipeTag tag = getSmithingRecipeTag(entry.get("block").getAsString());
        if (tag == null) {
            return;
        }

        try {
            registerRecipe(new SmithingTransformRecipe(
                    Integer.toUnsignedString(++RECIPE_COUNT, 36),
                    deserializeItem(entry.getAsJsonObject("output")),
                    Items.air(), //deserializeItem(entry.getAsJsonObject("template")), //TODO: 1.20.0+
                    deserializeItem(entry.getAsJsonObject("input")),
                    deserializeItem(entry.getAsJsonObject("addition")),
                    tag));
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported smithing recipe: {}", entry);
        }
    }

    protected RecipeTag getShapelessRecipeTag(String block, RecipeType type) {
        RecipeTag tag = RecipeTag.byName(block);
        if (tag == RecipeTag.DEPRECATED) {
            return null;
        }
        if (tag != RecipeTag.CRAFTING_TABLE && (type != RecipeType.SHAPELESS || tag != RecipeTag.CARTOGRAPHY_TABLE && tag != RecipeTag.STONECUTTER && tag != RecipeTag.SMITHING_TABLE)) {
            log.trace("Unexpected shapeless recipe block: {} (type {})", block, type);
            return null;
        }
        return tag;
    }

    protected RecipeTag getShapedRecipeTag(String block) {
        RecipeTag tag = RecipeTag.byName(block);
        if (tag == RecipeTag.DEPRECATED) {
            return null;
        }
        if (tag != RecipeTag.CRAFTING_TABLE) {
            log.trace("Unexpected shaped recipe block: {}", block);
            return null;
        }
        return tag;
    }

    protected RecipeTag getSmeltingRecipeTag(String block) {
        RecipeTag tag = RecipeTag.byName(block);
        if (tag == RecipeTag.DEPRECATED) {
            return null;
        }
        if (tag != RecipeTag.FURNACE && tag != RecipeTag.BLAST_FURNACE && tag != RecipeTag.SMOKER && tag != RecipeTag.CAMPFIRE && tag != RecipeTag.SOUL_CAMPFIRE) {
            log.trace("Unexpected smelting recipe block: {}", block);
            return null;
        }
        return tag;
    }

    protected RecipeTag getSmithingRecipeTag(String block) {
        RecipeTag tag = RecipeTag.byName(block);
        if (tag == RecipeTag.DEPRECATED) {
            return null;
        }
        if (tag != RecipeTag.SMITHING_TABLE) {
            log.trace("Unexpected smithing recipe block: {}", block);
            return null;
        }
        return tag;
    }

    protected Item deserializeItem(JsonObject itemEntry) throws UnsupportedOperationException {
        Item item = Item.getCraftingItem(
                itemEntry.get("id").getAsInt(),
                itemEntry.has("damage") ? itemEntry.get("damage").getAsInt() : 0,
                itemEntry.has("count") ? itemEntry.get("count").getAsInt() : 1,
                itemEntry.has("nbt_b64") ? Base64.getDecoder().decode(itemEntry.get("nbt_b64").getAsString()) : new byte[0]);
        if (item == null) {
            log.debug("unexpected unsupported item: {}", itemEntry);
            throw UNSUPPORTED_ITEM_EXCEPTION;
        }
        return item;
    }

    @FunctionalInterface
    protected interface Shapeless {
        Recipe create(String vanillaRecipeId, String recipeId, int priority, Item result, Collection<Item> ingredients, RecipeTag tag);
    }

    @FunctionalInterface
    protected interface Shaped {
        Recipe create(String vanillaRecipeId, String recipeId, int priority, Item primaryResult, String[] shape, Map<Character, Item> ingredients, List<Item> extraResults, RecipeTag tag);
    }
}

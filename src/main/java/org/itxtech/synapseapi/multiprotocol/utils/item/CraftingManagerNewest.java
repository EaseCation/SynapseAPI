package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.inventory.*;
import cn.nukkit.inventory.recipe.RecipeIngredient;
import cn.nukkit.inventory.recipe.descriptor.TagItemDescriptor;
import cn.nukkit.item.Item;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.log4j.Log4j2;

import java.util.List;

import static org.itxtech.synapseapi.SynapseSharedConstants.SERVER_AUTHORITATIVE_INVENTORY;

@Log4j2
public class CraftingManagerNewest extends CraftingManagerNew {
    private static final String DIRECTORY = "recipes12150/";
    private static final String TAGS_FILE = "item_tags_12150.json";

    @Override
    protected String getDirectory() {
        return DIRECTORY;
    }

    @Override
    protected String getTagsFile() {
        return TAGS_FILE;
    }

    @Override
    protected void loadShapeless(JsonElement element, Shapeless supplier, RecipeType type) {
        JsonObject entry = element.getAsJsonObject();

        RecipeTag tag = getShapelessRecipeTag(entry.get("block").getAsString(), type);
        if (tag == null) {
            return;
        }

        JsonArray input = entry.getAsJsonArray("input");
        JsonArray output = entry.getAsJsonArray("output");

        if (output.isEmpty()) {
            log.debug("Skip a shapeless recipe without output: {}", entry);
            return;
        }

        List<RecipeIngredient> ingredients = new ObjectArrayList<>();

        try {
            input.forEach(itemEntry -> ingredients.add(deserializeIngredient(itemEntry.getAsJsonObject())));

            List<Item> results = new ObjectArrayList<>(output.size());
            for (JsonElement itemEntry : output) {
                Item result = deserializeItem(itemEntry.getAsJsonObject());
                if (!SERVER_AUTHORITATIVE_INVENTORY && result.isBundle()) {
                    return;
                }
                results.add(result);
            }
            ShapelessRecipe recipe = (ShapelessRecipe) supplier.create(
                    null,
                    Integer.toUnsignedString(RECIPE_COUNT, 36),
                    entry.get("priority").getAsInt(),
                    results.getFirst(),
                    ingredients,
                    tag);
            recipe.getExtraResults().addAll(results.subList(1, results.size()));
            registerRecipe(recipe);
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported shapeless recipe: {}", entry);
        }
    }

    @Override
    protected void loadShaped(JsonElement element, Shaped supplier) {
        JsonObject entry = element.getAsJsonObject();

        RecipeTag tag = getShapedRecipeTag(entry.get("block").getAsString());
        if (tag == null) {
            return;
        }

        JsonArray shape = entry.getAsJsonArray("shape");
        JsonObject input = entry.getAsJsonObject("input");
        JsonArray output = entry.getAsJsonArray("output");

        Char2ObjectMap<RecipeIngredient> ingredients = new Char2ObjectOpenHashMap<>();
        Item firstResult;
        List<Item> extraResults = new ObjectArrayList<>();

        try {
            input.asMap().forEach((symbol, item) -> ingredients.put(symbol.charAt(0), deserializeIngredient(item.getAsJsonObject())));

            if (output.size() == 1) {
                firstResult = deserializeItem(output.get(0).getAsJsonObject());
            } else {
                firstResult = deserializeItem(output.remove(0).getAsJsonObject());
                output.forEach(item -> extraResults.add(deserializeItem(item.getAsJsonObject())));
            }

            if (!SERVER_AUTHORITATIVE_INVENTORY && firstResult.isBundle()) {
                return;
            }
            registerRecipe(supplier.create(
                    null,
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

    @Override
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
                    deserializeIngredient(entry.getAsJsonObject("template")),
                    deserializeIngredient(entry.getAsJsonObject("input")),
                    deserializeIngredient(entry.getAsJsonObject("addition")),
                    tag));
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported smithing recipe: {}", entry);
        }
    }

    @Override
    protected void loadSmithingTrim(JsonElement element) {
        JsonObject entry = element.getAsJsonObject();

        RecipeTag tag = getSmithingRecipeTag(entry.get("block").getAsString());
        if (tag == null) {
            return;
        }

        try {
            registerRecipe(new SmithingTrimRecipe(
                    Integer.toUnsignedString(++RECIPE_COUNT, 36),
                    deserializeIngredient(entry.getAsJsonObject("template")),
                    deserializeIngredient(entry.getAsJsonObject("input")),
                    deserializeIngredient(entry.getAsJsonObject("addition")),
                    tag));
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported smithing trim recipe: {}", entry);
        }
    }

    protected RecipeIngredient deserializeIngredient(JsonObject itemEntry) throws UnsupportedOperationException {
        if (!itemEntry.has("tag")) {
            return new RecipeIngredient(deserializeItem(itemEntry));
        }
        int count = itemEntry.has("count") ? itemEntry.get("count").getAsInt() : 1;

        String tag = itemEntry.get("tag").getAsString();
        List<Item> items = tags.get(tag);
        if (items == null) {
            log.warn("Unknown item tag: {}", tag);
            throw UNSUPPORTED_ITEM_EXCEPTION;
        }
        return new RecipeIngredient(new TagItemDescriptor(tag), count);
    }
}

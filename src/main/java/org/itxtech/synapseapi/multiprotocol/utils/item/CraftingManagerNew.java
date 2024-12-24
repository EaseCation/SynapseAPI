package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.inventory.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.Items;
import cn.nukkit.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static cn.nukkit.GameVersion.*;
import static org.itxtech.synapseapi.SynapseSharedConstants.*;

@Log4j2
public class CraftingManagerNew extends CraftingManagerLegacy {
    private static final String DIRECTORY = "recipes12010/";
    private static final String TAGS_FILE = "item_tags_120.json";

    @Override
    protected void initialize() {
        log.debug("Loading advanced crafting manager...");

        Map<String, List<String>> tagMap;
        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(TAGS_FILE)) {
            tagMap = JsonUtil.TRUSTED_JSON_MAPPER.readValue(stream, new TypeReference<>() {
            });
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load item_tags.json", e);
        }

        tagMap.forEach((tagName, itemNames) -> {
            List<Item> items = new ArrayList<>();
            itemNames.forEach(itemName -> items.addAll(TaggedItemFlattener.get(itemName)));
            this.tags.put(tagName, items);
        });

        RECIPE_COUNT = 1;

        loadRecipes("shapeless_crafting.json").forEach(element -> loadShapeless(element, ShapelessRecipe::new, RecipeType.SHAPELESS));
        loadRecipes("shapeless_shulker_box.json").forEach(element -> loadShapeless(element, ShapelessUserDataRecipe::new, RecipeType.SHAPELESS_USER_DATA));
        if (ENABLE_CHEMISTRY_FEATURE) {
            loadRecipes("shapeless_chemistry.json").forEach(element -> loadShapeless(element, ShapelessChemistryRecipe::new, RecipeType.SHAPELESS_CHEMISTRY));
        }

        loadRecipes("shaped_crafting.json").forEach(element -> loadShaped(element, ShapedRecipe::new));
        if (ENABLE_CHEMISTRY_FEATURE) {
            loadRecipes("shaped_chemistry.json").forEach(element -> loadShaped(element, ShapedChemistryRecipe::new));
        }

        loadRecipes("smelting.json").forEach(this::loadSmelting);

        loadRecipes("special_hardcoded.json").forEach(this::loadHardcoded);

        if (SERVER_AUTHORITATIVE_INVENTORY && V1_19_60.isAvailable()) {
            loadRecipes("smithing.json").forEach(this::loadSmithing);
        }

        if (SERVER_AUTHORITATIVE_INVENTORY && V1_19_80.isAvailable()) {
            loadRecipes("smithing_trim.json").forEach(this::loadSmithingTrim);
        }

        loadRecipes("potion_type.json").forEach(this::loadPotionType);

        loadRecipes("potion_container_change.json").forEach(this::loadPotionContainer);

        log.info("Loaded " + this.recipes.size() + " recipes.");
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

        // TODO: handle multiple result items
        if (output.size() > 1) {
            log.debug("Skip multiple result recipe: {}", entry);
            return;
        }

        try {
            List<List<Item>> inputs = new ObjectArrayList<>();
            inputs.add(new ObjectArrayList<>());
            input.forEach(itemEntry -> {
                List<Item> items = deserializeItemOrTag(itemEntry.getAsJsonObject());
                Item ingredient = items.get(0);

                int count = inputs.size();
                for (int i = 0; i < count; i++) {
                    List<Item> ingredients = inputs.get(i);

                    if (items.size() > 1) {
                        for (int t = 1; t < items.size(); t++) {
                            List<Item> materials = new ObjectArrayList<>();
                            ingredients.forEach(material -> materials.add(material.clone()));
                            materials.add(items.get(t).clone());
                            inputs.add(materials);
                        }
                    }

                    ingredients.add(ingredient.clone());
                }
            });

            int priority = entry.get("priority").getAsInt();
            Item result = deserializeItem(output.get(0).getAsJsonObject());

            for (List<Item> ingredients : inputs) {
                ingredients.sort(recipeComparator);

                String recipeId = Integer.toUnsignedString(RECIPE_COUNT, 36);
                registerRecipe(supplier.create(
                        null,
                        recipeId,
                        priority,
                        result,
                        ingredients,
                        tag));
            }
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

        List<Char2ObjectMap<Item>> ingredients = new ArrayList<>();
        Item firstResult;
        List<Item> extraResults = new ObjectArrayList<>();

        try {
            Char2ObjectMap<List<Item>> inputs = new Char2ObjectOpenHashMap<>();
            input.asMap().forEach((symbol, item) -> inputs.put(symbol.charAt(0), deserializeItemOrTag(item.getAsJsonObject())));
            flattenTags(new CharArrayList(inputs.keySet()), inputs, 0, new Char2ObjectOpenHashMap<>(), ingredients);

            if (output.size() == 1) {
                firstResult = deserializeItem(output.get(0).getAsJsonObject());
            } else {
                firstResult = deserializeItem(output.remove(0).getAsJsonObject());
                output.forEach(item -> extraResults.add(deserializeItem(item.getAsJsonObject())));
            }

            for (Char2ObjectMap<Item> materials : ingredients) {
                registerRecipe(supplier.create(
                        null,
                        Integer.toUnsignedString(RECIPE_COUNT, 36),
                        entry.get("priority").getAsInt(),
                        firstResult,
                        shape.asList().stream().map(JsonElement::getAsString).toArray(String[]::new),
                        materials,
                        extraResults,
                        tag));
            }
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported shaped recipe: {}", entry);
        }
    }

    private static void flattenTags(CharList symbols, Char2ObjectMap<List<Item>> ingredients, int index, Char2ObjectMap<Item> current, List<Char2ObjectMap<Item>> result) {
        if (index == symbols.size()) {
            Char2ObjectMap<Item> materials = new Char2ObjectOpenHashMap<>();
            for (Char2ObjectMap.Entry<Item> entry : current.char2ObjectEntrySet()) {
                materials.put(entry.getCharKey(), entry.getValue().clone());
            }
            result.add(materials);
            return;
        }

        char symbol = symbols.getChar(index);
        for (Item ingredient : ingredients.get(symbol)) {
            current.put(symbol, ingredient);
            flattenTags(symbols, ingredients, index + 1, current, result);
            current.remove(symbol);
        }
    }

    @Override
    protected void loadPotionContainer(JsonElement element) {
        JsonObject entry = element.getAsJsonObject();
        String inputName = entry.get("input_item_name").getAsString();
        String outputName = entry.get("output_item_name").getAsString();

        int inputId = Items.getIdByName(inputName, true, true);
        int outputId = Items.getIdByName(outputName, true, true);

        if (inputId == -1) {
            log.trace("Skip an unsupported potion container recipe (input item): {}", entry);
            return;
        }
        if (outputId == -1) {
            log.trace("Skip an unsupported potion container recipe (output item): {}", entry);
            return;
        }

        try {
            registerContainerRecipe(new ContainerRecipe(
                    Item.get(inputId),
                    deserializeItem(entry.getAsJsonObject("ingredient")),
                    Item.get(outputId)));
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported potion container recipe: {}", entry);
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
                    deserializeItem(entry.getAsJsonObject("template")),
                    deserializeItem(entry.getAsJsonObject("input")),
                    deserializeItem(entry.getAsJsonObject("addition")),
                    tag));
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported smithing recipe: {}", entry);
        }
    }

    protected void loadSmithingTrim(JsonElement element) {
        JsonObject entry = element.getAsJsonObject();

        RecipeTag tag = getSmithingRecipeTag(entry.get("block").getAsString());
        if (tag == null) {
            return;
        }

        try {
            List<Item> templates = deserializeItemOrTag(entry.getAsJsonObject("template"));
            List<Item> inputs = deserializeItemOrTag(entry.getAsJsonObject("input"));
            List<Item> additions = deserializeItemOrTag(entry.getAsJsonObject("addition"));

            for (Item template : templates) {
                for (Item input : inputs) {
                    for (Item addition : additions) {
                        registerRecipe(new SmithingTrimRecipe(
                                Integer.toUnsignedString(++RECIPE_COUNT, 36),
                                template.clone(),
                                input.clone(),
                                addition.clone(),
                                tag));
                    }
                }
            }
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported smithing trim recipe: {}", entry);
        }
    }

    protected List<Item> deserializeItemOrTag(JsonObject itemEntry) throws UnsupportedOperationException {
        if (!itemEntry.has("tag")) {
            return Collections.singletonList(deserializeItem(itemEntry));
        }

        String tag = itemEntry.get("tag").getAsString();
        List<Item> items = tags.get(tag);
        if (items == null) {
            log.warn("Unknown item tag: {}", tag);
            throw UNSUPPORTED_ITEM_EXCEPTION;
        }
        return items;
    }

    @Override
    protected Item deserializeItem(JsonObject itemEntry) throws UnsupportedOperationException {
        Item item = ItemUtil.deserializeItem(itemEntry);
        if (item == null) {
            throw UNSUPPORTED_ITEM_EXCEPTION;
        }
        return item;
    }

    private JsonArray loadRecipes(String fileName) {
        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(ByteStreams.toByteArray(SynapseAPI.class.getClassLoader().getResourceAsStream(DIRECTORY + fileName))))) {
            return new Gson().fromJson(reader, JsonArray.class);
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load " + fileName, e);
        }
    }
}

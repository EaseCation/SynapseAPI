package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.data.ItemIdMap;
import cn.nukkit.inventory.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.Items;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static cn.nukkit.GameVersion.*;
import static org.itxtech.synapseapi.SynapseSharedConstants.*;

@Log4j2
public class CraftingManagerNew extends CraftingManagerLegacy {
    private static final String DIRECTORY = "recipes119/";

    @Override
    protected void initialize() {
        log.debug("Loading advanced crafting manager...");

        RECIPE_COUNT = 1;

        loadRecipes("shapeless_crafting.json").forEach(element -> loadShapeless(element, ShapelessRecipe::new, RecipeType.SHAPELESS));
//        loadRecipes("shapeless_shulker_box.json").forEach(element -> loadShapeless(element, ShulkerBoxRecipe::new, RecipeType.SHULKER_BOX)); //TODO: nbt
        if (ENABLE_CHEMISTRY_FEATURE) {
            loadRecipes("shapeless_chemistry.json").forEach(element -> loadShapeless(element, ShapelessChemistryRecipe::new, RecipeType.SHAPELESS_CHEMISTRY));
        }

        loadRecipes("shaped_crafting.json").forEach(element -> loadShaped(element, ShapedRecipe::new));
        if (ENABLE_CHEMISTRY_FEATURE) {
            loadRecipes("shaped_chemistry.json").forEach(element -> loadShaped(element, ShapedChemistryRecipe::new));
        }

        loadRecipes("smelting.json").forEach(this::loadSmelting);

        loadRecipes("special_hardcoded.json").forEach(this::loadHardcoded);

        if (V1_19_60.isAvailable()) {
            loadRecipes("smithing.json").forEach(this::loadSmithing);
        }

        loadRecipes("potion_type.json").forEach(this::loadPotionType);

        loadRecipes("potion_container_change.json").forEach(this::loadPotionContainer);

        log.info("Loaded " + this.recipes.size() + " recipes.");
    }

    @Override
    protected void loadPotionContainer(JsonElement element) {
        JsonObject entry = element.getAsJsonObject();
        String inputName = entry.get("input_item_name").getAsString();
        String outputName = entry.get("output_item_name").getAsString();

        int inputId = ItemIdMap.getIdByName(inputName);
        int outputId = ItemIdMap.getIdByName(outputName);

        if (inputId == Integer.MAX_VALUE) {
            log.trace("Skip an unsupported potion container recipe (input item): {}", entry);
            return;
        }
        if (outputId == Integer.MAX_VALUE) {
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
                    String.valueOf(++RECIPE_COUNT),
                    deserializeItem(entry.getAsJsonObject("output")),
                    Items.air(), //deserializeItem(entry.getAsJsonObject("template")), //TODO: 1.20.0+
                    deserializeItem(entry.getAsJsonObject("input")),
                    deserializeItem(entry.getAsJsonObject("addition")),
                    tag));
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported smithing recipe: {}", entry);
        }
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

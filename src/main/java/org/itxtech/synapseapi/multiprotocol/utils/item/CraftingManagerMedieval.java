package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.block.Block;
import cn.nukkit.inventory.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.Items;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.UUID;

import static org.itxtech.synapseapi.SynapseSharedConstants.*;

@Log4j2
public class CraftingManagerMedieval extends CraftingManagerLegacy {
    @Override
    protected void initialize() {
        log.debug("Loading advanced (medieval) crafting manager...");

        JsonObject root;
        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(ByteStreams.toByteArray(SynapseAPI.class.getClassLoader().getResourceAsStream("recipes118.json"))))) {
            root = new Gson().fromJson(reader, JsonObject.class);
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load recipes.json", e);
        }

        RECIPE_COUNT = 1;

        root.getAsJsonArray("recipes").forEach(element -> {
            switch (element.getAsJsonObject().get("type").getAsInt()) {
                case 0:
                    loadShapeless(element, ShapelessRecipe::new, RecipeType.SHAPELESS);
                    break;
                case 5:
//                    loadShapeless(element, ShapelessUserDataRecipe::new, RecipeType.SHAPELESS_USER_DATA); //TODO: nbt
                    break;
                case 6:
                    if (!ENABLE_CHEMISTRY_FEATURE) {
                        break;
                    }
                    loadShapeless(element, ShapelessChemistryRecipe::new, RecipeType.SHAPELESS_CHEMISTRY);
                    break;
                case 1:
                    loadShaped(element, ShapedRecipe::new);
                    break;
                case 7:
                    if (!ENABLE_CHEMISTRY_FEATURE) {
                        break;
                    }
                    loadShaped(element, ShapedChemistryRecipe::new);
                    break;
                case 2:
                case 3:
                    loadSmelting(element);
                    break;
                case 4:
                    loadHardcoded(element);
                    break;
            }
        });

        root.getAsJsonArray("potionMixes").forEach(this::loadPotionType);

        root.getAsJsonArray("containerMixes").forEach(this::loadPotionContainer);

        if (SERVER_AUTHORITATIVE_INVENTORY) {
            root.getAsJsonArray("smithing").forEach(this::loadSmithing);
        }

        log.info("Loaded " + this.recipes.size() + " recipes.");
    }

    @Override
    protected void loadHardcoded(JsonElement element) {
        registerRecipe(new MultiRecipe(UUID.fromString(element.getAsJsonObject().get("uuid").getAsString())));
    }

    @Override
    protected void loadPotionType(JsonElement element) {
        JsonObject entry = element.getAsJsonObject();

        try {
            IntIntPair input = nameToItem(entry.get("inputId").getAsString());
            int inputMeta = input.rightInt();
            IntIntPair reagent = nameToItem(entry.get("reagentId").getAsString());
            int reagentMeta = reagent.rightInt();
            IntIntPair output = nameToItem(entry.get("outputId").getAsString());
            int outputMeta = output.rightInt();

            registerBrewingRecipe(new BrewingRecipe(
                    Item.get(input.leftInt(), inputMeta != Integer.MIN_VALUE ? inputMeta : entry.get("inputMeta").getAsInt()),
                    Item.get(reagent.leftInt(), reagentMeta != Integer.MIN_VALUE ? reagentMeta : entry.get("reagentMeta").getAsInt()),
                    Item.get(output.leftInt(), outputMeta != Integer.MIN_VALUE ? outputMeta : entry.get("outputMeta").getAsInt())));
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported potion type recipe: {}", entry);
        }
    }

    @Override
    protected void loadPotionContainer(JsonElement element) {
        JsonObject entry = element.getAsJsonObject();

        try {
            IntIntPair input = nameToItem(entry.get("inputId").getAsString());
            int inputMeta = input.rightInt();
            IntIntPair reagent = nameToItem(entry.get("reagentId").getAsString());
            int reagentMeta = reagent.rightInt();
            IntIntPair output = nameToItem(entry.get("outputId").getAsString());
            int outputMeta = output.rightInt();

            registerContainerRecipe(new ContainerRecipe(
                    Item.get(input.leftInt(), inputMeta != Integer.MIN_VALUE ? inputMeta : 0),
                    Item.get(reagent.leftInt(), reagentMeta != Integer.MIN_VALUE ? reagentMeta : 0),
                    Item.get(output.leftInt(), outputMeta != Integer.MIN_VALUE ? outputMeta : 0)));
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
                    deserializeLegacyItem(entry.getAsJsonObject("output")),
                    Items.air(), //deserializeItem(entry.getAsJsonObject("template")), //TODO: 1.20.0+
                    deserializeLegacyItem(entry.getAsJsonObject("input")),
                    deserializeLegacyItem(entry.getAsJsonObject("addition")),
                    tag));
        } catch (UnsupportedOperationException e) {
            log.trace("Skip an unsupported smithing recipe: {}", entry);
        }
    }

    @Override
    protected Item deserializeItem(JsonObject itemEntry) throws UnsupportedOperationException {
        IntIntPair item = nameToItem(itemEntry.get("name").getAsString());
        int id = item.leftInt();
        int meta = item.rightInt();
        if (itemEntry.has("blockNetId")) {
            int blockRuntimeId = itemEntry.get("blockNetId").getAsInt();
            int legacyId = AdvancedGlobalBlockPalette.getLegacyId(AbstractProtocol.PROTOCOL_118, false, blockRuntimeId);

            if (legacyId == -1) {
                log.debug("Invalid block runtime ID: " + blockRuntimeId);
                throw UNSUPPORTED_ITEM_EXCEPTION;
            }

            meta = legacyId & 0x3fff;

            assert id == Block.getItemId(legacyId >> 14);
        } else if (meta == Integer.MIN_VALUE) {
            if (itemEntry.has("meta")) {
                meta = itemEntry.get("meta").getAsInt();
            } else {
                meta = 0;
            }
        }
        Item result = Item.getCraftingItem(
                id,
                meta,
                itemEntry.has("count") ? itemEntry.get("count").getAsInt() : 1,
                itemEntry.has("nbt") ? Base64.getDecoder().decode(itemEntry.get("nbt").getAsString()) : new byte[0]);
        if (result == null) {
            log.debug("Unexpected unsupported item: {}", itemEntry);
            throw UNSUPPORTED_ITEM_EXCEPTION;
        }
        return result;
    }

    private Item deserializeLegacyItem(JsonObject itemEntry) throws UnsupportedOperationException {
        return super.deserializeItem(itemEntry);
    }

    /**
     * @return Pair.of(itemId, meta ? Integer.MIN_VALUE)
     */
    private static IntIntPair nameToItem(String name) throws UnsupportedOperationException {
        int id;
        int meta;

        ObjectIntPair<String> legacy = ItemUtil.FLATTENED_TO_LEGACY.get(name);
        if (legacy != null) {
            id = ItemUtil.ITEM_NAME_TO_ID.getOrDefault(legacy.left(), Integer.MIN_VALUE);
            meta = legacy.rightInt();
        } else {
            id = ItemUtil.ITEM_NAME_TO_ID.getOrDefault(name, Integer.MIN_VALUE);
            meta = Integer.MIN_VALUE;
        }

        if (id == Integer.MIN_VALUE) {
            throw UNSUPPORTED_ITEM_EXCEPTION;
        }

        return IntIntPair.of(id, meta);
    }
}

package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.block.Blocks;
import cn.nukkit.item.Item;
import cn.nukkit.item.Items;
import cn.nukkit.utils.JsonUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.itxtech.synapseapi.SynapseAPI;
import tools.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class TagManager {
    private static final String CURRENT_BLOCK_TAGS_FILE_NAME = "block_tags_12150.json";
    private static final String CURRENT_ITEM_TAGS_FILE_NAME = "item_tags_12150.json";

    public static void registerVanillaTags() {
        registerVanillaBlockTags();
        registerVanillaItemTags();
    }

    private static void registerVanillaBlockTags() {
        Map<String, List<String>> tagMap;
        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(CURRENT_BLOCK_TAGS_FILE_NAME)) {
            tagMap = JsonUtil.TRUSTED_JSON_MAPPER.readValue(stream, new TypeReference<>() {
            });
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load block_tags.json", e);
        }
        tagMap.forEach((tagName, blockNames) -> {
            IntSet blockIds = new IntOpenHashSet();
            for (String blockName : blockNames) {
                blockIds.add(Blocks.getIdByBlockName(blockName, true));
            }
            Blocks.registerBlockTag(tagName, blockIds.toArray(new int[0]));
        });
    }

    private static void registerVanillaItemTags() {
        Map<String, List<String>> tagMap;
        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(CURRENT_ITEM_TAGS_FILE_NAME)) {
            tagMap = JsonUtil.TRUSTED_JSON_MAPPER.readValue(stream, new TypeReference<>() {
            });
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load item_tags.json", e);
        }
        tagMap.forEach((tagName, itemNames) -> {
            IntList itemFullIds = new IntArrayList();
            for (String itemName : itemNames) {
                int itemFullId = Items.getFullIdByName(itemName, true, true);
                Item item = Item.fromFullId(itemFullId);
                if (!item.isStackedByData() || item.getDamage() == 0) {
                    itemFullId = Item.getFullId(item.getId(), 0xffff);
                }
                itemFullIds.add(itemFullId);
            }
            Items.registerItemTags(tagName, itemFullIds.toArray(new int[0]));
        });
    }
}

package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.GameVersion;
import cn.nukkit.block.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlockFullNames;
import cn.nukkit.item.ItemBlockID;
import cn.nukkit.item.Items;
import lombok.extern.log4j.Log4j2;

import java.util.*;

import static cn.nukkit.GameVersion.*;

@Log4j2
public final class TaggedItemFlattener {
    private static final Map<String, List<Item>> REGISTRY = new HashMap<>();

    static {
/*
        register(V1_19_70, ItemBlockFullNames.WOOL);

        register(V1_20_0, ItemBlockFullNames.CARPET);
*/
        register(V1_20_50, ItemBlockFullNames.PLANKS,
                item(ItemBlockID.PLANKS, BlockPlanks.OAK),
                item(ItemBlockID.PLANKS, BlockPlanks.SPRUCE),
                item(ItemBlockID.PLANKS, BlockPlanks.BIRCH),
                item(ItemBlockID.PLANKS, BlockPlanks.JUNGLE),
                item(ItemBlockID.PLANKS, BlockPlanks.ACACIA),
                item(ItemBlockID.PLANKS, BlockPlanks.DARK_OAK)
        );

        register(V1_20_70, ItemBlockFullNames.WOODEN_SLAB,
                item(ItemBlockID.WOODEN_SLAB, BlockSlabWood.OAK),
                item(ItemBlockID.WOODEN_SLAB, BlockSlabWood.SPRUCE),
                item(ItemBlockID.WOODEN_SLAB, BlockSlabWood.BIRCH),
                item(ItemBlockID.WOODEN_SLAB, BlockSlabWood.JUNGLE),
                item(ItemBlockID.WOODEN_SLAB, BlockSlabWood.ACACIA),
                item(ItemBlockID.WOODEN_SLAB, BlockSlabWood.DARK_OAK)
        );

        register(V1_20_70, ItemBlockFullNames.WOOD,
                item(ItemBlockID.WOOD, BlockWoodBark.OAK),
                item(ItemBlockID.WOOD, BlockWoodBark.SPRUCE),
                item(ItemBlockID.WOOD, BlockWoodBark.BIRCH),
                item(ItemBlockID.WOOD, BlockWoodBark.JUNGLE),
                item(ItemBlockID.WOOD, BlockWoodBark.ACACIA),
                item(ItemBlockID.WOOD, BlockWoodBark.DARK_OAK),
                item(ItemBlockID.WOOD, BlockWoodBark.STRIPPED_BIT | BlockWoodBark.OAK),
                item(ItemBlockID.WOOD, BlockWoodBark.STRIPPED_BIT | BlockWoodBark.SPRUCE),
                item(ItemBlockID.WOOD, BlockWoodBark.STRIPPED_BIT | BlockWoodBark.BIRCH),
                item(ItemBlockID.WOOD, BlockWoodBark.STRIPPED_BIT | BlockWoodBark.JUNGLE),
                item(ItemBlockID.WOOD, BlockWoodBark.STRIPPED_BIT | BlockWoodBark.ACACIA),
                item(ItemBlockID.WOOD, BlockWoodBark.STRIPPED_BIT | BlockWoodBark.DARK_OAK)
        );

        register(V1_21_20, ItemBlockFullNames.STONEBRICK,
                item(ItemBlockID.STONEBRICK, BlockBricksStone.NORMAL),
                item(ItemBlockID.STONEBRICK, BlockBricksStone.MOSSY),
                item(ItemBlockID.STONEBRICK, BlockBricksStone.CRACKED),
                item(ItemBlockID.STONEBRICK, BlockBricksStone.CHISELED),
                item(ItemBlockID.STONEBRICK, BlockBricksStone.SMOOTH)
        );

        register(V1_21_20, ItemBlockFullNames.SAND,
                item(ItemBlockID.SAND, BlockSand.DEFAULT),
                item(ItemBlockID.SAND, BlockSand.RED)
        );
    }

    private static void register(GameVersion version, String name, Item... items) {
        if (version.isAvailable()) {
            return;
        }
        REGISTRY.put(name, Arrays.asList(items));
    }

    private static Item item(int id) {
        return item(id, 0);
    }

    private static Item item(int id, int meta) {
        return Item.get(id, meta);
    }

    public static List<Item> get(String name) {
        List<Item> items = REGISTRY.get(name);
        if (items != null) {
            return items;
        }

        int fullId = Items.getFullIdByName(name, true, true);
        if (fullId == Integer.MIN_VALUE) {
            log.warn("Unknown tagged item: {}", name);
            return Collections.emptyList();
        }
        return Collections.singletonList(Item.get(Item.getIdFromFullId(fullId), Item.getMetaFromFullId(fullId)));
    }

    private TaggedItemFlattener() {
        throw new IllegalStateException();
    }
}

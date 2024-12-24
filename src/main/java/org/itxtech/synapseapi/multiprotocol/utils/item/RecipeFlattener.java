package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.block.BlockSlabStone4;
import cn.nukkit.inventory.*;
import cn.nukkit.inventory.recipe.RecipeIngredient;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlockID;
import cn.nukkit.item.ItemID;
import cn.nukkit.inventory.recipe.DefaultItemDescriptor;
import cn.nukkit.inventory.recipe.TagItemDescriptor;
import cn.nukkit.utils.DyeColor;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.util.Collection;
import java.util.Map;

import static cn.nukkit.GameVersion.*;

public final class RecipeFlattener {
    public static void addFlattenedRecipes(AbstractProtocol protocol, Collection<Recipe> recipes) {
/*
        if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_119_70.getProtocolStart()) {
            return;
        }
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.PAINTING),
                new String[]{
                        "AAA",
                        "ABA",
                        "AAA"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemID.STICK),
                        'B', cerateRecipeIngredient("minecraft:wool")
                )));

        if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_119_80.getProtocolStart()) {
            return;
        }
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.SMOKER),
                new String[]{
                        " A ",
                        "ABA",
                        " A "
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:logs"),
                        'B', cerateRecipeIngredient(ItemBlockID.FURNACE)
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.CAMPFIRE),
                new String[]{
                        " A ",
                        "ABA",
                        "CCC"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemID.STICK),
                        'B', cerateRecipeIngredient("minecraft:coals"),
                        'C', cerateRecipeIngredient("minecraft:logs")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.SOUL_CAMPFIRE),
                new String[]{
                        " A ",
                        "ABA",
                        "CCC"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemID.STICK),
                        'B', cerateRecipeIngredient("minecraft:soul_fire_base_blocks"),
                        'C', cerateRecipeIngredient("minecraft:logs")
                )));
*/
        if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_120_50.getProtocolStart()) {
            return;
        }
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.STICK),
                new String[]{
                        "A",
                        "A"
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BOWL),
                new String[]{
                        "A A",
                        " A "
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.CRAFTING_TABLE),
                new String[]{
                        "AA",
                        "AA"
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.CHEST),
                new String[]{
                        "AAA",
                        "A A",
                        "AAA"
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.WOODEN_SWORD),
                new String[]{
                        "A",
                        "A",
                        "B"
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:planks"),
                        'B', cerateRecipeIngredient(ItemID.STICK)
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.WOODEN_SHOVEL),
                new String[]{
                        "A",
                        "B",
                        "B"
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:planks"),
                        'B', cerateRecipeIngredient(ItemID.STICK)
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.WOODEN_PICKAXE),
                new String[]{
                        "AAA",
                        " B ",
                        " B "
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:planks"),
                        'B', cerateRecipeIngredient(ItemID.STICK)
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.WOODEN_AXE),
                new String[]{
                        "AA",
                        "AB",
                        " B"
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:planks"),
                        'B', cerateRecipeIngredient(ItemID.STICK)
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.WOODEN_HOE),
                new String[]{
                        "AA ",
                        " B ",
                        " B "
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:planks"),
                        'B', cerateRecipeIngredient(ItemID.STICK)
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.NOTEBLOCK),
                new String[]{
                        "AAA",
                        "ABA",
                        "AAA"
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:planks"),
                        'B', cerateRecipeIngredient(ItemID.REDSTONE)
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.JUKEBOX),
                new String[]{
                        "AAA",
                        "ABA",
                        "AAA"
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:planks"),
                        'B', cerateRecipeIngredient(ItemID.DIAMOND)
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.BOOKSHELF),
                new String[]{
                        "AAA",
                        "BBB",
                        "AAA"
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:planks"),
                        'B', cerateRecipeIngredient(ItemID.BOOK)
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.TRIPWIRE_HOOK),
                new String[]{
                        "A",
                        "B",
                        "C"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemID.IRON_INGOT),
                        'B', cerateRecipeIngredient(ItemID.STICK),
                        'C', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.PISTON),
                new String[]{
                        "AAA",
                        "BCB",
                        "BDB"
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:planks"),
                        'B', cerateRecipeIngredient(ItemBlockID.COBBLESTONE),
                        'C', cerateRecipeIngredient(ItemID.IRON_INGOT),
                        'D', cerateRecipeIngredient(ItemID.REDSTONE)
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.LOOM),
                new String[]{
                        "AA",
                        "BB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemID.STRING),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.SMITHING_TABLE),
                new String[]{
                        "AA",
                        "BB",
                        "BB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemID.IRON_INGOT),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.CARTOGRAPHY_TABLE),
                new String[]{
                        "AA",
                        "BB",
                        "BB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemID.PAPER),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.FLETCHING_TABLE),
                new String[]{
                        "AA",
                        "BB",
                        "BB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemID.FLINT),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.GRINDSTONE),
                new String[]{
                        "ABA",
                        "C C"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemID.STICK),
                        'B', cerateRecipeIngredient(ItemBlockID.STONE_SLAB4, BlockSlabStone4.TYPE_STONE),
                        'C', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.SHIELD),
                new String[]{
                        "ABA",
                        "AAA",
                        " A "
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:planks"),
                        'B', cerateRecipeIngredient(ItemID.IRON_INGOT)
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.BEEHIVE),
                new String[]{
                        "AAA",
                        "BBB",
                        "AAA"
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:planks"),
                        'B', cerateRecipeIngredient(ItemID.HONEYCOMB)
                )));
        if (V1_20_0.isAvailable()) {
            recipes.add(TodoCraftingRecipe.shaped(
                    Item.get(ItemBlockID.CHISELED_BOOKSHELF),
                    new String[]{
                            "AAA",
                            "BBB",
                            "AAA"
                    },
                    Map.of(
                            'A', cerateRecipeIngredient("minecraft:planks"),
                            'B', cerateRecipeIngredient("minecraft:wooden_slabs")
                    )));
        }
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.WHITE.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.WHITE_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.ORANGE.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.ORANGE_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.MAGENTA.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.MAGENTA_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.LIGHT_BLUE.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.LIGHT_BLUE_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.YELLOW.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.YELLOW_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.LIME.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.LIME_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.PINK.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.PINK_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.GRAY.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.GRAY_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.LIGHT_GRAY.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.LIGHT_GRAY_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.CYAN.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.CYAN_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.PURPLE.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.PURPLE_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.BLUE.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.BLUE_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.BROWN.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.BROWN_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.GREEN.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.GREEN_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.RED.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.RED_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemID.BED, DyeColor.BLACK.getWoolData()),
                new String[]{
                        "AAA",
                        "BBB"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.BLACK_WOOL),
                        'B', cerateRecipeIngredient("minecraft:planks")
                )));

        if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_120_70.getProtocolStart()) {
            return;
        }
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.COMPOSTER),
                new String[]{
                        "A A",
                        "A A",
                        "AAA"
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:wooden_slabs")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.BARREL),
                new String[]{
                        "ABA",
                        "A A",
                        "ABA"
                },
                Map.of(
                        'A', V1_20_10.isAvailable() ? cerateRecipeIngredient("minecraft:planks") : cerateRecipeIngredient(ItemID.STICK), //TODO parity 1.20.10
                        'B', cerateRecipeIngredient("minecraft:wooden_slabs")
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.LECTERN),
                new String[]{
                        "AAA",
                        " B ",
                        " A "
                },
                Map.of(
                        'A', cerateRecipeIngredient("minecraft:wooden_slabs"),
                        'B', cerateRecipeIngredient(ItemBlockID.BOOKSHELF)
                )));
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.DAYLIGHT_DETECTOR),
                new String[]{
                        "AAA",
                        "BBB",
                        "CCC"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemBlockID.GLASS),
                        'B', cerateRecipeIngredient(ItemID.QUARTZ),
                        'C', cerateRecipeIngredient("minecraft:wooden_slabs")
                )));

        if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
            return;
        }
        recipes.add(TodoCraftingRecipe.shaped(
                Item.get(ItemBlockID.TNT),
                new String[]{
                        "ABA",
                        "BAB",
                        "ABA"
                },
                Map.of(
                        'A', cerateRecipeIngredient(ItemID.GUNPOWDER),
                        'B', cerateRecipeIngredient("minecraft:sand")
                )));
    }

    private static RecipeIngredient cerateRecipeIngredient(int itemId) {
        return cerateRecipeIngredient(itemId, -1);
    }

    private static RecipeIngredient cerateRecipeIngredient(int itemId, int itemMeta) {
        return new RecipeIngredient(new DefaultItemDescriptor(Item.getCraftingItem(itemId, itemMeta, 1, new byte[0])), 1);
    }

    private static RecipeIngredient cerateRecipeIngredient(String itemTag) {
        return new RecipeIngredient(new TagItemDescriptor(itemTag), 1);
    }

    private RecipeFlattener() {
        throw new IllegalStateException();
    }
}

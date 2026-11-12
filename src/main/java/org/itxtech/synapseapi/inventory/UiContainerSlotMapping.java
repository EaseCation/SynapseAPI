package org.itxtech.synapseapi.inventory;

import cn.nukkit.network.protocol.types.UiContainerSlots;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.Map;

public interface UiContainerSlotMapping {
    Int2IntMap CURSOR = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.CURSOR, 0
    )));
    Int2IntMap ANVIL = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.ANVIL_INPUT, 0,
            UiContainerSlots.ANVIL_INGREDIENT, 1
    )));
    Int2IntMap STONE_CUTTER_INPUT = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.STONECUTTER_INPUT, 0
    )));
    Int2IntMap TRADE2_INGREDIENT = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.TRADE2_INPUT_A, 0,
            UiContainerSlots.TRADE2_INPUT_B, 1
    )));
    Int2IntMap TRADE_INGREDIENT = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.TRADE_INPUT_A, 0,
            UiContainerSlots.TRADE_INPUT_B, 1
    )));
    Int2IntMap MATERIAL_REDUCER_INPUT = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.MATERIAL_REDUCER_INPUT, 0
    )));
    Int2IntMap LOOM = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.LOOM_BANNER, 0,
            UiContainerSlots.LOOM_DYE, 1,
            UiContainerSlots.LOOM_PATTERN, 2
    )));
    Int2IntMap CARTOGRAPHY_TABLE = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.CARTOGRAPHY_TABLE_INPUT, 0,
            UiContainerSlots.CARTOGRAPHY_TABLE_INGREDIENT, 1
    )));
    Int2IntMap ENCHANTING_TABLE = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.ENCHANTING_TABLE_INPUT, 0,
            UiContainerSlots.ENCHANTING_TABLE_LAPIS_LAZULI, 1
    )));
    Int2IntMap GRINDSTONE = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.GRINDSTONE_INPUT, 0,
            UiContainerSlots.GRINDSTONE_INGREDIENT, 1
    )));
    Int2IntMap COMPOUND_CREATOR_INPUT = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.COMPOUND_CREATOR_INPUT_0, 0,
            UiContainerSlots.COMPOUND_CREATOR_INPUT_1, 1,
            UiContainerSlots.COMPOUND_CREATOR_INPUT_2, 2,
            UiContainerSlots.COMPOUND_CREATOR_INPUT_3, 3,
            UiContainerSlots.COMPOUND_CREATOR_INPUT_4, 4,
            UiContainerSlots.COMPOUND_CREATOR_INPUT_5, 5,
            UiContainerSlots.COMPOUND_CREATOR_INPUT_6, 6,
            UiContainerSlots.COMPOUND_CREATOR_INPUT_7, 7,
            UiContainerSlots.COMPOUND_CREATOR_INPUT_8, 8
    )));
    Int2IntMap BEACON_PAYMENT = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.BEACON_PAYMENT, 0
    )));
    Int2IntMap CRAFTING2X2_INPUT = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.CRAFTING_INVENTORY_INPUT_0, 0,
            UiContainerSlots.CRAFTING_INVENTORY_INPUT_1, 1,
            UiContainerSlots.CRAFTING_INVENTORY_INPUT_2, 2,
            UiContainerSlots.CRAFTING_INVENTORY_INPUT_3, 3
    )));
    Int2IntMap CRAFTING3X3_INPUT = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.CRAFTING_TABLE_INPUT_0, 0,
            UiContainerSlots.CRAFTING_TABLE_INPUT_1, 1,
            UiContainerSlots.CRAFTING_TABLE_INPUT_2, 2,
            UiContainerSlots.CRAFTING_TABLE_INPUT_3, 3,
            UiContainerSlots.CRAFTING_TABLE_INPUT_4, 4,
            UiContainerSlots.CRAFTING_TABLE_INPUT_5, 5,
            UiContainerSlots.CRAFTING_TABLE_INPUT_6, 6,
            UiContainerSlots.CRAFTING_TABLE_INPUT_7, 7,
            UiContainerSlots.CRAFTING_TABLE_INPUT_8, 8
    )));
    Int2IntMap MATERIAL_REDUCER_OUTPUT = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.MATERIAL_REDUCER_OUTPUT_0, 0,
            UiContainerSlots.MATERIAL_REDUCER_OUTPUT_1, 1,
            UiContainerSlots.MATERIAL_REDUCER_OUTPUT_2, 2,
            UiContainerSlots.MATERIAL_REDUCER_OUTPUT_3, 3,
            UiContainerSlots.MATERIAL_REDUCER_OUTPUT_4, 4,
            UiContainerSlots.MATERIAL_REDUCER_OUTPUT_5, 5,
            UiContainerSlots.MATERIAL_REDUCER_OUTPUT_6, 6,
            UiContainerSlots.MATERIAL_REDUCER_OUTPUT_7, 7,
            UiContainerSlots.MATERIAL_REDUCER_OUTPUT_8, 8
    )));
    Int2IntMap CREATED_ITEM_OUTPUT = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.CREATED_ITEM_OUTPUT, 0
    )));
    Int2IntMap SMITHING_TABLE = map(new Int2IntOpenHashMap(Map.of(
            UiContainerSlots.SMITHING_TABLE_INPUT, 0,
            UiContainerSlots.SMITHING_TABLE_INGREDIENT, 1,
            UiContainerSlots.SMITHING_TABLE_TEMPLATE, 2
    )));

    static Int2IntMap map(Int2IntMap map) {
        map.defaultReturnValue(-1);
        return map;
    }
}

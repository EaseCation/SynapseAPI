package org.itxtech.synapseapi.inventory;

import cn.nukkit.network.protocol.types.ContainerIds;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.inventory.ContainerSlotType;

import java.util.Arrays;

/**
 * @see ContainerSlotType
 */
public class ItemStackContainerIdTranslator {
    private static final int[] V116 = new int[256];
    public static final int V116_ANVIL_INPUT = 0;
    public static final int V116_ANVIL_MATERIAL = 1;
    public static final int V116_ANVIL_RESULT_PREVIEW = 2;
    public static final int V116_SMITHING_TABLE_INPUT = 3;
    public static final int V116_SMITHING_TABLE_MATERIAL = 4;
    public static final int V116_SMITHING_TABLE_RESULT_PREVIEW = 5;
    public static final int V116_ARMOR = 6;
    public static final int V116_LEVEL_ENTITY = 7;
    public static final int V116_BEACON_PAYMENT = 8;
    public static final int V116_BREWING_STAND_INPUT = 9;
    public static final int V116_BREWING_STAND_RESULT = 10;
    public static final int V116_BREWING_STAND_FUEL = 11;
    public static final int V116_COMBINED_HOTBAR_AND_INVENTORY = 12;
    public static final int V116_CRAFTING_INPUT = 13;
    public static final int V116_CRAFTING_OUTPUT_PREVIEW = 14;
    public static final int V116_RECIPE_CONSTRUCTION = 15;
    public static final int V116_RECIPE_NATURE = 16;
    public static final int V116_RECIPE_ITEMS = 17;
    public static final int V116_RECIPE_SEARCH = 18;
    public static final int V116_RECIPE_SEARCH_BAR = 19;
    public static final int V116_RECIPE_EQUIPMENT = 20;
    public static final int V116_ENCHANTING_INPUT = 21;
    public static final int V116_ENCHANTING_MATERIAL = 22;
    public static final int V116_FURNACE_FUEL = 23;
    public static final int V116_FURNACE_INGREDIENT = 24;
    public static final int V116_FURNACE_RESULT = 25;
    public static final int V116_HORSE_EQUIP = 26;
    public static final int V116_HOTBAR = 27;
    public static final int V116_INVENTORY = 28;
    public static final int V116_SHULKER_BOX = 29;
    public static final int V116_TRADE_INGREDIENT1 = 30;
    public static final int V116_TRADE_INGREDIENT2 = 31;
    public static final int V116_TRADE_RESULT_PREVIEW = 32;
    public static final int V116_OFFHAND = 33;
    public static final int V116_COMPOUND_CREATOR_INPUT = 34;
    public static final int V116_COMPOUND_CREATOR_OUTPUT_PREVIEW = 35;
    public static final int V116_ELEMENT_CONSTRUCTOR_OUTPUT_PREVIEW = 36;
    public static final int V116_MATERIAL_REDUCER_INPUT = 37;
    public static final int V116_MATERIAL_REDUCER_OUTPUT = 38;
    public static final int V116_LAB_TABLE_INPUT = 39;
    public static final int V116_LOOM_INPUT = 40;
    public static final int V116_LOOM_DYE = 41;
    public static final int V116_LOOM_MATERIAL = 42;
    public static final int V116_LOOM_RESULT_PREVIEW = 43;
    public static final int V116_BLAST_FURNACE_INGREDIENT = 44;
    public static final int V116_SMOKER_INGREDIENT = 45;
    public static final int V116_TRADE2_INGREDIENT1 = 46;
    public static final int V116_TRADE2_INGREDIENT2 = 47;
    public static final int V116_TRADE2_RESULT_PREVIEW = 48;
    public static final int V116_GRINDSTONE_INPUT = 49;
    public static final int V116_GRINDSTONE_ADDITIONAL = 50;
    public static final int V116_GRINDSTONE_RESULT_PREVIEW = 51;
    public static final int V116_STONECUTTER_INPUT = 52;
    public static final int V116_STONECUTTER_RESULT_PREVIEW = 53;
    public static final int V116_CARTOGRAPHY_INPUT = 54;
    public static final int V116_CARTOGRAPHY_ADDITIONAL = 55;
    public static final int V116_CARTOGRAPHY_RESULT_PREVIEW = 56;
    public static final int V116_BARREL = 57;
    public static final int V116_CURSOR = 58;
    public static final int V116_CREATED_OUTPUT = 59;

    private static final int[] V11950 = new int[256];
    public static final int V11950_ANVIL_INPUT = 0;
    public static final int V11950_ANVIL_MATERIAL = 1;
    public static final int V11950_ANVIL_RESULT_PREVIEW = 2;
    public static final int V11950_SMITHING_TABLE_INPUT = 3;
    public static final int V11950_SMITHING_TABLE_MATERIAL = 4;
    public static final int V11950_SMITHING_TABLE_RESULT_PREVIEW = 5;
    public static final int V11950_ARMOR = 6;
    public static final int V11950_LEVEL_ENTITY = 7;
    public static final int V11950_BEACON_PAYMENT = 8;
    public static final int V11950_BREWING_STAND_INPUT = 9;
    public static final int V11950_BREWING_STAND_RESULT = 10;
    public static final int V11950_BREWING_STAND_FUEL = 11;
    public static final int V11950_COMBINED_HOTBAR_AND_INVENTORY = 12;
    public static final int V11950_CRAFTING_INPUT = 13;
    public static final int V11950_CRAFTING_OUTPUT_PREVIEW = 14;
    public static final int V11950_RECIPE_CONSTRUCTION = 15;
    public static final int V11950_RECIPE_NATURE = 16;
    public static final int V11950_RECIPE_ITEMS = 17;
    public static final int V11950_RECIPE_SEARCH = 18;
    public static final int V11950_RECIPE_SEARCH_BAR = 19;
    public static final int V11950_RECIPE_EQUIPMENT = 20;
    public static final int V11950_RECIPE_BOOK = 21;
    public static final int V11950_ENCHANTING_INPUT = 22;
    public static final int V11950_ENCHANTING_MATERIAL = 23;
    public static final int V11950_FURNACE_FUEL = 24;
    public static final int V11950_FURNACE_INGREDIENT = 25;
    public static final int V11950_FURNACE_RESULT = 26;
    public static final int V11950_HORSE_EQUIP = 27;
    public static final int V11950_HOTBAR = 28;
    public static final int V11950_INVENTORY = 29;
    public static final int V11950_SHULKER_BOX = 30;
    public static final int V11950_TRADE_INGREDIENT1 = 31;
    public static final int V11950_TRADE_INGREDIENT2 = 32;
    public static final int V11950_TRADE_RESULT_PREVIEW = 33;
    public static final int V11950_OFFHAND = 34;
    public static final int V11950_COMPOUND_CREATOR_INPUT = 35;
    public static final int V11950_COMPOUND_CREATOR_OUTPUT_PREVIEW = 36;
    public static final int V11950_ELEMENT_CONSTRUCTOR_OUTPUT_PREVIEW = 37;
    public static final int V11950_MATERIAL_REDUCER_INPUT = 38;
    public static final int V11950_MATERIAL_REDUCER_OUTPUT = 39;
    public static final int V11950_LAB_TABLE_INPUT = 40;
    public static final int V11950_LOOM_INPUT = 41;
    public static final int V11950_LOOM_DYE = 42;
    public static final int V11950_LOOM_MATERIAL = 43;
    public static final int V11950_LOOM_RESULT_PREVIEW = 44;
    public static final int V11950_BLAST_FURNACE_INGREDIENT = 45;
    public static final int V11950_SMOKER_INGREDIENT = 46;
    public static final int V11950_TRADE2_INGREDIENT1 = 47;
    public static final int V11950_TRADE2_INGREDIENT2 = 48;
    public static final int V11950_TRADE2_RESULT_PREVIEW = 49;
    public static final int V11950_GRINDSTONE_INPUT = 50;
    public static final int V11950_GRINDSTONE_ADDITIONAL = 51;
    public static final int V11950_GRINDSTONE_RESULT_PREVIEW = 52;
    public static final int V11950_STONECUTTER_INPUT = 53;
    public static final int V11950_STONECUTTER_RESULT_PREVIEW = 54;
    public static final int V11950_CARTOGRAPHY_INPUT = 55;
    public static final int V11950_CARTOGRAPHY_ADDITIONAL = 56;
    public static final int V11950_CARTOGRAPHY_RESULT_PREVIEW = 57;
    public static final int V11950_BARREL = 58;
    public static final int V11950_CURSOR = 59;
    public static final int V11950_CREATED_OUTPUT = 60;
    public static final int V11980_SMITHING_TABLE_TEMPLATE = 61;
    public static final int V12050_CRAFTER = 62;
    public static final int V12120_DYNAMIC = 63;

    private static final int[] N11980 = new int[256];
    public static final int N11980_ANVIL_INPUT = 0;
    public static final int N11980_ANVIL_MATERIAL = 1;
    public static final int N11980_ANVIL_RESULT_PREVIEW = 2;
    public static final int N11980_SMITHING_TABLE_INPUT = 3;
    public static final int N11980_SMITHING_TABLE_MATERIAL = 4;
    public static final int N11980_SMITHING_TABLE_RESULT_PREVIEW = 5;
    public static final int N11980_ARMOR = 6;
    public static final int N11980_LEVEL_ENTITY = 7;
    public static final int N11980_BEACON_PAYMENT = 8;
    public static final int N11980_BREWING_STAND_INPUT = 9;
    public static final int N11980_BREWING_STAND_RESULT = 10;
    public static final int N11980_BREWING_STAND_FUEL = 11;
    public static final int N11980_COMBINED_HOTBAR_AND_INVENTORY = 12;
    public static final int N11980_CRAFTING_INPUT = 13;
    public static final int N11980_CRAFTING_OUTPUT_PREVIEW = 14;
    public static final int N11980_RECIPE_CONSTRUCTION = 15;
    public static final int N11980_RECIPE_NATURE = 16;
    public static final int N11980_RECIPE_CUSTOM = 17; //NETEASE
    public static final int N11980_RECIPE_ITEMS = 18;
    public static final int N11980_RECIPE_SEARCH = 19;
    public static final int N11980_RECIPE_SEARCH_BAR = 20;
    public static final int N11980_RECIPE_EQUIPMENT = 21;
    public static final int N11980_RECIPE_BOOK = 22;
    public static final int N11980_ENCHANTING_INPUT = 23;
    public static final int N11980_ENCHANTING_MATERIAL = 24;
    public static final int N11980_FURNACE_FUEL = 25;
    public static final int N11980_FURNACE_INGREDIENT = 26;
    public static final int N11980_FURNACE_RESULT = 27;
    public static final int N11980_HORSE_EQUIP = 28;
    public static final int N11980_HOTBAR = 29;
    public static final int N11980_INVENTORY = 30; //NETEASE: storage only
    public static final int N11980_SHULKER_BOX = 31;
    public static final int N11980_TRADE_INGREDIENT1 = 32;
    public static final int N11980_TRADE_INGREDIENT2 = 33;
    public static final int N11980_TRADE_RESULT_PREVIEW = 34;
    public static final int N11980_OFFHAND = 35;
    public static final int N11980_COMPOUND_CREATOR_INPUT = 36;
    public static final int N11980_COMPOUND_CREATOR_OUTPUT_PREVIEW = 37;
    public static final int N11980_ELEMENT_CONSTRUCTOR_OUTPUT_PREVIEW = 38;
    public static final int N11980_MATERIAL_REDUCER_INPUT = 39;
    public static final int N11980_MATERIAL_REDUCER_OUTPUT = 40;
    public static final int N11980_LAB_TABLE_INPUT = 41;
    public static final int N11980_LOOM_INPUT = 42;
    public static final int N11980_LOOM_DYE = 43;
    public static final int N11980_LOOM_MATERIAL = 44;
    public static final int N11980_LOOM_RESULT_PREVIEW = 45;
    public static final int N11980_BLAST_FURNACE_INGREDIENT = 46;
    public static final int N11980_SMOKER_INGREDIENT = 47;
    public static final int N11980_TRADE2_INGREDIENT1 = 48;
    public static final int N11980_TRADE2_INGREDIENT2 = 49;
    public static final int N11980_TRADE2_RESULT_PREVIEW = 50;
    public static final int N11980_GRINDSTONE_INPUT = 51;
    public static final int N11980_GRINDSTONE_ADDITIONAL = 52;
    public static final int N11980_GRINDSTONE_RESULT_PREVIEW = 53;
    public static final int N11980_STONECUTTER_INPUT = 54;
    public static final int N11980_STONECUTTER_RESULT_PREVIEW = 55;
    public static final int N11980_CARTOGRAPHY_INPUT = 56;
    public static final int N11980_CARTOGRAPHY_ADDITIONAL = 57;
    public static final int N11980_CARTOGRAPHY_RESULT_PREVIEW = 58;
    public static final int N11980_BARREL = 59;
    public static final int N11980_CURSOR = 60;
    public static final int N11980_CREATED_OUTPUT = 61;
    public static final int N11980_SMITHING_TABLE_TEMPLATE = 62;
    public static final int N12050_CRAFTER = 63;
    public static final int N12120_DYNAMIC = 64;

    static {
        Arrays.fill(V116, ContainerIds.NONE);

        V116[V116_ARMOR] = ContainerIds.ARMOR;

        V116[V116_COMBINED_HOTBAR_AND_INVENTORY] = ContainerIds.INVENTORY;
        V116[V116_HOTBAR] = ContainerIds.INVENTORY;
        V116[V116_INVENTORY] = ContainerIds.INVENTORY;

        V116[V116_OFFHAND] = ContainerIds.OFFHAND;

        V116[V116_ANVIL_INPUT] = ContainerIds.UI;
        V116[V116_ANVIL_MATERIAL] = ContainerIds.UI;
        V116[V116_BEACON_PAYMENT] = ContainerIds.UI;
        V116[V116_CARTOGRAPHY_ADDITIONAL] = ContainerIds.UI;
        V116[V116_CARTOGRAPHY_INPUT] = ContainerIds.UI;
        V116[V116_COMPOUND_CREATOR_INPUT] = ContainerIds.UI;
        V116[V116_CRAFTING_INPUT] = ContainerIds.UI;
        V116[V116_CREATED_OUTPUT] = ContainerIds.UI;
        V116[V116_CURSOR] = ContainerIds.UI;
        V116[V116_ENCHANTING_INPUT] = ContainerIds.UI;
        V116[V116_ENCHANTING_MATERIAL] = ContainerIds.UI;
        V116[V116_GRINDSTONE_ADDITIONAL] = ContainerIds.UI;
        V116[V116_GRINDSTONE_INPUT] = ContainerIds.UI;
        V116[V116_LAB_TABLE_INPUT] = ContainerIds.UI;
        V116[V116_LOOM_DYE] = ContainerIds.UI;
        V116[V116_LOOM_INPUT] = ContainerIds.UI;
        V116[V116_LOOM_MATERIAL] = ContainerIds.UI;
        V116[V116_MATERIAL_REDUCER_INPUT] = ContainerIds.UI;
        V116[V116_MATERIAL_REDUCER_OUTPUT] = ContainerIds.UI;
        V116[V116_SMITHING_TABLE_INPUT] = ContainerIds.UI;
        V116[V116_SMITHING_TABLE_MATERIAL] = ContainerIds.UI;
        V116[V116_STONECUTTER_INPUT] = ContainerIds.UI;
        V116[V116_TRADE2_INGREDIENT1] = ContainerIds.UI;
        V116[V116_TRADE2_INGREDIENT2] = ContainerIds.UI;
        V116[V116_TRADE_INGREDIENT1] = ContainerIds.UI;
        V116[V116_TRADE_INGREDIENT2] = ContainerIds.UI;

        V116[V116_BARREL] = Integer.MIN_VALUE;
        V116[V116_BLAST_FURNACE_INGREDIENT] = Integer.MIN_VALUE;
        V116[V116_BREWING_STAND_FUEL] = Integer.MIN_VALUE;
        V116[V116_BREWING_STAND_INPUT] = Integer.MIN_VALUE;
        V116[V116_BREWING_STAND_RESULT] = Integer.MIN_VALUE;
        V116[V116_FURNACE_FUEL] = Integer.MIN_VALUE;
        V116[V116_FURNACE_INGREDIENT] = Integer.MIN_VALUE;
        V116[V116_FURNACE_RESULT] = Integer.MIN_VALUE;
        V116[V116_HORSE_EQUIP] = Integer.MIN_VALUE;
        V116[V116_LEVEL_ENTITY] = Integer.MIN_VALUE; //chest
        V116[V116_SHULKER_BOX] = Integer.MIN_VALUE;
        V116[V116_SMOKER_INGREDIENT] = Integer.MIN_VALUE;

        // all preview slots are ignored, since the client shouldn't be modifying those directly

        Arrays.fill(V11950, ContainerIds.NONE);

        V11950[V11950_ARMOR] = ContainerIds.ARMOR;

        V11950[V11950_COMBINED_HOTBAR_AND_INVENTORY] = ContainerIds.INVENTORY;
        V11950[V11950_HOTBAR] = ContainerIds.INVENTORY;
        V11950[V11950_INVENTORY] = ContainerIds.INVENTORY;

        V11950[V11950_OFFHAND] = ContainerIds.OFFHAND;

        V11950[V11950_ANVIL_INPUT] = ContainerIds.UI;
        V11950[V11950_ANVIL_MATERIAL] = ContainerIds.UI;
        V11950[V11950_BEACON_PAYMENT] = ContainerIds.UI;
        V11950[V11950_CARTOGRAPHY_ADDITIONAL] = ContainerIds.UI;
        V11950[V11950_CARTOGRAPHY_INPUT] = ContainerIds.UI;
        V11950[V11950_COMPOUND_CREATOR_INPUT] = ContainerIds.UI;
        V11950[V11950_CRAFTING_INPUT] = ContainerIds.UI;
        V11950[V11950_CREATED_OUTPUT] = ContainerIds.UI;
        V11950[V11950_CURSOR] = ContainerIds.UI;
        V11950[V11950_ENCHANTING_INPUT] = ContainerIds.UI;
        V11950[V11950_ENCHANTING_MATERIAL] = ContainerIds.UI;
        V11950[V11950_GRINDSTONE_ADDITIONAL] = ContainerIds.UI;
        V11950[V11950_GRINDSTONE_INPUT] = ContainerIds.UI;
        V11950[V11950_LAB_TABLE_INPUT] = ContainerIds.UI;
        V11950[V11950_LOOM_DYE] = ContainerIds.UI;
        V11950[V11950_LOOM_INPUT] = ContainerIds.UI;
        V11950[V11950_LOOM_MATERIAL] = ContainerIds.UI;
        V11950[V11950_MATERIAL_REDUCER_INPUT] = ContainerIds.UI;
        V11950[V11950_MATERIAL_REDUCER_OUTPUT] = ContainerIds.UI;
        V11950[V11950_SMITHING_TABLE_INPUT] = ContainerIds.UI;
        V11950[V11950_SMITHING_TABLE_MATERIAL] = ContainerIds.UI;
        V11950[V11980_SMITHING_TABLE_TEMPLATE] = ContainerIds.UI;
        V11950[V11950_STONECUTTER_INPUT] = ContainerIds.UI;
        V11950[V11950_TRADE2_INGREDIENT1] = ContainerIds.UI;
        V11950[V11950_TRADE2_INGREDIENT2] = ContainerIds.UI;
        V11950[V11950_TRADE_INGREDIENT1] = ContainerIds.UI;
        V11950[V11950_TRADE_INGREDIENT2] = ContainerIds.UI;

        V11950[V12120_DYNAMIC] = ContainerIds.REGISTRY;

        V11950[V11950_BARREL] = Integer.MIN_VALUE;
        V11950[V11950_BLAST_FURNACE_INGREDIENT] = Integer.MIN_VALUE;
        V11950[V11950_BREWING_STAND_FUEL] = Integer.MIN_VALUE;
        V11950[V11950_BREWING_STAND_INPUT] = Integer.MIN_VALUE;
        V11950[V11950_BREWING_STAND_RESULT] = Integer.MIN_VALUE;
        V11950[V12050_CRAFTER] = Integer.MIN_VALUE;
        V11950[V11950_FURNACE_FUEL] = Integer.MIN_VALUE;
        V11950[V11950_FURNACE_INGREDIENT] = Integer.MIN_VALUE;
        V11950[V11950_FURNACE_RESULT] = Integer.MIN_VALUE;
        V11950[V11950_HORSE_EQUIP] = Integer.MIN_VALUE;
        V11950[V11950_LEVEL_ENTITY] = Integer.MIN_VALUE;
        V11950[V11950_SHULKER_BOX] = Integer.MIN_VALUE;
        V11950[V11950_SMOKER_INGREDIENT] = Integer.MIN_VALUE;

        Arrays.fill(N11980, ContainerIds.NONE);

        N11980[N11980_ARMOR] = ContainerIds.ARMOR;

        N11980[N11980_COMBINED_HOTBAR_AND_INVENTORY] = ContainerIds.INVENTORY;
        N11980[N11980_HOTBAR] = ContainerIds.INVENTORY;
        N11980[N11980_INVENTORY] = ContainerIds.INVENTORY;

        N11980[N11980_OFFHAND] = ContainerIds.OFFHAND;

        N11980[N11980_ANVIL_INPUT] = ContainerIds.UI;
        N11980[N11980_ANVIL_MATERIAL] = ContainerIds.UI;
        N11980[N11980_BEACON_PAYMENT] = ContainerIds.UI;
        N11980[N11980_CARTOGRAPHY_ADDITIONAL] = ContainerIds.UI;
        N11980[N11980_CARTOGRAPHY_INPUT] = ContainerIds.UI;
        N11980[N11980_COMPOUND_CREATOR_INPUT] = ContainerIds.UI;
        N11980[N11980_CRAFTING_INPUT] = ContainerIds.UI;
        N11980[N11980_CREATED_OUTPUT] = ContainerIds.UI;
        N11980[N11980_CURSOR] = ContainerIds.UI;
        N11980[N11980_ENCHANTING_INPUT] = ContainerIds.UI;
        N11980[N11980_ENCHANTING_MATERIAL] = ContainerIds.UI;
        N11980[N11980_GRINDSTONE_ADDITIONAL] = ContainerIds.UI;
        N11980[N11980_GRINDSTONE_INPUT] = ContainerIds.UI;
        N11980[N11980_LAB_TABLE_INPUT] = ContainerIds.UI;
        N11980[N11980_LOOM_DYE] = ContainerIds.UI;
        N11980[N11980_LOOM_INPUT] = ContainerIds.UI;
        N11980[N11980_LOOM_MATERIAL] = ContainerIds.UI;
        N11980[N11980_MATERIAL_REDUCER_INPUT] = ContainerIds.UI;
        N11980[N11980_MATERIAL_REDUCER_OUTPUT] = ContainerIds.UI;
        N11980[N11980_SMITHING_TABLE_INPUT] = ContainerIds.UI;
        N11980[N11980_SMITHING_TABLE_MATERIAL] = ContainerIds.UI;
        N11980[N11980_SMITHING_TABLE_TEMPLATE] = ContainerIds.UI;
        N11980[N11980_STONECUTTER_INPUT] = ContainerIds.UI;
        N11980[N11980_TRADE2_INGREDIENT1] = ContainerIds.UI;
        N11980[N11980_TRADE2_INGREDIENT2] = ContainerIds.UI;
        N11980[N11980_TRADE_INGREDIENT1] = ContainerIds.UI;
        N11980[N11980_TRADE_INGREDIENT2] = ContainerIds.UI;

        N11980[N12120_DYNAMIC] = ContainerIds.REGISTRY;

        N11980[N11980_BARREL] = Integer.MIN_VALUE;
        N11980[N11980_BLAST_FURNACE_INGREDIENT] = Integer.MIN_VALUE;
        N11980[N11980_BREWING_STAND_FUEL] = Integer.MIN_VALUE;
        N11980[N11980_BREWING_STAND_INPUT] = Integer.MIN_VALUE;
        N11980[N11980_BREWING_STAND_RESULT] = Integer.MIN_VALUE;
        N11980[N12050_CRAFTER] = Integer.MIN_VALUE;
        N11980[N11980_FURNACE_FUEL] = Integer.MIN_VALUE;
        N11980[N11980_FURNACE_INGREDIENT] = Integer.MIN_VALUE;
        N11980[N11980_FURNACE_RESULT] = Integer.MIN_VALUE;
        N11980[N11980_HORSE_EQUIP] = Integer.MIN_VALUE;
        N11980[N11980_LEVEL_ENTITY] = Integer.MIN_VALUE;
        N11980[N11980_SHULKER_BOX] = Integer.MIN_VALUE;
        N11980[N11980_SMOKER_INGREDIENT] = Integer.MIN_VALUE;
    }

    public static int translate(AbstractProtocol protocol, boolean netease, int containerSlotType, int currentWindowId) {
        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
            if (netease) {
                return translate12010NE(containerSlotType, currentWindowId);
            }
            return translate11950(containerSlotType, currentWindowId);
        }
        return translate116(containerSlotType, currentWindowId);
    }

    public static int translate116(int containerSlotType, int currentWindowId) {
        int containerId = V116[containerSlotType & 0xff];
        if (containerId == Integer.MIN_VALUE) {
            return currentWindowId;
        }
        if (containerId == ContainerIds.NONE) {
            throw new IllegalArgumentException("Unexpected container UI ID " + containerSlotType);
        }
        return containerId;
    }

    public static int translate11950(int containerSlotType, int currentWindowId) {
        int containerId = V11950[containerSlotType & 0xff];
        if (containerId == Integer.MIN_VALUE) {
            return currentWindowId;
        }
        if (containerId == ContainerIds.NONE) {
            throw new IllegalArgumentException("Unexpected container UI ID " + containerSlotType);
        }
        return containerId;
    }

    public static int translate12010NE(int containerSlotType, int currentWindowId) {
        int containerId = N11980[containerSlotType & 0xff];
        if (containerId == Integer.MIN_VALUE) {
            return currentWindowId;
        }
        if (containerId == ContainerIds.NONE) {
            throw new IllegalArgumentException("Unexpected container UI ID " + containerSlotType);
        }
        return containerId;
    }

    public static int getCreatedOutputSlotTypeId(AbstractProtocol protocol, boolean netease) {
        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
            if (netease) {
                return N11980_CREATED_OUTPUT;
            }
            return V11950_CREATED_OUTPUT;
        }
        return V116_CREATED_OUTPUT;
    }

    public static boolean isOrdinaryStorageSlot(AbstractProtocol protocol, boolean netease, int slotType) {
        if (slotType < 0 || slotType >= V116.length) {
            return false;
        }
        int[] slots;
        int createdOutput;
        int materialReducerOutput;
        if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
            slots = V116;
            createdOutput = V116_CREATED_OUTPUT;
            materialReducerOutput = V116_MATERIAL_REDUCER_OUTPUT;
        } else if (netease) {
            slots = N11980;
            createdOutput = N11980_CREATED_OUTPUT;
            materialReducerOutput = N11980_MATERIAL_REDUCER_OUTPUT;
        } else {
            slots = V11950;
            createdOutput = V11950_CREATED_OUTPUT;
            materialReducerOutput = V11950_MATERIAL_REDUCER_OUTPUT;
        }
        return slots[slotType] != ContainerIds.NONE && slots[slotType] != ContainerIds.REGISTRY
                && slotType != createdOutput && slotType != materialReducerOutput;
    }

    public static int getHotbarSlotTypeId(AbstractProtocol protocol, boolean netease) {
        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
            return netease ? N11980_HOTBAR : V11950_HOTBAR;
        }
        return V116_HOTBAR;
    }
}

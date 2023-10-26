package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.item.ItemBlockID;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.util.EnumMap;
import java.util.Map;

public final class BlockItemFlattener {
    private static final AbstractProtocol BASE_GAME_VERSION = AbstractProtocol.PROTOCOL_118;
    private static final Map<AbstractProtocol, Int2IntFunction> DOWNGRADERS = new EnumMap<>(AbstractProtocol.class);

    public static int downgrade(AbstractProtocol protocol, int id) {
        Int2IntFunction func = DOWNGRADERS.get(protocol);
        if (func == null) {
            return id;
        }
        return func.applyAsInt(id);
    }

    static {
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_119_70, BlockItemFlattener::downgrader11970);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_119_80, BlockItemFlattener::downgrader11980);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_120, BlockItemFlattener::downgrader120);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_120_10, BlockItemFlattener::downgrader12010);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_120_30, BlockItemFlattener::downgrader12030);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_120_40, BlockItemFlattener::downgrader12030);
    }

    private static int downgrader11970(int id) {
        if (id <= ItemBlockID.BLACK_WOOL && id >= ItemBlockID.PINK_WOOL) {
            return ItemBlockID.WOOL;
        }
        return id;
    }

    private static int downgrader11980(int id) {
        if (id <= ItemBlockID.SPRUCE_LOG && id >= ItemBlockID.JUNGLE_LOG) {
            return ItemBlockID.LOG;
        }
        if (id == ItemBlockID.DARK_OAK_LOG) {
            return ItemBlockID.LOG2;
        }
        if (id <= ItemBlockID.ACACIA_FENCE && id >= ItemBlockID.SPRUCE_FENCE) {
            return ItemBlockID.FENCE;
        }
        return downgrader11970(id);
    }

    private static int downgrader120(int id) {
        if (id <= ItemBlockID.BRAIN_CORAL && id >= ItemBlockID.DEAD_HORN_CORAL) {
            return ItemBlockID.CORAL;
        }
        if (id <= ItemBlockID.ORANGE_CARPET && id >= ItemBlockID.BLACK_CARPET) {
            return ItemBlockID.CARPET;
        }
        return downgrader11980(id);
    }

    private static int downgrader12010(int id) {
        if (id <= ItemBlockID.ORANGE_SHULKER_BOX && id >= ItemBlockID.BLACK_SHULKER_BOX) {
            return ItemBlockID.SHULKER_BOX;
        }
        if (id <= ItemBlockID.ORANGE_CONCRETE && id >= ItemBlockID.BLACK_CONCRETE) {
            return ItemBlockID.CONCRETE;
        }
        return downgrader120(id);
    }

    private static int downgrader12020(int id) {
        if (id <= ItemBlockID.ORANGE_STAINED_GLASS && id >= ItemBlockID.BLACK_STAINED_GLASS) {
            return ItemBlockID.STAINED_GLASS;
        }
        if (id <= ItemBlockID.ORANGE_STAINED_GLASS_PANE && id >= ItemBlockID.BLACK_STAINED_GLASS_PANE) {
            return ItemBlockID.STAINED_GLASS_PANE;
        }
        return downgrader12010(id);
    }

    private static int downgrader12030(int id) {
        if (id <= ItemBlockID.ORANGE_CONCRETE_POWDER && id >= ItemBlockID.BLACK_CONCRETE_POWDER) {
            return ItemBlockID.CONCRETE_POWDER;
        }
        if (id <= ItemBlockID.ORANGE_TERRACOTTA && id >= ItemBlockID.BLACK_TERRACOTTA) {
            return ItemBlockID.STAINED_HARDENED_CLAY;
        }
        return downgrader12020(id);
    }

    private static int downgrader12050(int id) {
        if (id <= ItemBlockID.GRANITE && id >= ItemBlockID.POLISHED_ANDESITE) {
            return ItemBlockID.STONE;
        }
        if (id <= ItemBlockID.SPRUCE_PLANKS && id >= ItemBlockID.DARK_OAK_PLANKS) {
            return ItemBlockID.PLANKS;
        }
        return downgrader12030(id);
    }

    private BlockItemFlattener() {
        throw new IllegalStateException();
    }
}

//dirt
//sapling
//sand
//leaves
//sandstone
//tallgrass
//red_flower
//double_stone_slab
//stone_slab
//tnt
//monster_egg
//stonebrick
//cobblestone_wall
//quartz_block
//double_wooden_slab
//wooden_slab
//leaves2
//prismarine
//double_plant
//red_sandstone
//double_stone_slab2
//stone_slab2
//hard_stained_glass_pane
//purpur_block
//colored_torch_rg
//colored_torch_bp
//chemistry_table
//hard_stained_glass
//coral_block
//coral_fan
//coral_fan_dead
//coral_fan_hang
//coral_fan_hang2
//coral_fan_hang3
//stone_slab3
//stone_slab4
//double_stone_slab3
//double_stone_slab4
//wood

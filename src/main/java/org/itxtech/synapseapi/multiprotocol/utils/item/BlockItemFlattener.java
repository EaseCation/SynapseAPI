package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.block.BlockTallGrass;
import cn.nukkit.item.ItemBlockID;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.util.EnumMap;
import java.util.Map;

public final class BlockItemFlattener {
    private static final AbstractProtocol BASE_GAME_VERSION = AbstractProtocol.PROTOCOL_118;
    private static final Map<AbstractProtocol, Int2IntFunction> DOWNGRADERS = new EnumMap<>(AbstractProtocol.class);
    private static final Map<AbstractProtocol, AuxValueFixer> AUX_VALUE_FIXERS = new EnumMap<>(AbstractProtocol.class);

    public static int downgrade(AbstractProtocol protocol, int id) {
        Int2IntFunction func = DOWNGRADERS.get(protocol);
        if (func == null) {
            return id;
        }
        return func.applyAsInt(id);
    }

    public static int fixMeta(AbstractProtocol protocol, int id, int meta) {
        AuxValueFixer fixer = AUX_VALUE_FIXERS.get(protocol);
        if (fixer == null) {
            return meta;
        }
        return fixer.fix(id, meta);
    }

    static {
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_119_70, BlockItemFlattener::downgrader11970);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_119_80, BlockItemFlattener::downgrader11980);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_120, BlockItemFlattener::downgrader120);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_120_10, BlockItemFlattener::downgrader12010);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_120_30, BlockItemFlattener::downgrader12030);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_120_40, BlockItemFlattener::downgrader12030);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_120_50, BlockItemFlattener::downgrader12050);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_120_60, BlockItemFlattener::downgrader12060);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_120_70, BlockItemFlattener::downgrader12070);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_120_80, BlockItemFlattener::downgrader12080);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_121, BlockItemFlattener::downgrader121);

        AUX_VALUE_FIXERS.put(AbstractProtocol.PROTOCOL_121, BlockItemFlattener::metaFixer121);
    }

    private static int downgrader11970(int id) {
        if (id <= ItemBlockID.LIGHT_GRAY_WOOL && id >= ItemBlockID.PINK_WOOL) {
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

    private static int downgrader12060(int id) {
        if (id <= ItemBlockID.HARD_ORANGE_STAINED_GLASS && id >= ItemBlockID.HARD_BLACK_STAINED_GLASS) {
            return ItemBlockID.HARD_STAINED_GLASS;
        }
        if (id <= ItemBlockID.HARD_ORANGE_STAINED_GLASS_PANE && id >= ItemBlockID.HARD_BLACK_STAINED_GLASS_PANE) {
            return ItemBlockID.HARD_STAINED_GLASS_PANE;
        }
        return downgrader12050(id);
    }

    private static int downgrader12070(int id) {
        if (id <= ItemBlockID.SPRUCE_LEAVES && id >= ItemBlockID.JUNGLE_LEAVES) {
            return ItemBlockID.LEAVES;
        }
        if (id == ItemBlockID.DARK_OAK_LEAVES) {
            return ItemBlockID.LEAVES2;
        }
        if (id <= ItemBlockID.SPRUCE_SLAB && id >= ItemBlockID.DARK_OAK_SLAB) {
            return ItemBlockID.WOODEN_SLAB;
        }
        if (id <= ItemBlockID.SPRUCE_DOUBLE_SLAB && id >= ItemBlockID.DARK_OAK_DOUBLE_SLAB) {
            return ItemBlockID.DOUBLE_WOODEN_SLAB;
        }
        if (id <= ItemBlockID.SPRUCE_WOOD && id >= ItemBlockID.STRIPPED_DARK_OAK_WOOD) {
            return ItemBlockID.WOOD;
        }
        return downgrader12060(id);
    }

    private static int downgrader12080(int id) {
        if (id <= ItemBlockID.SPRUCE_SAPLING && id >= ItemBlockID.DARK_OAK_SAPLING) {
            return ItemBlockID.SAPLING;
        }
        if (id <= ItemBlockID.BRAIN_CORAL_FAN && id >= ItemBlockID.HORN_CORAL_FAN) {
            return ItemBlockID.CORAL_FAN;
        }
        if (id <= ItemBlockID.DEAD_BRAIN_CORAL_FAN && id >= ItemBlockID.DEAD_HORN_CORAL_FAN) {
            return ItemBlockID.CORAL_FAN_DEAD;
        }
        if (id <= ItemBlockID.BLUE_ORCHID && id >= ItemBlockID.LILY_OF_THE_VALLEY) {
            return ItemBlockID.RED_FLOWER;
        }
        return downgrader12070(id);
    }

    private static int downgrader121(int id) {
        if (id == ItemBlockID.FERN) {
            return ItemBlockID.SHORT_GRASS;
        }
        if (id <= ItemBlockID.LILAC && id >= ItemBlockID.PEONY) {
            return ItemBlockID.DOUBLE_PLANT;
        }
        if (id <= ItemBlockID.BRAIN_CORAL_BLOCK && id >= ItemBlockID.DEAD_HORN_CORAL_BLOCK) {
            return ItemBlockID.CORAL_BLOCK;
        }
        if (id <= ItemBlockID.SANDSTONE_SLAB && id >= ItemBlockID.NETHER_BRICK_SLAB) {
            return ItemBlockID.STONE_SLAB;
        }
        if (id == ItemBlockID.PETRIFIED_OAK_SLAB) {
            return ItemBlockID.STONE_SLAB;
        }
        return downgrader12080(id);
    }

    private static int metaFixer121(int id, int meta) {
        if (id == ItemBlockID.SHORT_GRASS) {
            if (meta == 0) {
                return BlockTallGrass.TYPE_GRASS;
            }
        }
        return meta;
    }

    @FunctionalInterface
    private interface AuxValueFixer {
        int fix(int id, int meta);
    }

    private BlockItemFlattener() {
        throw new IllegalStateException();
    }
}

//dirt
//sand
//sandstone
//double_stone_slab
//tnt
//monster_egg
//stonebrick
//cobblestone_wall
//quartz_block
//prismarine
//red_sandstone
//double_stone_slab2
//stone_slab2
//purpur_block
//colored_torch_rg
//colored_torch_bp
//chemistry_table
//coral_fan_hang
//coral_fan_hang2
//coral_fan_hang3
//stone_slab3
//stone_slab4
//double_stone_slab3
//double_stone_slab4

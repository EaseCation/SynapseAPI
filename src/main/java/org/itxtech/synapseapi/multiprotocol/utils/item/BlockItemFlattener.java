package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.block.BlockTallGrass;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlockID;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.ItemSkull;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.util.EnumMap;
import java.util.Map;

public final class BlockItemFlattener {
    private static final AbstractProtocol BASE_GAME_VERSION = AbstractProtocol.PROTOCOL_120_10;
    private static final Map<AbstractProtocol, Int2IntFunction> DOWNGRADERS = new EnumMap<>(AbstractProtocol.class);
    private static final Map<AbstractProtocol, AuxValueFixer> AUX_VALUE_FIXERS = new EnumMap<>(AbstractProtocol.class);

    /**
     * @return item id
     */
    public static int downgrade(AbstractProtocol protocol, int id) {
        Int2IntFunction func = DOWNGRADERS.get(protocol);
        if (func == null) {
            return id;
        }
        return func.applyAsInt(id);
    }

    /**
     * @return item full id
     */
    public static int fixMeta(AbstractProtocol protocol, int flattenedId, int id, int meta) {
        AuxValueFixer fixer = AUX_VALUE_FIXERS.get(protocol);
        if (fixer == null) {
            return Item.getFullId(id, meta);
        }
        return fixer.fix(flattenedId, id, meta);
    }

    static {
        registerIdDowngrader(AbstractProtocol.PROTOCOL_119_70, BlockItemFlattener::downgrader11970);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_119_80, BlockItemFlattener::downgrader11980);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_120, BlockItemFlattener::downgrader120);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_120_10, BlockItemFlattener::downgrader12010);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_120_30, BlockItemFlattener::downgrader12030);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_120_40, BlockItemFlattener::downgrader12030);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_120_50, BlockItemFlattener::downgrader12050);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_120_60, BlockItemFlattener::downgrader12060);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_120_70, BlockItemFlattener::downgrader12070);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_120_80, BlockItemFlattener::downgrader12080);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_121, BlockItemFlattener::downgrader121);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_121_2, BlockItemFlattener::downgrader121);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_121_20, BlockItemFlattener::downgrader12120);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_121_30, BlockItemFlattener::downgrader12130);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_121_40, BlockItemFlattener::downgrader12140);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_121_50, BlockItemFlattener::downgrader12140);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_121_60, BlockItemFlattener::downgrader12140);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_121_70, BlockItemFlattener::downgrader12140);
        registerIdDowngrader(AbstractProtocol.PROTOCOL_121_80, BlockItemFlattener::downgrader12140);

        registerAuxValueFixer(AbstractProtocol.PROTOCOL_121, BlockItemFlattener::metaFixer121);
        registerAuxValueFixer(AbstractProtocol.PROTOCOL_121_2, BlockItemFlattener::metaFixer121);
        registerAuxValueFixer(AbstractProtocol.PROTOCOL_121_20, BlockItemFlattener::metaFixer121);
        registerAuxValueFixer(AbstractProtocol.PROTOCOL_121_30, BlockItemFlattener::metaFixer121);
        registerAuxValueFixer(AbstractProtocol.PROTOCOL_121_40, BlockItemFlattener::metaFixer12140);
        registerAuxValueFixer(AbstractProtocol.PROTOCOL_121_50, BlockItemFlattener::metaFixer12140);
        registerAuxValueFixer(AbstractProtocol.PROTOCOL_121_60, BlockItemFlattener::metaFixer12140);
        registerAuxValueFixer(AbstractProtocol.PROTOCOL_121_70, BlockItemFlattener::metaFixer12140);
        registerAuxValueFixer(AbstractProtocol.PROTOCOL_121_80, BlockItemFlattener::metaFixer12140);
    }

    private static void registerIdDowngrader(AbstractProtocol protocol, Int2IntFunction downgrader) {
        if (protocol.getProtocolStart() <= BASE_GAME_VERSION.getProtocolStart()) {
            return;
        }
        DOWNGRADERS.put(protocol, downgrader);
    }

    private static void registerAuxValueFixer(AbstractProtocol protocol, AuxValueFixer fixer) {
        if (protocol.getProtocolStart() <= BASE_GAME_VERSION.getProtocolStart()) {
            return;
        }
        AUX_VALUE_FIXERS.put(protocol, fixer);
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
        if (true) { // base-game-version: 1.20.10
            return id;
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

    private static int downgrader12120(int id) {
        if (id <= ItemBlockID.SANDSTONE_DOUBLE_SLAB && id >= ItemBlockID.NETHER_BRICK_DOUBLE_SLAB) {
            return ItemBlockID.DOUBLE_STONE_SLAB;
        }
        if (id == ItemBlockID.PETRIFIED_OAK_DOUBLE_SLAB) {
            return ItemBlockID.DOUBLE_STONE_SLAB;
        }
        if (id <= ItemBlockID.PURPUR_SLAB && id >= ItemBlockID.RED_NETHER_BRICK_SLAB) {
            return ItemBlockID.STONE_SLAB2;
        }
        if (id <= ItemBlockID.PURPUR_DOUBLE_SLAB && id >= ItemBlockID.RED_NETHER_BRICK_DOUBLE_SLAB) {
            return ItemBlockID.DOUBLE_STONE_SLAB2;
        }
        if (id <= ItemBlockID.SMOOTH_RED_SANDSTONE_SLAB && id >= ItemBlockID.POLISHED_GRANITE_SLAB) {
            return ItemBlockID.STONE_SLAB3;
        }
        if (id <= ItemBlockID.SMOOTH_RED_SANDSTONE_DOUBLE_SLAB && id >= ItemBlockID.POLISHED_GRANITE_DOUBLE_SLAB) {
            return ItemBlockID.DOUBLE_STONE_SLAB3;
        }
        if (id <= ItemBlockID.SMOOTH_QUARTZ_SLAB && id >= ItemBlockID.CUT_RED_SANDSTONE_SLAB) {
            return ItemBlockID.STONE_SLAB4;
        }
        if (id <= ItemBlockID.SMOOTH_QUARTZ_DOUBLE_SLAB && id >= ItemBlockID.CUT_RED_SANDSTONE_DOUBLE_SLAB) {
            return ItemBlockID.DOUBLE_STONE_SLAB4;
        }
        if (id <= ItemBlockID.BRAIN_CORAL_WALL_FAN && id >= ItemBlockID.DEAD_BRAIN_CORAL_WALL_FAN) {
            return ItemBlockID.CORAL_FAN_HANG;
        }
        if (id <= ItemBlockID.FIRE_CORAL_WALL_FAN && id >= ItemBlockID.DEAD_FIRE_CORAL_WALL_FAN) {
            return ItemBlockID.CORAL_FAN_HANG2;
        }
        if (id == ItemBlockID.DEAD_HORN_CORAL_WALL_FAN) {
            return ItemBlockID.CORAL_FAN_HANG3;
        }
        if (id <= ItemBlockID.INFESTED_COBBLESTONE && id >= ItemBlockID.INFESTED_CHISELED_STONE_BRICKS) {
            return ItemBlockID.MONSTER_EGG;
        }
        if (id <= ItemBlockID.MOSSY_STONE_BRICKS && id >= ItemBlockID.CHISELED_STONE_BRICKS) {
            return ItemBlockID.STONEBRICK;
        }
        if (id <= ItemBlockID.DARK_PRISMARINE && id >= ItemBlockID.PRISMARINE_BRICKS) {
            return ItemBlockID.PRISMARINE;
        }
        if (id <= ItemBlockID.CHISELED_SANDSTONE && id >= ItemBlockID.SMOOTH_SANDSTONE) {
            return ItemBlockID.SANDSTONE;
        }
        if (id <= ItemBlockID.CHISELED_RED_SANDSTONE && id >= ItemBlockID.SMOOTH_RED_SANDSTONE) {
            return ItemBlockID.RED_SANDSTONE;
        }
        if (id <= ItemBlockID.LIGHT_BLOCK_1 && id >= ItemBlockID.LIGHT_BLOCK_15) {
            return ItemBlockID.LIGHT_BLOCK;
        }
        if (id <= ItemBlockID.CHISELED_QUARTZ_BLOCK && id >= ItemBlockID.SMOOTH_QUARTZ) {
            return ItemBlockID.QUARTZ_BLOCK;
        }
        if (id <= ItemBlockID.CHIPPED_ANVIL && id >= ItemBlockID.DEPRECATED_ANVIL) {
            return ItemBlockID.ANVIL;
        }
        if (id == ItemBlockID.COARSE_DIRT) {
            return ItemBlockID.DIRT;
        }
        if (id == ItemBlockID.RED_SAND) {
            return ItemBlockID.SAND;
        }
        return downgrader121(id);
    }

    private static int downgrader12130(int id) {
        if (id <= ItemBlockID.DEPRECATED_PURPUR_BLOCK_1 && id >= ItemBlockID.DEPRECATED_PURPUR_BLOCK_2) {
            return ItemBlockID.PURPUR_BLOCK;
        }
        if (id <= ItemBlockID.MOSSY_COBBLESTONE_WALL && id >= ItemBlockID.RED_NETHER_BRICK_WALL) {
            return ItemBlockID.COBBLESTONE_WALL;
        }
        if (id == ItemBlockID.WET_SPONGE) {
            return ItemBlockID.SPONGE;
        }
        if (id == ItemBlockID.COLORED_TORCH_GREEN) {
            return ItemBlockID.COLORED_TORCH_RG;
        }
        if (id == ItemBlockID.COLORED_TORCH_PURPLE) {
            return ItemBlockID.COLORED_TORCH_BP;
        }
        if (id <= ItemBlockID.MATERIAL_REDUCER && id >= ItemBlockID.LAB_TABLE) {
            return ItemBlockID.CHEMISTRY_TABLE;
        }
        if (id == ItemBlockID.UNDERWATER_TNT) {
            return ItemBlockID.TNT;
        }
        return downgrader12120(id);
    }

    private static int downgrader12140(int id) {
        if (id <= ItemBlockID.WITHER_SKELETON_SKULL && id >= ItemBlockID.PIGLIN_HEAD) {
            return ItemID.SKULL;
        }
        return downgrader12130(id);
    }

    private static int metaFixer121(int flattenedId, int id, int meta) {
        if (id == ItemBlockID.SHORT_GRASS) {
            if (meta == 0) {
                return Item.getFullId(id, BlockTallGrass.TYPE_GRASS);
            }
        }
        return Item.getFullId(id, meta);
    }

    private static int metaFixer12140(int flattenedId, int id, int meta) {
        if (flattenedId == ItemBlockID.WITHER_SKELETON_SKULL) {
            return Item.getFullId(ItemID.SKULL, ItemSkull.HEAD_WITHER_SKELETON);
        }
        if (flattenedId == ItemBlockID.ZOMBIE_HEAD) {
            return Item.getFullId(ItemID.SKULL, ItemSkull.HEAD_ZOMBIE);
        }
        if (flattenedId == ItemBlockID.PLAYER_HEAD) {
            return Item.getFullId(ItemID.SKULL, ItemSkull.HEAD_PLAYER);
        }
        if (flattenedId == ItemBlockID.CREEPER_HEAD) {
            return Item.getFullId(ItemID.SKULL, ItemSkull.HEAD_CREEPER);
        }
        if (flattenedId == ItemBlockID.DRAGON_HEAD) {
            return Item.getFullId(ItemID.SKULL, ItemSkull.HEAD_DRAGON);
        }
        if (flattenedId == ItemBlockID.PIGLIN_HEAD) {
            return Item.getFullId(ItemID.SKULL, ItemSkull.HEAD_PIGLIN);
        }
        return metaFixer121(flattenedId, id, meta);
    }

    @FunctionalInterface
    private interface AuxValueFixer {
        /**
         * @return item full id
         */
        int fix(int flattenedId, int id, int meta);
    }

    private BlockItemFlattener() {
        throw new IllegalStateException();
    }
}

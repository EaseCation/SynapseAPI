package org.itxtech.synapseapi.multiprotocol.utils.block.state;

import cn.nukkit.block.BlockID;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockStateIntegerValues;

public class BlockTypes {
    private static final BlockRegistry REGISTRY = new BlockRegistry((1 << 24) | (21 << 16) | (0 << 8) | 3);

    public static final BlockLegacy ACACIA_BUTTON = REGISTRY.registerBlock("minecraft:acacia_button", BlockID.ACACIA_BUTTON)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.BUTTON_PRESSED_BIT);
    public static final BlockLegacy ACACIA_DOOR = REGISTRY.registerBlock("minecraft:acacia_door", BlockID.BLOCK_ACACIA_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy ACACIA_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:acacia_double_slab", BlockID.ACACIA_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy ACACIA_FENCE = REGISTRY.registerBlock("minecraft:acacia_fence", BlockID.ACACIA_FENCE);
    public static final BlockLegacy ACACIA_FENCE_GATE = REGISTRY.registerBlock("minecraft:acacia_fence_gate", BlockID.ACACIA_FENCE_GATE)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.IN_WALL_BIT);
    public static final BlockLegacy ACACIA_HANGING_SIGN = REGISTRY.registerBlock("minecraft:acacia_hanging_sign", BlockID.ACACIA_HANGING_SIGN)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.GROUND_SIGN_DIRECTION)
            .addState(BlockStates.HANGING)
            .addState(BlockStates.ATTACHED_BIT);
    public static final BlockLegacy ACACIA_LEAVES = REGISTRY.registerBlock("minecraft:acacia_leaves", BlockID.ACACIA_LEAVES)
            .addState(BlockStates.UPDATE_BIT)
            .addState(BlockStates.PERSISTENT_BIT);
    public static final BlockLegacy ACACIA_LOG = REGISTRY.registerBlock("minecraft:acacia_log", BlockID.ACACIA_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy ACACIA_PLANKS = REGISTRY.registerBlock("minecraft:acacia_planks", BlockID.ACACIA_PLANKS);
    public static final BlockLegacy ACACIA_PRESSURE_PLATE = REGISTRY.registerBlock("minecraft:acacia_pressure_plate", BlockID.ACACIA_PRESSURE_PLATE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy ACACIA_SAPLING = REGISTRY.registerBlock("minecraft:acacia_sapling", BlockID.ACACIA_SAPLING)
            .addState(BlockStates.AGE_BIT);
    public static final BlockLegacy ACACIA_SLAB = REGISTRY.registerBlock("minecraft:acacia_slab", BlockID.ACACIA_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy ACACIA_STAIRS = REGISTRY.registerBlock("minecraft:acacia_stairs", BlockID.ACACIA_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy ACACIA_STANDING_SIGN = REGISTRY.registerBlock("minecraft:acacia_standing_sign", BlockID.ACACIA_STANDING_SIGN)
            .addState(BlockStates.GROUND_SIGN_DIRECTION);
    public static final BlockLegacy ACACIA_TRAPDOOR = REGISTRY.registerBlock("minecraft:acacia_trapdoor", BlockID.ACACIA_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy ACACIA_WALL_SIGN = REGISTRY.registerBlock("minecraft:acacia_wall_sign", BlockID.ACACIA_WALL_SIGN)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy ACACIA_WOOD = REGISTRY.registerBlock("minecraft:acacia_wood", BlockID.ACACIA_WOOD)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy ACTIVATOR_RAIL = REGISTRY.registerBlock("minecraft:activator_rail", BlockID.ACTIVATOR_RAIL)
            .addState(BlockStates.RAIL_DIRECTION, BlockStateIntegerValues.ACTIVATOR_RAIL_MAX_RAIL_DIRECTION + 1)
            .addState(BlockStates.RAIL_DATA_BIT);
    public static final BlockLegacy AIR = REGISTRY.registerBlock("minecraft:air", BlockID.AIR);
    public static final BlockLegacy ALLIUM = REGISTRY.registerBlock("minecraft:allium", BlockID.ALLIUM);
    public static final BlockLegacy ALLOW = REGISTRY.registerBlock("minecraft:allow", BlockID.ALLOW);
    public static final BlockLegacy AMETHYST_BLOCK = REGISTRY.registerBlock("minecraft:amethyst_block", BlockID.AMETHYST_BLOCK);
    public static final BlockLegacy AMETHYST_CLUSTER = REGISTRY.registerBlock("minecraft:amethyst_cluster", BlockID.AMETHYST_CLUSTER)
            .addState(BlockStates.MINECRAFT_BLOCK_FACE);
    public static final BlockLegacy ANCIENT_DEBRIS = REGISTRY.registerBlock("minecraft:ancient_debris", BlockID.ANCIENT_DEBRIS);
    public static final BlockLegacy ANDESITE = REGISTRY.registerBlock("minecraft:andesite", BlockID.ANDESITE);
    public static final BlockLegacy ANDESITE_STAIRS = REGISTRY.registerBlock("minecraft:andesite_stairs", BlockID.ANDESITE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy ANVIL = REGISTRY.registerBlock("minecraft:anvil", BlockID.ANVIL)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION)
            .addState(BlockStates.DAMAGE);
    public static final BlockLegacy AZALEA = REGISTRY.registerBlock("minecraft:azalea", BlockID.AZALEA);
    public static final BlockLegacy AZALEA_LEAVES = REGISTRY.registerBlock("minecraft:azalea_leaves", BlockID.AZALEA_LEAVES)
            .addState(BlockStates.UPDATE_BIT)
            .addState(BlockStates.PERSISTENT_BIT);
    public static final BlockLegacy AZALEA_LEAVES_FLOWERED = REGISTRY.registerBlock("minecraft:azalea_leaves_flowered", BlockID.AZALEA_LEAVES_FLOWERED)
            .addState(BlockStates.UPDATE_BIT)
            .addState(BlockStates.PERSISTENT_BIT);
    public static final BlockLegacy AZURE_BLUET = REGISTRY.registerBlock("minecraft:azure_bluet", BlockID.AZURE_BLUET);
    public static final BlockLegacy BAMBOO = REGISTRY.registerBlock("minecraft:bamboo", BlockID.BAMBOO)
            .addState(BlockStates.BAMBOO_STALK_THICKNESS)
            .addState(BlockStates.BAMBOO_LEAF_SIZE)
            .addState(BlockStates.AGE_BIT);
    public static final BlockLegacy BAMBOO_BLOCK = REGISTRY.registerBlock("minecraft:bamboo_block", BlockID.BAMBOO_BLOCK)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy BAMBOO_BUTTON = REGISTRY.registerBlock("minecraft:bamboo_button", BlockID.BAMBOO_BUTTON)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.BUTTON_PRESSED_BIT);
    public static final BlockLegacy BAMBOO_DOOR = REGISTRY.registerBlock("minecraft:bamboo_door", BlockID.BAMBOO_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy BAMBOO_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:bamboo_double_slab", BlockID.BAMBOO_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy BAMBOO_FENCE = REGISTRY.registerBlock("minecraft:bamboo_fence", BlockID.BAMBOO_FENCE);
    public static final BlockLegacy BAMBOO_FENCE_GATE = REGISTRY.registerBlock("minecraft:bamboo_fence_gate", BlockID.BAMBOO_FENCE_GATE)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.IN_WALL_BIT);
    public static final BlockLegacy BAMBOO_HANGING_SIGN = REGISTRY.registerBlock("minecraft:bamboo_hanging_sign", BlockID.BAMBOO_HANGING_SIGN)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.GROUND_SIGN_DIRECTION)
            .addState(BlockStates.HANGING)
            .addState(BlockStates.ATTACHED_BIT);
    public static final BlockLegacy BAMBOO_MOSAIC = REGISTRY.registerBlock("minecraft:bamboo_mosaic", BlockID.BAMBOO_MOSAIC);
    public static final BlockLegacy BAMBOO_MOSAIC_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:bamboo_mosaic_double_slab", BlockID.BAMBOO_MOSAIC_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy BAMBOO_MOSAIC_SLAB = REGISTRY.registerBlock("minecraft:bamboo_mosaic_slab", BlockID.BAMBOO_MOSAIC_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy BAMBOO_MOSAIC_STAIRS = REGISTRY.registerBlock("minecraft:bamboo_mosaic_stairs", BlockID.BAMBOO_MOSAIC_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy BAMBOO_PLANKS = REGISTRY.registerBlock("minecraft:bamboo_planks", BlockID.BAMBOO_PLANKS);
    public static final BlockLegacy BAMBOO_PRESSURE_PLATE = REGISTRY.registerBlock("minecraft:bamboo_pressure_plate", BlockID.BAMBOO_PRESSURE_PLATE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy BAMBOO_SAPLING = REGISTRY.registerBlock("minecraft:bamboo_sapling", BlockID.BAMBOO_SAPLING)
            .addState(BlockStates.AGE_BIT);
    public static final BlockLegacy BAMBOO_SLAB = REGISTRY.registerBlock("minecraft:bamboo_slab", BlockID.BAMBOO_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy BAMBOO_STAIRS = REGISTRY.registerBlock("minecraft:bamboo_stairs", BlockID.BAMBOO_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy BAMBOO_STANDING_SIGN = REGISTRY.registerBlock("minecraft:bamboo_standing_sign", BlockID.BAMBOO_STANDING_SIGN)
            .addState(BlockStates.GROUND_SIGN_DIRECTION);
    public static final BlockLegacy BAMBOO_TRAPDOOR = REGISTRY.registerBlock("minecraft:bamboo_trapdoor", BlockID.BAMBOO_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy BAMBOO_WALL_SIGN = REGISTRY.registerBlock("minecraft:bamboo_wall_sign", BlockID.BAMBOO_WALL_SIGN)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy BARREL = REGISTRY.registerBlock("minecraft:barrel", BlockID.BARREL)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy BARRIER = REGISTRY.registerBlock("minecraft:barrier", BlockID.BARRIER);
    public static final BlockLegacy BASALT = REGISTRY.registerBlock("minecraft:basalt", BlockID.BASALT)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy BEACON = REGISTRY.registerBlock("minecraft:beacon", BlockID.BEACON);
    public static final BlockLegacy BED = REGISTRY.registerBlock("minecraft:bed", BlockID.BLOCK_BED)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OCCUPIED_BIT)
            .addState(BlockStates.HEAD_PIECE_BIT);
    public static final BlockLegacy BEDROCK = REGISTRY.registerBlock("minecraft:bedrock", BlockID.BEDROCK)
            .addState(BlockStates.INFINIBURN_BIT);
    public static final BlockLegacy BEE_NEST = REGISTRY.registerBlock("minecraft:bee_nest", BlockID.BEE_NEST)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.HONEY_LEVEL);
    public static final BlockLegacy BEEHIVE = REGISTRY.registerBlock("minecraft:beehive", BlockID.BEEHIVE)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.HONEY_LEVEL);
    public static final BlockLegacy BEETROOT = REGISTRY.registerBlock("minecraft:beetroot", BlockID.BLOCK_BEETROOT)
            .addState(BlockStates.GROWTH);
    public static final BlockLegacy BELL = REGISTRY.registerBlock("minecraft:bell", BlockID.BELL)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.ATTACHMENT)
            .addState(BlockStates.TOGGLE_BIT);
    public static final BlockLegacy BIG_DRIPLEAF = REGISTRY.registerBlock("minecraft:big_dripleaf", BlockID.BIG_DRIPLEAF)
            .addState(BlockStates.BIG_DRIPLEAF_TILT)
            .addState(BlockStates.BIG_DRIPLEAF_HEAD)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy BIRCH_BUTTON = REGISTRY.registerBlock("minecraft:birch_button", BlockID.BIRCH_BUTTON)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.BUTTON_PRESSED_BIT);
    public static final BlockLegacy BIRCH_DOOR = REGISTRY.registerBlock("minecraft:birch_door", BlockID.BLOCK_BIRCH_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy BIRCH_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:birch_double_slab", BlockID.BIRCH_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy BIRCH_FENCE = REGISTRY.registerBlock("minecraft:birch_fence", BlockID.BIRCH_FENCE);
    public static final BlockLegacy BIRCH_FENCE_GATE = REGISTRY.registerBlock("minecraft:birch_fence_gate", BlockID.BIRCH_FENCE_GATE)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.IN_WALL_BIT);
    public static final BlockLegacy BIRCH_HANGING_SIGN = REGISTRY.registerBlock("minecraft:birch_hanging_sign", BlockID.BIRCH_HANGING_SIGN)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.GROUND_SIGN_DIRECTION)
            .addState(BlockStates.HANGING)
            .addState(BlockStates.ATTACHED_BIT);
    public static final BlockLegacy BIRCH_LEAVES = REGISTRY.registerBlock("minecraft:birch_leaves", BlockID.BIRCH_LEAVES)
            .addState(BlockStates.UPDATE_BIT)
            .addState(BlockStates.PERSISTENT_BIT);
    public static final BlockLegacy BIRCH_LOG = REGISTRY.registerBlock("minecraft:birch_log", BlockID.BIRCH_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy BIRCH_PLANKS = REGISTRY.registerBlock("minecraft:birch_planks", BlockID.BIRCH_PLANKS);
    public static final BlockLegacy BIRCH_PRESSURE_PLATE = REGISTRY.registerBlock("minecraft:birch_pressure_plate", BlockID.BIRCH_PRESSURE_PLATE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy BIRCH_SAPLING = REGISTRY.registerBlock("minecraft:birch_sapling", BlockID.BIRCH_SAPLING)
            .addState(BlockStates.AGE_BIT);
    public static final BlockLegacy BIRCH_SLAB = REGISTRY.registerBlock("minecraft:birch_slab", BlockID.BIRCH_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy BIRCH_STAIRS = REGISTRY.registerBlock("minecraft:birch_stairs", BlockID.BIRCH_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy BIRCH_STANDING_SIGN = REGISTRY.registerBlock("minecraft:birch_standing_sign", BlockID.BIRCH_STANDING_SIGN)
            .addState(BlockStates.GROUND_SIGN_DIRECTION);
    public static final BlockLegacy BIRCH_TRAPDOOR = REGISTRY.registerBlock("minecraft:birch_trapdoor", BlockID.BIRCH_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy BIRCH_WALL_SIGN = REGISTRY.registerBlock("minecraft:birch_wall_sign", BlockID.BIRCH_WALL_SIGN)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy BIRCH_WOOD = REGISTRY.registerBlock("minecraft:birch_wood", BlockID.BIRCH_WOOD)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy BLACK_CANDLE = REGISTRY.registerBlock("minecraft:black_candle", BlockID.BLACK_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy BLACK_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:black_candle_cake", BlockID.BLACK_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy BLACK_CARPET = REGISTRY.registerBlock("minecraft:black_carpet", BlockID.BLACK_CARPET);
    public static final BlockLegacy BLACK_CONCRETE = REGISTRY.registerBlock("minecraft:black_concrete", BlockID.BLACK_CONCRETE);
    public static final BlockLegacy BLACK_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:black_concrete_powder", BlockID.BLACK_CONCRETE_POWDER);
    public static final BlockLegacy BLACK_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:black_glazed_terracotta", BlockID.BLACK_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy BLACK_SHULKER_BOX = REGISTRY.registerBlock("minecraft:black_shulker_box", BlockID.BLACK_SHULKER_BOX);
    public static final BlockLegacy BLACK_STAINED_GLASS = REGISTRY.registerBlock("minecraft:black_stained_glass", BlockID.BLACK_STAINED_GLASS);
    public static final BlockLegacy BLACK_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:black_stained_glass_pane", BlockID.BLACK_STAINED_GLASS_PANE);
    public static final BlockLegacy BLACK_TERRACOTTA = REGISTRY.registerBlock("minecraft:black_terracotta", BlockID.BLACK_TERRACOTTA);
    public static final BlockLegacy BLACK_WOOL = REGISTRY.registerBlock("minecraft:black_wool", BlockID.BLACK_WOOL);
    public static final BlockLegacy BLACKSTONE = REGISTRY.registerBlock("minecraft:blackstone", BlockID.BLACKSTONE);
    public static final BlockLegacy BLACKSTONE_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:blackstone_double_slab", BlockID.BLACKSTONE_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy BLACKSTONE_SLAB = REGISTRY.registerBlock("minecraft:blackstone_slab", BlockID.BLACKSTONE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy BLACKSTONE_STAIRS = REGISTRY.registerBlock("minecraft:blackstone_stairs", BlockID.BLACKSTONE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy BLACKSTONE_WALL = REGISTRY.registerBlock("minecraft:blackstone_wall", BlockID.BLACKSTONE_WALL)
            .addState(BlockStates.WALL_POST_BIT)
            .addState(BlockStates.WALL_CONNECTION_TYPE_NORTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_EAST)
            .addState(BlockStates.WALL_CONNECTION_TYPE_SOUTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_WEST);
    public static final BlockLegacy BLAST_FURNACE = REGISTRY.registerBlock("minecraft:blast_furnace", BlockID.BLAST_FURNACE)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy BLUE_CANDLE = REGISTRY.registerBlock("minecraft:blue_candle", BlockID.BLUE_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy BLUE_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:blue_candle_cake", BlockID.BLUE_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy BLUE_CARPET = REGISTRY.registerBlock("minecraft:blue_carpet", BlockID.BLUE_CARPET);
    public static final BlockLegacy BLUE_CONCRETE = REGISTRY.registerBlock("minecraft:blue_concrete", BlockID.BLUE_CONCRETE);
    public static final BlockLegacy BLUE_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:blue_concrete_powder", BlockID.BLUE_CONCRETE_POWDER);
    public static final BlockLegacy BLUE_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:blue_glazed_terracotta", BlockID.BLUE_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy BLUE_ICE = REGISTRY.registerBlock("minecraft:blue_ice", BlockID.BLUE_ICE);
    public static final BlockLegacy BLUE_ORCHID = REGISTRY.registerBlock("minecraft:blue_orchid", BlockID.BLUE_ORCHID);
    public static final BlockLegacy BLUE_SHULKER_BOX = REGISTRY.registerBlock("minecraft:blue_shulker_box", BlockID.BLUE_SHULKER_BOX);
    public static final BlockLegacy BLUE_STAINED_GLASS = REGISTRY.registerBlock("minecraft:blue_stained_glass", BlockID.BLUE_STAINED_GLASS);
    public static final BlockLegacy BLUE_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:blue_stained_glass_pane", BlockID.BLUE_STAINED_GLASS_PANE);
    public static final BlockLegacy BLUE_TERRACOTTA = REGISTRY.registerBlock("minecraft:blue_terracotta", BlockID.BLUE_TERRACOTTA);
    public static final BlockLegacy BLUE_WOOL = REGISTRY.registerBlock("minecraft:blue_wool", BlockID.BLUE_WOOL);
    public static final BlockLegacy BONE_BLOCK = REGISTRY.registerBlock("minecraft:bone_block", BlockID.BONE_BLOCK)
            .addState(BlockStates.DEPRECATED)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy BOOKSHELF = REGISTRY.registerBlock("minecraft:bookshelf", BlockID.BOOKSHELF);
    public static final BlockLegacy BORDER_BLOCK = REGISTRY.registerBlock("minecraft:border_block", BlockID.BORDER_BLOCK)
            .addState(BlockStates.WALL_POST_BIT)
            .addState(BlockStates.WALL_CONNECTION_TYPE_NORTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_EAST)
            .addState(BlockStates.WALL_CONNECTION_TYPE_SOUTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_WEST);
    public static final BlockLegacy BRAIN_CORAL = REGISTRY.registerBlock("minecraft:brain_coral", BlockID.BRAIN_CORAL);
    public static final BlockLegacy BRAIN_CORAL_BLOCK = REGISTRY.registerBlock("minecraft:brain_coral_block", BlockID.BRAIN_CORAL_BLOCK);
    public static final BlockLegacy BRAIN_CORAL_FAN = REGISTRY.registerBlock("minecraft:brain_coral_fan", BlockID.BRAIN_CORAL_FAN)
            .addState(BlockStates.CORAL_FAN_DIRECTION);
    public static final BlockLegacy BREWING_STAND = REGISTRY.registerBlock("minecraft:brewing_stand", BlockID.BLOCK_BREWING_STAND)
            .addState(BlockStates.BREWING_STAND_SLOT_A_BIT)
            .addState(BlockStates.BREWING_STAND_SLOT_B_BIT)
            .addState(BlockStates.BREWING_STAND_SLOT_C_BIT);
    public static final BlockLegacy BRICK_BLOCK = REGISTRY.registerBlock("minecraft:brick_block", BlockID.BRICK_BLOCK);
    public static final BlockLegacy BRICK_SLAB = REGISTRY.registerBlock("minecraft:brick_slab", BlockID.BRICK_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy BRICK_STAIRS = REGISTRY.registerBlock("minecraft:brick_stairs", BlockID.BRICK_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy BROWN_CANDLE = REGISTRY.registerBlock("minecraft:brown_candle", BlockID.BROWN_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy BROWN_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:brown_candle_cake", BlockID.BROWN_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy BROWN_CARPET = REGISTRY.registerBlock("minecraft:brown_carpet", BlockID.BROWN_CARPET);
    public static final BlockLegacy BROWN_CONCRETE = REGISTRY.registerBlock("minecraft:brown_concrete", BlockID.BROWN_CONCRETE);
    public static final BlockLegacy BROWN_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:brown_concrete_powder", BlockID.BROWN_CONCRETE_POWDER);
    public static final BlockLegacy BROWN_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:brown_glazed_terracotta", BlockID.BROWN_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy BROWN_MUSHROOM = REGISTRY.registerBlock("minecraft:brown_mushroom", BlockID.BROWN_MUSHROOM);
    public static final BlockLegacy BROWN_MUSHROOM_BLOCK = REGISTRY.registerBlock("minecraft:brown_mushroom_block", BlockID.BROWN_MUSHROOM_BLOCK)
            .addState(BlockStates.HUGE_MUSHROOM_BITS);
    public static final BlockLegacy BROWN_SHULKER_BOX = REGISTRY.registerBlock("minecraft:brown_shulker_box", BlockID.BROWN_SHULKER_BOX);
    public static final BlockLegacy BROWN_STAINED_GLASS = REGISTRY.registerBlock("minecraft:brown_stained_glass", BlockID.BROWN_STAINED_GLASS);
    public static final BlockLegacy BROWN_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:brown_stained_glass_pane", BlockID.BROWN_STAINED_GLASS_PANE);
    public static final BlockLegacy BROWN_TERRACOTTA = REGISTRY.registerBlock("minecraft:brown_terracotta", BlockID.BROWN_TERRACOTTA);
    public static final BlockLegacy BROWN_WOOL = REGISTRY.registerBlock("minecraft:brown_wool", BlockID.BROWN_WOOL);
    public static final BlockLegacy BUBBLE_COLUMN = REGISTRY.registerBlock("minecraft:bubble_column", BlockID.BUBBLE_COLUMN)
            .addState(BlockStates.DRAG_DOWN);
    public static final BlockLegacy BUBBLE_CORAL = REGISTRY.registerBlock("minecraft:bubble_coral", BlockID.BUBBLE_CORAL);
    public static final BlockLegacy BUBBLE_CORAL_BLOCK = REGISTRY.registerBlock("minecraft:bubble_coral_block", BlockID.BUBBLE_CORAL_BLOCK);
    public static final BlockLegacy BUBBLE_CORAL_FAN = REGISTRY.registerBlock("minecraft:bubble_coral_fan", BlockID.BUBBLE_CORAL_FAN)
            .addState(BlockStates.CORAL_FAN_DIRECTION);
    public static final BlockLegacy BUDDING_AMETHYST = REGISTRY.registerBlock("minecraft:budding_amethyst", BlockID.BUDDING_AMETHYST);
    public static final BlockLegacy CACTUS = REGISTRY.registerBlock("minecraft:cactus", BlockID.CACTUS)
            .addState(BlockStates.AGE);
    public static final BlockLegacy CAKE = REGISTRY.registerBlock("minecraft:cake", BlockID.BLOCK_CAKE)
            .addState(BlockStates.BITE_COUNTER);
    public static final BlockLegacy CALCITE = REGISTRY.registerBlock("minecraft:calcite", BlockID.CALCITE);
    public static final BlockLegacy CALIBRATED_SCULK_SENSOR = REGISTRY.registerBlock("minecraft:calibrated_sculk_sensor", BlockID.CALIBRATED_SCULK_SENSOR)
            .addState(BlockStates.SCULK_SENSOR_PHASE)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy CAMERA = REGISTRY.registerBlock("minecraft:camera", BlockID.BLOCK_CAMERA);
    public static final BlockLegacy CAMPFIRE = REGISTRY.registerBlock("minecraft:campfire", BlockID.BLOCK_CAMPFIRE)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION)
            .addState(BlockStates.EXTINGUISHED);
    public static final BlockLegacy CANDLE = REGISTRY.registerBlock("minecraft:candle", BlockID.CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy CANDLE_CAKE = REGISTRY.registerBlock("minecraft:candle_cake", BlockID.CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy CARROTS = REGISTRY.registerBlock("minecraft:carrots", BlockID.CARROTS)
            .addState(BlockStates.GROWTH);
    public static final BlockLegacy CARTOGRAPHY_TABLE = REGISTRY.registerBlock("minecraft:cartography_table", BlockID.CARTOGRAPHY_TABLE);
    public static final BlockLegacy CARVED_PUMPKIN = REGISTRY.registerBlock("minecraft:carved_pumpkin", BlockID.CARVED_PUMPKIN)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy CAULDRON = REGISTRY.registerBlock("minecraft:cauldron", BlockID.BLOCK_CAULDRON)
            .addState(BlockStates.FILL_LEVEL)
            .addState(BlockStates.CAULDRON_LIQUID);
    public static final BlockLegacy CAVE_VINES = REGISTRY.registerBlock("minecraft:cave_vines", BlockID.CAVE_VINES)
            .addState(BlockStates.GROWING_PLANT_AGE);
    public static final BlockLegacy CAVE_VINES_BODY_WITH_BERRIES = REGISTRY.registerBlock("minecraft:cave_vines_body_with_berries", BlockID.CAVE_VINES_BODY_WITH_BERRIES)
            .addState(BlockStates.GROWING_PLANT_AGE);
    public static final BlockLegacy CAVE_VINES_HEAD_WITH_BERRIES = REGISTRY.registerBlock("minecraft:cave_vines_head_with_berries", BlockID.CAVE_VINES_HEAD_WITH_BERRIES)
            .addState(BlockStates.GROWING_PLANT_AGE);
    public static final BlockLegacy CHAIN = REGISTRY.registerBlock("minecraft:chain", BlockID.BLOCK_CHAIN)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy CHAIN_COMMAND_BLOCK = REGISTRY.registerBlock("minecraft:chain_command_block", BlockID.CHAIN_COMMAND_BLOCK)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.CONDITIONAL_BIT);
    public static final BlockLegacy CHEMICAL_HEAT = REGISTRY.registerBlock("minecraft:chemical_heat", BlockID.CHEMICAL_HEAT);
    public static final BlockLegacy CHEMISTRY_TABLE = REGISTRY.registerBlock("minecraft:chemistry_table", BlockID.CHEMISTRY_TABLE)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.CHEMISTRY_TABLE_TYPE);
    public static final BlockLegacy CHERRY_BUTTON = REGISTRY.registerBlock("minecraft:cherry_button", BlockID.CHERRY_BUTTON)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.BUTTON_PRESSED_BIT);
    public static final BlockLegacy CHERRY_DOOR = REGISTRY.registerBlock("minecraft:cherry_door", BlockID.CHERRY_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy CHERRY_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:cherry_double_slab", BlockID.CHERRY_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy CHERRY_FENCE = REGISTRY.registerBlock("minecraft:cherry_fence", BlockID.CHERRY_FENCE);
    public static final BlockLegacy CHERRY_FENCE_GATE = REGISTRY.registerBlock("minecraft:cherry_fence_gate", BlockID.CHERRY_FENCE_GATE)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.IN_WALL_BIT);
    public static final BlockLegacy CHERRY_HANGING_SIGN = REGISTRY.registerBlock("minecraft:cherry_hanging_sign", BlockID.CHERRY_HANGING_SIGN)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.GROUND_SIGN_DIRECTION)
            .addState(BlockStates.HANGING)
            .addState(BlockStates.ATTACHED_BIT);
    public static final BlockLegacy CHERRY_LEAVES = REGISTRY.registerBlock("minecraft:cherry_leaves", BlockID.CHERRY_LEAVES)
            .addState(BlockStates.UPDATE_BIT)
            .addState(BlockStates.PERSISTENT_BIT);
    public static final BlockLegacy CHERRY_LOG = REGISTRY.registerBlock("minecraft:cherry_log", BlockID.CHERRY_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy CHERRY_PLANKS = REGISTRY.registerBlock("minecraft:cherry_planks", BlockID.CHERRY_PLANKS);
    public static final BlockLegacy CHERRY_PRESSURE_PLATE = REGISTRY.registerBlock("minecraft:cherry_pressure_plate", BlockID.CHERRY_PRESSURE_PLATE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy CHERRY_SAPLING = REGISTRY.registerBlock("minecraft:cherry_sapling", BlockID.CHERRY_SAPLING)
            .addState(BlockStates.AGE_BIT);
    public static final BlockLegacy CHERRY_SLAB = REGISTRY.registerBlock("minecraft:cherry_slab", BlockID.CHERRY_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy CHERRY_STAIRS = REGISTRY.registerBlock("minecraft:cherry_stairs", BlockID.CHERRY_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy CHERRY_STANDING_SIGN = REGISTRY.registerBlock("minecraft:cherry_standing_sign", BlockID.CHERRY_STANDING_SIGN)
            .addState(BlockStates.GROUND_SIGN_DIRECTION);
    public static final BlockLegacy CHERRY_TRAPDOOR = REGISTRY.registerBlock("minecraft:cherry_trapdoor", BlockID.CHERRY_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy CHERRY_WALL_SIGN = REGISTRY.registerBlock("minecraft:cherry_wall_sign", BlockID.CHERRY_WALL_SIGN)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy CHERRY_WOOD = REGISTRY.registerBlock("minecraft:cherry_wood", BlockID.CHERRY_WOOD)
            .addState(BlockStates.STRIPPED_BIT)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy CHEST = REGISTRY.registerBlock("minecraft:chest", BlockID.CHEST)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy CHISELED_BOOKSHELF = REGISTRY.registerBlock("minecraft:chiseled_bookshelf", BlockID.CHISELED_BOOKSHELF)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.BOOKS_STORED);
    public static final BlockLegacy CHISELED_COPPER = REGISTRY.registerBlock("minecraft:chiseled_copper", BlockID.CHISELED_COPPER);
    public static final BlockLegacy CHISELED_DEEPSLATE = REGISTRY.registerBlock("minecraft:chiseled_deepslate", BlockID.CHISELED_DEEPSLATE);
    public static final BlockLegacy CHISELED_NETHER_BRICKS = REGISTRY.registerBlock("minecraft:chiseled_nether_bricks", BlockID.CHISELED_NETHER_BRICKS);
    public static final BlockLegacy CHISELED_POLISHED_BLACKSTONE = REGISTRY.registerBlock("minecraft:chiseled_polished_blackstone", BlockID.CHISELED_POLISHED_BLACKSTONE);
    public static final BlockLegacy CHISELED_TUFF = REGISTRY.registerBlock("minecraft:chiseled_tuff", BlockID.CHISELED_TUFF);
    public static final BlockLegacy CHISELED_TUFF_BRICKS = REGISTRY.registerBlock("minecraft:chiseled_tuff_bricks", BlockID.CHISELED_TUFF_BRICKS);
    public static final BlockLegacy CHORUS_FLOWER = REGISTRY.registerBlock("minecraft:chorus_flower", BlockID.CHORUS_FLOWER)
            .addState(BlockStates.AGE, BlockStateIntegerValues.CHORUS_FLOWER_MAX_AGE + 1);
    public static final BlockLegacy CHORUS_PLANT = REGISTRY.registerBlock("minecraft:chorus_plant", BlockID.CHORUS_PLANT);
    public static final BlockLegacy CLAY = REGISTRY.registerBlock("minecraft:clay", BlockID.CLAY);
    public static final BlockLegacy CLIENT_REQUEST_PLACEHOLDER_BLOCK = REGISTRY.registerBlock("minecraft:client_request_placeholder_block", BlockID.CLIENT_REQUEST_PLACEHOLDER_BLOCK);
    public static final BlockLegacy COAL_BLOCK = REGISTRY.registerBlock("minecraft:coal_block", BlockID.COAL_BLOCK);
    public static final BlockLegacy COAL_ORE = REGISTRY.registerBlock("minecraft:coal_ore", BlockID.COAL_ORE);
    public static final BlockLegacy COBBLED_DEEPSLATE = REGISTRY.registerBlock("minecraft:cobbled_deepslate", BlockID.COBBLED_DEEPSLATE);
    public static final BlockLegacy COBBLED_DEEPSLATE_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:cobbled_deepslate_double_slab", BlockID.COBBLED_DEEPSLATE_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy COBBLED_DEEPSLATE_SLAB = REGISTRY.registerBlock("minecraft:cobbled_deepslate_slab", BlockID.COBBLED_DEEPSLATE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy COBBLED_DEEPSLATE_STAIRS = REGISTRY.registerBlock("minecraft:cobbled_deepslate_stairs", BlockID.COBBLED_DEEPSLATE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy COBBLED_DEEPSLATE_WALL = REGISTRY.registerBlock("minecraft:cobbled_deepslate_wall", BlockID.COBBLED_DEEPSLATE_WALL)
            .addState(BlockStates.WALL_POST_BIT)
            .addState(BlockStates.WALL_CONNECTION_TYPE_NORTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_EAST)
            .addState(BlockStates.WALL_CONNECTION_TYPE_SOUTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_WEST);
    public static final BlockLegacy COBBLESTONE = REGISTRY.registerBlock("minecraft:cobblestone", BlockID.COBBLESTONE);
    public static final BlockLegacy COBBLESTONE_SLAB = REGISTRY.registerBlock("minecraft:cobblestone_slab", BlockID.COBBLESTONE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy COBBLESTONE_WALL = REGISTRY.registerBlock("minecraft:cobblestone_wall", BlockID.COBBLESTONE_WALL)
            .addState(BlockStates.WALL_BLOCK_TYPE)
            .addState(BlockStates.WALL_POST_BIT)
            .addState(BlockStates.WALL_CONNECTION_TYPE_NORTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_EAST)
            .addState(BlockStates.WALL_CONNECTION_TYPE_SOUTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_WEST);
    public static final BlockLegacy COCOA = REGISTRY.registerBlock("minecraft:cocoa", BlockID.COCOA)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.AGE, BlockStateIntegerValues.COCOA_MAX_AGE + 1);
    public static final BlockLegacy COLORED_TORCH_BP = REGISTRY.registerBlock("minecraft:colored_torch_bp", BlockID.COLORED_TORCH_BP)
            .addState(BlockStates.TORCH_FACING_DIRECTION)
            .addState(BlockStates.COLOR_BIT);
    public static final BlockLegacy COLORED_TORCH_RG = REGISTRY.registerBlock("minecraft:colored_torch_rg", BlockID.COLORED_TORCH_RG)
            .addState(BlockStates.TORCH_FACING_DIRECTION)
            .addState(BlockStates.COLOR_BIT);
    public static final BlockLegacy COMMAND_BLOCK = REGISTRY.registerBlock("minecraft:command_block", BlockID.COMMAND_BLOCK)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.CONDITIONAL_BIT);
    public static final BlockLegacy COMPOSTER = REGISTRY.registerBlock("minecraft:composter", BlockID.COMPOSTER)
            .addState(BlockStates.COMPOSTER_FILL_LEVEL);
    public static final BlockLegacy CONDUIT = REGISTRY.registerBlock("minecraft:conduit", BlockID.CONDUIT);
    public static final BlockLegacy COPPER_BLOCK = REGISTRY.registerBlock("minecraft:copper_block", BlockID.COPPER_BLOCK);
    public static final BlockLegacy COPPER_BULB = REGISTRY.registerBlock("minecraft:copper_bulb", BlockID.COPPER_BULB)
            .addState(BlockStates.LIT)
            .addState(BlockStates.POWERED_BIT);
    public static final BlockLegacy COPPER_DOOR = REGISTRY.registerBlock("minecraft:copper_door", BlockID.COPPER_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy COPPER_GRATE = REGISTRY.registerBlock("minecraft:copper_grate", BlockID.COPPER_GRATE);
    public static final BlockLegacy COPPER_ORE = REGISTRY.registerBlock("minecraft:copper_ore", BlockID.COPPER_ORE);
    public static final BlockLegacy COPPER_TRAPDOOR = REGISTRY.registerBlock("minecraft:copper_trapdoor", BlockID.COPPER_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy CORAL_FAN_HANG = REGISTRY.registerBlock("minecraft:coral_fan_hang", BlockID.CORAL_FAN_HANG)
            .addState(BlockStates.CORAL_HANG_TYPE_BIT)
            .addState(BlockStates.DEAD_BIT)
            .addState(BlockStates.CORAL_DIRECTION);
    public static final BlockLegacy CORAL_FAN_HANG2 = REGISTRY.registerBlock("minecraft:coral_fan_hang2", BlockID.CORAL_FAN_HANG2)
            .addState(BlockStates.CORAL_HANG_TYPE_BIT)
            .addState(BlockStates.DEAD_BIT)
            .addState(BlockStates.CORAL_DIRECTION);
    public static final BlockLegacy CORAL_FAN_HANG3 = REGISTRY.registerBlock("minecraft:coral_fan_hang3", BlockID.CORAL_FAN_HANG3)
            .addState(BlockStates.CORAL_HANG_TYPE_BIT)
            .addState(BlockStates.DEAD_BIT)
            .addState(BlockStates.CORAL_DIRECTION);
    public static final BlockLegacy CORNFLOWER = REGISTRY.registerBlock("minecraft:cornflower", BlockID.CORNFLOWER);
    public static final BlockLegacy CRACKED_DEEPSLATE_BRICKS = REGISTRY.registerBlock("minecraft:cracked_deepslate_bricks", BlockID.CRACKED_DEEPSLATE_BRICKS);
    public static final BlockLegacy CRACKED_DEEPSLATE_TILES = REGISTRY.registerBlock("minecraft:cracked_deepslate_tiles", BlockID.CRACKED_DEEPSLATE_TILES);
    public static final BlockLegacy CRACKED_NETHER_BRICKS = REGISTRY.registerBlock("minecraft:cracked_nether_bricks", BlockID.CRACKED_NETHER_BRICKS);
    public static final BlockLegacy CRACKED_POLISHED_BLACKSTONE_BRICKS = REGISTRY.registerBlock("minecraft:cracked_polished_blackstone_bricks", BlockID.CRACKED_POLISHED_BLACKSTONE_BRICKS);
    public static final BlockLegacy CRAFTER = REGISTRY.registerBlock("minecraft:crafter", BlockID.CRAFTER)
            .addState(BlockStates.ORIENTATION)
            .addState(BlockStates.TRIGGERED_BIT)
            .addState(BlockStates.CRAFTING);
    public static final BlockLegacy CRAFTING_TABLE = REGISTRY.registerBlock("minecraft:crafting_table", BlockID.CRAFTING_TABLE);
    public static final BlockLegacy CRIMSON_BUTTON = REGISTRY.registerBlock("minecraft:crimson_button", BlockID.CRIMSON_BUTTON)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.BUTTON_PRESSED_BIT);
    public static final BlockLegacy CRIMSON_DOOR = REGISTRY.registerBlock("minecraft:crimson_door", BlockID.BLOCK_CRIMSON_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy CRIMSON_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:crimson_double_slab", BlockID.CRIMSON_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy CRIMSON_FENCE = REGISTRY.registerBlock("minecraft:crimson_fence", BlockID.CRIMSON_FENCE);
    public static final BlockLegacy CRIMSON_FENCE_GATE = REGISTRY.registerBlock("minecraft:crimson_fence_gate", BlockID.CRIMSON_FENCE_GATE)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.IN_WALL_BIT);
    public static final BlockLegacy CRIMSON_FUNGUS = REGISTRY.registerBlock("minecraft:crimson_fungus", BlockID.CRIMSON_FUNGUS);
    public static final BlockLegacy CRIMSON_HANGING_SIGN = REGISTRY.registerBlock("minecraft:crimson_hanging_sign", BlockID.CRIMSON_HANGING_SIGN)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.GROUND_SIGN_DIRECTION)
            .addState(BlockStates.HANGING)
            .addState(BlockStates.ATTACHED_BIT);
    public static final BlockLegacy CRIMSON_HYPHAE = REGISTRY.registerBlock("minecraft:crimson_hyphae", BlockID.CRIMSON_HYPHAE)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy CRIMSON_NYLIUM = REGISTRY.registerBlock("minecraft:crimson_nylium", BlockID.CRIMSON_NYLIUM);
    public static final BlockLegacy CRIMSON_PLANKS = REGISTRY.registerBlock("minecraft:crimson_planks", BlockID.CRIMSON_PLANKS);
    public static final BlockLegacy CRIMSON_PRESSURE_PLATE = REGISTRY.registerBlock("minecraft:crimson_pressure_plate", BlockID.CRIMSON_PRESSURE_PLATE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy CRIMSON_ROOTS = REGISTRY.registerBlock("minecraft:crimson_roots", BlockID.CRIMSON_ROOTS);
    public static final BlockLegacy CRIMSON_SLAB = REGISTRY.registerBlock("minecraft:crimson_slab", BlockID.CRIMSON_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy CRIMSON_STAIRS = REGISTRY.registerBlock("minecraft:crimson_stairs", BlockID.CRIMSON_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy CRIMSON_STANDING_SIGN = REGISTRY.registerBlock("minecraft:crimson_standing_sign", BlockID.CRIMSON_STANDING_SIGN)
            .addState(BlockStates.GROUND_SIGN_DIRECTION);
    public static final BlockLegacy CRIMSON_STEM = REGISTRY.registerBlock("minecraft:crimson_stem", BlockID.CRIMSON_STEM)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy CRIMSON_TRAPDOOR = REGISTRY.registerBlock("minecraft:crimson_trapdoor", BlockID.CRIMSON_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy CRIMSON_WALL_SIGN = REGISTRY.registerBlock("minecraft:crimson_wall_sign", BlockID.CRIMSON_WALL_SIGN)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy CRYING_OBSIDIAN = REGISTRY.registerBlock("minecraft:crying_obsidian", BlockID.CRYING_OBSIDIAN);
    public static final BlockLegacy CUT_COPPER = REGISTRY.registerBlock("minecraft:cut_copper", BlockID.CUT_COPPER);
    public static final BlockLegacy CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:cut_copper_slab", BlockID.CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy CUT_COPPER_STAIRS = REGISTRY.registerBlock("minecraft:cut_copper_stairs", BlockID.CUT_COPPER_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy CYAN_CANDLE = REGISTRY.registerBlock("minecraft:cyan_candle", BlockID.CYAN_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy CYAN_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:cyan_candle_cake", BlockID.CYAN_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy CYAN_CARPET = REGISTRY.registerBlock("minecraft:cyan_carpet", BlockID.CYAN_CARPET);
    public static final BlockLegacy CYAN_CONCRETE = REGISTRY.registerBlock("minecraft:cyan_concrete", BlockID.CYAN_CONCRETE);
    public static final BlockLegacy CYAN_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:cyan_concrete_powder", BlockID.CYAN_CONCRETE_POWDER);
    public static final BlockLegacy CYAN_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:cyan_glazed_terracotta", BlockID.CYAN_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy CYAN_SHULKER_BOX = REGISTRY.registerBlock("minecraft:cyan_shulker_box", BlockID.CYAN_SHULKER_BOX);
    public static final BlockLegacy CYAN_STAINED_GLASS = REGISTRY.registerBlock("minecraft:cyan_stained_glass", BlockID.CYAN_STAINED_GLASS);
    public static final BlockLegacy CYAN_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:cyan_stained_glass_pane", BlockID.CYAN_STAINED_GLASS_PANE);
    public static final BlockLegacy CYAN_TERRACOTTA = REGISTRY.registerBlock("minecraft:cyan_terracotta", BlockID.CYAN_TERRACOTTA);
    public static final BlockLegacy CYAN_WOOL = REGISTRY.registerBlock("minecraft:cyan_wool", BlockID.CYAN_WOOL);
    public static final BlockLegacy DARK_OAK_BUTTON = REGISTRY.registerBlock("minecraft:dark_oak_button", BlockID.DARK_OAK_BUTTON)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.BUTTON_PRESSED_BIT);
    public static final BlockLegacy DARK_OAK_DOOR = REGISTRY.registerBlock("minecraft:dark_oak_door", BlockID.BLOCK_DARK_OAK_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy DARK_OAK_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:dark_oak_double_slab", BlockID.DARK_OAK_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy DARK_OAK_FENCE = REGISTRY.registerBlock("minecraft:dark_oak_fence", BlockID.DARK_OAK_FENCE);
    public static final BlockLegacy DARK_OAK_FENCE_GATE = REGISTRY.registerBlock("minecraft:dark_oak_fence_gate", BlockID.DARK_OAK_FENCE_GATE)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.IN_WALL_BIT);
    public static final BlockLegacy DARK_OAK_HANGING_SIGN = REGISTRY.registerBlock("minecraft:dark_oak_hanging_sign", BlockID.DARK_OAK_HANGING_SIGN)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.GROUND_SIGN_DIRECTION)
            .addState(BlockStates.HANGING)
            .addState(BlockStates.ATTACHED_BIT);
    public static final BlockLegacy DARK_OAK_LEAVES = REGISTRY.registerBlock("minecraft:dark_oak_leaves", BlockID.DARK_OAK_LEAVES)
            .addState(BlockStates.UPDATE_BIT)
            .addState(BlockStates.PERSISTENT_BIT);
    public static final BlockLegacy DARK_OAK_LOG = REGISTRY.registerBlock("minecraft:dark_oak_log", BlockID.DARK_OAK_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy DARK_OAK_PLANKS = REGISTRY.registerBlock("minecraft:dark_oak_planks", BlockID.DARK_OAK_PLANKS);
    public static final BlockLegacy DARK_OAK_PRESSURE_PLATE = REGISTRY.registerBlock("minecraft:dark_oak_pressure_plate", BlockID.DARK_OAK_PRESSURE_PLATE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy DARK_OAK_SAPLING = REGISTRY.registerBlock("minecraft:dark_oak_sapling", BlockID.DARK_OAK_SAPLING)
            .addState(BlockStates.AGE_BIT);
    public static final BlockLegacy DARK_OAK_SLAB = REGISTRY.registerBlock("minecraft:dark_oak_slab", BlockID.DARK_OAK_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy DARK_OAK_STAIRS = REGISTRY.registerBlock("minecraft:dark_oak_stairs", BlockID.DARK_OAK_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy DARK_OAK_TRAPDOOR = REGISTRY.registerBlock("minecraft:dark_oak_trapdoor", BlockID.DARK_OAK_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy DARK_OAK_WOOD = REGISTRY.registerBlock("minecraft:dark_oak_wood", BlockID.DARK_OAK_WOOD)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy DARK_PRISMARINE_STAIRS = REGISTRY.registerBlock("minecraft:dark_prismarine_stairs", BlockID.DARK_PRISMARINE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy DARKOAK_STANDING_SIGN = REGISTRY.registerBlock("minecraft:darkoak_standing_sign", BlockID.DARKOAK_STANDING_SIGN)
            .addState(BlockStates.GROUND_SIGN_DIRECTION);
    public static final BlockLegacy DARKOAK_WALL_SIGN = REGISTRY.registerBlock("minecraft:darkoak_wall_sign", BlockID.DARKOAK_WALL_SIGN)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy DAYLIGHT_DETECTOR = REGISTRY.registerBlock("minecraft:daylight_detector", BlockID.DAYLIGHT_DETECTOR)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy DAYLIGHT_DETECTOR_INVERTED = REGISTRY.registerBlock("minecraft:daylight_detector_inverted", BlockID.DAYLIGHT_DETECTOR_INVERTED)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy DEAD_BRAIN_CORAL = REGISTRY.registerBlock("minecraft:dead_brain_coral", BlockID.DEAD_BRAIN_CORAL);
    public static final BlockLegacy DEAD_BRAIN_CORAL_BLOCK = REGISTRY.registerBlock("minecraft:dead_brain_coral_block", BlockID.DEAD_BRAIN_CORAL_BLOCK);
    public static final BlockLegacy DEAD_BRAIN_CORAL_FAN = REGISTRY.registerBlock("minecraft:dead_brain_coral_fan", BlockID.DEAD_BRAIN_CORAL_FAN)
            .addState(BlockStates.CORAL_FAN_DIRECTION);
    public static final BlockLegacy DEAD_BUBBLE_CORAL = REGISTRY.registerBlock("minecraft:dead_bubble_coral", BlockID.DEAD_BUBBLE_CORAL);
    public static final BlockLegacy DEAD_BUBBLE_CORAL_BLOCK = REGISTRY.registerBlock("minecraft:dead_bubble_coral_block", BlockID.DEAD_BUBBLE_CORAL_BLOCK);
    public static final BlockLegacy DEAD_BUBBLE_CORAL_FAN = REGISTRY.registerBlock("minecraft:dead_bubble_coral_fan", BlockID.DEAD_BUBBLE_CORAL_FAN)
            .addState(BlockStates.CORAL_FAN_DIRECTION);
    public static final BlockLegacy DEAD_FIRE_CORAL = REGISTRY.registerBlock("minecraft:dead_fire_coral", BlockID.DEAD_FIRE_CORAL);
    public static final BlockLegacy DEAD_FIRE_CORAL_BLOCK = REGISTRY.registerBlock("minecraft:dead_fire_coral_block", BlockID.DEAD_FIRE_CORAL_BLOCK);
    public static final BlockLegacy DEAD_FIRE_CORAL_FAN = REGISTRY.registerBlock("minecraft:dead_fire_coral_fan", BlockID.DEAD_FIRE_CORAL_FAN)
            .addState(BlockStates.CORAL_FAN_DIRECTION);
    public static final BlockLegacy DEAD_HORN_CORAL = REGISTRY.registerBlock("minecraft:dead_horn_coral", BlockID.DEAD_HORN_CORAL);
    public static final BlockLegacy DEAD_HORN_CORAL_BLOCK = REGISTRY.registerBlock("minecraft:dead_horn_coral_block", BlockID.DEAD_HORN_CORAL_BLOCK);
    public static final BlockLegacy DEAD_HORN_CORAL_FAN = REGISTRY.registerBlock("minecraft:dead_horn_coral_fan", BlockID.DEAD_HORN_CORAL_FAN)
            .addState(BlockStates.CORAL_FAN_DIRECTION);
    public static final BlockLegacy DEAD_TUBE_CORAL = REGISTRY.registerBlock("minecraft:dead_tube_coral", BlockID.DEAD_TUBE_CORAL);
    public static final BlockLegacy DEAD_TUBE_CORAL_BLOCK = REGISTRY.registerBlock("minecraft:dead_tube_coral_block", BlockID.DEAD_TUBE_CORAL_BLOCK);
    public static final BlockLegacy DEAD_TUBE_CORAL_FAN = REGISTRY.registerBlock("minecraft:dead_tube_coral_fan", BlockID.DEAD_TUBE_CORAL_FAN)
            .addState(BlockStates.CORAL_FAN_DIRECTION);
    public static final BlockLegacy DEADBUSH = REGISTRY.registerBlock("minecraft:deadbush", BlockID.DEADBUSH);
    public static final BlockLegacy DECORATED_POT = REGISTRY.registerBlock("minecraft:decorated_pot", BlockID.DECORATED_POT)
            .addState(BlockStates.DIRECTION);
    public static final BlockLegacy DEEPSLATE = REGISTRY.registerBlock("minecraft:deepslate", BlockID.DEEPSLATE)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy DEEPSLATE_BRICK_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:deepslate_brick_double_slab", BlockID.DEEPSLATE_BRICK_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy DEEPSLATE_BRICK_SLAB = REGISTRY.registerBlock("minecraft:deepslate_brick_slab", BlockID.DEEPSLATE_BRICK_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy DEEPSLATE_BRICK_STAIRS = REGISTRY.registerBlock("minecraft:deepslate_brick_stairs", BlockID.DEEPSLATE_BRICK_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy DEEPSLATE_BRICK_WALL = REGISTRY.registerBlock("minecraft:deepslate_brick_wall", BlockID.DEEPSLATE_BRICK_WALL)
            .addState(BlockStates.WALL_POST_BIT)
            .addState(BlockStates.WALL_CONNECTION_TYPE_NORTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_EAST)
            .addState(BlockStates.WALL_CONNECTION_TYPE_SOUTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_WEST);
    public static final BlockLegacy DEEPSLATE_BRICKS = REGISTRY.registerBlock("minecraft:deepslate_bricks", BlockID.DEEPSLATE_BRICKS);
    public static final BlockLegacy DEEPSLATE_COAL_ORE = REGISTRY.registerBlock("minecraft:deepslate_coal_ore", BlockID.DEEPSLATE_COAL_ORE);
    public static final BlockLegacy DEEPSLATE_COPPER_ORE = REGISTRY.registerBlock("minecraft:deepslate_copper_ore", BlockID.DEEPSLATE_COPPER_ORE);
    public static final BlockLegacy DEEPSLATE_DIAMOND_ORE = REGISTRY.registerBlock("minecraft:deepslate_diamond_ore", BlockID.DEEPSLATE_DIAMOND_ORE);
    public static final BlockLegacy DEEPSLATE_EMERALD_ORE = REGISTRY.registerBlock("minecraft:deepslate_emerald_ore", BlockID.DEEPSLATE_EMERALD_ORE);
    public static final BlockLegacy DEEPSLATE_GOLD_ORE = REGISTRY.registerBlock("minecraft:deepslate_gold_ore", BlockID.DEEPSLATE_GOLD_ORE);
    public static final BlockLegacy DEEPSLATE_IRON_ORE = REGISTRY.registerBlock("minecraft:deepslate_iron_ore", BlockID.DEEPSLATE_IRON_ORE);
    public static final BlockLegacy DEEPSLATE_LAPIS_ORE = REGISTRY.registerBlock("minecraft:deepslate_lapis_ore", BlockID.DEEPSLATE_LAPIS_ORE);
    public static final BlockLegacy DEEPSLATE_REDSTONE_ORE = REGISTRY.registerBlock("minecraft:deepslate_redstone_ore", BlockID.DEEPSLATE_REDSTONE_ORE);
    public static final BlockLegacy DEEPSLATE_TILE_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:deepslate_tile_double_slab", BlockID.DEEPSLATE_TILE_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy DEEPSLATE_TILE_SLAB = REGISTRY.registerBlock("minecraft:deepslate_tile_slab", BlockID.DEEPSLATE_TILE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy DEEPSLATE_TILE_STAIRS = REGISTRY.registerBlock("minecraft:deepslate_tile_stairs", BlockID.DEEPSLATE_TILE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy DEEPSLATE_TILE_WALL = REGISTRY.registerBlock("minecraft:deepslate_tile_wall", BlockID.DEEPSLATE_TILE_WALL)
            .addState(BlockStates.WALL_POST_BIT)
            .addState(BlockStates.WALL_CONNECTION_TYPE_NORTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_EAST)
            .addState(BlockStates.WALL_CONNECTION_TYPE_SOUTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_WEST);
    public static final BlockLegacy DEEPSLATE_TILES = REGISTRY.registerBlock("minecraft:deepslate_tiles", BlockID.DEEPSLATE_TILES);
    public static final BlockLegacy DENY = REGISTRY.registerBlock("minecraft:deny", BlockID.DENY);
    public static final BlockLegacy DETECTOR_RAIL = REGISTRY.registerBlock("minecraft:detector_rail", BlockID.DETECTOR_RAIL)
            .addState(BlockStates.RAIL_DIRECTION, BlockStateIntegerValues.DETECTOR_RAIL_MAX_RAIL_DIRECTION + 1)
            .addState(BlockStates.RAIL_DATA_BIT);
    public static final BlockLegacy DIAMOND_BLOCK = REGISTRY.registerBlock("minecraft:diamond_block", BlockID.DIAMOND_BLOCK);
    public static final BlockLegacy DIAMOND_ORE = REGISTRY.registerBlock("minecraft:diamond_ore", BlockID.DIAMOND_ORE);
    public static final BlockLegacy DIORITE = REGISTRY.registerBlock("minecraft:diorite", BlockID.DIORITE);
    public static final BlockLegacy DIORITE_STAIRS = REGISTRY.registerBlock("minecraft:diorite_stairs", BlockID.DIORITE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy DIRT = REGISTRY.registerBlock("minecraft:dirt", BlockID.DIRT)
            .addState(BlockStates.DIRT_TYPE);
    public static final BlockLegacy DIRT_WITH_ROOTS = REGISTRY.registerBlock("minecraft:dirt_with_roots", BlockID.DIRT_WITH_ROOTS);
    public static final BlockLegacy DISPENSER = REGISTRY.registerBlock("minecraft:dispenser", BlockID.DISPENSER)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.TRIGGERED_BIT);
    public static final BlockLegacy DOUBLE_CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:double_cut_copper_slab", BlockID.DOUBLE_CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy DOUBLE_STONE_BLOCK_SLAB = REGISTRY.registerBlock("minecraft:double_stone_block_slab", BlockID.DOUBLE_STONE_BLOCK_SLAB)
            .addState(BlockStates.STONE_SLAB_TYPE)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy DOUBLE_STONE_BLOCK_SLAB2 = REGISTRY.registerBlock("minecraft:double_stone_block_slab2", BlockID.DOUBLE_STONE_BLOCK_SLAB2)
            .addState(BlockStates.STONE_SLAB_TYPE_2)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy DOUBLE_STONE_BLOCK_SLAB3 = REGISTRY.registerBlock("minecraft:double_stone_block_slab3", BlockID.DOUBLE_STONE_BLOCK_SLAB3)
            .addState(BlockStates.STONE_SLAB_TYPE_3)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy DOUBLE_STONE_BLOCK_SLAB4 = REGISTRY.registerBlock("minecraft:double_stone_block_slab4", BlockID.DOUBLE_STONE_BLOCK_SLAB4)
            .addState(BlockStates.STONE_SLAB_TYPE_4)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy DRAGON_EGG = REGISTRY.registerBlock("minecraft:dragon_egg", BlockID.DRAGON_EGG);
    public static final BlockLegacy DRIED_KELP_BLOCK = REGISTRY.registerBlock("minecraft:dried_kelp_block", BlockID.DRIED_KELP_BLOCK);
    public static final BlockLegacy DRIPSTONE_BLOCK = REGISTRY.registerBlock("minecraft:dripstone_block", BlockID.DRIPSTONE_BLOCK);
    public static final BlockLegacy DROPPER = REGISTRY.registerBlock("minecraft:dropper", BlockID.DROPPER)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.TRIGGERED_BIT);
    public static final BlockLegacy ELEMENT_0 = REGISTRY.registerBlock("minecraft:element_0", BlockID.ELEMENT_0);
    public static final BlockLegacy ELEMENT_1 = REGISTRY.registerBlock("minecraft:element_1", BlockID.ELEMENT_1);
    public static final BlockLegacy ELEMENT_10 = REGISTRY.registerBlock("minecraft:element_10", BlockID.ELEMENT_10);
    public static final BlockLegacy ELEMENT_100 = REGISTRY.registerBlock("minecraft:element_100", BlockID.ELEMENT_100);
    public static final BlockLegacy ELEMENT_101 = REGISTRY.registerBlock("minecraft:element_101", BlockID.ELEMENT_101);
    public static final BlockLegacy ELEMENT_102 = REGISTRY.registerBlock("minecraft:element_102", BlockID.ELEMENT_102);
    public static final BlockLegacy ELEMENT_103 = REGISTRY.registerBlock("minecraft:element_103", BlockID.ELEMENT_103);
    public static final BlockLegacy ELEMENT_104 = REGISTRY.registerBlock("minecraft:element_104", BlockID.ELEMENT_104);
    public static final BlockLegacy ELEMENT_105 = REGISTRY.registerBlock("minecraft:element_105", BlockID.ELEMENT_105);
    public static final BlockLegacy ELEMENT_106 = REGISTRY.registerBlock("minecraft:element_106", BlockID.ELEMENT_106);
    public static final BlockLegacy ELEMENT_107 = REGISTRY.registerBlock("minecraft:element_107", BlockID.ELEMENT_107);
    public static final BlockLegacy ELEMENT_108 = REGISTRY.registerBlock("minecraft:element_108", BlockID.ELEMENT_108);
    public static final BlockLegacy ELEMENT_109 = REGISTRY.registerBlock("minecraft:element_109", BlockID.ELEMENT_109);
    public static final BlockLegacy ELEMENT_11 = REGISTRY.registerBlock("minecraft:element_11", BlockID.ELEMENT_11);
    public static final BlockLegacy ELEMENT_110 = REGISTRY.registerBlock("minecraft:element_110", BlockID.ELEMENT_110);
    public static final BlockLegacy ELEMENT_111 = REGISTRY.registerBlock("minecraft:element_111", BlockID.ELEMENT_111);
    public static final BlockLegacy ELEMENT_112 = REGISTRY.registerBlock("minecraft:element_112", BlockID.ELEMENT_112);
    public static final BlockLegacy ELEMENT_113 = REGISTRY.registerBlock("minecraft:element_113", BlockID.ELEMENT_113);
    public static final BlockLegacy ELEMENT_114 = REGISTRY.registerBlock("minecraft:element_114", BlockID.ELEMENT_114);
    public static final BlockLegacy ELEMENT_115 = REGISTRY.registerBlock("minecraft:element_115", BlockID.ELEMENT_115);
    public static final BlockLegacy ELEMENT_116 = REGISTRY.registerBlock("minecraft:element_116", BlockID.ELEMENT_116);
    public static final BlockLegacy ELEMENT_117 = REGISTRY.registerBlock("minecraft:element_117", BlockID.ELEMENT_117);
    public static final BlockLegacy ELEMENT_118 = REGISTRY.registerBlock("minecraft:element_118", BlockID.ELEMENT_118);
    public static final BlockLegacy ELEMENT_12 = REGISTRY.registerBlock("minecraft:element_12", BlockID.ELEMENT_12);
    public static final BlockLegacy ELEMENT_13 = REGISTRY.registerBlock("minecraft:element_13", BlockID.ELEMENT_13);
    public static final BlockLegacy ELEMENT_14 = REGISTRY.registerBlock("minecraft:element_14", BlockID.ELEMENT_14);
    public static final BlockLegacy ELEMENT_15 = REGISTRY.registerBlock("minecraft:element_15", BlockID.ELEMENT_15);
    public static final BlockLegacy ELEMENT_16 = REGISTRY.registerBlock("minecraft:element_16", BlockID.ELEMENT_16);
    public static final BlockLegacy ELEMENT_17 = REGISTRY.registerBlock("minecraft:element_17", BlockID.ELEMENT_17);
    public static final BlockLegacy ELEMENT_18 = REGISTRY.registerBlock("minecraft:element_18", BlockID.ELEMENT_18);
    public static final BlockLegacy ELEMENT_19 = REGISTRY.registerBlock("minecraft:element_19", BlockID.ELEMENT_19);
    public static final BlockLegacy ELEMENT_2 = REGISTRY.registerBlock("minecraft:element_2", BlockID.ELEMENT_2);
    public static final BlockLegacy ELEMENT_20 = REGISTRY.registerBlock("minecraft:element_20", BlockID.ELEMENT_20);
    public static final BlockLegacy ELEMENT_21 = REGISTRY.registerBlock("minecraft:element_21", BlockID.ELEMENT_21);
    public static final BlockLegacy ELEMENT_22 = REGISTRY.registerBlock("minecraft:element_22", BlockID.ELEMENT_22);
    public static final BlockLegacy ELEMENT_23 = REGISTRY.registerBlock("minecraft:element_23", BlockID.ELEMENT_23);
    public static final BlockLegacy ELEMENT_24 = REGISTRY.registerBlock("minecraft:element_24", BlockID.ELEMENT_24);
    public static final BlockLegacy ELEMENT_25 = REGISTRY.registerBlock("minecraft:element_25", BlockID.ELEMENT_25);
    public static final BlockLegacy ELEMENT_26 = REGISTRY.registerBlock("minecraft:element_26", BlockID.ELEMENT_26);
    public static final BlockLegacy ELEMENT_27 = REGISTRY.registerBlock("minecraft:element_27", BlockID.ELEMENT_27);
    public static final BlockLegacy ELEMENT_28 = REGISTRY.registerBlock("minecraft:element_28", BlockID.ELEMENT_28);
    public static final BlockLegacy ELEMENT_29 = REGISTRY.registerBlock("minecraft:element_29", BlockID.ELEMENT_29);
    public static final BlockLegacy ELEMENT_3 = REGISTRY.registerBlock("minecraft:element_3", BlockID.ELEMENT_3);
    public static final BlockLegacy ELEMENT_30 = REGISTRY.registerBlock("minecraft:element_30", BlockID.ELEMENT_30);
    public static final BlockLegacy ELEMENT_31 = REGISTRY.registerBlock("minecraft:element_31", BlockID.ELEMENT_31);
    public static final BlockLegacy ELEMENT_32 = REGISTRY.registerBlock("minecraft:element_32", BlockID.ELEMENT_32);
    public static final BlockLegacy ELEMENT_33 = REGISTRY.registerBlock("minecraft:element_33", BlockID.ELEMENT_33);
    public static final BlockLegacy ELEMENT_34 = REGISTRY.registerBlock("minecraft:element_34", BlockID.ELEMENT_34);
    public static final BlockLegacy ELEMENT_35 = REGISTRY.registerBlock("minecraft:element_35", BlockID.ELEMENT_35);
    public static final BlockLegacy ELEMENT_36 = REGISTRY.registerBlock("minecraft:element_36", BlockID.ELEMENT_36);
    public static final BlockLegacy ELEMENT_37 = REGISTRY.registerBlock("minecraft:element_37", BlockID.ELEMENT_37);
    public static final BlockLegacy ELEMENT_38 = REGISTRY.registerBlock("minecraft:element_38", BlockID.ELEMENT_38);
    public static final BlockLegacy ELEMENT_39 = REGISTRY.registerBlock("minecraft:element_39", BlockID.ELEMENT_39);
    public static final BlockLegacy ELEMENT_4 = REGISTRY.registerBlock("minecraft:element_4", BlockID.ELEMENT_4);
    public static final BlockLegacy ELEMENT_40 = REGISTRY.registerBlock("minecraft:element_40", BlockID.ELEMENT_40);
    public static final BlockLegacy ELEMENT_41 = REGISTRY.registerBlock("minecraft:element_41", BlockID.ELEMENT_41);
    public static final BlockLegacy ELEMENT_42 = REGISTRY.registerBlock("minecraft:element_42", BlockID.ELEMENT_42);
    public static final BlockLegacy ELEMENT_43 = REGISTRY.registerBlock("minecraft:element_43", BlockID.ELEMENT_43);
    public static final BlockLegacy ELEMENT_44 = REGISTRY.registerBlock("minecraft:element_44", BlockID.ELEMENT_44);
    public static final BlockLegacy ELEMENT_45 = REGISTRY.registerBlock("minecraft:element_45", BlockID.ELEMENT_45);
    public static final BlockLegacy ELEMENT_46 = REGISTRY.registerBlock("minecraft:element_46", BlockID.ELEMENT_46);
    public static final BlockLegacy ELEMENT_47 = REGISTRY.registerBlock("minecraft:element_47", BlockID.ELEMENT_47);
    public static final BlockLegacy ELEMENT_48 = REGISTRY.registerBlock("minecraft:element_48", BlockID.ELEMENT_48);
    public static final BlockLegacy ELEMENT_49 = REGISTRY.registerBlock("minecraft:element_49", BlockID.ELEMENT_49);
    public static final BlockLegacy ELEMENT_5 = REGISTRY.registerBlock("minecraft:element_5", BlockID.ELEMENT_5);
    public static final BlockLegacy ELEMENT_50 = REGISTRY.registerBlock("minecraft:element_50", BlockID.ELEMENT_50);
    public static final BlockLegacy ELEMENT_51 = REGISTRY.registerBlock("minecraft:element_51", BlockID.ELEMENT_51);
    public static final BlockLegacy ELEMENT_52 = REGISTRY.registerBlock("minecraft:element_52", BlockID.ELEMENT_52);
    public static final BlockLegacy ELEMENT_53 = REGISTRY.registerBlock("minecraft:element_53", BlockID.ELEMENT_53);
    public static final BlockLegacy ELEMENT_54 = REGISTRY.registerBlock("minecraft:element_54", BlockID.ELEMENT_54);
    public static final BlockLegacy ELEMENT_55 = REGISTRY.registerBlock("minecraft:element_55", BlockID.ELEMENT_55);
    public static final BlockLegacy ELEMENT_56 = REGISTRY.registerBlock("minecraft:element_56", BlockID.ELEMENT_56);
    public static final BlockLegacy ELEMENT_57 = REGISTRY.registerBlock("minecraft:element_57", BlockID.ELEMENT_57);
    public static final BlockLegacy ELEMENT_58 = REGISTRY.registerBlock("minecraft:element_58", BlockID.ELEMENT_58);
    public static final BlockLegacy ELEMENT_59 = REGISTRY.registerBlock("minecraft:element_59", BlockID.ELEMENT_59);
    public static final BlockLegacy ELEMENT_6 = REGISTRY.registerBlock("minecraft:element_6", BlockID.ELEMENT_6);
    public static final BlockLegacy ELEMENT_60 = REGISTRY.registerBlock("minecraft:element_60", BlockID.ELEMENT_60);
    public static final BlockLegacy ELEMENT_61 = REGISTRY.registerBlock("minecraft:element_61", BlockID.ELEMENT_61);
    public static final BlockLegacy ELEMENT_62 = REGISTRY.registerBlock("minecraft:element_62", BlockID.ELEMENT_62);
    public static final BlockLegacy ELEMENT_63 = REGISTRY.registerBlock("minecraft:element_63", BlockID.ELEMENT_63);
    public static final BlockLegacy ELEMENT_64 = REGISTRY.registerBlock("minecraft:element_64", BlockID.ELEMENT_64);
    public static final BlockLegacy ELEMENT_65 = REGISTRY.registerBlock("minecraft:element_65", BlockID.ELEMENT_65);
    public static final BlockLegacy ELEMENT_66 = REGISTRY.registerBlock("minecraft:element_66", BlockID.ELEMENT_66);
    public static final BlockLegacy ELEMENT_67 = REGISTRY.registerBlock("minecraft:element_67", BlockID.ELEMENT_67);
    public static final BlockLegacy ELEMENT_68 = REGISTRY.registerBlock("minecraft:element_68", BlockID.ELEMENT_68);
    public static final BlockLegacy ELEMENT_69 = REGISTRY.registerBlock("minecraft:element_69", BlockID.ELEMENT_69);
    public static final BlockLegacy ELEMENT_7 = REGISTRY.registerBlock("minecraft:element_7", BlockID.ELEMENT_7);
    public static final BlockLegacy ELEMENT_70 = REGISTRY.registerBlock("minecraft:element_70", BlockID.ELEMENT_70);
    public static final BlockLegacy ELEMENT_71 = REGISTRY.registerBlock("minecraft:element_71", BlockID.ELEMENT_71);
    public static final BlockLegacy ELEMENT_72 = REGISTRY.registerBlock("minecraft:element_72", BlockID.ELEMENT_72);
    public static final BlockLegacy ELEMENT_73 = REGISTRY.registerBlock("minecraft:element_73", BlockID.ELEMENT_73);
    public static final BlockLegacy ELEMENT_74 = REGISTRY.registerBlock("minecraft:element_74", BlockID.ELEMENT_74);
    public static final BlockLegacy ELEMENT_75 = REGISTRY.registerBlock("minecraft:element_75", BlockID.ELEMENT_75);
    public static final BlockLegacy ELEMENT_76 = REGISTRY.registerBlock("minecraft:element_76", BlockID.ELEMENT_76);
    public static final BlockLegacy ELEMENT_77 = REGISTRY.registerBlock("minecraft:element_77", BlockID.ELEMENT_77);
    public static final BlockLegacy ELEMENT_78 = REGISTRY.registerBlock("minecraft:element_78", BlockID.ELEMENT_78);
    public static final BlockLegacy ELEMENT_79 = REGISTRY.registerBlock("minecraft:element_79", BlockID.ELEMENT_79);
    public static final BlockLegacy ELEMENT_8 = REGISTRY.registerBlock("minecraft:element_8", BlockID.ELEMENT_8);
    public static final BlockLegacy ELEMENT_80 = REGISTRY.registerBlock("minecraft:element_80", BlockID.ELEMENT_80);
    public static final BlockLegacy ELEMENT_81 = REGISTRY.registerBlock("minecraft:element_81", BlockID.ELEMENT_81);
    public static final BlockLegacy ELEMENT_82 = REGISTRY.registerBlock("minecraft:element_82", BlockID.ELEMENT_82);
    public static final BlockLegacy ELEMENT_83 = REGISTRY.registerBlock("minecraft:element_83", BlockID.ELEMENT_83);
    public static final BlockLegacy ELEMENT_84 = REGISTRY.registerBlock("minecraft:element_84", BlockID.ELEMENT_84);
    public static final BlockLegacy ELEMENT_85 = REGISTRY.registerBlock("minecraft:element_85", BlockID.ELEMENT_85);
    public static final BlockLegacy ELEMENT_86 = REGISTRY.registerBlock("minecraft:element_86", BlockID.ELEMENT_86);
    public static final BlockLegacy ELEMENT_87 = REGISTRY.registerBlock("minecraft:element_87", BlockID.ELEMENT_87);
    public static final BlockLegacy ELEMENT_88 = REGISTRY.registerBlock("minecraft:element_88", BlockID.ELEMENT_88);
    public static final BlockLegacy ELEMENT_89 = REGISTRY.registerBlock("minecraft:element_89", BlockID.ELEMENT_89);
    public static final BlockLegacy ELEMENT_9 = REGISTRY.registerBlock("minecraft:element_9", BlockID.ELEMENT_9);
    public static final BlockLegacy ELEMENT_90 = REGISTRY.registerBlock("minecraft:element_90", BlockID.ELEMENT_90);
    public static final BlockLegacy ELEMENT_91 = REGISTRY.registerBlock("minecraft:element_91", BlockID.ELEMENT_91);
    public static final BlockLegacy ELEMENT_92 = REGISTRY.registerBlock("minecraft:element_92", BlockID.ELEMENT_92);
    public static final BlockLegacy ELEMENT_93 = REGISTRY.registerBlock("minecraft:element_93", BlockID.ELEMENT_93);
    public static final BlockLegacy ELEMENT_94 = REGISTRY.registerBlock("minecraft:element_94", BlockID.ELEMENT_94);
    public static final BlockLegacy ELEMENT_95 = REGISTRY.registerBlock("minecraft:element_95", BlockID.ELEMENT_95);
    public static final BlockLegacy ELEMENT_96 = REGISTRY.registerBlock("minecraft:element_96", BlockID.ELEMENT_96);
    public static final BlockLegacy ELEMENT_97 = REGISTRY.registerBlock("minecraft:element_97", BlockID.ELEMENT_97);
    public static final BlockLegacy ELEMENT_98 = REGISTRY.registerBlock("minecraft:element_98", BlockID.ELEMENT_98);
    public static final BlockLegacy ELEMENT_99 = REGISTRY.registerBlock("minecraft:element_99", BlockID.ELEMENT_99);
    public static final BlockLegacy EMERALD_BLOCK = REGISTRY.registerBlock("minecraft:emerald_block", BlockID.EMERALD_BLOCK);
    public static final BlockLegacy EMERALD_ORE = REGISTRY.registerBlock("minecraft:emerald_ore", BlockID.EMERALD_ORE);
    public static final BlockLegacy ENCHANTING_TABLE = REGISTRY.registerBlock("minecraft:enchanting_table", BlockID.ENCHANTING_TABLE);
    public static final BlockLegacy END_BRICK_STAIRS = REGISTRY.registerBlock("minecraft:end_brick_stairs", BlockID.END_BRICK_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy END_BRICKS = REGISTRY.registerBlock("minecraft:end_bricks", BlockID.END_BRICKS);
    public static final BlockLegacy END_GATEWAY = REGISTRY.registerBlock("minecraft:end_gateway", BlockID.END_GATEWAY);
    public static final BlockLegacy END_PORTAL = REGISTRY.registerBlock("minecraft:end_portal", BlockID.END_PORTAL);
    public static final BlockLegacy END_PORTAL_FRAME = REGISTRY.registerBlock("minecraft:end_portal_frame", BlockID.END_PORTAL_FRAME)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION)
            .addState(BlockStates.END_PORTAL_EYE_BIT);
    public static final BlockLegacy END_ROD = REGISTRY.registerBlock("minecraft:end_rod", BlockID.END_ROD)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy END_STONE = REGISTRY.registerBlock("minecraft:end_stone", BlockID.END_STONE);
    public static final BlockLegacy ENDER_CHEST = REGISTRY.registerBlock("minecraft:ender_chest", BlockID.ENDER_CHEST)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy EXPOSED_CHISELED_COPPER = REGISTRY.registerBlock("minecraft:exposed_chiseled_copper", BlockID.EXPOSED_CHISELED_COPPER);
    public static final BlockLegacy EXPOSED_COPPER = REGISTRY.registerBlock("minecraft:exposed_copper", BlockID.EXPOSED_COPPER);
    public static final BlockLegacy EXPOSED_COPPER_BULB = REGISTRY.registerBlock("minecraft:exposed_copper_bulb", BlockID.EXPOSED_COPPER_BULB)
            .addState(BlockStates.LIT)
            .addState(BlockStates.POWERED_BIT);
    public static final BlockLegacy EXPOSED_COPPER_DOOR = REGISTRY.registerBlock("minecraft:exposed_copper_door", BlockID.EXPOSED_COPPER_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy EXPOSED_COPPER_GRATE = REGISTRY.registerBlock("minecraft:exposed_copper_grate", BlockID.EXPOSED_COPPER_GRATE);
    public static final BlockLegacy EXPOSED_COPPER_TRAPDOOR = REGISTRY.registerBlock("minecraft:exposed_copper_trapdoor", BlockID.EXPOSED_COPPER_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy EXPOSED_CUT_COPPER = REGISTRY.registerBlock("minecraft:exposed_cut_copper", BlockID.EXPOSED_CUT_COPPER);
    public static final BlockLegacy EXPOSED_CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:exposed_cut_copper_slab", BlockID.EXPOSED_CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy EXPOSED_CUT_COPPER_STAIRS = REGISTRY.registerBlock("minecraft:exposed_cut_copper_stairs", BlockID.EXPOSED_CUT_COPPER_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy EXPOSED_DOUBLE_CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:exposed_double_cut_copper_slab", BlockID.EXPOSED_DOUBLE_CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy FARMLAND = REGISTRY.registerBlock("minecraft:farmland", BlockID.FARMLAND)
            .addState(BlockStates.MOISTURIZED_AMOUNT);
    public static final BlockLegacy FENCE_GATE = REGISTRY.registerBlock("minecraft:fence_gate", BlockID.FENCE_GATE)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.IN_WALL_BIT);
    public static final BlockLegacy FERN = REGISTRY.registerBlock("minecraft:fern", BlockID.FERN);
    public static final BlockLegacy FIRE = REGISTRY.registerBlock("minecraft:fire", BlockID.FIRE)
            .addState(BlockStates.AGE);
    public static final BlockLegacy FIRE_CORAL = REGISTRY.registerBlock("minecraft:fire_coral", BlockID.FIRE_CORAL);
    public static final BlockLegacy FIRE_CORAL_BLOCK = REGISTRY.registerBlock("minecraft:fire_coral_block", BlockID.FIRE_CORAL_BLOCK);
    public static final BlockLegacy FIRE_CORAL_FAN = REGISTRY.registerBlock("minecraft:fire_coral_fan", BlockID.FIRE_CORAL_FAN)
            .addState(BlockStates.CORAL_FAN_DIRECTION);
    public static final BlockLegacy FLETCHING_TABLE = REGISTRY.registerBlock("minecraft:fletching_table", BlockID.FLETCHING_TABLE);
    public static final BlockLegacy FLOWER_POT = REGISTRY.registerBlock("minecraft:flower_pot", BlockID.BLOCK_FLOWER_POT)
            .addState(BlockStates.UPDATE_BIT);
    public static final BlockLegacy FLOWERING_AZALEA = REGISTRY.registerBlock("minecraft:flowering_azalea", BlockID.FLOWERING_AZALEA);
    public static final BlockLegacy FLOWING_LAVA = REGISTRY.registerBlock("minecraft:flowing_lava", BlockID.FLOWING_LAVA)
            .addState(BlockStates.LIQUID_DEPTH);
    public static final BlockLegacy FLOWING_WATER = REGISTRY.registerBlock("minecraft:flowing_water", BlockID.FLOWING_WATER)
            .addState(BlockStates.LIQUID_DEPTH);
    public static final BlockLegacy FRAME = REGISTRY.registerBlock("minecraft:frame", BlockID.BLOCK_FRAME)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.ITEM_FRAME_MAP_BIT)
            .addState(BlockStates.ITEM_FRAME_PHOTO_BIT);
    public static final BlockLegacy FROG_SPAWN = REGISTRY.registerBlock("minecraft:frog_spawn", BlockID.FROG_SPAWN);
    public static final BlockLegacy FROSTED_ICE = REGISTRY.registerBlock("minecraft:frosted_ice", BlockID.FROSTED_ICE)
            .addState(BlockStates.AGE, BlockStateIntegerValues.FROSTED_ICE_MAX_AGE + 1);
    public static final BlockLegacy FURNACE = REGISTRY.registerBlock("minecraft:furnace", BlockID.FURNACE)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy GILDED_BLACKSTONE = REGISTRY.registerBlock("minecraft:gilded_blackstone", BlockID.GILDED_BLACKSTONE);
    public static final BlockLegacy GLASS = REGISTRY.registerBlock("minecraft:glass", BlockID.GLASS);
    public static final BlockLegacy GLASS_PANE = REGISTRY.registerBlock("minecraft:glass_pane", BlockID.GLASS_PANE);
    public static final BlockLegacy GLOW_FRAME = REGISTRY.registerBlock("minecraft:glow_frame", BlockID.BLOCK_GLOW_FRAME)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.ITEM_FRAME_MAP_BIT)
            .addState(BlockStates.ITEM_FRAME_PHOTO_BIT);
    public static final BlockLegacy GLOW_LICHEN = REGISTRY.registerBlock("minecraft:glow_lichen", BlockID.GLOW_LICHEN)
            .addState(BlockStates.MULTI_FACE_DIRECTION_BITS);
    public static final BlockLegacy GLOWINGOBSIDIAN = REGISTRY.registerBlock("minecraft:glowingobsidian", BlockID.GLOWINGOBSIDIAN);
    public static final BlockLegacy GLOWSTONE = REGISTRY.registerBlock("minecraft:glowstone", BlockID.GLOWSTONE);
    public static final BlockLegacy GOLD_BLOCK = REGISTRY.registerBlock("minecraft:gold_block", BlockID.GOLD_BLOCK);
    public static final BlockLegacy GOLD_ORE = REGISTRY.registerBlock("minecraft:gold_ore", BlockID.GOLD_ORE);
    public static final BlockLegacy GOLDEN_RAIL = REGISTRY.registerBlock("minecraft:golden_rail", BlockID.GOLDEN_RAIL)
            .addState(BlockStates.RAIL_DIRECTION, BlockStateIntegerValues.GOLDEN_RAIL_MAX_RAIL_DIRECTION + 1)
            .addState(BlockStates.RAIL_DATA_BIT);
    public static final BlockLegacy GRANITE = REGISTRY.registerBlock("minecraft:granite", BlockID.GRANITE);
    public static final BlockLegacy GRANITE_STAIRS = REGISTRY.registerBlock("minecraft:granite_stairs", BlockID.GRANITE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy GRASS_BLOCK = REGISTRY.registerBlock("minecraft:grass_block", BlockID.GRASS_BLOCK);
    public static final BlockLegacy GRASS_PATH = REGISTRY.registerBlock("minecraft:grass_path", BlockID.GRASS_PATH);
    public static final BlockLegacy GRAVEL = REGISTRY.registerBlock("minecraft:gravel", BlockID.GRAVEL);
    public static final BlockLegacy GRAY_CANDLE = REGISTRY.registerBlock("minecraft:gray_candle", BlockID.GRAY_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy GRAY_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:gray_candle_cake", BlockID.GRAY_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy GRAY_CARPET = REGISTRY.registerBlock("minecraft:gray_carpet", BlockID.GRAY_CARPET);
    public static final BlockLegacy GRAY_CONCRETE = REGISTRY.registerBlock("minecraft:gray_concrete", BlockID.GRAY_CONCRETE);
    public static final BlockLegacy GRAY_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:gray_concrete_powder", BlockID.GRAY_CONCRETE_POWDER);
    public static final BlockLegacy GRAY_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:gray_glazed_terracotta", BlockID.GRAY_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy GRAY_SHULKER_BOX = REGISTRY.registerBlock("minecraft:gray_shulker_box", BlockID.GRAY_SHULKER_BOX);
    public static final BlockLegacy GRAY_STAINED_GLASS = REGISTRY.registerBlock("minecraft:gray_stained_glass", BlockID.GRAY_STAINED_GLASS);
    public static final BlockLegacy GRAY_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:gray_stained_glass_pane", BlockID.GRAY_STAINED_GLASS_PANE);
    public static final BlockLegacy GRAY_TERRACOTTA = REGISTRY.registerBlock("minecraft:gray_terracotta", BlockID.GRAY_TERRACOTTA);
    public static final BlockLegacy GRAY_WOOL = REGISTRY.registerBlock("minecraft:gray_wool", BlockID.GRAY_WOOL);
    public static final BlockLegacy GREEN_CANDLE = REGISTRY.registerBlock("minecraft:green_candle", BlockID.GREEN_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy GREEN_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:green_candle_cake", BlockID.GREEN_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy GREEN_CARPET = REGISTRY.registerBlock("minecraft:green_carpet", BlockID.GREEN_CARPET);
    public static final BlockLegacy GREEN_CONCRETE = REGISTRY.registerBlock("minecraft:green_concrete", BlockID.GREEN_CONCRETE);
    public static final BlockLegacy GREEN_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:green_concrete_powder", BlockID.GREEN_CONCRETE_POWDER);
    public static final BlockLegacy GREEN_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:green_glazed_terracotta", BlockID.GREEN_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy GREEN_SHULKER_BOX = REGISTRY.registerBlock("minecraft:green_shulker_box", BlockID.GREEN_SHULKER_BOX);
    public static final BlockLegacy GREEN_STAINED_GLASS = REGISTRY.registerBlock("minecraft:green_stained_glass", BlockID.GREEN_STAINED_GLASS);
    public static final BlockLegacy GREEN_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:green_stained_glass_pane", BlockID.GREEN_STAINED_GLASS_PANE);
    public static final BlockLegacy GREEN_TERRACOTTA = REGISTRY.registerBlock("minecraft:green_terracotta", BlockID.GREEN_TERRACOTTA);
    public static final BlockLegacy GREEN_WOOL = REGISTRY.registerBlock("minecraft:green_wool", BlockID.GREEN_WOOL);
    public static final BlockLegacy GRINDSTONE = REGISTRY.registerBlock("minecraft:grindstone", BlockID.GRINDSTONE)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.ATTACHMENT);
    public static final BlockLegacy HANGING_ROOTS = REGISTRY.registerBlock("minecraft:hanging_roots", BlockID.HANGING_ROOTS);
    public static final BlockLegacy HARD_BLACK_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_black_stained_glass", BlockID.HARD_BLACK_STAINED_GLASS);
    public static final BlockLegacy HARD_BLACK_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_black_stained_glass_pane", BlockID.HARD_BLACK_STAINED_GLASS_PANE);
    public static final BlockLegacy HARD_BLUE_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_blue_stained_glass", BlockID.HARD_BLUE_STAINED_GLASS);
    public static final BlockLegacy HARD_BLUE_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_blue_stained_glass_pane", BlockID.HARD_BLUE_STAINED_GLASS_PANE);
    public static final BlockLegacy HARD_BROWN_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_brown_stained_glass", BlockID.HARD_BROWN_STAINED_GLASS);
    public static final BlockLegacy HARD_BROWN_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_brown_stained_glass_pane", BlockID.HARD_BROWN_STAINED_GLASS_PANE);
    public static final BlockLegacy HARD_CYAN_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_cyan_stained_glass", BlockID.HARD_CYAN_STAINED_GLASS);
    public static final BlockLegacy HARD_CYAN_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_cyan_stained_glass_pane", BlockID.HARD_CYAN_STAINED_GLASS_PANE);
    public static final BlockLegacy HARD_GLASS = REGISTRY.registerBlock("minecraft:hard_glass", BlockID.HARD_GLASS);
    public static final BlockLegacy HARD_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_glass_pane", BlockID.HARD_GLASS_PANE);
    public static final BlockLegacy HARD_GRAY_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_gray_stained_glass", BlockID.HARD_GRAY_STAINED_GLASS);
    public static final BlockLegacy HARD_GRAY_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_gray_stained_glass_pane", BlockID.HARD_GRAY_STAINED_GLASS_PANE);
    public static final BlockLegacy HARD_GREEN_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_green_stained_glass", BlockID.HARD_GREEN_STAINED_GLASS);
    public static final BlockLegacy HARD_GREEN_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_green_stained_glass_pane", BlockID.HARD_GREEN_STAINED_GLASS_PANE);
    public static final BlockLegacy HARD_LIGHT_BLUE_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_light_blue_stained_glass", BlockID.HARD_LIGHT_BLUE_STAINED_GLASS);
    public static final BlockLegacy HARD_LIGHT_BLUE_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_light_blue_stained_glass_pane", BlockID.HARD_LIGHT_BLUE_STAINED_GLASS_PANE);
    public static final BlockLegacy HARD_LIGHT_GRAY_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_light_gray_stained_glass", BlockID.HARD_LIGHT_GRAY_STAINED_GLASS);
    public static final BlockLegacy HARD_LIGHT_GRAY_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_light_gray_stained_glass_pane", BlockID.HARD_LIGHT_GRAY_STAINED_GLASS_PANE);
    public static final BlockLegacy HARD_LIME_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_lime_stained_glass", BlockID.HARD_LIME_STAINED_GLASS);
    public static final BlockLegacy HARD_LIME_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_lime_stained_glass_pane", BlockID.HARD_LIME_STAINED_GLASS_PANE);
    public static final BlockLegacy HARD_MAGENTA_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_magenta_stained_glass", BlockID.HARD_MAGENTA_STAINED_GLASS);
    public static final BlockLegacy HARD_MAGENTA_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_magenta_stained_glass_pane", BlockID.HARD_MAGENTA_STAINED_GLASS_PANE);
    public static final BlockLegacy HARD_ORANGE_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_orange_stained_glass", BlockID.HARD_ORANGE_STAINED_GLASS);
    public static final BlockLegacy HARD_ORANGE_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_orange_stained_glass_pane", BlockID.HARD_ORANGE_STAINED_GLASS_PANE);
    public static final BlockLegacy HARD_PINK_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_pink_stained_glass", BlockID.HARD_PINK_STAINED_GLASS);
    public static final BlockLegacy HARD_PINK_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_pink_stained_glass_pane", BlockID.HARD_PINK_STAINED_GLASS_PANE);
    public static final BlockLegacy HARD_PURPLE_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_purple_stained_glass", BlockID.HARD_PURPLE_STAINED_GLASS);
    public static final BlockLegacy HARD_PURPLE_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_purple_stained_glass_pane", BlockID.HARD_PURPLE_STAINED_GLASS_PANE);
    public static final BlockLegacy HARD_RED_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_red_stained_glass", BlockID.HARD_RED_STAINED_GLASS);
    public static final BlockLegacy HARD_RED_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_red_stained_glass_pane", BlockID.HARD_RED_STAINED_GLASS_PANE);
    public static final BlockLegacy HARD_WHITE_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_white_stained_glass", BlockID.HARD_WHITE_STAINED_GLASS);
    public static final BlockLegacy HARD_WHITE_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_white_stained_glass_pane", BlockID.HARD_WHITE_STAINED_GLASS_PANE);
    public static final BlockLegacy HARD_YELLOW_STAINED_GLASS = REGISTRY.registerBlock("minecraft:hard_yellow_stained_glass", BlockID.HARD_YELLOW_STAINED_GLASS);
    public static final BlockLegacy HARD_YELLOW_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:hard_yellow_stained_glass_pane", BlockID.HARD_YELLOW_STAINED_GLASS_PANE);
    public static final BlockLegacy HARDENED_CLAY = REGISTRY.registerBlock("minecraft:hardened_clay", BlockID.HARDENED_CLAY);
    public static final BlockLegacy HAY_BLOCK = REGISTRY.registerBlock("minecraft:hay_block", BlockID.HAY_BLOCK)
            .addState(BlockStates.DEPRECATED)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy HEAVY_CORE = REGISTRY.registerBlock("minecraft:heavy_core", BlockID.HEAVY_CORE);
    public static final BlockLegacy HEAVY_WEIGHTED_PRESSURE_PLATE = REGISTRY.registerBlock("minecraft:heavy_weighted_pressure_plate", BlockID.HEAVY_WEIGHTED_PRESSURE_PLATE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy HONEY_BLOCK = REGISTRY.registerBlock("minecraft:honey_block", BlockID.HONEY_BLOCK);
    public static final BlockLegacy HONEYCOMB_BLOCK = REGISTRY.registerBlock("minecraft:honeycomb_block", BlockID.HONEYCOMB_BLOCK);
    public static final BlockLegacy HOPPER = REGISTRY.registerBlock("minecraft:hopper", BlockID.BLOCK_HOPPER)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.TOGGLE_BIT);
    public static final BlockLegacy HORN_CORAL = REGISTRY.registerBlock("minecraft:horn_coral", BlockID.HORN_CORAL);
    public static final BlockLegacy HORN_CORAL_BLOCK = REGISTRY.registerBlock("minecraft:horn_coral_block", BlockID.HORN_CORAL_BLOCK);
    public static final BlockLegacy HORN_CORAL_FAN = REGISTRY.registerBlock("minecraft:horn_coral_fan", BlockID.HORN_CORAL_FAN)
            .addState(BlockStates.CORAL_FAN_DIRECTION);
    public static final BlockLegacy ICE = REGISTRY.registerBlock("minecraft:ice", BlockID.ICE);
    public static final BlockLegacy INFESTED_DEEPSLATE = REGISTRY.registerBlock("minecraft:infested_deepslate", BlockID.INFESTED_DEEPSLATE)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy INFO_UPDATE = REGISTRY.registerBlock("minecraft:info_update", BlockID.INFO_UPDATE);
    public static final BlockLegacy INFO_UPDATE2 = REGISTRY.registerBlock("minecraft:info_update2", BlockID.INFO_UPDATE2);
    public static final BlockLegacy INVISIBLE_BEDROCK = REGISTRY.registerBlock("minecraft:invisible_bedrock", BlockID.INVISIBLE_BEDROCK);
    public static final BlockLegacy IRON_BARS = REGISTRY.registerBlock("minecraft:iron_bars", BlockID.IRON_BARS);
    public static final BlockLegacy IRON_BLOCK = REGISTRY.registerBlock("minecraft:iron_block", BlockID.IRON_BLOCK);
    public static final BlockLegacy IRON_DOOR = REGISTRY.registerBlock("minecraft:iron_door", BlockID.BLOCK_IRON_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy IRON_ORE = REGISTRY.registerBlock("minecraft:iron_ore", BlockID.IRON_ORE);
    public static final BlockLegacy IRON_TRAPDOOR = REGISTRY.registerBlock("minecraft:iron_trapdoor", BlockID.IRON_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy JIGSAW = REGISTRY.registerBlock("minecraft:jigsaw", BlockID.JIGSAW)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.ROTATION);
    public static final BlockLegacy JUKEBOX = REGISTRY.registerBlock("minecraft:jukebox", BlockID.JUKEBOX);
    public static final BlockLegacy JUNGLE_BUTTON = REGISTRY.registerBlock("minecraft:jungle_button", BlockID.JUNGLE_BUTTON)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.BUTTON_PRESSED_BIT);
    public static final BlockLegacy JUNGLE_DOOR = REGISTRY.registerBlock("minecraft:jungle_door", BlockID.BLOCK_JUNGLE_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy JUNGLE_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:jungle_double_slab", BlockID.JUNGLE_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy JUNGLE_FENCE = REGISTRY.registerBlock("minecraft:jungle_fence", BlockID.JUNGLE_FENCE);
    public static final BlockLegacy JUNGLE_FENCE_GATE = REGISTRY.registerBlock("minecraft:jungle_fence_gate", BlockID.JUNGLE_FENCE_GATE)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.IN_WALL_BIT);
    public static final BlockLegacy JUNGLE_HANGING_SIGN = REGISTRY.registerBlock("minecraft:jungle_hanging_sign", BlockID.JUNGLE_HANGING_SIGN)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.GROUND_SIGN_DIRECTION)
            .addState(BlockStates.HANGING)
            .addState(BlockStates.ATTACHED_BIT);
    public static final BlockLegacy JUNGLE_LEAVES = REGISTRY.registerBlock("minecraft:jungle_leaves", BlockID.JUNGLE_LEAVES)
            .addState(BlockStates.UPDATE_BIT)
            .addState(BlockStates.PERSISTENT_BIT);
    public static final BlockLegacy JUNGLE_LOG = REGISTRY.registerBlock("minecraft:jungle_log", BlockID.JUNGLE_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy JUNGLE_PLANKS = REGISTRY.registerBlock("minecraft:jungle_planks", BlockID.JUNGLE_PLANKS);
    public static final BlockLegacy JUNGLE_PRESSURE_PLATE = REGISTRY.registerBlock("minecraft:jungle_pressure_plate", BlockID.JUNGLE_PRESSURE_PLATE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy JUNGLE_SAPLING = REGISTRY.registerBlock("minecraft:jungle_sapling", BlockID.JUNGLE_SAPLING)
            .addState(BlockStates.AGE_BIT);
    public static final BlockLegacy JUNGLE_SLAB = REGISTRY.registerBlock("minecraft:jungle_slab", BlockID.JUNGLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy JUNGLE_STAIRS = REGISTRY.registerBlock("minecraft:jungle_stairs", BlockID.JUNGLE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy JUNGLE_STANDING_SIGN = REGISTRY.registerBlock("minecraft:jungle_standing_sign", BlockID.JUNGLE_STANDING_SIGN)
            .addState(BlockStates.GROUND_SIGN_DIRECTION);
    public static final BlockLegacy JUNGLE_TRAPDOOR = REGISTRY.registerBlock("minecraft:jungle_trapdoor", BlockID.JUNGLE_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy JUNGLE_WALL_SIGN = REGISTRY.registerBlock("minecraft:jungle_wall_sign", BlockID.JUNGLE_WALL_SIGN)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy JUNGLE_WOOD = REGISTRY.registerBlock("minecraft:jungle_wood", BlockID.JUNGLE_WOOD)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy KELP = REGISTRY.registerBlock("minecraft:kelp", BlockID.BLOCK_KELP)
            .addState(BlockStates.KELP_AGE);
    public static final BlockLegacy LADDER = REGISTRY.registerBlock("minecraft:ladder", BlockID.LADDER)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy LANTERN = REGISTRY.registerBlock("minecraft:lantern", BlockID.LANTERN)
            .addState(BlockStates.HANGING);
    public static final BlockLegacy LAPIS_BLOCK = REGISTRY.registerBlock("minecraft:lapis_block", BlockID.LAPIS_BLOCK);
    public static final BlockLegacy LAPIS_ORE = REGISTRY.registerBlock("minecraft:lapis_ore", BlockID.LAPIS_ORE);
    public static final BlockLegacy LARGE_AMETHYST_BUD = REGISTRY.registerBlock("minecraft:large_amethyst_bud", BlockID.LARGE_AMETHYST_BUD)
            .addState(BlockStates.MINECRAFT_BLOCK_FACE);
    public static final BlockLegacy LARGE_FERN = REGISTRY.registerBlock("minecraft:large_fern", BlockID.LARGE_FERN)
            .addState(BlockStates.UPPER_BLOCK_BIT);
    public static final BlockLegacy LAVA = REGISTRY.registerBlock("minecraft:lava", BlockID.LAVA)
            .addState(BlockStates.LIQUID_DEPTH);
    public static final BlockLegacy LECTERN = REGISTRY.registerBlock("minecraft:lectern", BlockID.LECTERN)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION)
            .addState(BlockStates.POWERED_BIT);
    public static final BlockLegacy LEVER = REGISTRY.registerBlock("minecraft:lever", BlockID.LEVER)
            .addState(BlockStates.LEVER_DIRECTION)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy LIGHT_BLOCK = REGISTRY.registerBlock("minecraft:light_block", BlockID.LIGHT_BLOCK)
            .addState(BlockStates.BLOCK_LIGHT_LEVEL);
    public static final BlockLegacy LIGHT_BLUE_CANDLE = REGISTRY.registerBlock("minecraft:light_blue_candle", BlockID.LIGHT_BLUE_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy LIGHT_BLUE_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:light_blue_candle_cake", BlockID.LIGHT_BLUE_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy LIGHT_BLUE_CARPET = REGISTRY.registerBlock("minecraft:light_blue_carpet", BlockID.LIGHT_BLUE_CARPET);
    public static final BlockLegacy LIGHT_BLUE_CONCRETE = REGISTRY.registerBlock("minecraft:light_blue_concrete", BlockID.LIGHT_BLUE_CONCRETE);
    public static final BlockLegacy LIGHT_BLUE_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:light_blue_concrete_powder", BlockID.LIGHT_BLUE_CONCRETE_POWDER);
    public static final BlockLegacy LIGHT_BLUE_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:light_blue_glazed_terracotta", BlockID.LIGHT_BLUE_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy LIGHT_BLUE_SHULKER_BOX = REGISTRY.registerBlock("minecraft:light_blue_shulker_box", BlockID.LIGHT_BLUE_SHULKER_BOX);
    public static final BlockLegacy LIGHT_BLUE_STAINED_GLASS = REGISTRY.registerBlock("minecraft:light_blue_stained_glass", BlockID.LIGHT_BLUE_STAINED_GLASS);
    public static final BlockLegacy LIGHT_BLUE_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:light_blue_stained_glass_pane", BlockID.LIGHT_BLUE_STAINED_GLASS_PANE);
    public static final BlockLegacy LIGHT_BLUE_TERRACOTTA = REGISTRY.registerBlock("minecraft:light_blue_terracotta", BlockID.LIGHT_BLUE_TERRACOTTA);
    public static final BlockLegacy LIGHT_BLUE_WOOL = REGISTRY.registerBlock("minecraft:light_blue_wool", BlockID.LIGHT_BLUE_WOOL);
    public static final BlockLegacy LIGHT_GRAY_CANDLE = REGISTRY.registerBlock("minecraft:light_gray_candle", BlockID.LIGHT_GRAY_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy LIGHT_GRAY_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:light_gray_candle_cake", BlockID.LIGHT_GRAY_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy LIGHT_GRAY_CARPET = REGISTRY.registerBlock("minecraft:light_gray_carpet", BlockID.LIGHT_GRAY_CARPET);
    public static final BlockLegacy LIGHT_GRAY_CONCRETE = REGISTRY.registerBlock("minecraft:light_gray_concrete", BlockID.LIGHT_GRAY_CONCRETE);
    public static final BlockLegacy LIGHT_GRAY_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:light_gray_concrete_powder", BlockID.LIGHT_GRAY_CONCRETE_POWDER);
    public static final BlockLegacy LIGHT_GRAY_SHULKER_BOX = REGISTRY.registerBlock("minecraft:light_gray_shulker_box", BlockID.LIGHT_GRAY_SHULKER_BOX);
    public static final BlockLegacy LIGHT_GRAY_STAINED_GLASS = REGISTRY.registerBlock("minecraft:light_gray_stained_glass", BlockID.LIGHT_GRAY_STAINED_GLASS);
    public static final BlockLegacy LIGHT_GRAY_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:light_gray_stained_glass_pane", BlockID.LIGHT_GRAY_STAINED_GLASS_PANE);
    public static final BlockLegacy LIGHT_GRAY_TERRACOTTA = REGISTRY.registerBlock("minecraft:light_gray_terracotta", BlockID.LIGHT_GRAY_TERRACOTTA);
    public static final BlockLegacy LIGHT_GRAY_WOOL = REGISTRY.registerBlock("minecraft:light_gray_wool", BlockID.LIGHT_GRAY_WOOL);
    public static final BlockLegacy LIGHT_WEIGHTED_PRESSURE_PLATE = REGISTRY.registerBlock("minecraft:light_weighted_pressure_plate", BlockID.LIGHT_WEIGHTED_PRESSURE_PLATE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy LIGHTNING_ROD = REGISTRY.registerBlock("minecraft:lightning_rod", BlockID.LIGHTNING_ROD)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy LILAC = REGISTRY.registerBlock("minecraft:lilac", BlockID.LILAC)
            .addState(BlockStates.UPPER_BLOCK_BIT);
    public static final BlockLegacy LILY_OF_THE_VALLEY = REGISTRY.registerBlock("minecraft:lily_of_the_valley", BlockID.LILY_OF_THE_VALLEY);
    public static final BlockLegacy LIME_CANDLE = REGISTRY.registerBlock("minecraft:lime_candle", BlockID.LIME_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy LIME_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:lime_candle_cake", BlockID.LIME_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy LIME_CARPET = REGISTRY.registerBlock("minecraft:lime_carpet", BlockID.LIME_CARPET);
    public static final BlockLegacy LIME_CONCRETE = REGISTRY.registerBlock("minecraft:lime_concrete", BlockID.LIME_CONCRETE);
    public static final BlockLegacy LIME_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:lime_concrete_powder", BlockID.LIME_CONCRETE_POWDER);
    public static final BlockLegacy LIME_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:lime_glazed_terracotta", BlockID.LIME_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy LIME_SHULKER_BOX = REGISTRY.registerBlock("minecraft:lime_shulker_box", BlockID.LIME_SHULKER_BOX);
    public static final BlockLegacy LIME_STAINED_GLASS = REGISTRY.registerBlock("minecraft:lime_stained_glass", BlockID.LIME_STAINED_GLASS);
    public static final BlockLegacy LIME_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:lime_stained_glass_pane", BlockID.LIME_STAINED_GLASS_PANE);
    public static final BlockLegacy LIME_TERRACOTTA = REGISTRY.registerBlock("minecraft:lime_terracotta", BlockID.LIME_TERRACOTTA);
    public static final BlockLegacy LIME_WOOL = REGISTRY.registerBlock("minecraft:lime_wool", BlockID.LIME_WOOL);
    public static final BlockLegacy LIT_BLAST_FURNACE = REGISTRY.registerBlock("minecraft:lit_blast_furnace", BlockID.LIT_BLAST_FURNACE)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy LIT_DEEPSLATE_REDSTONE_ORE = REGISTRY.registerBlock("minecraft:lit_deepslate_redstone_ore", BlockID.LIT_DEEPSLATE_REDSTONE_ORE);
    public static final BlockLegacy LIT_FURNACE = REGISTRY.registerBlock("minecraft:lit_furnace", BlockID.LIT_FURNACE)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy LIT_PUMPKIN = REGISTRY.registerBlock("minecraft:lit_pumpkin", BlockID.LIT_PUMPKIN)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy LIT_REDSTONE_LAMP = REGISTRY.registerBlock("minecraft:lit_redstone_lamp", BlockID.LIT_REDSTONE_LAMP);
    public static final BlockLegacy LIT_REDSTONE_ORE = REGISTRY.registerBlock("minecraft:lit_redstone_ore", BlockID.LIT_REDSTONE_ORE);
    public static final BlockLegacy LIT_SMOKER = REGISTRY.registerBlock("minecraft:lit_smoker", BlockID.LIT_SMOKER)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy LODESTONE = REGISTRY.registerBlock("minecraft:lodestone", BlockID.LODESTONE);
    public static final BlockLegacy LOOM = REGISTRY.registerBlock("minecraft:loom", BlockID.LOOM)
            .addState(BlockStates.DIRECTION);
    public static final BlockLegacy MAGENTA_CANDLE = REGISTRY.registerBlock("minecraft:magenta_candle", BlockID.MAGENTA_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy MAGENTA_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:magenta_candle_cake", BlockID.MAGENTA_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy MAGENTA_CARPET = REGISTRY.registerBlock("minecraft:magenta_carpet", BlockID.MAGENTA_CARPET);
    public static final BlockLegacy MAGENTA_CONCRETE = REGISTRY.registerBlock("minecraft:magenta_concrete", BlockID.MAGENTA_CONCRETE);
    public static final BlockLegacy MAGENTA_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:magenta_concrete_powder", BlockID.MAGENTA_CONCRETE_POWDER);
    public static final BlockLegacy MAGENTA_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:magenta_glazed_terracotta", BlockID.MAGENTA_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy MAGENTA_SHULKER_BOX = REGISTRY.registerBlock("minecraft:magenta_shulker_box", BlockID.MAGENTA_SHULKER_BOX);
    public static final BlockLegacy MAGENTA_STAINED_GLASS = REGISTRY.registerBlock("minecraft:magenta_stained_glass", BlockID.MAGENTA_STAINED_GLASS);
    public static final BlockLegacy MAGENTA_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:magenta_stained_glass_pane", BlockID.MAGENTA_STAINED_GLASS_PANE);
    public static final BlockLegacy MAGENTA_TERRACOTTA = REGISTRY.registerBlock("minecraft:magenta_terracotta", BlockID.MAGENTA_TERRACOTTA);
    public static final BlockLegacy MAGENTA_WOOL = REGISTRY.registerBlock("minecraft:magenta_wool", BlockID.MAGENTA_WOOL);
    public static final BlockLegacy MAGMA = REGISTRY.registerBlock("minecraft:magma", BlockID.MAGMA);
    public static final BlockLegacy MANGROVE_BUTTON = REGISTRY.registerBlock("minecraft:mangrove_button", BlockID.MANGROVE_BUTTON)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.BUTTON_PRESSED_BIT);
    public static final BlockLegacy MANGROVE_DOOR = REGISTRY.registerBlock("minecraft:mangrove_door", BlockID.BLOCK_MANGROVE_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy MANGROVE_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:mangrove_double_slab", BlockID.MANGROVE_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy MANGROVE_FENCE = REGISTRY.registerBlock("minecraft:mangrove_fence", BlockID.MANGROVE_FENCE);
    public static final BlockLegacy MANGROVE_FENCE_GATE = REGISTRY.registerBlock("minecraft:mangrove_fence_gate", BlockID.MANGROVE_FENCE_GATE)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.IN_WALL_BIT);
    public static final BlockLegacy MANGROVE_HANGING_SIGN = REGISTRY.registerBlock("minecraft:mangrove_hanging_sign", BlockID.MANGROVE_HANGING_SIGN)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.GROUND_SIGN_DIRECTION)
            .addState(BlockStates.HANGING)
            .addState(BlockStates.ATTACHED_BIT);
    public static final BlockLegacy MANGROVE_LEAVES = REGISTRY.registerBlock("minecraft:mangrove_leaves", BlockID.MANGROVE_LEAVES)
            .addState(BlockStates.UPDATE_BIT)
            .addState(BlockStates.PERSISTENT_BIT);
    public static final BlockLegacy MANGROVE_LOG = REGISTRY.registerBlock("minecraft:mangrove_log", BlockID.MANGROVE_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy MANGROVE_PLANKS = REGISTRY.registerBlock("minecraft:mangrove_planks", BlockID.MANGROVE_PLANKS);
    public static final BlockLegacy MANGROVE_PRESSURE_PLATE = REGISTRY.registerBlock("minecraft:mangrove_pressure_plate", BlockID.MANGROVE_PRESSURE_PLATE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy MANGROVE_PROPAGULE = REGISTRY.registerBlock("minecraft:mangrove_propagule", BlockID.MANGROVE_PROPAGULE)
            .addState(BlockStates.PROPAGULE_STAGE)
            .addState(BlockStates.HANGING);
    public static final BlockLegacy MANGROVE_ROOTS = REGISTRY.registerBlock("minecraft:mangrove_roots", BlockID.MANGROVE_ROOTS);
    public static final BlockLegacy MANGROVE_SLAB = REGISTRY.registerBlock("minecraft:mangrove_slab", BlockID.MANGROVE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy MANGROVE_STAIRS = REGISTRY.registerBlock("minecraft:mangrove_stairs", BlockID.MANGROVE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy MANGROVE_STANDING_SIGN = REGISTRY.registerBlock("minecraft:mangrove_standing_sign", BlockID.MANGROVE_STANDING_SIGN)
            .addState(BlockStates.GROUND_SIGN_DIRECTION);
    public static final BlockLegacy MANGROVE_TRAPDOOR = REGISTRY.registerBlock("minecraft:mangrove_trapdoor", BlockID.MANGROVE_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy MANGROVE_WALL_SIGN = REGISTRY.registerBlock("minecraft:mangrove_wall_sign", BlockID.MANGROVE_WALL_SIGN)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy MANGROVE_WOOD = REGISTRY.registerBlock("minecraft:mangrove_wood", BlockID.MANGROVE_WOOD)
            .addState(BlockStates.STRIPPED_BIT)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy MEDIUM_AMETHYST_BUD = REGISTRY.registerBlock("minecraft:medium_amethyst_bud", BlockID.MEDIUM_AMETHYST_BUD)
            .addState(BlockStates.MINECRAFT_BLOCK_FACE);
    public static final BlockLegacy MELON_BLOCK = REGISTRY.registerBlock("minecraft:melon_block", BlockID.MELON_BLOCK);
    public static final BlockLegacy MELON_STEM = REGISTRY.registerBlock("minecraft:melon_stem", BlockID.MELON_STEM)
            .addState(BlockStates.GROWTH)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy MOB_SPAWNER = REGISTRY.registerBlock("minecraft:mob_spawner", BlockID.MOB_SPAWNER);
    public static final BlockLegacy MONSTER_EGG = REGISTRY.registerBlock("minecraft:monster_egg", BlockID.MONSTER_EGG)
            .addState(BlockStates.MONSTER_EGG_STONE_TYPE);
    public static final BlockLegacy MOSS_BLOCK = REGISTRY.registerBlock("minecraft:moss_block", BlockID.MOSS_BLOCK);
    public static final BlockLegacy MOSS_CARPET = REGISTRY.registerBlock("minecraft:moss_carpet", BlockID.MOSS_CARPET);
    public static final BlockLegacy MOSSY_COBBLESTONE = REGISTRY.registerBlock("minecraft:mossy_cobblestone", BlockID.MOSSY_COBBLESTONE);
    public static final BlockLegacy MOSSY_COBBLESTONE_STAIRS = REGISTRY.registerBlock("minecraft:mossy_cobblestone_stairs", BlockID.MOSSY_COBBLESTONE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy MOSSY_STONE_BRICK_STAIRS = REGISTRY.registerBlock("minecraft:mossy_stone_brick_stairs", BlockID.MOSSY_STONE_BRICK_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy MOVING_BLOCK = REGISTRY.registerBlock("minecraft:moving_block", BlockID.MOVING_BLOCK);
    public static final BlockLegacy MUD = REGISTRY.registerBlock("minecraft:mud", BlockID.MUD);
    public static final BlockLegacy MUD_BRICK_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:mud_brick_double_slab", BlockID.MUD_BRICK_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy MUD_BRICK_SLAB = REGISTRY.registerBlock("minecraft:mud_brick_slab", BlockID.MUD_BRICK_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy MUD_BRICK_STAIRS = REGISTRY.registerBlock("minecraft:mud_brick_stairs", BlockID.MUD_BRICK_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy MUD_BRICK_WALL = REGISTRY.registerBlock("minecraft:mud_brick_wall", BlockID.MUD_BRICK_WALL)
            .addState(BlockStates.WALL_POST_BIT)
            .addState(BlockStates.WALL_CONNECTION_TYPE_NORTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_EAST)
            .addState(BlockStates.WALL_CONNECTION_TYPE_SOUTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_WEST);
    public static final BlockLegacy MUD_BRICKS = REGISTRY.registerBlock("minecraft:mud_bricks", BlockID.MUD_BRICKS);
    public static final BlockLegacy MUDDY_MANGROVE_ROOTS = REGISTRY.registerBlock("minecraft:muddy_mangrove_roots", BlockID.MUDDY_MANGROVE_ROOTS)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy MYCELIUM = REGISTRY.registerBlock("minecraft:mycelium", BlockID.MYCELIUM);
    public static final BlockLegacy NETHER_BRICK = REGISTRY.registerBlock("minecraft:nether_brick", BlockID.NETHER_BRICK);
    public static final BlockLegacy NETHER_BRICK_FENCE = REGISTRY.registerBlock("minecraft:nether_brick_fence", BlockID.NETHER_BRICK_FENCE);
    public static final BlockLegacy NETHER_BRICK_SLAB = REGISTRY.registerBlock("minecraft:nether_brick_slab", BlockID.NETHER_BRICK_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy NETHER_BRICK_STAIRS = REGISTRY.registerBlock("minecraft:nether_brick_stairs", BlockID.NETHER_BRICK_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy NETHER_GOLD_ORE = REGISTRY.registerBlock("minecraft:nether_gold_ore", BlockID.NETHER_GOLD_ORE);
    public static final BlockLegacy NETHER_SPROUTS = REGISTRY.registerBlock("minecraft:nether_sprouts", BlockID.BLOCK_NETHER_SPROUTS);
    public static final BlockLegacy NETHER_WART = REGISTRY.registerBlock("minecraft:nether_wart", BlockID.BLOCK_NETHER_WART)
            .addState(BlockStates.AGE, BlockStateIntegerValues.NETHER_WART_MAX_AGE + 1);
    public static final BlockLegacy NETHER_WART_BLOCK = REGISTRY.registerBlock("minecraft:nether_wart_block", BlockID.NETHER_WART_BLOCK);
    public static final BlockLegacy NETHERITE_BLOCK = REGISTRY.registerBlock("minecraft:netherite_block", BlockID.NETHERITE_BLOCK);
    public static final BlockLegacy NETHERRACK = REGISTRY.registerBlock("minecraft:netherrack", BlockID.NETHERRACK);
    public static final BlockLegacy NETHERREACTOR = REGISTRY.registerBlock("minecraft:netherreactor", BlockID.NETHERREACTOR);
    public static final BlockLegacy NORMAL_STONE_STAIRS = REGISTRY.registerBlock("minecraft:normal_stone_stairs", BlockID.NORMAL_STONE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy NOTEBLOCK = REGISTRY.registerBlock("minecraft:noteblock", BlockID.NOTEBLOCK);
    public static final BlockLegacy OAK_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:oak_double_slab", BlockID.OAK_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy OAK_FENCE = REGISTRY.registerBlock("minecraft:oak_fence", BlockID.OAK_FENCE);
    public static final BlockLegacy OAK_HANGING_SIGN = REGISTRY.registerBlock("minecraft:oak_hanging_sign", BlockID.OAK_HANGING_SIGN)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.GROUND_SIGN_DIRECTION)
            .addState(BlockStates.HANGING)
            .addState(BlockStates.ATTACHED_BIT);
    public static final BlockLegacy OAK_LEAVES = REGISTRY.registerBlock("minecraft:oak_leaves", BlockID.OAK_LEAVES)
            .addState(BlockStates.UPDATE_BIT)
            .addState(BlockStates.PERSISTENT_BIT);
    public static final BlockLegacy OAK_LOG = REGISTRY.registerBlock("minecraft:oak_log", BlockID.OAK_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy OAK_PLANKS = REGISTRY.registerBlock("minecraft:oak_planks", BlockID.OAK_PLANKS);
    public static final BlockLegacy OAK_SAPLING = REGISTRY.registerBlock("minecraft:oak_sapling", BlockID.OAK_SAPLING)
            .addState(BlockStates.AGE_BIT);
    public static final BlockLegacy OAK_SLAB = REGISTRY.registerBlock("minecraft:oak_slab", BlockID.OAK_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy OAK_STAIRS = REGISTRY.registerBlock("minecraft:oak_stairs", BlockID.OAK_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy OAK_WOOD = REGISTRY.registerBlock("minecraft:oak_wood", BlockID.OAK_WOOD)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy OBSERVER = REGISTRY.registerBlock("minecraft:observer", BlockID.OBSERVER)
            .addState(BlockStates.MINECRAFT_FACING_DIRECTION)
            .addState(BlockStates.POWERED_BIT);
    public static final BlockLegacy OBSIDIAN = REGISTRY.registerBlock("minecraft:obsidian", BlockID.OBSIDIAN);
    public static final BlockLegacy OCHRE_FROGLIGHT = REGISTRY.registerBlock("minecraft:ochre_froglight", BlockID.OCHRE_FROGLIGHT)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy ORANGE_CANDLE = REGISTRY.registerBlock("minecraft:orange_candle", BlockID.ORANGE_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy ORANGE_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:orange_candle_cake", BlockID.ORANGE_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy ORANGE_CARPET = REGISTRY.registerBlock("minecraft:orange_carpet", BlockID.ORANGE_CARPET);
    public static final BlockLegacy ORANGE_CONCRETE = REGISTRY.registerBlock("minecraft:orange_concrete", BlockID.ORANGE_CONCRETE);
    public static final BlockLegacy ORANGE_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:orange_concrete_powder", BlockID.ORANGE_CONCRETE_POWDER);
    public static final BlockLegacy ORANGE_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:orange_glazed_terracotta", BlockID.ORANGE_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy ORANGE_SHULKER_BOX = REGISTRY.registerBlock("minecraft:orange_shulker_box", BlockID.ORANGE_SHULKER_BOX);
    public static final BlockLegacy ORANGE_STAINED_GLASS = REGISTRY.registerBlock("minecraft:orange_stained_glass", BlockID.ORANGE_STAINED_GLASS);
    public static final BlockLegacy ORANGE_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:orange_stained_glass_pane", BlockID.ORANGE_STAINED_GLASS_PANE);
    public static final BlockLegacy ORANGE_TERRACOTTA = REGISTRY.registerBlock("minecraft:orange_terracotta", BlockID.ORANGE_TERRACOTTA);
    public static final BlockLegacy ORANGE_TULIP = REGISTRY.registerBlock("minecraft:orange_tulip", BlockID.ORANGE_TULIP);
    public static final BlockLegacy ORANGE_WOOL = REGISTRY.registerBlock("minecraft:orange_wool", BlockID.ORANGE_WOOL);
    public static final BlockLegacy OXEYE_DAISY = REGISTRY.registerBlock("minecraft:oxeye_daisy", BlockID.OXEYE_DAISY);
    public static final BlockLegacy OXIDIZED_CHISELED_COPPER = REGISTRY.registerBlock("minecraft:oxidized_chiseled_copper", BlockID.OXIDIZED_CHISELED_COPPER);
    public static final BlockLegacy OXIDIZED_COPPER = REGISTRY.registerBlock("minecraft:oxidized_copper", BlockID.OXIDIZED_COPPER);
    public static final BlockLegacy OXIDIZED_COPPER_BULB = REGISTRY.registerBlock("minecraft:oxidized_copper_bulb", BlockID.OXIDIZED_COPPER_BULB)
            .addState(BlockStates.LIT)
            .addState(BlockStates.POWERED_BIT);
    public static final BlockLegacy OXIDIZED_COPPER_DOOR = REGISTRY.registerBlock("minecraft:oxidized_copper_door", BlockID.OXIDIZED_COPPER_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy OXIDIZED_COPPER_GRATE = REGISTRY.registerBlock("minecraft:oxidized_copper_grate", BlockID.OXIDIZED_COPPER_GRATE);
    public static final BlockLegacy OXIDIZED_COPPER_TRAPDOOR = REGISTRY.registerBlock("minecraft:oxidized_copper_trapdoor", BlockID.OXIDIZED_COPPER_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy OXIDIZED_CUT_COPPER = REGISTRY.registerBlock("minecraft:oxidized_cut_copper", BlockID.OXIDIZED_CUT_COPPER);
    public static final BlockLegacy OXIDIZED_CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:oxidized_cut_copper_slab", BlockID.OXIDIZED_CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy OXIDIZED_CUT_COPPER_STAIRS = REGISTRY.registerBlock("minecraft:oxidized_cut_copper_stairs", BlockID.OXIDIZED_CUT_COPPER_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy OXIDIZED_DOUBLE_CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:oxidized_double_cut_copper_slab", BlockID.OXIDIZED_DOUBLE_CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy PACKED_ICE = REGISTRY.registerBlock("minecraft:packed_ice", BlockID.PACKED_ICE);
    public static final BlockLegacy PACKED_MUD = REGISTRY.registerBlock("minecraft:packed_mud", BlockID.PACKED_MUD);
    public static final BlockLegacy PEARLESCENT_FROGLIGHT = REGISTRY.registerBlock("minecraft:pearlescent_froglight", BlockID.PEARLESCENT_FROGLIGHT)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy PEONY = REGISTRY.registerBlock("minecraft:peony", BlockID.PEONY)
            .addState(BlockStates.UPPER_BLOCK_BIT);
    public static final BlockLegacy PETRIFIED_OAK_SLAB = REGISTRY.registerBlock("minecraft:petrified_oak_slab", BlockID.PETRIFIED_OAK_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy PINK_CANDLE = REGISTRY.registerBlock("minecraft:pink_candle", BlockID.PINK_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy PINK_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:pink_candle_cake", BlockID.PINK_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy PINK_CARPET = REGISTRY.registerBlock("minecraft:pink_carpet", BlockID.PINK_CARPET);
    public static final BlockLegacy PINK_CONCRETE = REGISTRY.registerBlock("minecraft:pink_concrete", BlockID.PINK_CONCRETE);
    public static final BlockLegacy PINK_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:pink_concrete_powder", BlockID.PINK_CONCRETE_POWDER);
    public static final BlockLegacy PINK_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:pink_glazed_terracotta", BlockID.PINK_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy PINK_PETALS = REGISTRY.registerBlock("minecraft:pink_petals", BlockID.PINK_PETALS)
            .addState(BlockStates.GROWTH)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy PINK_SHULKER_BOX = REGISTRY.registerBlock("minecraft:pink_shulker_box", BlockID.PINK_SHULKER_BOX);
    public static final BlockLegacy PINK_STAINED_GLASS = REGISTRY.registerBlock("minecraft:pink_stained_glass", BlockID.PINK_STAINED_GLASS);
    public static final BlockLegacy PINK_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:pink_stained_glass_pane", BlockID.PINK_STAINED_GLASS_PANE);
    public static final BlockLegacy PINK_TERRACOTTA = REGISTRY.registerBlock("minecraft:pink_terracotta", BlockID.PINK_TERRACOTTA);
    public static final BlockLegacy PINK_TULIP = REGISTRY.registerBlock("minecraft:pink_tulip", BlockID.PINK_TULIP);
    public static final BlockLegacy PINK_WOOL = REGISTRY.registerBlock("minecraft:pink_wool", BlockID.PINK_WOOL);
    public static final BlockLegacy PISTON = REGISTRY.registerBlock("minecraft:piston", BlockID.PISTON)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy PISTON_ARM_COLLISION = REGISTRY.registerBlock("minecraft:piston_arm_collision", BlockID.PISTON_ARM_COLLISION)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy PITCHER_CROP = REGISTRY.registerBlock("minecraft:pitcher_crop", BlockID.PITCHER_CROP)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.GROWTH);
    public static final BlockLegacy PITCHER_PLANT = REGISTRY.registerBlock("minecraft:pitcher_plant", BlockID.PITCHER_PLANT)
            .addState(BlockStates.UPPER_BLOCK_BIT);
    public static final BlockLegacy PODZOL = REGISTRY.registerBlock("minecraft:podzol", BlockID.PODZOL);
    public static final BlockLegacy POINTED_DRIPSTONE = REGISTRY.registerBlock("minecraft:pointed_dripstone", BlockID.POINTED_DRIPSTONE)
            .addState(BlockStates.DRIPSTONE_THICKNESS)
            .addState(BlockStates.HANGING);
    public static final BlockLegacy POLISHED_ANDESITE = REGISTRY.registerBlock("minecraft:polished_andesite", BlockID.POLISHED_ANDESITE);
    public static final BlockLegacy POLISHED_ANDESITE_STAIRS = REGISTRY.registerBlock("minecraft:polished_andesite_stairs", BlockID.POLISHED_ANDESITE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy POLISHED_BASALT = REGISTRY.registerBlock("minecraft:polished_basalt", BlockID.POLISHED_BASALT)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy POLISHED_BLACKSTONE = REGISTRY.registerBlock("minecraft:polished_blackstone", BlockID.POLISHED_BLACKSTONE);
    public static final BlockLegacy POLISHED_BLACKSTONE_BRICK_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:polished_blackstone_brick_double_slab", BlockID.POLISHED_BLACKSTONE_BRICK_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy POLISHED_BLACKSTONE_BRICK_SLAB = REGISTRY.registerBlock("minecraft:polished_blackstone_brick_slab", BlockID.POLISHED_BLACKSTONE_BRICK_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy POLISHED_BLACKSTONE_BRICK_STAIRS = REGISTRY.registerBlock("minecraft:polished_blackstone_brick_stairs", BlockID.POLISHED_BLACKSTONE_BRICK_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy POLISHED_BLACKSTONE_BRICK_WALL = REGISTRY.registerBlock("minecraft:polished_blackstone_brick_wall", BlockID.POLISHED_BLACKSTONE_BRICK_WALL)
            .addState(BlockStates.WALL_POST_BIT)
            .addState(BlockStates.WALL_CONNECTION_TYPE_NORTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_EAST)
            .addState(BlockStates.WALL_CONNECTION_TYPE_SOUTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_WEST);
    public static final BlockLegacy POLISHED_BLACKSTONE_BRICKS = REGISTRY.registerBlock("minecraft:polished_blackstone_bricks", BlockID.POLISHED_BLACKSTONE_BRICKS);
    public static final BlockLegacy POLISHED_BLACKSTONE_BUTTON = REGISTRY.registerBlock("minecraft:polished_blackstone_button", BlockID.POLISHED_BLACKSTONE_BUTTON)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.BUTTON_PRESSED_BIT);
    public static final BlockLegacy POLISHED_BLACKSTONE_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:polished_blackstone_double_slab", BlockID.POLISHED_BLACKSTONE_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy POLISHED_BLACKSTONE_PRESSURE_PLATE = REGISTRY.registerBlock("minecraft:polished_blackstone_pressure_plate", BlockID.POLISHED_BLACKSTONE_PRESSURE_PLATE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy POLISHED_BLACKSTONE_SLAB = REGISTRY.registerBlock("minecraft:polished_blackstone_slab", BlockID.POLISHED_BLACKSTONE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy POLISHED_BLACKSTONE_STAIRS = REGISTRY.registerBlock("minecraft:polished_blackstone_stairs", BlockID.POLISHED_BLACKSTONE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy POLISHED_BLACKSTONE_WALL = REGISTRY.registerBlock("minecraft:polished_blackstone_wall", BlockID.POLISHED_BLACKSTONE_WALL)
            .addState(BlockStates.WALL_POST_BIT)
            .addState(BlockStates.WALL_CONNECTION_TYPE_NORTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_EAST)
            .addState(BlockStates.WALL_CONNECTION_TYPE_SOUTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_WEST);
    public static final BlockLegacy POLISHED_DEEPSLATE = REGISTRY.registerBlock("minecraft:polished_deepslate", BlockID.POLISHED_DEEPSLATE);
    public static final BlockLegacy POLISHED_DEEPSLATE_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:polished_deepslate_double_slab", BlockID.POLISHED_DEEPSLATE_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy POLISHED_DEEPSLATE_SLAB = REGISTRY.registerBlock("minecraft:polished_deepslate_slab", BlockID.POLISHED_DEEPSLATE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy POLISHED_DEEPSLATE_STAIRS = REGISTRY.registerBlock("minecraft:polished_deepslate_stairs", BlockID.POLISHED_DEEPSLATE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy POLISHED_DEEPSLATE_WALL = REGISTRY.registerBlock("minecraft:polished_deepslate_wall", BlockID.POLISHED_DEEPSLATE_WALL)
            .addState(BlockStates.WALL_POST_BIT)
            .addState(BlockStates.WALL_CONNECTION_TYPE_NORTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_EAST)
            .addState(BlockStates.WALL_CONNECTION_TYPE_SOUTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_WEST);
    public static final BlockLegacy POLISHED_DIORITE = REGISTRY.registerBlock("minecraft:polished_diorite", BlockID.POLISHED_DIORITE);
    public static final BlockLegacy POLISHED_DIORITE_STAIRS = REGISTRY.registerBlock("minecraft:polished_diorite_stairs", BlockID.POLISHED_DIORITE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy POLISHED_GRANITE = REGISTRY.registerBlock("minecraft:polished_granite", BlockID.POLISHED_GRANITE);
    public static final BlockLegacy POLISHED_GRANITE_STAIRS = REGISTRY.registerBlock("minecraft:polished_granite_stairs", BlockID.POLISHED_GRANITE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy POLISHED_TUFF = REGISTRY.registerBlock("minecraft:polished_tuff", BlockID.POLISHED_TUFF);
    public static final BlockLegacy POLISHED_TUFF_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:polished_tuff_double_slab", BlockID.POLISHED_TUFF_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy POLISHED_TUFF_SLAB = REGISTRY.registerBlock("minecraft:polished_tuff_slab", BlockID.POLISHED_TUFF_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy POLISHED_TUFF_STAIRS = REGISTRY.registerBlock("minecraft:polished_tuff_stairs", BlockID.POLISHED_TUFF_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy POLISHED_TUFF_WALL = REGISTRY.registerBlock("minecraft:polished_tuff_wall", BlockID.POLISHED_TUFF_WALL)
            .addState(BlockStates.WALL_POST_BIT)
            .addState(BlockStates.WALL_CONNECTION_TYPE_NORTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_EAST)
            .addState(BlockStates.WALL_CONNECTION_TYPE_SOUTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_WEST);
    public static final BlockLegacy POPPY = REGISTRY.registerBlock("minecraft:poppy", BlockID.POPPY);
    public static final BlockLegacy PORTAL = REGISTRY.registerBlock("minecraft:portal", BlockID.PORTAL)
            .addState(BlockStates.PORTAL_AXIS);
    public static final BlockLegacy POTATOES = REGISTRY.registerBlock("minecraft:potatoes", BlockID.POTATOES)
            .addState(BlockStates.GROWTH);
    public static final BlockLegacy POWDER_SNOW = REGISTRY.registerBlock("minecraft:powder_snow", BlockID.POWDER_SNOW);
    public static final BlockLegacy POWERED_COMPARATOR = REGISTRY.registerBlock("minecraft:powered_comparator", BlockID.POWERED_COMPARATOR)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION)
            .addState(BlockStates.OUTPUT_SUBTRACT_BIT)
            .addState(BlockStates.OUTPUT_LIT_BIT);
    public static final BlockLegacy POWERED_REPEATER = REGISTRY.registerBlock("minecraft:powered_repeater", BlockID.POWERED_REPEATER)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION)
            .addState(BlockStates.REPEATER_DELAY);
    public static final BlockLegacy PRISMARINE = REGISTRY.registerBlock("minecraft:prismarine", BlockID.PRISMARINE)
            .addState(BlockStates.PRISMARINE_BLOCK_TYPE);
    public static final BlockLegacy PRISMARINE_BRICKS_STAIRS = REGISTRY.registerBlock("minecraft:prismarine_bricks_stairs", BlockID.PRISMARINE_BRICKS_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy PRISMARINE_STAIRS = REGISTRY.registerBlock("minecraft:prismarine_stairs", BlockID.PRISMARINE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy PUMPKIN = REGISTRY.registerBlock("minecraft:pumpkin", BlockID.PUMPKIN)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy PUMPKIN_STEM = REGISTRY.registerBlock("minecraft:pumpkin_stem", BlockID.PUMPKIN_STEM)
            .addState(BlockStates.GROWTH)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy PURPLE_CANDLE = REGISTRY.registerBlock("minecraft:purple_candle", BlockID.PURPLE_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy PURPLE_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:purple_candle_cake", BlockID.PURPLE_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy PURPLE_CARPET = REGISTRY.registerBlock("minecraft:purple_carpet", BlockID.PURPLE_CARPET);
    public static final BlockLegacy PURPLE_CONCRETE = REGISTRY.registerBlock("minecraft:purple_concrete", BlockID.PURPLE_CONCRETE);
    public static final BlockLegacy PURPLE_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:purple_concrete_powder", BlockID.PURPLE_CONCRETE_POWDER);
    public static final BlockLegacy PURPLE_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:purple_glazed_terracotta", BlockID.PURPLE_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy PURPLE_SHULKER_BOX = REGISTRY.registerBlock("minecraft:purple_shulker_box", BlockID.PURPLE_SHULKER_BOX);
    public static final BlockLegacy PURPLE_STAINED_GLASS = REGISTRY.registerBlock("minecraft:purple_stained_glass", BlockID.PURPLE_STAINED_GLASS);
    public static final BlockLegacy PURPLE_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:purple_stained_glass_pane", BlockID.PURPLE_STAINED_GLASS_PANE);
    public static final BlockLegacy PURPLE_TERRACOTTA = REGISTRY.registerBlock("minecraft:purple_terracotta", BlockID.PURPLE_TERRACOTTA);
    public static final BlockLegacy PURPLE_WOOL = REGISTRY.registerBlock("minecraft:purple_wool", BlockID.PURPLE_WOOL);
    public static final BlockLegacy PURPUR_BLOCK = REGISTRY.registerBlock("minecraft:purpur_block", BlockID.PURPUR_BLOCK)
            .addState(BlockStates.CHISEL_TYPE)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy PURPUR_STAIRS = REGISTRY.registerBlock("minecraft:purpur_stairs", BlockID.PURPUR_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy QUARTZ_BLOCK = REGISTRY.registerBlock("minecraft:quartz_block", BlockID.QUARTZ_BLOCK)
            .addState(BlockStates.CHISEL_TYPE)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy QUARTZ_BRICKS = REGISTRY.registerBlock("minecraft:quartz_bricks", BlockID.QUARTZ_BRICKS);
    public static final BlockLegacy QUARTZ_ORE = REGISTRY.registerBlock("minecraft:quartz_ore", BlockID.QUARTZ_ORE);
    public static final BlockLegacy QUARTZ_SLAB = REGISTRY.registerBlock("minecraft:quartz_slab", BlockID.QUARTZ_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy QUARTZ_STAIRS = REGISTRY.registerBlock("minecraft:quartz_stairs", BlockID.QUARTZ_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy RAIL = REGISTRY.registerBlock("minecraft:rail", BlockID.RAIL)
            .addState(BlockStates.RAIL_DIRECTION);
    public static final BlockLegacy RAW_COPPER_BLOCK = REGISTRY.registerBlock("minecraft:raw_copper_block", BlockID.RAW_COPPER_BLOCK);
    public static final BlockLegacy RAW_GOLD_BLOCK = REGISTRY.registerBlock("minecraft:raw_gold_block", BlockID.RAW_GOLD_BLOCK);
    public static final BlockLegacy RAW_IRON_BLOCK = REGISTRY.registerBlock("minecraft:raw_iron_block", BlockID.RAW_IRON_BLOCK);
    public static final BlockLegacy RED_CANDLE = REGISTRY.registerBlock("minecraft:red_candle", BlockID.RED_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy RED_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:red_candle_cake", BlockID.RED_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy RED_CARPET = REGISTRY.registerBlock("minecraft:red_carpet", BlockID.RED_CARPET);
    public static final BlockLegacy RED_CONCRETE = REGISTRY.registerBlock("minecraft:red_concrete", BlockID.RED_CONCRETE);
    public static final BlockLegacy RED_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:red_concrete_powder", BlockID.RED_CONCRETE_POWDER);
    public static final BlockLegacy RED_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:red_glazed_terracotta", BlockID.RED_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy RED_MUSHROOM = REGISTRY.registerBlock("minecraft:red_mushroom", BlockID.RED_MUSHROOM);
    public static final BlockLegacy RED_MUSHROOM_BLOCK = REGISTRY.registerBlock("minecraft:red_mushroom_block", BlockID.RED_MUSHROOM_BLOCK)
            .addState(BlockStates.HUGE_MUSHROOM_BITS);
    public static final BlockLegacy RED_NETHER_BRICK = REGISTRY.registerBlock("minecraft:red_nether_brick", BlockID.RED_NETHER_BRICK);
    public static final BlockLegacy RED_NETHER_BRICK_STAIRS = REGISTRY.registerBlock("minecraft:red_nether_brick_stairs", BlockID.RED_NETHER_BRICK_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy RED_SANDSTONE = REGISTRY.registerBlock("minecraft:red_sandstone", BlockID.RED_SANDSTONE)
            .addState(BlockStates.SAND_STONE_TYPE);
    public static final BlockLegacy RED_SANDSTONE_STAIRS = REGISTRY.registerBlock("minecraft:red_sandstone_stairs", BlockID.RED_SANDSTONE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy RED_SHULKER_BOX = REGISTRY.registerBlock("minecraft:red_shulker_box", BlockID.RED_SHULKER_BOX);
    public static final BlockLegacy RED_STAINED_GLASS = REGISTRY.registerBlock("minecraft:red_stained_glass", BlockID.RED_STAINED_GLASS);
    public static final BlockLegacy RED_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:red_stained_glass_pane", BlockID.RED_STAINED_GLASS_PANE);
    public static final BlockLegacy RED_TERRACOTTA = REGISTRY.registerBlock("minecraft:red_terracotta", BlockID.RED_TERRACOTTA);
    public static final BlockLegacy RED_TULIP = REGISTRY.registerBlock("minecraft:red_tulip", BlockID.RED_TULIP);
    public static final BlockLegacy RED_WOOL = REGISTRY.registerBlock("minecraft:red_wool", BlockID.RED_WOOL);
    public static final BlockLegacy REDSTONE_BLOCK = REGISTRY.registerBlock("minecraft:redstone_block", BlockID.REDSTONE_BLOCK);
    public static final BlockLegacy REDSTONE_LAMP = REGISTRY.registerBlock("minecraft:redstone_lamp", BlockID.REDSTONE_LAMP);
    public static final BlockLegacy REDSTONE_ORE = REGISTRY.registerBlock("minecraft:redstone_ore", BlockID.REDSTONE_ORE);
    public static final BlockLegacy REDSTONE_TORCH = REGISTRY.registerBlock("minecraft:redstone_torch", BlockID.REDSTONE_TORCH)
            .addState(BlockStates.TORCH_FACING_DIRECTION);
    public static final BlockLegacy REDSTONE_WIRE = REGISTRY.registerBlock("minecraft:redstone_wire", BlockID.REDSTONE_WIRE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy REEDS = REGISTRY.registerBlock("minecraft:reeds", BlockID.BLOCK_REEDS)
            .addState(BlockStates.AGE);
    public static final BlockLegacy REINFORCED_DEEPSLATE = REGISTRY.registerBlock("minecraft:reinforced_deepslate", BlockID.REINFORCED_DEEPSLATE);
    public static final BlockLegacy REPEATING_COMMAND_BLOCK = REGISTRY.registerBlock("minecraft:repeating_command_block", BlockID.REPEATING_COMMAND_BLOCK)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.CONDITIONAL_BIT);
    public static final BlockLegacy RESERVED6 = REGISTRY.registerBlock("minecraft:reserved6", BlockID.RESERVED6);
    public static final BlockLegacy RESPAWN_ANCHOR = REGISTRY.registerBlock("minecraft:respawn_anchor", BlockID.RESPAWN_ANCHOR)
            .addState(BlockStates.RESPAWN_ANCHOR_CHARGE);
    public static final BlockLegacy ROSE_BUSH = REGISTRY.registerBlock("minecraft:rose_bush", BlockID.ROSE_BUSH)
            .addState(BlockStates.UPPER_BLOCK_BIT);
    public static final BlockLegacy SAND = REGISTRY.registerBlock("minecraft:sand", BlockID.SAND)
            .addState(BlockStates.SAND_TYPE);
    public static final BlockLegacy SANDSTONE = REGISTRY.registerBlock("minecraft:sandstone", BlockID.SANDSTONE)
            .addState(BlockStates.SAND_STONE_TYPE);
    public static final BlockLegacy SANDSTONE_SLAB = REGISTRY.registerBlock("minecraft:sandstone_slab", BlockID.SANDSTONE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy SANDSTONE_STAIRS = REGISTRY.registerBlock("minecraft:sandstone_stairs", BlockID.SANDSTONE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy SCAFFOLDING = REGISTRY.registerBlock("minecraft:scaffolding", BlockID.SCAFFOLDING)
            .addState(BlockStates.STABILITY)
            .addState(BlockStates.STABILITY_CHECK);
    public static final BlockLegacy SCULK = REGISTRY.registerBlock("minecraft:sculk", BlockID.SCULK);
    public static final BlockLegacy SCULK_CATALYST = REGISTRY.registerBlock("minecraft:sculk_catalyst", BlockID.SCULK_CATALYST)
            .addState(BlockStates.BLOOM);
    public static final BlockLegacy SCULK_SENSOR = REGISTRY.registerBlock("minecraft:sculk_sensor", BlockID.SCULK_SENSOR)
            .addState(BlockStates.SCULK_SENSOR_PHASE);
    public static final BlockLegacy SCULK_SHRIEKER = REGISTRY.registerBlock("minecraft:sculk_shrieker", BlockID.SCULK_SHRIEKER)
            .addState(BlockStates.ACTIVE)
            .addState(BlockStates.CAN_SUMMON);
    public static final BlockLegacy SCULK_VEIN = REGISTRY.registerBlock("minecraft:sculk_vein", BlockID.SCULK_VEIN)
            .addState(BlockStates.MULTI_FACE_DIRECTION_BITS);
    public static final BlockLegacy SEA_LANTERN = REGISTRY.registerBlock("minecraft:sea_lantern", BlockID.SEA_LANTERN);
    public static final BlockLegacy SEA_PICKLE = REGISTRY.registerBlock("minecraft:sea_pickle", BlockID.SEA_PICKLE)
            .addState(BlockStates.CLUSTER_COUNT)
            .addState(BlockStates.DEAD_BIT);
    public static final BlockLegacy SEAGRASS = REGISTRY.registerBlock("minecraft:seagrass", BlockID.SEAGRASS)
            .addState(BlockStates.SEA_GRASS_TYPE);
    public static final BlockLegacy SHORT_GRASS = REGISTRY.registerBlock("minecraft:short_grass", BlockID.SHORT_GRASS);
    public static final BlockLegacy SHROOMLIGHT = REGISTRY.registerBlock("minecraft:shroomlight", BlockID.SHROOMLIGHT);
    public static final BlockLegacy SILVER_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:silver_glazed_terracotta", BlockID.SILVER_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy SKULL = REGISTRY.registerBlock("minecraft:skull", BlockID.BLOCK_SKULL)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy SLIME = REGISTRY.registerBlock("minecraft:slime", BlockID.SLIME);
    public static final BlockLegacy SMALL_AMETHYST_BUD = REGISTRY.registerBlock("minecraft:small_amethyst_bud", BlockID.SMALL_AMETHYST_BUD)
            .addState(BlockStates.MINECRAFT_BLOCK_FACE);
    public static final BlockLegacy SMALL_DRIPLEAF_BLOCK = REGISTRY.registerBlock("minecraft:small_dripleaf_block", BlockID.SMALL_DRIPLEAF_BLOCK)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy SMITHING_TABLE = REGISTRY.registerBlock("minecraft:smithing_table", BlockID.SMITHING_TABLE);
    public static final BlockLegacy SMOKER = REGISTRY.registerBlock("minecraft:smoker", BlockID.SMOKER)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy SMOOTH_BASALT = REGISTRY.registerBlock("minecraft:smooth_basalt", BlockID.SMOOTH_BASALT);
    public static final BlockLegacy SMOOTH_QUARTZ_STAIRS = REGISTRY.registerBlock("minecraft:smooth_quartz_stairs", BlockID.SMOOTH_QUARTZ_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy SMOOTH_RED_SANDSTONE_STAIRS = REGISTRY.registerBlock("minecraft:smooth_red_sandstone_stairs", BlockID.SMOOTH_RED_SANDSTONE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy SMOOTH_SANDSTONE_STAIRS = REGISTRY.registerBlock("minecraft:smooth_sandstone_stairs", BlockID.SMOOTH_SANDSTONE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy SMOOTH_STONE = REGISTRY.registerBlock("minecraft:smooth_stone", BlockID.SMOOTH_STONE);
    public static final BlockLegacy SMOOTH_STONE_SLAB = REGISTRY.registerBlock("minecraft:smooth_stone_slab", BlockID.SMOOTH_STONE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy SNIFFER_EGG = REGISTRY.registerBlock("minecraft:sniffer_egg", BlockID.SNIFFER_EGG)
            .addState(BlockStates.CRACKED_STATE);
    public static final BlockLegacy SNOW = REGISTRY.registerBlock("minecraft:snow", BlockID.SNOW);
    public static final BlockLegacy SNOW_LAYER = REGISTRY.registerBlock("minecraft:snow_layer", BlockID.SNOW_LAYER)
            .addState(BlockStates.HEIGHT)
            .addState(BlockStates.COVERED_BIT);
    public static final BlockLegacy SOUL_CAMPFIRE = REGISTRY.registerBlock("minecraft:soul_campfire", BlockID.BLOCK_SOUL_CAMPFIRE)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION)
            .addState(BlockStates.EXTINGUISHED);
    public static final BlockLegacy SOUL_FIRE = REGISTRY.registerBlock("minecraft:soul_fire", BlockID.SOUL_FIRE)
            .addState(BlockStates.AGE);
    public static final BlockLegacy SOUL_LANTERN = REGISTRY.registerBlock("minecraft:soul_lantern", BlockID.SOUL_LANTERN)
            .addState(BlockStates.HANGING);
    public static final BlockLegacy SOUL_SAND = REGISTRY.registerBlock("minecraft:soul_sand", BlockID.SOUL_SAND);
    public static final BlockLegacy SOUL_SOIL = REGISTRY.registerBlock("minecraft:soul_soil", BlockID.SOUL_SOIL);
    public static final BlockLegacy SOUL_TORCH = REGISTRY.registerBlock("minecraft:soul_torch", BlockID.SOUL_TORCH)
            .addState(BlockStates.TORCH_FACING_DIRECTION);
    public static final BlockLegacy SPONGE = REGISTRY.registerBlock("minecraft:sponge", BlockID.SPONGE)
            .addState(BlockStates.SPONGE_TYPE);
    public static final BlockLegacy SPORE_BLOSSOM = REGISTRY.registerBlock("minecraft:spore_blossom", BlockID.SPORE_BLOSSOM);
    public static final BlockLegacy SPRUCE_BUTTON = REGISTRY.registerBlock("minecraft:spruce_button", BlockID.SPRUCE_BUTTON)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.BUTTON_PRESSED_BIT);
    public static final BlockLegacy SPRUCE_DOOR = REGISTRY.registerBlock("minecraft:spruce_door", BlockID.BLOCK_SPRUCE_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy SPRUCE_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:spruce_double_slab", BlockID.SPRUCE_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy SPRUCE_FENCE = REGISTRY.registerBlock("minecraft:spruce_fence", BlockID.SPRUCE_FENCE);
    public static final BlockLegacy SPRUCE_FENCE_GATE = REGISTRY.registerBlock("minecraft:spruce_fence_gate", BlockID.SPRUCE_FENCE_GATE)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.IN_WALL_BIT);
    public static final BlockLegacy SPRUCE_HANGING_SIGN = REGISTRY.registerBlock("minecraft:spruce_hanging_sign", BlockID.SPRUCE_HANGING_SIGN)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.GROUND_SIGN_DIRECTION)
            .addState(BlockStates.HANGING)
            .addState(BlockStates.ATTACHED_BIT);
    public static final BlockLegacy SPRUCE_LEAVES = REGISTRY.registerBlock("minecraft:spruce_leaves", BlockID.SPRUCE_LEAVES)
            .addState(BlockStates.UPDATE_BIT)
            .addState(BlockStates.PERSISTENT_BIT);
    public static final BlockLegacy SPRUCE_LOG = REGISTRY.registerBlock("minecraft:spruce_log", BlockID.SPRUCE_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy SPRUCE_PLANKS = REGISTRY.registerBlock("minecraft:spruce_planks", BlockID.SPRUCE_PLANKS);
    public static final BlockLegacy SPRUCE_PRESSURE_PLATE = REGISTRY.registerBlock("minecraft:spruce_pressure_plate", BlockID.SPRUCE_PRESSURE_PLATE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy SPRUCE_SAPLING = REGISTRY.registerBlock("minecraft:spruce_sapling", BlockID.SPRUCE_SAPLING)
            .addState(BlockStates.AGE_BIT);
    public static final BlockLegacy SPRUCE_SLAB = REGISTRY.registerBlock("minecraft:spruce_slab", BlockID.SPRUCE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy SPRUCE_STAIRS = REGISTRY.registerBlock("minecraft:spruce_stairs", BlockID.SPRUCE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy SPRUCE_STANDING_SIGN = REGISTRY.registerBlock("minecraft:spruce_standing_sign", BlockID.SPRUCE_STANDING_SIGN)
            .addState(BlockStates.GROUND_SIGN_DIRECTION);
    public static final BlockLegacy SPRUCE_TRAPDOOR = REGISTRY.registerBlock("minecraft:spruce_trapdoor", BlockID.SPRUCE_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy SPRUCE_WALL_SIGN = REGISTRY.registerBlock("minecraft:spruce_wall_sign", BlockID.SPRUCE_WALL_SIGN)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy SPRUCE_WOOD = REGISTRY.registerBlock("minecraft:spruce_wood", BlockID.SPRUCE_WOOD)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STANDING_BANNER = REGISTRY.registerBlock("minecraft:standing_banner", BlockID.STANDING_BANNER)
            .addState(BlockStates.GROUND_SIGN_DIRECTION);
    public static final BlockLegacy STANDING_SIGN = REGISTRY.registerBlock("minecraft:standing_sign", BlockID.STANDING_SIGN)
            .addState(BlockStates.GROUND_SIGN_DIRECTION);
    public static final BlockLegacy STICKY_PISTON = REGISTRY.registerBlock("minecraft:sticky_piston", BlockID.STICKY_PISTON)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy STICKY_PISTON_ARM_COLLISION = REGISTRY.registerBlock("minecraft:sticky_piston_arm_collision", BlockID.STICKY_PISTON_ARM_COLLISION)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy STONE = REGISTRY.registerBlock("minecraft:stone", BlockID.STONE);
    public static final BlockLegacy STONE_BLOCK_SLAB2 = REGISTRY.registerBlock("minecraft:stone_block_slab2", BlockID.STONE_BLOCK_SLAB2)
            .addState(BlockStates.STONE_SLAB_TYPE_2)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy STONE_BLOCK_SLAB3 = REGISTRY.registerBlock("minecraft:stone_block_slab3", BlockID.STONE_BLOCK_SLAB3)
            .addState(BlockStates.STONE_SLAB_TYPE_3)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy STONE_BLOCK_SLAB4 = REGISTRY.registerBlock("minecraft:stone_block_slab4", BlockID.STONE_BLOCK_SLAB4)
            .addState(BlockStates.STONE_SLAB_TYPE_4)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy STONE_BRICK_SLAB = REGISTRY.registerBlock("minecraft:stone_brick_slab", BlockID.STONE_BRICK_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy STONE_BRICK_STAIRS = REGISTRY.registerBlock("minecraft:stone_brick_stairs", BlockID.STONE_BRICK_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy STONE_BUTTON = REGISTRY.registerBlock("minecraft:stone_button", BlockID.STONE_BUTTON)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.BUTTON_PRESSED_BIT);
    public static final BlockLegacy STONE_PRESSURE_PLATE = REGISTRY.registerBlock("minecraft:stone_pressure_plate", BlockID.STONE_PRESSURE_PLATE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy STONE_STAIRS = REGISTRY.registerBlock("minecraft:stone_stairs", BlockID.STONE_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy STONEBRICK = REGISTRY.registerBlock("minecraft:stonebrick", BlockID.STONEBRICK)
            .addState(BlockStates.STONE_BRICK_TYPE);
    public static final BlockLegacy STONECUTTER = REGISTRY.registerBlock("minecraft:stonecutter", BlockID.STONECUTTER);
    public static final BlockLegacy STONECUTTER_BLOCK = REGISTRY.registerBlock("minecraft:stonecutter_block", BlockID.STONECUTTER_BLOCK)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy STRIPPED_ACACIA_LOG = REGISTRY.registerBlock("minecraft:stripped_acacia_log", BlockID.STRIPPED_ACACIA_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_ACACIA_WOOD = REGISTRY.registerBlock("minecraft:stripped_acacia_wood", BlockID.STRIPPED_ACACIA_WOOD)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_BAMBOO_BLOCK = REGISTRY.registerBlock("minecraft:stripped_bamboo_block", BlockID.STRIPPED_BAMBOO_BLOCK)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_BIRCH_LOG = REGISTRY.registerBlock("minecraft:stripped_birch_log", BlockID.STRIPPED_BIRCH_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_BIRCH_WOOD = REGISTRY.registerBlock("minecraft:stripped_birch_wood", BlockID.STRIPPED_BIRCH_WOOD)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_CHERRY_LOG = REGISTRY.registerBlock("minecraft:stripped_cherry_log", BlockID.STRIPPED_CHERRY_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_CHERRY_WOOD = REGISTRY.registerBlock("minecraft:stripped_cherry_wood", BlockID.STRIPPED_CHERRY_WOOD)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_CRIMSON_HYPHAE = REGISTRY.registerBlock("minecraft:stripped_crimson_hyphae", BlockID.STRIPPED_CRIMSON_HYPHAE)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_CRIMSON_STEM = REGISTRY.registerBlock("minecraft:stripped_crimson_stem", BlockID.STRIPPED_CRIMSON_STEM)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_DARK_OAK_LOG = REGISTRY.registerBlock("minecraft:stripped_dark_oak_log", BlockID.STRIPPED_DARK_OAK_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_DARK_OAK_WOOD = REGISTRY.registerBlock("minecraft:stripped_dark_oak_wood", BlockID.STRIPPED_DARK_OAK_WOOD)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_JUNGLE_LOG = REGISTRY.registerBlock("minecraft:stripped_jungle_log", BlockID.STRIPPED_JUNGLE_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_JUNGLE_WOOD = REGISTRY.registerBlock("minecraft:stripped_jungle_wood", BlockID.STRIPPED_JUNGLE_WOOD)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_MANGROVE_LOG = REGISTRY.registerBlock("minecraft:stripped_mangrove_log", BlockID.STRIPPED_MANGROVE_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_MANGROVE_WOOD = REGISTRY.registerBlock("minecraft:stripped_mangrove_wood", BlockID.STRIPPED_MANGROVE_WOOD)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_OAK_LOG = REGISTRY.registerBlock("minecraft:stripped_oak_log", BlockID.STRIPPED_OAK_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_OAK_WOOD = REGISTRY.registerBlock("minecraft:stripped_oak_wood", BlockID.STRIPPED_OAK_WOOD)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_SPRUCE_LOG = REGISTRY.registerBlock("minecraft:stripped_spruce_log", BlockID.STRIPPED_SPRUCE_LOG)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_SPRUCE_WOOD = REGISTRY.registerBlock("minecraft:stripped_spruce_wood", BlockID.STRIPPED_SPRUCE_WOOD)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_WARPED_HYPHAE = REGISTRY.registerBlock("minecraft:stripped_warped_hyphae", BlockID.STRIPPED_WARPED_HYPHAE)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRIPPED_WARPED_STEM = REGISTRY.registerBlock("minecraft:stripped_warped_stem", BlockID.STRIPPED_WARPED_STEM)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy STRUCTURE_BLOCK = REGISTRY.registerBlock("minecraft:structure_block", BlockID.STRUCTURE_BLOCK)
            .addState(BlockStates.STRUCTURE_BLOCK_TYPE);
    public static final BlockLegacy STRUCTURE_VOID = REGISTRY.registerBlock("minecraft:structure_void", BlockID.STRUCTURE_VOID)
            .addState(BlockStates.STRUCTURE_VOID_TYPE);
    public static final BlockLegacy SUNFLOWER = REGISTRY.registerBlock("minecraft:sunflower", BlockID.SUNFLOWER)
            .addState(BlockStates.UPPER_BLOCK_BIT);
    public static final BlockLegacy SUSPICIOUS_GRAVEL = REGISTRY.registerBlock("minecraft:suspicious_gravel", BlockID.SUSPICIOUS_GRAVEL)
            .addState(BlockStates.HANGING)
            .addState(BlockStates.BRUSHED_PROGRESS);
    public static final BlockLegacy SUSPICIOUS_SAND = REGISTRY.registerBlock("minecraft:suspicious_sand", BlockID.SUSPICIOUS_SAND)
            .addState(BlockStates.HANGING)
            .addState(BlockStates.BRUSHED_PROGRESS);
    public static final BlockLegacy SWEET_BERRY_BUSH = REGISTRY.registerBlock("minecraft:sweet_berry_bush", BlockID.SWEET_BERRY_BUSH)
            .addState(BlockStates.GROWTH);
    public static final BlockLegacy TALL_GRASS = REGISTRY.registerBlock("minecraft:tall_grass", BlockID.TALL_GRASS)
            .addState(BlockStates.UPPER_BLOCK_BIT);
    public static final BlockLegacy TARGET = REGISTRY.registerBlock("minecraft:target", BlockID.TARGET);
    public static final BlockLegacy TINTED_GLASS = REGISTRY.registerBlock("minecraft:tinted_glass", BlockID.TINTED_GLASS);
    public static final BlockLegacy TNT = REGISTRY.registerBlock("minecraft:tnt", BlockID.TNT)
            .addState(BlockStates.EXPLODE_BIT)
            .addState(BlockStates.ALLOW_UNDERWATER_BIT);
    public static final BlockLegacy TORCH = REGISTRY.registerBlock("minecraft:torch", BlockID.TORCH)
            .addState(BlockStates.TORCH_FACING_DIRECTION);
    public static final BlockLegacy TORCHFLOWER = REGISTRY.registerBlock("minecraft:torchflower", BlockID.TORCHFLOWER);
    public static final BlockLegacy TORCHFLOWER_CROP = REGISTRY.registerBlock("minecraft:torchflower_crop", BlockID.TORCHFLOWER_CROP)
            .addState(BlockStates.GROWTH);
    public static final BlockLegacy TRAPDOOR = REGISTRY.registerBlock("minecraft:trapdoor", BlockID.TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy TRAPPED_CHEST = REGISTRY.registerBlock("minecraft:trapped_chest", BlockID.TRAPPED_CHEST)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION);
    public static final BlockLegacy TRIAL_SPAWNER = REGISTRY.registerBlock("minecraft:trial_spawner", BlockID.TRIAL_SPAWNER)
            .addState(BlockStates.TRIAL_SPAWNER_STATE)
            .addState(BlockStates.OMINOUS);
    public static final BlockLegacy TRIP_WIRE = REGISTRY.registerBlock("minecraft:trip_wire", BlockID.TRIP_WIRE)
            .addState(BlockStates.POWERED_BIT)
            .addState(BlockStates.SUSPENDED_BIT)
            .addState(BlockStates.ATTACHED_BIT)
            .addState(BlockStates.DISARMED_BIT);
    public static final BlockLegacy TRIPWIRE_HOOK = REGISTRY.registerBlock("minecraft:tripwire_hook", BlockID.TRIPWIRE_HOOK)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.ATTACHED_BIT)
            .addState(BlockStates.POWERED_BIT);
    public static final BlockLegacy TUBE_CORAL = REGISTRY.registerBlock("minecraft:tube_coral", BlockID.TUBE_CORAL);
    public static final BlockLegacy TUBE_CORAL_BLOCK = REGISTRY.registerBlock("minecraft:tube_coral_block", BlockID.TUBE_CORAL_BLOCK);
    public static final BlockLegacy TUBE_CORAL_FAN = REGISTRY.registerBlock("minecraft:tube_coral_fan", BlockID.TUBE_CORAL_FAN)
            .addState(BlockStates.CORAL_FAN_DIRECTION);
    public static final BlockLegacy TUFF = REGISTRY.registerBlock("minecraft:tuff", BlockID.TUFF);
    public static final BlockLegacy TUFF_BRICK_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:tuff_brick_double_slab", BlockID.TUFF_BRICK_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy TUFF_BRICK_SLAB = REGISTRY.registerBlock("minecraft:tuff_brick_slab", BlockID.TUFF_BRICK_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy TUFF_BRICK_STAIRS = REGISTRY.registerBlock("minecraft:tuff_brick_stairs", BlockID.TUFF_BRICK_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy TUFF_BRICK_WALL = REGISTRY.registerBlock("minecraft:tuff_brick_wall", BlockID.TUFF_BRICK_WALL)
            .addState(BlockStates.WALL_POST_BIT)
            .addState(BlockStates.WALL_CONNECTION_TYPE_NORTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_EAST)
            .addState(BlockStates.WALL_CONNECTION_TYPE_SOUTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_WEST);
    public static final BlockLegacy TUFF_BRICKS = REGISTRY.registerBlock("minecraft:tuff_bricks", BlockID.TUFF_BRICKS);
    public static final BlockLegacy TUFF_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:tuff_double_slab", BlockID.TUFF_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy TUFF_SLAB = REGISTRY.registerBlock("minecraft:tuff_slab", BlockID.TUFF_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy TUFF_STAIRS = REGISTRY.registerBlock("minecraft:tuff_stairs", BlockID.TUFF_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy TUFF_WALL = REGISTRY.registerBlock("minecraft:tuff_wall", BlockID.TUFF_WALL)
            .addState(BlockStates.WALL_POST_BIT)
            .addState(BlockStates.WALL_CONNECTION_TYPE_NORTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_EAST)
            .addState(BlockStates.WALL_CONNECTION_TYPE_SOUTH)
            .addState(BlockStates.WALL_CONNECTION_TYPE_WEST);
    public static final BlockLegacy TURTLE_EGG = REGISTRY.registerBlock("minecraft:turtle_egg", BlockID.TURTLE_EGG)
            .addState(BlockStates.TURTLE_EGG_COUNT)
            .addState(BlockStates.CRACKED_STATE);
    public static final BlockLegacy TWISTING_VINES = REGISTRY.registerBlock("minecraft:twisting_vines", BlockID.TWISTING_VINES)
            .addState(BlockStates.TWISTING_VINES_AGE);
    public static final BlockLegacy UNDERWATER_TORCH = REGISTRY.registerBlock("minecraft:underwater_torch", BlockID.UNDERWATER_TORCH)
            .addState(BlockStates.TORCH_FACING_DIRECTION);
    public static final BlockLegacy UNDYED_SHULKER_BOX = REGISTRY.registerBlock("minecraft:undyed_shulker_box", BlockID.UNDYED_SHULKER_BOX);
    public static final BlockLegacy UNKNOWN = REGISTRY.registerBlock("minecraft:unknown", BlockID.UNKNOWN);
    public static final BlockLegacy UNLIT_REDSTONE_TORCH = REGISTRY.registerBlock("minecraft:unlit_redstone_torch", BlockID.UNLIT_REDSTONE_TORCH)
            .addState(BlockStates.TORCH_FACING_DIRECTION);
    public static final BlockLegacy UNPOWERED_COMPARATOR = REGISTRY.registerBlock("minecraft:unpowered_comparator", BlockID.UNPOWERED_COMPARATOR)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION)
            .addState(BlockStates.OUTPUT_SUBTRACT_BIT)
            .addState(BlockStates.OUTPUT_LIT_BIT);
    public static final BlockLegacy UNPOWERED_REPEATER = REGISTRY.registerBlock("minecraft:unpowered_repeater", BlockID.UNPOWERED_REPEATER)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION)
            .addState(BlockStates.REPEATER_DELAY);
    public static final BlockLegacy VAULT = REGISTRY.registerBlock("minecraft:vault", BlockID.VAULT)
            .addState(BlockStates.MINECRAFT_CARDINAL_DIRECTION)
            .addState(BlockStates.VAULT_STATE)
            .addState(BlockStates.OMINOUS);
    public static final BlockLegacy VERDANT_FROGLIGHT = REGISTRY.registerBlock("minecraft:verdant_froglight", BlockID.VERDANT_FROGLIGHT)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy VINE = REGISTRY.registerBlock("minecraft:vine", BlockID.VINE)
            .addState(BlockStates.VINE_DIRECTION_BITS);
    public static final BlockLegacy WALL_BANNER = REGISTRY.registerBlock("minecraft:wall_banner", BlockID.WALL_BANNER)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy WALL_SIGN = REGISTRY.registerBlock("minecraft:wall_sign", BlockID.WALL_SIGN)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy WARPED_BUTTON = REGISTRY.registerBlock("minecraft:warped_button", BlockID.WARPED_BUTTON)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.BUTTON_PRESSED_BIT);
    public static final BlockLegacy WARPED_DOOR = REGISTRY.registerBlock("minecraft:warped_door", BlockID.BLOCK_WARPED_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy WARPED_DOUBLE_SLAB = REGISTRY.registerBlock("minecraft:warped_double_slab", BlockID.WARPED_DOUBLE_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy WARPED_FENCE = REGISTRY.registerBlock("minecraft:warped_fence", BlockID.WARPED_FENCE);
    public static final BlockLegacy WARPED_FENCE_GATE = REGISTRY.registerBlock("minecraft:warped_fence_gate", BlockID.WARPED_FENCE_GATE)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.IN_WALL_BIT);
    public static final BlockLegacy WARPED_FUNGUS = REGISTRY.registerBlock("minecraft:warped_fungus", BlockID.WARPED_FUNGUS);
    public static final BlockLegacy WARPED_HANGING_SIGN = REGISTRY.registerBlock("minecraft:warped_hanging_sign", BlockID.WARPED_HANGING_SIGN)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.GROUND_SIGN_DIRECTION)
            .addState(BlockStates.HANGING)
            .addState(BlockStates.ATTACHED_BIT);
    public static final BlockLegacy WARPED_HYPHAE = REGISTRY.registerBlock("minecraft:warped_hyphae", BlockID.WARPED_HYPHAE)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy WARPED_NYLIUM = REGISTRY.registerBlock("minecraft:warped_nylium", BlockID.WARPED_NYLIUM);
    public static final BlockLegacy WARPED_PLANKS = REGISTRY.registerBlock("minecraft:warped_planks", BlockID.WARPED_PLANKS);
    public static final BlockLegacy WARPED_PRESSURE_PLATE = REGISTRY.registerBlock("minecraft:warped_pressure_plate", BlockID.WARPED_PRESSURE_PLATE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy WARPED_ROOTS = REGISTRY.registerBlock("minecraft:warped_roots", BlockID.WARPED_ROOTS);
    public static final BlockLegacy WARPED_SLAB = REGISTRY.registerBlock("minecraft:warped_slab", BlockID.WARPED_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy WARPED_STAIRS = REGISTRY.registerBlock("minecraft:warped_stairs", BlockID.WARPED_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy WARPED_STANDING_SIGN = REGISTRY.registerBlock("minecraft:warped_standing_sign", BlockID.WARPED_STANDING_SIGN)
            .addState(BlockStates.GROUND_SIGN_DIRECTION);
    public static final BlockLegacy WARPED_STEM = REGISTRY.registerBlock("minecraft:warped_stem", BlockID.WARPED_STEM)
            .addState(BlockStates.PILLAR_AXIS);
    public static final BlockLegacy WARPED_TRAPDOOR = REGISTRY.registerBlock("minecraft:warped_trapdoor", BlockID.WARPED_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy WARPED_WALL_SIGN = REGISTRY.registerBlock("minecraft:warped_wall_sign", BlockID.WARPED_WALL_SIGN)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy WARPED_WART_BLOCK = REGISTRY.registerBlock("minecraft:warped_wart_block", BlockID.WARPED_WART_BLOCK);
    public static final BlockLegacy WATER = REGISTRY.registerBlock("minecraft:water", BlockID.WATER)
            .addState(BlockStates.LIQUID_DEPTH);
    public static final BlockLegacy WATERLILY = REGISTRY.registerBlock("minecraft:waterlily", BlockID.WATERLILY);
    public static final BlockLegacy WAXED_CHISELED_COPPER = REGISTRY.registerBlock("minecraft:waxed_chiseled_copper", BlockID.WAXED_CHISELED_COPPER);
    public static final BlockLegacy WAXED_COPPER = REGISTRY.registerBlock("minecraft:waxed_copper", BlockID.WAXED_COPPER);
    public static final BlockLegacy WAXED_COPPER_BULB = REGISTRY.registerBlock("minecraft:waxed_copper_bulb", BlockID.WAXED_COPPER_BULB)
            .addState(BlockStates.LIT)
            .addState(BlockStates.POWERED_BIT);
    public static final BlockLegacy WAXED_COPPER_DOOR = REGISTRY.registerBlock("minecraft:waxed_copper_door", BlockID.WAXED_COPPER_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy WAXED_COPPER_GRATE = REGISTRY.registerBlock("minecraft:waxed_copper_grate", BlockID.WAXED_COPPER_GRATE);
    public static final BlockLegacy WAXED_COPPER_TRAPDOOR = REGISTRY.registerBlock("minecraft:waxed_copper_trapdoor", BlockID.WAXED_COPPER_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy WAXED_CUT_COPPER = REGISTRY.registerBlock("minecraft:waxed_cut_copper", BlockID.WAXED_CUT_COPPER);
    public static final BlockLegacy WAXED_CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:waxed_cut_copper_slab", BlockID.WAXED_CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy WAXED_CUT_COPPER_STAIRS = REGISTRY.registerBlock("minecraft:waxed_cut_copper_stairs", BlockID.WAXED_CUT_COPPER_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy WAXED_DOUBLE_CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:waxed_double_cut_copper_slab", BlockID.WAXED_DOUBLE_CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy WAXED_EXPOSED_CHISELED_COPPER = REGISTRY.registerBlock("minecraft:waxed_exposed_chiseled_copper", BlockID.WAXED_EXPOSED_CHISELED_COPPER);
    public static final BlockLegacy WAXED_EXPOSED_COPPER = REGISTRY.registerBlock("minecraft:waxed_exposed_copper", BlockID.WAXED_EXPOSED_COPPER);
    public static final BlockLegacy WAXED_EXPOSED_COPPER_BULB = REGISTRY.registerBlock("minecraft:waxed_exposed_copper_bulb", BlockID.WAXED_EXPOSED_COPPER_BULB)
            .addState(BlockStates.LIT)
            .addState(BlockStates.POWERED_BIT);
    public static final BlockLegacy WAXED_EXPOSED_COPPER_DOOR = REGISTRY.registerBlock("minecraft:waxed_exposed_copper_door", BlockID.WAXED_EXPOSED_COPPER_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy WAXED_EXPOSED_COPPER_GRATE = REGISTRY.registerBlock("minecraft:waxed_exposed_copper_grate", BlockID.WAXED_EXPOSED_COPPER_GRATE);
    public static final BlockLegacy WAXED_EXPOSED_COPPER_TRAPDOOR = REGISTRY.registerBlock("minecraft:waxed_exposed_copper_trapdoor", BlockID.WAXED_EXPOSED_COPPER_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy WAXED_EXPOSED_CUT_COPPER = REGISTRY.registerBlock("minecraft:waxed_exposed_cut_copper", BlockID.WAXED_EXPOSED_CUT_COPPER);
    public static final BlockLegacy WAXED_EXPOSED_CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:waxed_exposed_cut_copper_slab", BlockID.WAXED_EXPOSED_CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy WAXED_EXPOSED_CUT_COPPER_STAIRS = REGISTRY.registerBlock("minecraft:waxed_exposed_cut_copper_stairs", BlockID.WAXED_EXPOSED_CUT_COPPER_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy WAXED_EXPOSED_DOUBLE_CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:waxed_exposed_double_cut_copper_slab", BlockID.WAXED_EXPOSED_DOUBLE_CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy WAXED_OXIDIZED_CHISELED_COPPER = REGISTRY.registerBlock("minecraft:waxed_oxidized_chiseled_copper", BlockID.WAXED_OXIDIZED_CHISELED_COPPER);
    public static final BlockLegacy WAXED_OXIDIZED_COPPER = REGISTRY.registerBlock("minecraft:waxed_oxidized_copper", BlockID.WAXED_OXIDIZED_COPPER);
    public static final BlockLegacy WAXED_OXIDIZED_COPPER_BULB = REGISTRY.registerBlock("minecraft:waxed_oxidized_copper_bulb", BlockID.WAXED_OXIDIZED_COPPER_BULB)
            .addState(BlockStates.LIT)
            .addState(BlockStates.POWERED_BIT);
    public static final BlockLegacy WAXED_OXIDIZED_COPPER_DOOR = REGISTRY.registerBlock("minecraft:waxed_oxidized_copper_door", BlockID.WAXED_OXIDIZED_COPPER_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy WAXED_OXIDIZED_COPPER_GRATE = REGISTRY.registerBlock("minecraft:waxed_oxidized_copper_grate", BlockID.WAXED_OXIDIZED_COPPER_GRATE);
    public static final BlockLegacy WAXED_OXIDIZED_COPPER_TRAPDOOR = REGISTRY.registerBlock("minecraft:waxed_oxidized_copper_trapdoor", BlockID.WAXED_OXIDIZED_COPPER_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy WAXED_OXIDIZED_CUT_COPPER = REGISTRY.registerBlock("minecraft:waxed_oxidized_cut_copper", BlockID.WAXED_OXIDIZED_CUT_COPPER);
    public static final BlockLegacy WAXED_OXIDIZED_CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:waxed_oxidized_cut_copper_slab", BlockID.WAXED_OXIDIZED_CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy WAXED_OXIDIZED_CUT_COPPER_STAIRS = REGISTRY.registerBlock("minecraft:waxed_oxidized_cut_copper_stairs", BlockID.WAXED_OXIDIZED_CUT_COPPER_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy WAXED_OXIDIZED_DOUBLE_CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:waxed_oxidized_double_cut_copper_slab", BlockID.WAXED_OXIDIZED_DOUBLE_CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy WAXED_WEATHERED_CHISELED_COPPER = REGISTRY.registerBlock("minecraft:waxed_weathered_chiseled_copper", BlockID.WAXED_WEATHERED_CHISELED_COPPER);
    public static final BlockLegacy WAXED_WEATHERED_COPPER = REGISTRY.registerBlock("minecraft:waxed_weathered_copper", BlockID.WAXED_WEATHERED_COPPER);
    public static final BlockLegacy WAXED_WEATHERED_COPPER_BULB = REGISTRY.registerBlock("minecraft:waxed_weathered_copper_bulb", BlockID.WAXED_WEATHERED_COPPER_BULB)
            .addState(BlockStates.LIT)
            .addState(BlockStates.POWERED_BIT);
    public static final BlockLegacy WAXED_WEATHERED_COPPER_DOOR = REGISTRY.registerBlock("minecraft:waxed_weathered_copper_door", BlockID.WAXED_WEATHERED_COPPER_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy WAXED_WEATHERED_COPPER_GRATE = REGISTRY.registerBlock("minecraft:waxed_weathered_copper_grate", BlockID.WAXED_WEATHERED_COPPER_GRATE);
    public static final BlockLegacy WAXED_WEATHERED_COPPER_TRAPDOOR = REGISTRY.registerBlock("minecraft:waxed_weathered_copper_trapdoor", BlockID.WAXED_WEATHERED_COPPER_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy WAXED_WEATHERED_CUT_COPPER = REGISTRY.registerBlock("minecraft:waxed_weathered_cut_copper", BlockID.WAXED_WEATHERED_CUT_COPPER);
    public static final BlockLegacy WAXED_WEATHERED_CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:waxed_weathered_cut_copper_slab", BlockID.WAXED_WEATHERED_CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy WAXED_WEATHERED_CUT_COPPER_STAIRS = REGISTRY.registerBlock("minecraft:waxed_weathered_cut_copper_stairs", BlockID.WAXED_WEATHERED_CUT_COPPER_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy WAXED_WEATHERED_DOUBLE_CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:waxed_weathered_double_cut_copper_slab", BlockID.WAXED_WEATHERED_DOUBLE_CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy WEATHERED_CHISELED_COPPER = REGISTRY.registerBlock("minecraft:weathered_chiseled_copper", BlockID.WEATHERED_CHISELED_COPPER);
    public static final BlockLegacy WEATHERED_COPPER = REGISTRY.registerBlock("minecraft:weathered_copper", BlockID.WEATHERED_COPPER);
    public static final BlockLegacy WEATHERED_COPPER_BULB = REGISTRY.registerBlock("minecraft:weathered_copper_bulb", BlockID.WEATHERED_COPPER_BULB)
            .addState(BlockStates.LIT)
            .addState(BlockStates.POWERED_BIT);
    public static final BlockLegacy WEATHERED_COPPER_DOOR = REGISTRY.registerBlock("minecraft:weathered_copper_door", BlockID.WEATHERED_COPPER_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy WEATHERED_COPPER_GRATE = REGISTRY.registerBlock("minecraft:weathered_copper_grate", BlockID.WEATHERED_COPPER_GRATE);
    public static final BlockLegacy WEATHERED_COPPER_TRAPDOOR = REGISTRY.registerBlock("minecraft:weathered_copper_trapdoor", BlockID.WEATHERED_COPPER_TRAPDOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT)
            .addState(BlockStates.OPEN_BIT);
    public static final BlockLegacy WEATHERED_CUT_COPPER = REGISTRY.registerBlock("minecraft:weathered_cut_copper", BlockID.WEATHERED_CUT_COPPER);
    public static final BlockLegacy WEATHERED_CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:weathered_cut_copper_slab", BlockID.WEATHERED_CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy WEATHERED_CUT_COPPER_STAIRS = REGISTRY.registerBlock("minecraft:weathered_cut_copper_stairs", BlockID.WEATHERED_CUT_COPPER_STAIRS)
            .addState(BlockStates.WEIRDO_DIRECTION)
            .addState(BlockStates.UPSIDE_DOWN_BIT);
    public static final BlockLegacy WEATHERED_DOUBLE_CUT_COPPER_SLAB = REGISTRY.registerBlock("minecraft:weathered_double_cut_copper_slab", BlockID.WEATHERED_DOUBLE_CUT_COPPER_SLAB)
            .addState(BlockStates.MINECRAFT_VERTICAL_HALF);
    public static final BlockLegacy WEB = REGISTRY.registerBlock("minecraft:web", BlockID.WEB);
    public static final BlockLegacy WEEPING_VINES = REGISTRY.registerBlock("minecraft:weeping_vines", BlockID.WEEPING_VINES)
            .addState(BlockStates.WEEPING_VINES_AGE);
    public static final BlockLegacy WHEAT = REGISTRY.registerBlock("minecraft:wheat", BlockID.BLOCK_WHEAT)
            .addState(BlockStates.GROWTH);
    public static final BlockLegacy WHITE_CANDLE = REGISTRY.registerBlock("minecraft:white_candle", BlockID.WHITE_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy WHITE_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:white_candle_cake", BlockID.WHITE_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy WHITE_CARPET = REGISTRY.registerBlock("minecraft:white_carpet", BlockID.WHITE_CARPET);
    public static final BlockLegacy WHITE_CONCRETE = REGISTRY.registerBlock("minecraft:white_concrete", BlockID.WHITE_CONCRETE);
    public static final BlockLegacy WHITE_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:white_concrete_powder", BlockID.WHITE_CONCRETE_POWDER);
    public static final BlockLegacy WHITE_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:white_glazed_terracotta", BlockID.WHITE_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy WHITE_SHULKER_BOX = REGISTRY.registerBlock("minecraft:white_shulker_box", BlockID.WHITE_SHULKER_BOX);
    public static final BlockLegacy WHITE_STAINED_GLASS = REGISTRY.registerBlock("minecraft:white_stained_glass", BlockID.WHITE_STAINED_GLASS);
    public static final BlockLegacy WHITE_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:white_stained_glass_pane", BlockID.WHITE_STAINED_GLASS_PANE);
    public static final BlockLegacy WHITE_TERRACOTTA = REGISTRY.registerBlock("minecraft:white_terracotta", BlockID.WHITE_TERRACOTTA);
    public static final BlockLegacy WHITE_TULIP = REGISTRY.registerBlock("minecraft:white_tulip", BlockID.WHITE_TULIP);
    public static final BlockLegacy WHITE_WOOL = REGISTRY.registerBlock("minecraft:white_wool", BlockID.WHITE_WOOL);
    public static final BlockLegacy WITHER_ROSE = REGISTRY.registerBlock("minecraft:wither_rose", BlockID.WITHER_ROSE);
    public static final BlockLegacy WOODEN_BUTTON = REGISTRY.registerBlock("minecraft:wooden_button", BlockID.WOODEN_BUTTON)
            .addState(BlockStates.FACING_DIRECTION)
            .addState(BlockStates.BUTTON_PRESSED_BIT);
    public static final BlockLegacy WOODEN_DOOR = REGISTRY.registerBlock("minecraft:wooden_door", BlockID.BLOCK_WOODEN_DOOR)
            .addState(BlockStates.DIRECTION)
            .addState(BlockStates.OPEN_BIT)
            .addState(BlockStates.UPPER_BLOCK_BIT)
            .addState(BlockStates.DOOR_HINGE_BIT);
    public static final BlockLegacy WOODEN_PRESSURE_PLATE = REGISTRY.registerBlock("minecraft:wooden_pressure_plate", BlockID.WOODEN_PRESSURE_PLATE)
            .addState(BlockStates.REDSTONE_SIGNAL);
    public static final BlockLegacy YELLOW_CANDLE = REGISTRY.registerBlock("minecraft:yellow_candle", BlockID.YELLOW_CANDLE)
            .addState(BlockStates.CANDLES)
            .addState(BlockStates.LIT);
    public static final BlockLegacy YELLOW_CANDLE_CAKE = REGISTRY.registerBlock("minecraft:yellow_candle_cake", BlockID.YELLOW_CANDLE_CAKE)
            .addState(BlockStates.LIT);
    public static final BlockLegacy YELLOW_CARPET = REGISTRY.registerBlock("minecraft:yellow_carpet", BlockID.YELLOW_CARPET);
    public static final BlockLegacy YELLOW_CONCRETE = REGISTRY.registerBlock("minecraft:yellow_concrete", BlockID.YELLOW_CONCRETE);
    public static final BlockLegacy YELLOW_CONCRETE_POWDER = REGISTRY.registerBlock("minecraft:yellow_concrete_powder", BlockID.YELLOW_CONCRETE_POWDER);
    public static final BlockLegacy YELLOW_FLOWER = REGISTRY.registerBlock("minecraft:yellow_flower", BlockID.YELLOW_FLOWER);
    public static final BlockLegacy YELLOW_GLAZED_TERRACOTTA = REGISTRY.registerBlock("minecraft:yellow_glazed_terracotta", BlockID.YELLOW_GLAZED_TERRACOTTA)
            .addState(BlockStates.FACING_DIRECTION);
    public static final BlockLegacy YELLOW_SHULKER_BOX = REGISTRY.registerBlock("minecraft:yellow_shulker_box", BlockID.YELLOW_SHULKER_BOX);
    public static final BlockLegacy YELLOW_STAINED_GLASS = REGISTRY.registerBlock("minecraft:yellow_stained_glass", BlockID.YELLOW_STAINED_GLASS);
    public static final BlockLegacy YELLOW_STAINED_GLASS_PANE = REGISTRY.registerBlock("minecraft:yellow_stained_glass_pane", BlockID.YELLOW_STAINED_GLASS_PANE);
    public static final BlockLegacy YELLOW_TERRACOTTA = REGISTRY.registerBlock("minecraft:yellow_terracotta", BlockID.YELLOW_TERRACOTTA);
    public static final BlockLegacy YELLOW_WOOL = REGISTRY.registerBlock("minecraft:yellow_wool", BlockID.YELLOW_WOOL);

    static {
        REGISTRY.createBlockPermutations();
    }

    public static BlockRegistry getBlockRegistry() {
        return REGISTRY;
    }

    public static class V1_21_20 extends BlockTypes {
        private static final BlockRegistry REGISTRY = new BlockRegistry((1 << 24) | (21 << 16) | (20 << 8) | 6, BlockTypes.REGISTRY);

        public static final BlockLegacy CHIPPED_ANVIL = REGISTRY.flatten(BlockTypes.ANVIL, "minecraft:chipped_anvil", BlockID.CHIPPED_ANVIL,
                BlockStates.DAMAGE);
        public static final BlockLegacy DAMAGED_ANVIL = REGISTRY.flatten(BlockTypes.ANVIL, "minecraft:damaged_anvil", BlockID.DAMAGED_ANVIL,
                BlockStates.DAMAGE);
        public static final BlockLegacy DEPRECATED_ANVIL = REGISTRY.flatten(BlockTypes.ANVIL, "minecraft:deprecated_anvil", BlockID.DEPRECATED_ANVIL,
                BlockStates.DAMAGE);
        public static final BlockLegacy ANVIL = REGISTRY.flatten(BlockTypes.ANVIL, "minecraft:anvil", BlockID.ANVIL,
                BlockStates.DAMAGE);

        public static final BlockLegacy TUBE_CORAL_WALL_FAN = REGISTRY.flatten(BlockTypes.CORAL_FAN_HANG, "minecraft:tube_coral_wall_fan", BlockID.TUBE_CORAL_WALL_FAN,
                BlockStates.CORAL_HANG_TYPE_BIT,
                BlockStates.DEAD_BIT);
        public static final BlockLegacy BRAIN_CORAL_WALL_FAN = REGISTRY.flatten(BlockTypes.CORAL_FAN_HANG, "minecraft:brain_coral_wall_fan", BlockID.BRAIN_CORAL_WALL_FAN,
                BlockStates.CORAL_HANG_TYPE_BIT,
                BlockStates.DEAD_BIT);
        public static final BlockLegacy DEAD_TUBE_CORAL_WALL_FAN = REGISTRY.flatten(BlockTypes.CORAL_FAN_HANG, "minecraft:dead_tube_coral_wall_fan", BlockID.DEAD_TUBE_CORAL_WALL_FAN,
                BlockStates.CORAL_HANG_TYPE_BIT,
                BlockStates.DEAD_BIT);
        public static final BlockLegacy DEAD_BRAIN_CORAL_WALL_FAN = REGISTRY.flatten(BlockTypes.CORAL_FAN_HANG, "minecraft:dead_brain_coral_wall_fan", BlockID.DEAD_BRAIN_CORAL_WALL_FAN,
                BlockStates.CORAL_HANG_TYPE_BIT,
                BlockStates.DEAD_BIT);
        public static final BlockLegacy CORAL_FAN_HANG = TUBE_CORAL_WALL_FAN;

        public static final BlockLegacy BUBBLE_CORAL_WALL_FAN = REGISTRY.flatten(BlockTypes.CORAL_FAN_HANG2, "minecraft:bubble_coral_wall_fan", BlockID.BUBBLE_CORAL_WALL_FAN,
                BlockStates.CORAL_HANG_TYPE_BIT,
                BlockStates.DEAD_BIT);
        public static final BlockLegacy FIRE_CORAL_WALL_FAN = REGISTRY.flatten(BlockTypes.CORAL_FAN_HANG2, "minecraft:fire_coral_wall_fan", BlockID.FIRE_CORAL_WALL_FAN,
                BlockStates.CORAL_HANG_TYPE_BIT,
                BlockStates.DEAD_BIT);
        public static final BlockLegacy DEAD_BUBBLE_CORAL_WALL_FAN = REGISTRY.flatten(BlockTypes.CORAL_FAN_HANG2, "minecraft:dead_bubble_coral_wall_fan", BlockID.DEAD_BUBBLE_CORAL_WALL_FAN,
                BlockStates.CORAL_HANG_TYPE_BIT,
                BlockStates.DEAD_BIT);
        public static final BlockLegacy DEAD_FIRE_CORAL_WALL_FAN = REGISTRY.flatten(BlockTypes.CORAL_FAN_HANG2, "minecraft:dead_fire_coral_wall_fan", BlockID.DEAD_FIRE_CORAL_WALL_FAN,
                BlockStates.CORAL_HANG_TYPE_BIT,
                BlockStates.DEAD_BIT);
        public static final BlockLegacy CORAL_FAN_HANG2 = BUBBLE_CORAL_WALL_FAN;

        public static final BlockLegacy HORN_CORAL_WALL_FAN = REGISTRY.flatten(BlockTypes.CORAL_FAN_HANG3, "minecraft:horn_coral_wall_fan", BlockID.HORN_CORAL_WALL_FAN,
                BlockStates.CORAL_HANG_TYPE_BIT,
                BlockStates.DEAD_BIT);
        public static final BlockLegacy DEAD_HORN_CORAL_WALL_FAN = REGISTRY.flatten(BlockTypes.CORAL_FAN_HANG3, "minecraft:dead_horn_coral_wall_fan", BlockID.DEAD_HORN_CORAL_WALL_FAN,
                BlockStates.CORAL_HANG_TYPE_BIT,
                BlockStates.DEAD_BIT);
        public static final BlockLegacy CORAL_FAN_HANG3 = HORN_CORAL_WALL_FAN;

        public static final BlockLegacy COARSE_DIRT = REGISTRY.flatten(BlockTypes.DIRT, "minecraft:coarse_dirt", BlockID.COARSE_DIRT,
                BlockStates.DIRT_TYPE);
        public static final BlockLegacy DIRT = REGISTRY.flatten(BlockTypes.DIRT, "minecraft:dirt", BlockID.DIRT,
                BlockStates.DIRT_TYPE);

        public static final BlockLegacy SMOOTH_STONE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB, "minecraft:smooth_stone_double_slab", BlockID.SMOOTH_STONE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE);
        public static final BlockLegacy SANDSTONE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB, "minecraft:sandstone_double_slab", BlockID.SANDSTONE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE);
        public static final BlockLegacy PETRIFIED_OAK_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB, "minecraft:petrified_oak_double_slab", BlockID.PETRIFIED_OAK_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE);
        public static final BlockLegacy COBBLESTONE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB, "minecraft:cobblestone_double_slab", BlockID.COBBLESTONE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE);
        public static final BlockLegacy BRICK_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB, "minecraft:brick_double_slab", BlockID.BRICK_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE);
        public static final BlockLegacy STONE_BRICK_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB, "minecraft:stone_brick_double_slab", BlockID.STONE_BRICK_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE);
        public static final BlockLegacy QUARTZ_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB, "minecraft:quartz_double_slab", BlockID.QUARTZ_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE);
        public static final BlockLegacy NETHER_BRICK_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB, "minecraft:nether_brick_double_slab", BlockID.NETHER_BRICK_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE);
        public static final BlockLegacy DOUBLE_STONE_BLOCK_SLAB = SMOOTH_STONE_DOUBLE_SLAB;

        public static final BlockLegacy RED_SANDSTONE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB2, "minecraft:red_sandstone_double_slab", BlockID.RED_SANDSTONE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy PURPUR_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB2, "minecraft:purpur_double_slab", BlockID.PURPUR_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy PRISMARINE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB2, "minecraft:prismarine_double_slab", BlockID.PRISMARINE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy DARK_PRISMARINE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB2, "minecraft:dark_prismarine_double_slab", BlockID.DARK_PRISMARINE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy PRISMARINE_BRICK_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB2, "minecraft:prismarine_brick_double_slab", BlockID.PRISMARINE_BRICK_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy MOSSY_COBBLESTONE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB2, "minecraft:mossy_cobblestone_double_slab", BlockID.MOSSY_COBBLESTONE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy SMOOTH_SANDSTONE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB2, "minecraft:smooth_sandstone_double_slab", BlockID.SMOOTH_SANDSTONE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy RED_NETHER_BRICK_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB2, "minecraft:red_nether_brick_double_slab", BlockID.RED_NETHER_BRICK_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy DOUBLE_STONE_BLOCK_SLAB2 = RED_SANDSTONE_DOUBLE_SLAB;

        public static final BlockLegacy END_STONE_BRICK_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB3, "minecraft:end_stone_brick_double_slab", BlockID.END_STONE_BRICK_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy SMOOTH_RED_SANDSTONE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB3, "minecraft:smooth_red_sandstone_double_slab", BlockID.SMOOTH_RED_SANDSTONE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy POLISHED_ANDESITE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB3, "minecraft:polished_andesite_double_slab", BlockID.POLISHED_ANDESITE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy ANDESITE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB3, "minecraft:andesite_double_slab", BlockID.ANDESITE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy DIORITE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB3, "minecraft:diorite_double_slab", BlockID.DIORITE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy POLISHED_DIORITE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB3, "minecraft:polished_diorite_double_slab", BlockID.POLISHED_DIORITE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy GRANITE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB3, "minecraft:granite_double_slab", BlockID.GRANITE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy POLISHED_GRANITE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB3, "minecraft:polished_granite_double_slab", BlockID.POLISHED_GRANITE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy DOUBLE_STONE_BLOCK_SLAB3 = END_STONE_BRICK_DOUBLE_SLAB;

        public static final BlockLegacy MOSSY_STONE_BRICK_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB4, "minecraft:mossy_stone_brick_double_slab", BlockID.MOSSY_STONE_BRICK_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_4);
        public static final BlockLegacy SMOOTH_QUARTZ_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB4, "minecraft:smooth_quartz_double_slab", BlockID.SMOOTH_QUARTZ_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_4);
        public static final BlockLegacy NORMAL_STONE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB4, "minecraft:normal_stone_double_slab", BlockID.NORMAL_STONE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_4);
        public static final BlockLegacy CUT_SANDSTONE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB4, "minecraft:cut_sandstone_double_slab", BlockID.CUT_SANDSTONE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_4);
        public static final BlockLegacy CUT_RED_SANDSTONE_DOUBLE_SLAB = REGISTRY.flatten(BlockTypes.DOUBLE_STONE_BLOCK_SLAB4, "minecraft:cut_red_sandstone_double_slab", BlockID.CUT_RED_SANDSTONE_DOUBLE_SLAB,
                BlockStates.STONE_SLAB_TYPE_4);
        public static final BlockLegacy DOUBLE_STONE_BLOCK_SLAB4 = MOSSY_STONE_BRICK_DOUBLE_SLAB;

        public static final BlockLegacy LIGHT_BLOCK_0 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_0", BlockID.LIGHT_BLOCK_0,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK_1 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_1", BlockID.LIGHT_BLOCK_1,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK_2 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_2", BlockID.LIGHT_BLOCK_2,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK_3 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_3", BlockID.LIGHT_BLOCK_3,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK_4 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_4", BlockID.LIGHT_BLOCK_4,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK_5 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_5", BlockID.LIGHT_BLOCK_5,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK_6 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_6", BlockID.LIGHT_BLOCK_6,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK_7 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_7", BlockID.LIGHT_BLOCK_7,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK_8 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_8", BlockID.LIGHT_BLOCK_8,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK_9 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_9", BlockID.LIGHT_BLOCK_9,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK_10 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_10", BlockID.LIGHT_BLOCK_10,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK_11 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_11", BlockID.LIGHT_BLOCK_11,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK_12 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_12", BlockID.LIGHT_BLOCK_12,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK_13 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_13", BlockID.LIGHT_BLOCK_13,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK_14 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_14", BlockID.LIGHT_BLOCK_14,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK_15 = REGISTRY.flatten(BlockTypes.LIGHT_BLOCK, "minecraft:light_block_15", BlockID.LIGHT_BLOCK_15,
                BlockStates.BLOCK_LIGHT_LEVEL);
        public static final BlockLegacy LIGHT_BLOCK = LIGHT_BLOCK_0;

        public static final BlockLegacy INFESTED_STONE = REGISTRY.flatten(BlockTypes.MONSTER_EGG, "minecraft:infested_stone", BlockID.INFESTED_STONE,
                BlockStates.MONSTER_EGG_STONE_TYPE);
        public static final BlockLegacy INFESTED_COBBLESTONE = REGISTRY.flatten(BlockTypes.MONSTER_EGG, "minecraft:infested_cobblestone", BlockID.INFESTED_COBBLESTONE,
                BlockStates.MONSTER_EGG_STONE_TYPE);
        public static final BlockLegacy INFESTED_STONE_BRICKS = REGISTRY.flatten(BlockTypes.MONSTER_EGG, "minecraft:infested_stone_bricks", BlockID.INFESTED_STONE_BRICKS,
                BlockStates.MONSTER_EGG_STONE_TYPE);
        public static final BlockLegacy INFESTED_MOSSY_STONE_BRICKS = REGISTRY.flatten(BlockTypes.MONSTER_EGG, "minecraft:infested_mossy_stone_bricks", BlockID.INFESTED_MOSSY_STONE_BRICKS,
                BlockStates.MONSTER_EGG_STONE_TYPE);
        public static final BlockLegacy INFESTED_CRACKED_STONE_BRICKS = REGISTRY.flatten(BlockTypes.MONSTER_EGG, "minecraft:infested_cracked_stone_bricks", BlockID.INFESTED_CRACKED_STONE_BRICKS,
                BlockStates.MONSTER_EGG_STONE_TYPE);
        public static final BlockLegacy INFESTED_CHISELED_STONE_BRICKS = REGISTRY.flatten(BlockTypes.MONSTER_EGG, "minecraft:infested_chiseled_stone_bricks", BlockID.INFESTED_CHISELED_STONE_BRICKS,
                BlockStates.MONSTER_EGG_STONE_TYPE);
        public static final BlockLegacy MONSTER_EGG = INFESTED_STONE;

        public static final BlockLegacy DARK_PRISMARINE = REGISTRY.flatten(BlockTypes.PRISMARINE, "minecraft:dark_prismarine", BlockID.DARK_PRISMARINE,
                BlockStates.PRISMARINE_BLOCK_TYPE);
        public static final BlockLegacy PRISMARINE_BRICKS = REGISTRY.flatten(BlockTypes.PRISMARINE, "minecraft:prismarine_bricks", BlockID.PRISMARINE_BRICKS,
                BlockStates.PRISMARINE_BLOCK_TYPE);
        public static final BlockLegacy PRISMARINE = REGISTRY.flatten(BlockTypes.PRISMARINE, "minecraft:prismarine", BlockID.PRISMARINE,
                BlockStates.PRISMARINE_BLOCK_TYPE);

        public static final BlockLegacy CHISELED_QUARTZ_BLOCK = REGISTRY.flatten(BlockTypes.QUARTZ_BLOCK, "minecraft:chiseled_quartz_block", BlockID.CHISELED_QUARTZ_BLOCK,
                BlockStates.CHISEL_TYPE);
        public static final BlockLegacy QUARTZ_PILLAR = REGISTRY.flatten(BlockTypes.QUARTZ_BLOCK, "minecraft:quartz_pillar", BlockID.QUARTZ_PILLAR,
                BlockStates.CHISEL_TYPE);
        public static final BlockLegacy SMOOTH_QUARTZ = REGISTRY.flatten(BlockTypes.QUARTZ_BLOCK, "minecraft:smooth_quartz", BlockID.SMOOTH_QUARTZ,
                BlockStates.CHISEL_TYPE);
        public static final BlockLegacy QUARTZ_BLOCK = REGISTRY.flatten(BlockTypes.QUARTZ_BLOCK, "minecraft:quartz_block", BlockID.QUARTZ_BLOCK,
                BlockStates.CHISEL_TYPE);

        public static final BlockLegacy CHISELED_RED_SANDSTONE = REGISTRY.flatten(BlockTypes.RED_SANDSTONE, "minecraft:chiseled_red_sandstone", BlockID.CHISELED_RED_SANDSTONE,
                BlockStates.SAND_STONE_TYPE);
        public static final BlockLegacy CUT_RED_SANDSTONE = REGISTRY.flatten(BlockTypes.RED_SANDSTONE, "minecraft:cut_red_sandstone", BlockID.CUT_RED_SANDSTONE,
                BlockStates.SAND_STONE_TYPE);
        public static final BlockLegacy SMOOTH_RED_SANDSTONE = REGISTRY.flatten(BlockTypes.RED_SANDSTONE, "minecraft:smooth_red_sandstone", BlockID.SMOOTH_RED_SANDSTONE,
                BlockStates.SAND_STONE_TYPE);
        public static final BlockLegacy RED_SANDSTONE = REGISTRY.flatten(BlockTypes.RED_SANDSTONE, "minecraft:red_sandstone", BlockID.RED_SANDSTONE,
                BlockStates.SAND_STONE_TYPE);

        public static final BlockLegacy RED_SAND = REGISTRY.flatten(BlockTypes.SAND, "minecraft:red_sand", BlockID.RED_SAND,
                BlockStates.SAND_TYPE);
        public static final BlockLegacy SAND = REGISTRY.flatten(BlockTypes.SAND, "minecraft:sand", BlockID.SAND,
                BlockStates.SAND_TYPE);

        public static final BlockLegacy CHISELED_SANDSTONE = REGISTRY.flatten(BlockTypes.SANDSTONE, "minecraft:chiseled_sandstone", BlockID.CHISELED_SANDSTONE,
                BlockStates.SAND_STONE_TYPE);
        public static final BlockLegacy CUT_SANDSTONE = REGISTRY.flatten(BlockTypes.SANDSTONE, "minecraft:cut_sandstone", BlockID.CUT_SANDSTONE,
                BlockStates.SAND_STONE_TYPE);
        public static final BlockLegacy SMOOTH_SANDSTONE = REGISTRY.flatten(BlockTypes.SANDSTONE, "minecraft:smooth_sandstone", BlockID.SMOOTH_SANDSTONE,
                BlockStates.SAND_STONE_TYPE);
        public static final BlockLegacy SANDSTONE = REGISTRY.flatten(BlockTypes.SANDSTONE, "minecraft:sandstone", BlockID.SANDSTONE,
                BlockStates.SAND_STONE_TYPE);

        public static final BlockLegacy RED_SANDSTONE_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB2, "minecraft:red_sandstone_slab", BlockID.RED_SANDSTONE_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy PURPUR_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB2, "minecraft:purpur_slab", BlockID.PURPUR_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy PRISMARINE_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB2, "minecraft:prismarine_slab", BlockID.PRISMARINE_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy DARK_PRISMARINE_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB2, "minecraft:dark_prismarine_slab", BlockID.DARK_PRISMARINE_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy PRISMARINE_BRICK_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB2, "minecraft:prismarine_brick_slab", BlockID.PRISMARINE_BRICK_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy MOSSY_COBBLESTONE_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB2, "minecraft:mossy_cobblestone_slab", BlockID.MOSSY_COBBLESTONE_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy SMOOTH_SANDSTONE_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB2, "minecraft:smooth_sandstone_slab", BlockID.SMOOTH_SANDSTONE_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy RED_NETHER_BRICK_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB2, "minecraft:red_nether_brick_slab", BlockID.RED_NETHER_BRICK_SLAB,
                BlockStates.STONE_SLAB_TYPE_2);
        public static final BlockLegacy STONE_BLOCK_SLAB2 = RED_SANDSTONE_SLAB;

        public static final BlockLegacy END_STONE_BRICK_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB3, "minecraft:end_stone_brick_slab", BlockID.END_STONE_BRICK_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy SMOOTH_RED_SANDSTONE_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB3, "minecraft:smooth_red_sandstone_slab", BlockID.SMOOTH_RED_SANDSTONE_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy POLISHED_ANDESITE_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB3, "minecraft:polished_andesite_slab", BlockID.POLISHED_ANDESITE_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy ANDESITE_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB3, "minecraft:andesite_slab", BlockID.ANDESITE_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy DIORITE_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB3, "minecraft:diorite_slab", BlockID.DIORITE_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy POLISHED_DIORITE_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB3, "minecraft:polished_diorite_slab", BlockID.POLISHED_DIORITE_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy GRANITE_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB3, "minecraft:granite_slab", BlockID.GRANITE_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy POLISHED_GRANITE_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB3, "minecraft:polished_granite_slab", BlockID.POLISHED_GRANITE_SLAB,
                BlockStates.STONE_SLAB_TYPE_3);
        public static final BlockLegacy STONE_BLOCK_SLAB3 = END_STONE_BRICK_SLAB;

        public static final BlockLegacy MOSSY_STONE_BRICK_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB4, "minecraft:mossy_stone_brick_slab", BlockID.MOSSY_STONE_BRICK_SLAB,
                BlockStates.STONE_SLAB_TYPE_4);
        public static final BlockLegacy SMOOTH_QUARTZ_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB4, "minecraft:smooth_quartz_slab", BlockID.SMOOTH_QUARTZ_SLAB,
                BlockStates.STONE_SLAB_TYPE_4);
        public static final BlockLegacy NORMAL_STONE_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB4, "minecraft:normal_stone_slab", BlockID.NORMAL_STONE_SLAB,
                BlockStates.STONE_SLAB_TYPE_4);
        public static final BlockLegacy CUT_SANDSTONE_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB4, "minecraft:cut_sandstone_slab", BlockID.CUT_SANDSTONE_SLAB,
                BlockStates.STONE_SLAB_TYPE_4);
        public static final BlockLegacy CUT_RED_SANDSTONE_SLAB = REGISTRY.flatten(BlockTypes.STONE_BLOCK_SLAB4, "minecraft:cut_red_sandstone_slab", BlockID.CUT_RED_SANDSTONE_SLAB,
                BlockStates.STONE_SLAB_TYPE_4);
        public static final BlockLegacy STONE_BLOCK_SLAB4 = MOSSY_STONE_BRICK_SLAB;

        public static final BlockLegacy STONE_BRICKS = REGISTRY.flatten(BlockTypes.STONEBRICK, "minecraft:stone_bricks", BlockID.STONE_BRICKS,
                BlockStates.STONE_BRICK_TYPE);
        public static final BlockLegacy MOSSY_STONE_BRICKS = REGISTRY.flatten(BlockTypes.STONEBRICK, "minecraft:mossy_stone_bricks", BlockID.MOSSY_STONE_BRICKS,
                BlockStates.STONE_BRICK_TYPE);
        public static final BlockLegacy CRACKED_STONE_BRICKS = REGISTRY.flatten(BlockTypes.STONEBRICK, "minecraft:cracked_stone_bricks", BlockID.CRACKED_STONE_BRICKS,
                BlockStates.STONE_BRICK_TYPE);
        public static final BlockLegacy CHISELED_STONE_BRICKS = REGISTRY.flatten(BlockTypes.STONEBRICK, "minecraft:chiseled_stone_bricks", BlockID.CHISELED_STONE_BRICKS,
                BlockStates.STONE_BRICK_TYPE);
        public static final BlockLegacy STONEBRICK = STONE_BRICKS;

        public static final BlockLegacy DANDELION = REGISTRY.rename(BlockTypes.YELLOW_FLOWER, "minecraft:dandelion");
        public static final BlockLegacy YELLOW_FLOWER = DANDELION;

        static {
            REGISTRY.createBlockPermutations();
        }

        public static BlockRegistry getBlockRegistry() {
            return REGISTRY;
        }
    }

    public static class V1_21_30 extends V1_21_20 {
        private static final BlockRegistry REGISTRY = new BlockRegistry((1 << 24) | (21 << 16) | (30 << 8) | 7, V1_21_20.REGISTRY);

        public static final BlockLegacy COMPOUND_CREATOR = REGISTRY.flatten(V1_21_20.CHEMISTRY_TABLE, "minecraft:compound_creator", BlockID.COMPOUND_CREATOR,
                BlockStates.CHEMISTRY_TABLE_TYPE);
        public static final BlockLegacy MATERIAL_REDUCER = REGISTRY.flatten(V1_21_20.CHEMISTRY_TABLE, "minecraft:material_reducer", BlockID.MATERIAL_REDUCER,
                BlockStates.CHEMISTRY_TABLE_TYPE);
        public static final BlockLegacy ELEMENT_CONSTRUCTOR = REGISTRY.flatten(V1_21_20.CHEMISTRY_TABLE, "minecraft:element_constructor", BlockID.ELEMENT_CONSTRUCTOR,
                BlockStates.CHEMISTRY_TABLE_TYPE);
        public static final BlockLegacy LAB_TABLE = REGISTRY.flatten(V1_21_20.CHEMISTRY_TABLE, "minecraft:lab_table", BlockID.LAB_TABLE,
                BlockStates.CHEMISTRY_TABLE_TYPE);
        public static final BlockLegacy CHEMISTRY_TABLE = COMPOUND_CREATOR;

        public static final BlockLegacy MOSSY_COBBLESTONE_WALL = REGISTRY.flatten(V1_21_20.COBBLESTONE_WALL, "minecraft:mossy_cobblestone_wall", BlockID.MOSSY_COBBLESTONE_WALL,
                BlockStates.WALL_BLOCK_TYPE);
        public static final BlockLegacy GRANITE_WALL = REGISTRY.flatten(V1_21_20.COBBLESTONE_WALL, "minecraft:granite_wall", BlockID.GRANITE_WALL,
                BlockStates.WALL_BLOCK_TYPE);
        public static final BlockLegacy DIORITE_WALL = REGISTRY.flatten(V1_21_20.COBBLESTONE_WALL, "minecraft:diorite_wall", BlockID.DIORITE_WALL,
                BlockStates.WALL_BLOCK_TYPE);
        public static final BlockLegacy ANDESITE_WALL = REGISTRY.flatten(V1_21_20.COBBLESTONE_WALL, "minecraft:andesite_wall", BlockID.ANDESITE_WALL,
                BlockStates.WALL_BLOCK_TYPE);
        public static final BlockLegacy SANDSTONE_WALL = REGISTRY.flatten(V1_21_20.COBBLESTONE_WALL, "minecraft:sandstone_wall", BlockID.SANDSTONE_WALL,
                BlockStates.WALL_BLOCK_TYPE);
        public static final BlockLegacy BRICK_WALL = REGISTRY.flatten(V1_21_20.COBBLESTONE_WALL, "minecraft:brick_wall", BlockID.BRICK_WALL,
                BlockStates.WALL_BLOCK_TYPE);
        public static final BlockLegacy STONE_BRICK_WALL = REGISTRY.flatten(V1_21_20.COBBLESTONE_WALL, "minecraft:stone_brick_wall", BlockID.STONE_BRICK_WALL,
                BlockStates.WALL_BLOCK_TYPE);
        public static final BlockLegacy MOSSY_STONE_BRICK_WALL = REGISTRY.flatten(V1_21_20.COBBLESTONE_WALL, "minecraft:mossy_stone_brick_wall", BlockID.MOSSY_STONE_BRICK_WALL,
                BlockStates.WALL_BLOCK_TYPE);
        public static final BlockLegacy NETHER_BRICK_WALL = REGISTRY.flatten(V1_21_20.COBBLESTONE_WALL, "minecraft:nether_brick_wall", BlockID.NETHER_BRICK_WALL,
                BlockStates.WALL_BLOCK_TYPE);
        public static final BlockLegacy END_STONE_BRICK_WALL = REGISTRY.flatten(V1_21_20.COBBLESTONE_WALL, "minecraft:end_stone_brick_wall", BlockID.END_STONE_BRICK_WALL,
                BlockStates.WALL_BLOCK_TYPE);
        public static final BlockLegacy PRISMARINE_WALL = REGISTRY.flatten(V1_21_20.COBBLESTONE_WALL, "minecraft:prismarine_wall", BlockID.PRISMARINE_WALL,
                BlockStates.WALL_BLOCK_TYPE);
        public static final BlockLegacy RED_SANDSTONE_WALL = REGISTRY.flatten(V1_21_20.COBBLESTONE_WALL, "minecraft:red_sandstone_wall", BlockID.RED_SANDSTONE_WALL,
                BlockStates.WALL_BLOCK_TYPE);
        public static final BlockLegacy RED_NETHER_BRICK_WALL = REGISTRY.flatten(V1_21_20.COBBLESTONE_WALL, "minecraft:red_nether_brick_wall", BlockID.RED_NETHER_BRICK_WALL,
                BlockStates.WALL_BLOCK_TYPE);
        public static final BlockLegacy COBBLESTONE_WALL = REGISTRY.flatten(V1_21_20.COBBLESTONE_WALL, "minecraft:cobblestone_wall", BlockID.COBBLESTONE_WALL,
                BlockStates.WALL_BLOCK_TYPE);

        public static final BlockLegacy COLORED_TORCH_BLUE = REGISTRY.flatten(V1_21_20.COLORED_TORCH_BP, "minecraft:colored_torch_blue", BlockID.COLORED_TORCH_BLUE,
                BlockStates.COLOR_BIT);
        public static final BlockLegacy COLORED_TORCH_PURPLE = REGISTRY.flatten(V1_21_20.COLORED_TORCH_BP, "minecraft:colored_torch_purple", BlockID.COLORED_TORCH_PURPLE,
                BlockStates.COLOR_BIT);
        public static final BlockLegacy COLORED_TORCH_BP = COLORED_TORCH_BLUE;

        public static final BlockLegacy COLORED_TORCH_RED = REGISTRY.flatten(V1_21_20.COLORED_TORCH_RG, "minecraft:colored_torch_red", BlockID.COLORED_TORCH_RED,
                BlockStates.COLOR_BIT);
        public static final BlockLegacy COLORED_TORCH_GREEN = REGISTRY.flatten(V1_21_20.COLORED_TORCH_RG, "minecraft:colored_torch_green", BlockID.COLORED_TORCH_GREEN,
                BlockStates.COLOR_BIT);
        public static final BlockLegacy COLORED_TORCH_RG = COLORED_TORCH_RED;

        public static final BlockLegacy DEPRECATED_PURPUR_BLOCK_1 = REGISTRY.flatten(V1_21_20.PURPUR_BLOCK, "minecraft:deprecated_purpur_block_1", BlockID.DEPRECATED_PURPUR_BLOCK_1,
                BlockStates.CHISEL_TYPE);
        public static final BlockLegacy PURPUR_PILLAR = REGISTRY.flatten(V1_21_20.PURPUR_BLOCK, "minecraft:purpur_pillar", BlockID.PURPUR_PILLAR,
                BlockStates.CHISEL_TYPE);
        public static final BlockLegacy DEPRECATED_PURPUR_BLOCK_2 = REGISTRY.flatten(V1_21_20.PURPUR_BLOCK, "minecraft:deprecated_purpur_block_2", BlockID.DEPRECATED_PURPUR_BLOCK_2,
                BlockStates.CHISEL_TYPE);
        public static final BlockLegacy PURPUR_BLOCK = REGISTRY.flatten(V1_21_20.PURPUR_BLOCK, "minecraft:purpur_block", BlockID.PURPUR_BLOCK,
                BlockStates.CHISEL_TYPE);

        public static final BlockLegacy WET_SPONGE = REGISTRY.flatten(V1_21_20.SPONGE, "minecraft:wet_sponge", BlockID.WET_SPONGE,
                BlockStates.SPONGE_TYPE);
        public static final BlockLegacy SPONGE = REGISTRY.flatten(V1_21_20.SPONGE, "minecraft:sponge", BlockID.SPONGE,
                BlockStates.SPONGE_TYPE);

        public static final BlockLegacy STRUCTURE_VOID = REGISTRY.flatten(V1_21_20.STRUCTURE_VOID, "minecraft:structure_void", BlockID.STRUCTURE_VOID,
                BlockStates.STRUCTURE_VOID_TYPE);

        public static final BlockLegacy UNDERWATER_TNT = REGISTRY.flatten(V1_21_20.TNT, "minecraft:underwater_tnt", BlockID.UNDERWATER_TNT,
                BlockStates.ALLOW_UNDERWATER_BIT);
        public static final BlockLegacy TNT = REGISTRY.flatten(V1_21_20.TNT, "minecraft:tnt", BlockID.TNT,
                BlockStates.ALLOW_UNDERWATER_BIT);

        static {
            REGISTRY.createBlockPermutations();
        }

        public static BlockRegistry getBlockRegistry() {
            return REGISTRY;
        }
    }

    public static class V1_21_40 extends V1_21_30 {
        private static final BlockRegistry REGISTRY = new BlockRegistry((1 << 24) | (21 << 16) | (40 << 8) | 1, V1_21_30.REGISTRY);

        public static final BlockLegacy CHERRY_WOOD = REGISTRY.flatten(V1_21_30.CHERRY_WOOD, "minecraft:cherry_wood", BlockID.CHERRY_WOOD,
                BlockStates.STRIPPED_BIT);

        public static final BlockLegacy MANGROVE_WOOD = REGISTRY.flatten(V1_21_30.MANGROVE_WOOD, "minecraft:mangrove_wood", BlockID.MANGROVE_WOOD,
                BlockStates.STRIPPED_BIT);

        public static final BlockLegacy MUSHROOM_STEM = REGISTRY.registerBlock("minecraft:mushroom_stem", BlockID.MUSHROOM_STEM)
                .addState(BlockStates.HUGE_MUSHROOM_BITS);

        public static final BlockLegacy SKELETON_SKULL = REGISTRY.flatten(V1_21_30.SKULL, "minecraft:skeleton_skull", BlockID.SKELETON_SKULL);
        public static final BlockLegacy WITHER_SKELETON_SKULL = REGISTRY.flatten(V1_21_30.SKULL, "minecraft:wither_skeleton_skull", BlockID.WITHER_SKELETON_SKULL);
        public static final BlockLegacy ZOMBIE_HEAD = REGISTRY.flatten(V1_21_30.SKULL, "minecraft:zombie_head", BlockID.ZOMBIE_HEAD);
        public static final BlockLegacy PLAYER_HEAD = REGISTRY.flatten(V1_21_30.SKULL, "minecraft:player_head", BlockID.PLAYER_HEAD);
        public static final BlockLegacy CREEPER_HEAD = REGISTRY.flatten(V1_21_30.SKULL, "minecraft:creeper_head", BlockID.CREEPER_HEAD);
        public static final BlockLegacy DRAGON_HEAD = REGISTRY.flatten(V1_21_30.SKULL, "minecraft:dragon_head", BlockID.DRAGON_HEAD);
        public static final BlockLegacy PIGLIN_HEAD = REGISTRY.flatten(V1_21_30.SKULL, "minecraft:piglin_head", BlockID.PIGLIN_HEAD);
        public static final BlockLegacy SKULL = SKELETON_SKULL;

        static {
            REGISTRY.createBlockPermutations();
        }

        public static BlockRegistry getBlockRegistry() {
            return REGISTRY;
        }
    }
}

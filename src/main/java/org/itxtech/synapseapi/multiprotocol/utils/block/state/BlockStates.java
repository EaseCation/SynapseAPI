package org.itxtech.synapseapi.multiprotocol.utils.block.state;

import org.itxtech.synapseapi.multiprotocol.utils.block.BlockStateNames;

import java.util.ArrayList;
import java.util.List;

import static org.itxtech.synapseapi.multiprotocol.utils.block.BlockStateIntegerValues.*;
import static org.itxtech.synapseapi.multiprotocol.utils.block.BlockStateStringValues.*;

public final class BlockStates {
    private static final List<BlockState> REGISTRY = new ArrayList<>();

    public static final BooleanBlockState ACTIVE = register(new BooleanBlockState(BlockStateNames.ACTIVE));
    public static final IntegerBlockState AGE = register(new IntegerBlockState(BlockStateNames.AGE, MAX_AGE + 1));
    public static final BooleanBlockState AGE_BIT = register(new BooleanBlockState(BlockStateNames.AGE_BIT));
    public static final BooleanBlockState ALLOW_UNDERWATER_BIT = register(new BooleanBlockState(BlockStateNames.ALLOW_UNDERWATER_BIT));
    public static final BooleanBlockState ATTACHED_BIT = register(new BooleanBlockState(BlockStateNames.ATTACHED_BIT));
    public static final StringBlockState ATTACHMENT = register(new StringBlockState(BlockStateNames.ATTACHMENT,
            ATTACHMENT_STANDING,
            ATTACHMENT_HANGING,
            ATTACHMENT_SIDE,
            ATTACHMENT_MULTIPLE));
    public static final StringBlockState BAMBOO_LEAF_SIZE = register(new StringBlockState(BlockStateNames.BAMBOO_LEAF_SIZE,
            BAMBOO_LEAF_SIZE_NO_LEAVES,
            BAMBOO_LEAF_SIZE_SMALL_LEAVES,
            BAMBOO_LEAF_SIZE_LARGE_LEAVES));
    public static final StringBlockState BAMBOO_STALK_THICKNESS = register(new StringBlockState(BlockStateNames.BAMBOO_STALK_THICKNESS,
            BAMBOO_STALK_THICKNESS_THIN,
            BAMBOO_STALK_THICKNESS_THICK));
    public static final BooleanBlockState BIG_DRIPLEAF_HEAD = register(new BooleanBlockState(BlockStateNames.BIG_DRIPLEAF_HEAD));
    public static final StringBlockState BIG_DRIPLEAF_TILT = register(new StringBlockState(BlockStateNames.BIG_DRIPLEAF_TILT,
            BIG_DRIPLEAF_TILT_NONE,
            BIG_DRIPLEAF_TILT_UNSTABLE,
            BIG_DRIPLEAF_TILT_PARTIAL_TILT,
            BIG_DRIPLEAF_TILT_FULL_TILT));
    public static final IntegerBlockState BITE_COUNTER = register(new IntegerBlockState(BlockStateNames.BITE_COUNTER, MAX_BITE_COUNTER + 1));
    public static final IntegerBlockState BLOCK_LIGHT_LEVEL = register(new IntegerBlockState(BlockStateNames.BLOCK_LIGHT_LEVEL, MAX_BLOCK_LIGHT_LEVEL + 1));
    public static final BooleanBlockState BLOOM = register(new BooleanBlockState(BlockStateNames.BLOOM));
    public static final IntegerBlockState BOOKS_STORED = register(new IntegerBlockState(BlockStateNames.BOOKS_STORED, MAX_BOOKS_STORED + 1));
    public static final BooleanBlockState BREWING_STAND_SLOT_A_BIT = register(new BooleanBlockState(BlockStateNames.BREWING_STAND_SLOT_A_BIT));
    public static final BooleanBlockState BREWING_STAND_SLOT_B_BIT = register(new BooleanBlockState(BlockStateNames.BREWING_STAND_SLOT_B_BIT));
    public static final BooleanBlockState BREWING_STAND_SLOT_C_BIT = register(new BooleanBlockState(BlockStateNames.BREWING_STAND_SLOT_C_BIT));
    public static final IntegerBlockState BRUSHED_PROGRESS = register(new IntegerBlockState(BlockStateNames.BRUSHED_PROGRESS, MAX_BRUSHED_PROGRESS + 1));
    public static final BooleanBlockState BUTTON_PRESSED_BIT = register(new BooleanBlockState(BlockStateNames.BUTTON_PRESSED_BIT));
    public static final BooleanBlockState CAN_SUMMON = register(new BooleanBlockState(BlockStateNames.CAN_SUMMON));
    public static final IntegerBlockState CANDLES = register(new IntegerBlockState(BlockStateNames.CANDLES, MAX_CANDLES + 1));
    public static final StringBlockState CAULDRON_LIQUID = register(new StringBlockState(BlockStateNames.CAULDRON_LIQUID,
            CAULDRON_LIQUID_WATER,
            CAULDRON_LIQUID_LAVA,
            CAULDRON_LIQUID_POWDER_SNOW));
    public static final StringBlockState CHEMISTRY_TABLE_TYPE = register(new StringBlockState(BlockStateNames.CHEMISTRY_TABLE_TYPE,
            CHEMISTRY_TABLE_TYPE_COMPOUND_CREATOR,
            CHEMISTRY_TABLE_TYPE_MATERIAL_REDUCER,
            CHEMISTRY_TABLE_TYPE_ELEMENT_CONSTRUCTOR,
            CHEMISTRY_TABLE_TYPE_LAB_TABLE));
    public static final StringBlockState CHISEL_TYPE = register(new StringBlockState(BlockStateNames.CHISEL_TYPE,
            CHISEL_TYPE_DEFAULT,
            CHISEL_TYPE_CHISELED,
            CHISEL_TYPE_LINES,
            CHISEL_TYPE_SMOOTH));
    public static final IntegerBlockState CLUSTER_COUNT = register(new IntegerBlockState(BlockStateNames.CLUSTER_COUNT, MAX_CLUSTER_COUNT + 1));
    public static final StringBlockState COLOR = register(new StringBlockState(BlockStateNames.COLOR,
            COLOR_WHITE,
            COLOR_ORANGE,
            COLOR_MAGENTA,
            COLOR_LIGHT_BLUE,
            COLOR_YELLOW,
            COLOR_LIME,
            COLOR_PINK,
            COLOR_GRAY,
            COLOR_SILVER,
            COLOR_CYAN,
            COLOR_PURPLE,
            COLOR_BLUE,
            COLOR_BROWN,
            COLOR_GREEN,
            COLOR_RED,
            COLOR_BLACK));
    public static final BooleanBlockState COLOR_BIT = register(new BooleanBlockState(BlockStateNames.COLOR_BIT));
    public static final IntegerBlockState COMPOSTER_FILL_LEVEL = register(new IntegerBlockState(BlockStateNames.COMPOSTER_FILL_LEVEL, MAX_COMPOSTER_FILL_LEVEL + 1));
    public static final BooleanBlockState CONDITIONAL_BIT = register(new BooleanBlockState(BlockStateNames.CONDITIONAL_BIT));
    public static final StringBlockState CORAL_COLOR = register(new StringBlockState(BlockStateNames.CORAL_COLOR,
            CORAL_COLOR_BLUE,
            CORAL_COLOR_PINK,
            CORAL_COLOR_PURPLE,
            CORAL_COLOR_RED,
            CORAL_COLOR_YELLOW));
    public static final IntegerBlockState CORAL_DIRECTION = register(new IntegerBlockState(BlockStateNames.CORAL_DIRECTION, MAX_CORAL_DIRECTION + 1));
    public static final IntegerBlockState CORAL_FAN_DIRECTION = register(new IntegerBlockState(BlockStateNames.CORAL_FAN_DIRECTION, MAX_CORAL_FAN_DIRECTION + 1));
    public static final BooleanBlockState CORAL_HANG_TYPE_BIT = register(new BooleanBlockState(BlockStateNames.CORAL_HANG_TYPE_BIT));
    public static final BooleanBlockState COVERED_BIT = register(new BooleanBlockState(BlockStateNames.COVERED_BIT));
    public static final StringBlockState CRACKED_STATE = register(new StringBlockState(BlockStateNames.CRACKED_STATE,
            CRACKED_STATE_NO_CRACKS,
            CRACKED_STATE_CRACKED,
            CRACKED_STATE_MAX_CRACKED));
    public static final BooleanBlockState CRAFTING = register(new BooleanBlockState(BlockStateNames.CRAFTING));
    public static final StringBlockState DAMAGE = register(new StringBlockState(BlockStateNames.DAMAGE,
            DAMAGE_UNDAMAGED,
            DAMAGE_SLIGHTLY_DAMAGED,
            DAMAGE_VERY_DAMAGED,
            DAMAGE_BROKEN));
    public static final BooleanBlockState DEAD_BIT = register(new BooleanBlockState(BlockStateNames.DEAD_BIT));
    public static final IntegerBlockState DEPRECATED = register(new IntegerBlockState(BlockStateNames.DEPRECATED, MAX_DEPRECATED + 1));
    public static final IntegerBlockState DIRECTION = register(new IntegerBlockState(BlockStateNames.DIRECTION, MAX_DIRECTION + 1));
    public static final StringBlockState DIRT_TYPE = register(new StringBlockState(BlockStateNames.DIRT_TYPE,
            DIRT_TYPE_NORMAL,
            DIRT_TYPE_COARSE));
    public static final BooleanBlockState DISARMED_BIT = register(new BooleanBlockState(BlockStateNames.DISARMED_BIT));
    public static final BooleanBlockState DOOR_HINGE_BIT = register(new BooleanBlockState(BlockStateNames.DOOR_HINGE_BIT));
    public static final StringBlockState DOUBLE_PLANT_TYPE = register(new StringBlockState(BlockStateNames.DOUBLE_PLANT_TYPE,
            DOUBLE_PLANT_TYPE_SUNFLOWER,
            DOUBLE_PLANT_TYPE_SYRINGA,
            DOUBLE_PLANT_TYPE_GRASS,
            DOUBLE_PLANT_TYPE_FERN,
            DOUBLE_PLANT_TYPE_ROSE,
            DOUBLE_PLANT_TYPE_PAEONIA));
    public static final BooleanBlockState DRAG_DOWN = register(new BooleanBlockState(BlockStateNames.DRAG_DOWN));
    public static final StringBlockState DRIPSTONE_THICKNESS = register(new StringBlockState(BlockStateNames.DRIPSTONE_THICKNESS,
            DRIPSTONE_THICKNESS_TIP,
            DRIPSTONE_THICKNESS_FRUSTUM,
            DRIPSTONE_THICKNESS_MIDDLE,
            DRIPSTONE_THICKNESS_BASE,
            DRIPSTONE_THICKNESS_MERGE));
    public static final BooleanBlockState END_PORTAL_EYE_BIT = register(new BooleanBlockState(BlockStateNames.END_PORTAL_EYE_BIT));
    public static final BooleanBlockState EXPLODE_BIT = register(new BooleanBlockState(BlockStateNames.EXPLODE_BIT));
    public static final BooleanBlockState EXTINGUISHED = register(new BooleanBlockState(BlockStateNames.EXTINGUISHED));
    public static final IntegerBlockState FACING_DIRECTION = register(new IntegerBlockState(BlockStateNames.FACING_DIRECTION, MAX_FACING_DIRECTION + 1));
    public static final IntegerBlockState FILL_LEVEL = register(new IntegerBlockState(BlockStateNames.FILL_LEVEL, MAX_FILL_LEVEL + 1));
    public static final StringBlockState FLOWER_TYPE = register(new StringBlockState(BlockStateNames.FLOWER_TYPE,
            FLOWER_TYPE_POPPY,
            FLOWER_TYPE_ORCHID,
            FLOWER_TYPE_ALLIUM,
            FLOWER_TYPE_HOUSTONIA,
            FLOWER_TYPE_TULIP_RED,
            FLOWER_TYPE_TULIP_ORANGE,
            FLOWER_TYPE_TULIP_WHITE,
            FLOWER_TYPE_TULIP_PINK,
            FLOWER_TYPE_OXEYE,
            FLOWER_TYPE_CORNFLOWER,
            FLOWER_TYPE_LILY_OF_THE_VALLEY));
    public static final IntegerBlockState GROUND_SIGN_DIRECTION = register(new IntegerBlockState(BlockStateNames.GROUND_SIGN_DIRECTION, MAX_GROUND_SIGN_DIRECTION + 1));
    public static final IntegerBlockState GROWING_PLANT_AGE = register(new IntegerBlockState(BlockStateNames.GROWING_PLANT_AGE, MAX_GROWING_PLANT_AGE + 1));
    public static final IntegerBlockState GROWTH = register(new IntegerBlockState(BlockStateNames.GROWTH, MAX_GROWTH + 1));
    public static final BooleanBlockState HANGING = register(new BooleanBlockState(BlockStateNames.HANGING));
    public static final BooleanBlockState HEAD_PIECE_BIT = register(new BooleanBlockState(BlockStateNames.HEAD_PIECE_BIT));
    public static final IntegerBlockState HEIGHT = register(new IntegerBlockState(BlockStateNames.HEIGHT, MAX_HEIGHT + 1));
    public static final IntegerBlockState HONEY_LEVEL = register(new IntegerBlockState(BlockStateNames.HONEY_LEVEL, MAX_HONEY_LEVEL + 1));
    public static final IntegerBlockState HUGE_MUSHROOM_BITS = register(new IntegerBlockState(BlockStateNames.HUGE_MUSHROOM_BITS, MAX_HUGE_MUSHROOM_BITS + 1));
    public static final BooleanBlockState IN_WALL_BIT = register(new BooleanBlockState(BlockStateNames.IN_WALL_BIT));
    public static final BooleanBlockState INFINIBURN_BIT = register(new BooleanBlockState(BlockStateNames.INFINIBURN_BIT));
    public static final BooleanBlockState ITEM_FRAME_MAP_BIT = register(new BooleanBlockState(BlockStateNames.ITEM_FRAME_MAP_BIT));
    public static final BooleanBlockState ITEM_FRAME_PHOTO_BIT = register(new BooleanBlockState(BlockStateNames.ITEM_FRAME_PHOTO_BIT));
    public static final IntegerBlockState KELP_AGE = register(new IntegerBlockState(BlockStateNames.KELP_AGE, MAX_KELP_AGE + 1));
    public static final StringBlockState LEVER_DIRECTION = register(new StringBlockState(BlockStateNames.LEVER_DIRECTION,
            LEVER_DIRECTION_DOWN_EAST_WEST,
            LEVER_DIRECTION_EAST,
            LEVER_DIRECTION_WEST,
            LEVER_DIRECTION_SOUTH,
            LEVER_DIRECTION_NORTH,
            LEVER_DIRECTION_UP_NORTH_SOUTH,
            LEVER_DIRECTION_UP_EAST_WEST,
            LEVER_DIRECTION_DOWN_NORTH_SOUTH));
    public static final IntegerBlockState LIQUID_DEPTH = register(new IntegerBlockState(BlockStateNames.LIQUID_DEPTH, MAX_LIQUID_DEPTH + 1));
    public static final BooleanBlockState LIT = register(new BooleanBlockState(BlockStateNames.LIT));
    public static final StringBlockState MINECRAFT_BLOCK_FACE = register(new StringBlockState(BlockStateNames.MINECRAFT_BLOCK_FACE,
            MINECRAFT_BLOCK_FACE_DOWN,
            MINECRAFT_BLOCK_FACE_UP,
            MINECRAFT_BLOCK_FACE_NORTH,
            MINECRAFT_BLOCK_FACE_SOUTH,
            MINECRAFT_BLOCK_FACE_WEST,
            MINECRAFT_BLOCK_FACE_EAST));
    public static final StringBlockState MINECRAFT_CARDINAL_DIRECTION = register(new StringBlockState(BlockStateNames.MINECRAFT_CARDINAL_DIRECTION,
            MINECRAFT_CARDINAL_DIRECTION_SOUTH,
            MINECRAFT_CARDINAL_DIRECTION_WEST,
            MINECRAFT_CARDINAL_DIRECTION_NORTH,
            MINECRAFT_CARDINAL_DIRECTION_EAST));
    public static final StringBlockState MINECRAFT_FACING_DIRECTION = register(new StringBlockState(BlockStateNames.MINECRAFT_FACING_DIRECTION,
            MINECRAFT_FACING_DIRECTION_DOWN,
            MINECRAFT_FACING_DIRECTION_UP,
            MINECRAFT_FACING_DIRECTION_NORTH,
            MINECRAFT_FACING_DIRECTION_SOUTH,
            MINECRAFT_FACING_DIRECTION_WEST,
            MINECRAFT_FACING_DIRECTION_EAST));
    public static final StringBlockState MINECRAFT_VERTICAL_HALF = register(new StringBlockState(BlockStateNames.MINECRAFT_VERTICAL_HALF,
            MINECRAFT_VERTICAL_HALF_BOTTOM,
            MINECRAFT_VERTICAL_HALF_TOP));
    public static final IntegerBlockState MOISTURIZED_AMOUNT = register(new IntegerBlockState(BlockStateNames.MOISTURIZED_AMOUNT, MAX_MOISTURIZED_AMOUNT + 1));
    public static final StringBlockState MONSTER_EGG_STONE_TYPE = register(new StringBlockState(BlockStateNames.MONSTER_EGG_STONE_TYPE,
            MONSTER_EGG_STONE_TYPE_STONE,
            MONSTER_EGG_STONE_TYPE_COBBLESTONE,
            MONSTER_EGG_STONE_TYPE_STONE_BRICK,
            MONSTER_EGG_STONE_TYPE_MOSSY_STONE_BRICK,
            MONSTER_EGG_STONE_TYPE_CRACKED_STONE_BRICK,
            MONSTER_EGG_STONE_TYPE_CHISELED_STONE_BRICK));
    public static final IntegerBlockState MULTI_FACE_DIRECTION_BITS = register(new IntegerBlockState(BlockStateNames.MULTI_FACE_DIRECTION_BITS, MAX_MULTI_FACE_DIRECTION_BITS + 1));
    public static final StringBlockState NEW_LEAF_TYPE = register(new StringBlockState(BlockStateNames.NEW_LEAF_TYPE,
            NEW_LEAF_TYPE_ACACIA,
            NEW_LEAF_TYPE_DARK_OAK));
    public static final StringBlockState NEW_LOG_TYPE = register(new StringBlockState(BlockStateNames.NEW_LOG_TYPE,
            NEW_LOG_TYPE_ACACIA,
            NEW_LOG_TYPE_DARK_OAK));
    public static final BooleanBlockState NO_DROP_BIT = register(new BooleanBlockState(BlockStateNames.NO_DROP_BIT));
    public static final BooleanBlockState OCCUPIED_BIT = register(new BooleanBlockState(BlockStateNames.OCCUPIED_BIT));
    public static final StringBlockState OLD_LEAF_TYPE = register(new StringBlockState(BlockStateNames.OLD_LEAF_TYPE,
            OLD_LEAF_TYPE_OAK,
            OLD_LEAF_TYPE_SPRUCE,
            OLD_LEAF_TYPE_BIRCH,
            OLD_LEAF_TYPE_JUNGLE));
    public static final StringBlockState OLD_LOG_TYPE = register(new StringBlockState(BlockStateNames.OLD_LOG_TYPE,
            OLD_LOG_TYPE_OAK,
            OLD_LOG_TYPE_SPRUCE,
            OLD_LOG_TYPE_BIRCH,
            OLD_LOG_TYPE_JUNGLE));
    public static final BooleanBlockState OMINOUS = register(new BooleanBlockState(BlockStateNames.OMINOUS));
    public static final BooleanBlockState OPEN_BIT = register(new BooleanBlockState(BlockStateNames.OPEN_BIT));
    public static final StringBlockState ORIENTATION = register(new StringBlockState(BlockStateNames.ORIENTATION,
            ORIENTATION_DOWN_EAST,
            ORIENTATION_DOWN_NORTH,
            ORIENTATION_DOWN_SOUTH,
            ORIENTATION_DOWN_WEST,
            ORIENTATION_UP_EAST,
            ORIENTATION_UP_NORTH,
            ORIENTATION_UP_SOUTH,
            ORIENTATION_UP_WEST,
            ORIENTATION_WEST_UP,
            ORIENTATION_EAST_UP,
            ORIENTATION_NORTH_UP,
            ORIENTATION_SOUTH_UP));
    public static final BooleanBlockState OUTPUT_LIT_BIT = register(new BooleanBlockState(BlockStateNames.OUTPUT_LIT_BIT));
    public static final BooleanBlockState OUTPUT_SUBTRACT_BIT = register(new BooleanBlockState(BlockStateNames.OUTPUT_SUBTRACT_BIT));
    public static final BooleanBlockState PERSISTENT_BIT = register(new BooleanBlockState(BlockStateNames.PERSISTENT_BIT));
    public static final StringBlockState PILLAR_AXIS = register(new StringBlockState(BlockStateNames.PILLAR_AXIS,
            PILLAR_AXIS_Y,
            PILLAR_AXIS_X,
            PILLAR_AXIS_Z));
    public static final StringBlockState PORTAL_AXIS = register(new StringBlockState(BlockStateNames.PORTAL_AXIS,
            PORTAL_AXIS_UNKNOWN,
            PORTAL_AXIS_X,
            PORTAL_AXIS_Z));
    public static final BooleanBlockState POWERED_BIT = register(new BooleanBlockState(BlockStateNames.POWERED_BIT));
    public static final StringBlockState PRISMARINE_BLOCK_TYPE = register(new StringBlockState(BlockStateNames.PRISMARINE_BLOCK_TYPE,
            PRISMARINE_BLOCK_TYPE_DEFAULT,
            PRISMARINE_BLOCK_TYPE_DARK,
            PRISMARINE_BLOCK_TYPE_BRICKS));
    public static final IntegerBlockState PROPAGULE_STAGE = register(new IntegerBlockState(BlockStateNames.PROPAGULE_STAGE, MAX_PROPAGULE_STAGE + 1));
    public static final BooleanBlockState RAIL_DATA_BIT = register(new BooleanBlockState(BlockStateNames.RAIL_DATA_BIT));
    public static final IntegerBlockState RAIL_DIRECTION = register(new IntegerBlockState(BlockStateNames.RAIL_DIRECTION, MAX_RAIL_DIRECTION + 1));
    public static final IntegerBlockState REDSTONE_SIGNAL = register(new IntegerBlockState(BlockStateNames.REDSTONE_SIGNAL, MAX_REDSTONE_SIGNAL + 1));
    public static final IntegerBlockState REPEATER_DELAY = register(new IntegerBlockState(BlockStateNames.REPEATER_DELAY, MAX_REPEATER_DELAY + 1));
    public static final IntegerBlockState RESPAWN_ANCHOR_CHARGE = register(new IntegerBlockState(BlockStateNames.RESPAWN_ANCHOR_CHARGE, MAX_RESPAWN_ANCHOR_CHARGE + 1));
    public static final IntegerBlockState ROTATION = register(new IntegerBlockState(BlockStateNames.ROTATION, MAX_ROTATION + 1));
    public static final StringBlockState SAND_STONE_TYPE = register(new StringBlockState(BlockStateNames.SAND_STONE_TYPE,
            SAND_STONE_TYPE_DEFAULT,
            SAND_STONE_TYPE_HEIROGLYPHS,
            SAND_STONE_TYPE_CUT,
            SAND_STONE_TYPE_SMOOTH));
    public static final StringBlockState SAND_TYPE = register(new StringBlockState(BlockStateNames.SAND_TYPE,
            SAND_TYPE_NORMAL,
            SAND_TYPE_RED));
    public static final StringBlockState SAPLING_TYPE = register(new StringBlockState(BlockStateNames.SAPLING_TYPE,
            SAPLING_TYPE_OAK,
            SAPLING_TYPE_SPRUCE,
            SAPLING_TYPE_BIRCH,
            SAPLING_TYPE_JUNGLE,
            SAPLING_TYPE_ACACIA,
            SAPLING_TYPE_DARK_OAK));
    public static final IntegerBlockState SCULK_SENSOR_PHASE = register(new IntegerBlockState(BlockStateNames.SCULK_SENSOR_PHASE, MAX_SCULK_SENSOR_PHASE + 1));
    public static final StringBlockState SEA_GRASS_TYPE = register(new StringBlockState(BlockStateNames.SEA_GRASS_TYPE,
            SEA_GRASS_TYPE_DEFAULT,
            SEA_GRASS_TYPE_DOUBLE_TOP,
            SEA_GRASS_TYPE_DOUBLE_BOT));
    public static final StringBlockState SPONGE_TYPE = register(new StringBlockState(BlockStateNames.SPONGE_TYPE,
            SPONGE_TYPE_DRY,
            SPONGE_TYPE_WET));
    public static final IntegerBlockState STABILITY = register(new IntegerBlockState(BlockStateNames.STABILITY, MAX_STABILITY + 1));
    public static final BooleanBlockState STABILITY_CHECK = register(new BooleanBlockState(BlockStateNames.STABILITY_CHECK));
    public static final StringBlockState STONE_BRICK_TYPE = register(new StringBlockState(BlockStateNames.STONE_BRICK_TYPE,
            STONE_BRICK_TYPE_DEFAULT,
            STONE_BRICK_TYPE_MOSSY,
            STONE_BRICK_TYPE_CRACKED,
            STONE_BRICK_TYPE_CHISELED,
            STONE_BRICK_TYPE_SMOOTH));
    public static final StringBlockState STONE_SLAB_TYPE = register(new StringBlockState(BlockStateNames.STONE_SLAB_TYPE,
            STONE_SLAB_TYPE_SMOOTH_STONE,
            STONE_SLAB_TYPE_SANDSTONE,
            STONE_SLAB_TYPE_WOOD,
            STONE_SLAB_TYPE_COBBLESTONE,
            STONE_SLAB_TYPE_BRICK,
            STONE_SLAB_TYPE_STONE_BRICK,
            STONE_SLAB_TYPE_QUARTZ,
            STONE_SLAB_TYPE_NETHER_BRICK));
    public static final StringBlockState STONE_SLAB_TYPE_2 = register(new StringBlockState(BlockStateNames.STONE_SLAB_TYPE_2,
            STONE_SLAB_TYPE_2_RED_SANDSTONE,
            STONE_SLAB_TYPE_2_PURPUR,
            STONE_SLAB_TYPE_2_PRISMARINE_ROUGH,
            STONE_SLAB_TYPE_2_PRISMARINE_DARK,
            STONE_SLAB_TYPE_2_PRISMARINE_BRICK,
            STONE_SLAB_TYPE_2_MOSSY_COBBLESTONE,
            STONE_SLAB_TYPE_2_SMOOTH_SANDSTONE,
            STONE_SLAB_TYPE_2_RED_NETHER_BRICK));
    public static final StringBlockState STONE_SLAB_TYPE_3 = register(new StringBlockState(BlockStateNames.STONE_SLAB_TYPE_3,
            STONE_SLAB_TYPE_3_END_STONE_BRICK,
            STONE_SLAB_TYPE_3_SMOOTH_RED_SANDSTONE,
            STONE_SLAB_TYPE_3_POLISHED_ANDESITE,
            STONE_SLAB_TYPE_3_ANDESITE,
            STONE_SLAB_TYPE_3_DIORITE,
            STONE_SLAB_TYPE_3_POLISHED_DIORITE,
            STONE_SLAB_TYPE_3_GRANITE,
            STONE_SLAB_TYPE_3_POLISHED_GRANITE));
    public static final StringBlockState STONE_SLAB_TYPE_4 = register(new StringBlockState(BlockStateNames.STONE_SLAB_TYPE_4,
            STONE_SLAB_TYPE_4_MOSSY_STONE_BRICK,
            STONE_SLAB_TYPE_4_SMOOTH_QUARTZ,
            STONE_SLAB_TYPE_4_STONE,
            STONE_SLAB_TYPE_4_CUT_SANDSTONE,
            STONE_SLAB_TYPE_4_CUT_RED_SANDSTONE));
    public static final StringBlockState STONE_TYPE = register(new StringBlockState(BlockStateNames.STONE_TYPE,
            STONE_TYPE_STONE,
            STONE_TYPE_GRANITE,
            STONE_TYPE_GRANITE_SMOOTH,
            STONE_TYPE_DIORITE,
            STONE_TYPE_DIORITE_SMOOTH,
            STONE_TYPE_ANDESITE,
            STONE_TYPE_ANDESITE_SMOOTH));
    public static final BooleanBlockState STRIPPED_BIT = register(new BooleanBlockState(BlockStateNames.STRIPPED_BIT));
    public static final StringBlockState STRUCTURE_BLOCK_TYPE = register(new StringBlockState(BlockStateNames.STRUCTURE_BLOCK_TYPE,
            STRUCTURE_BLOCK_TYPE_DATA,
            STRUCTURE_BLOCK_TYPE_SAVE,
            STRUCTURE_BLOCK_TYPE_LOAD,
            STRUCTURE_BLOCK_TYPE_CORNER,
            STRUCTURE_BLOCK_TYPE_INVALID,
            STRUCTURE_BLOCK_TYPE_EXPORT));
    public static final StringBlockState STRUCTURE_VOID_TYPE = register(new StringBlockState(BlockStateNames.STRUCTURE_VOID_TYPE,
            STRUCTURE_VOID_TYPE_VOID,
            STRUCTURE_VOID_TYPE_AIR));
    public static final BooleanBlockState SUSPENDED_BIT = register(new BooleanBlockState(BlockStateNames.SUSPENDED_BIT));
    public static final StringBlockState TALL_GRASS_TYPE = register(new StringBlockState(BlockStateNames.TALL_GRASS_TYPE,
            TALL_GRASS_TYPE_DEFAULT,
            TALL_GRASS_TYPE_TALL,
            TALL_GRASS_TYPE_FERN,
            TALL_GRASS_TYPE_SNOW));
    public static final BooleanBlockState TOGGLE_BIT = register(new BooleanBlockState(BlockStateNames.TOGGLE_BIT));
    public static final BooleanBlockState TOP_SLOT_BIT = register(new BooleanBlockState(BlockStateNames.TOP_SLOT_BIT));
    public static final StringBlockState TORCH_FACING_DIRECTION = register(new StringBlockState(BlockStateNames.TORCH_FACING_DIRECTION,
            TORCH_FACING_DIRECTION_UNKNOWN,
            TORCH_FACING_DIRECTION_WEST,
            TORCH_FACING_DIRECTION_EAST,
            TORCH_FACING_DIRECTION_NORTH,
            TORCH_FACING_DIRECTION_SOUTH,
            TORCH_FACING_DIRECTION_TOP));
    public static final IntegerBlockState TRIAL_SPAWNER_STATE = register(new IntegerBlockState(BlockStateNames.TRIAL_SPAWNER_STATE, MAX_TRIAL_SPAWNER_STATE + 1));
    public static final BooleanBlockState TRIGGERED_BIT = register(new BooleanBlockState(BlockStateNames.TRIGGERED_BIT));
    public static final StringBlockState TURTLE_EGG_COUNT = register(new StringBlockState(BlockStateNames.TURTLE_EGG_COUNT,
            TURTLE_EGG_COUNT_ONE_EGG,
            TURTLE_EGG_COUNT_TWO_EGG,
            TURTLE_EGG_COUNT_THREE_EGG,
            TURTLE_EGG_COUNT_FOUR_EGG));
    public static final IntegerBlockState TWISTING_VINES_AGE = register(new IntegerBlockState(BlockStateNames.TWISTING_VINES_AGE, MAX_TWISTING_VINES_AGE + 1));
    public static final BooleanBlockState UPDATE_BIT = register(new BooleanBlockState(BlockStateNames.UPDATE_BIT));
    public static final BooleanBlockState UPPER_BLOCK_BIT = register(new BooleanBlockState(BlockStateNames.UPPER_BLOCK_BIT));
    public static final BooleanBlockState UPSIDE_DOWN_BIT = register(new BooleanBlockState(BlockStateNames.UPSIDE_DOWN_BIT));
    public static final StringBlockState VAULT_STATE = register(new StringBlockState(BlockStateNames.VAULT_STATE,
            VAULT_STATE_INACTIVE,
            VAULT_STATE_ACTIVE,
            VAULT_STATE_UNLOCKING,
            VAULT_STATE_EJECTING));
    public static final IntegerBlockState VINE_DIRECTION_BITS = register(new IntegerBlockState(BlockStateNames.VINE_DIRECTION_BITS, MAX_VINE_DIRECTION_BITS + 1));
    public static final StringBlockState WALL_BLOCK_TYPE = register(new StringBlockState(BlockStateNames.WALL_BLOCK_TYPE,
            WALL_BLOCK_TYPE_COBBLESTONE,
            WALL_BLOCK_TYPE_MOSSY_COBBLESTONE,
            WALL_BLOCK_TYPE_GRANITE,
            WALL_BLOCK_TYPE_DIORITE,
            WALL_BLOCK_TYPE_ANDESITE,
            WALL_BLOCK_TYPE_SANDSTONE,
            WALL_BLOCK_TYPE_BRICK,
            WALL_BLOCK_TYPE_STONE_BRICK,
            WALL_BLOCK_TYPE_MOSSY_STONE_BRICK,
            WALL_BLOCK_TYPE_NETHER_BRICK,
            WALL_BLOCK_TYPE_END_BRICK,
            WALL_BLOCK_TYPE_PRISMARINE,
            WALL_BLOCK_TYPE_RED_SANDSTONE,
            WALL_BLOCK_TYPE_RED_NETHER_BRICK));
    public static final StringBlockState WALL_CONNECTION_TYPE_EAST = register(new StringBlockState(BlockStateNames.WALL_CONNECTION_TYPE_EAST,
            WALL_CONNECTION_TYPE_EAST_NONE,
            WALL_CONNECTION_TYPE_EAST_SHORT,
            WALL_CONNECTION_TYPE_EAST_TALL));
    public static final StringBlockState WALL_CONNECTION_TYPE_NORTH = register(new StringBlockState(BlockStateNames.WALL_CONNECTION_TYPE_NORTH,
            WALL_CONNECTION_TYPE_NORTH_NONE,
            WALL_CONNECTION_TYPE_NORTH_SHORT,
            WALL_CONNECTION_TYPE_NORTH_TALL));
    public static final StringBlockState WALL_CONNECTION_TYPE_SOUTH = register(new StringBlockState(BlockStateNames.WALL_CONNECTION_TYPE_SOUTH,
            WALL_CONNECTION_TYPE_SOUTH_NONE,
            WALL_CONNECTION_TYPE_SOUTH_SHORT,
            WALL_CONNECTION_TYPE_SOUTH_TALL));
    public static final StringBlockState WALL_CONNECTION_TYPE_WEST = register(new StringBlockState(BlockStateNames.WALL_CONNECTION_TYPE_WEST,
            WALL_CONNECTION_TYPE_WEST_NONE,
            WALL_CONNECTION_TYPE_WEST_SHORT,
            WALL_CONNECTION_TYPE_WEST_TALL));
    public static final BooleanBlockState WALL_POST_BIT = register(new BooleanBlockState(BlockStateNames.WALL_POST_BIT));
    public static final IntegerBlockState WEEPING_VINES_AGE = register(new IntegerBlockState(BlockStateNames.WEEPING_VINES_AGE, MAX_WEEPING_VINES_AGE + 1));
    public static final IntegerBlockState WEIRDO_DIRECTION = register(new IntegerBlockState(BlockStateNames.WEIRDO_DIRECTION, MAX_WEIRDO_DIRECTION + 1));
    public static final StringBlockState WOOD_TYPE = register(new StringBlockState(BlockStateNames.WOOD_TYPE,
            WOOD_TYPE_OAK,
            WOOD_TYPE_SPRUCE,
            WOOD_TYPE_BIRCH,
            WOOD_TYPE_JUNGLE,
            WOOD_TYPE_ACACIA,
            WOOD_TYPE_DARK_OAK));

    public static final int STATE_COUNT = REGISTRY.size();

    public static int assignId() {
        return REGISTRY.size();
    }

    public static <T extends BlockState> T register(T state) {
        REGISTRY.add(state);
        return state;
    }

    public static BlockState get(int id) {
        return REGISTRY.get(id);
    }

    private BlockStates() {
        throw new IllegalStateException();
    }
}

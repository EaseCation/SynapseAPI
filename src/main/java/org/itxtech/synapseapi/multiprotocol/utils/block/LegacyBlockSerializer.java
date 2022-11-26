package org.itxtech.synapseapi.multiprotocol.utils.block;

import cn.nukkit.block.*;
import cn.nukkit.block.BlockSerializer.RuntimeBlockSerializer;
import cn.nukkit.block.edu.*;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.nbt.tag.CompoundTag;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import lombok.extern.log4j.Log4j2;

import static cn.nukkit.block.BlockID.*;
import static org.itxtech.synapseapi.multiprotocol.utils.block.BlockStateNames.*;
import static org.itxtech.synapseapi.multiprotocol.utils.block.BlockStateStringValues.*;

@Log4j2
public final class LegacyBlockSerializer {
//    private static final Int2ObjectFunction<CompoundTag>[] SERIALIZERS = new Int2ObjectFunction[Block.BLOCK_ID_COUNT];
    private static Int2ObjectFunction<CompoundTag> FORWARD_SERIALIZER;

    private static final DeserializationFunction[] DESERIALIZERS = new DeserializationFunction[Block.BLOCK_ID_COUNT];

    public static void setSerializer(Int2ObjectFunction<CompoundTag> serializer) {
        FORWARD_SERIALIZER = serializer;
    }

    public static CompoundTag serialize(int fullId) {
        return FORWARD_SERIALIZER.get(fullId);
    }

    public static int deserialize(CompoundTag tag) {
        String name = tag.getString("name");
        CompoundTag states = tag.getCompound("states");

        int id = GlobalBlockPalette.getBlockIdByName(name); //TODO: 1.18.30 renamed block
        if (id == -1) {
            log.warn("Unmapped block name: {}", name);
            return Block.INFO_UPDATE << Block.BLOCK_META_BITS;
        }

        if (states.isEmpty()) {
            return id << Block.BLOCK_META_BITS;
        }

        DeserializationFunction deserializer = DESERIALIZERS[id];
        if (deserializer == null) {
            log.warn("Missing block state deserializer: {}", name);
            return Block.INFO_UPDATE << Block.BLOCK_META_BITS;
        }

        return (id << Block.BLOCK_META_BITS) | (deserializer.deserialize(states) & Block.BLOCK_META_MASK);
    }

    // we use forward serializer
//    private static void registerSerializer(int id, Int2ObjectFunction<CompoundTag> serializer) {
//        if (SERIALIZERS[id] != null) {
//            throw new IllegalArgumentException("already registered: " + id);
//        }
//        SERIALIZERS[id] = serializer;
//    }

    private static void registerDeserializer(int id, DeserializationFunction deserializer) {
        if (DESERIALIZERS[id] != null) {
            throw new IllegalArgumentException("already registered: " + id);
        }
        DESERIALIZERS[id] = deserializer;
    }

    private static IllegalArgumentException badValueException(String stateName, String value) {
        return new IllegalArgumentException("State '" + stateName + "' has unexpected value '" + value + "'");
    }

    private static IllegalArgumentException badValueException(String stateName, int value) {
        return new IllegalArgumentException("State '" + stateName + "' has unexpected value '" + value + "'");
    }

    static {
        log.debug("Loading block serializer...");

        registerDeserializer(AIR, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GRASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(COBBLESTONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GRAVEL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GOLD_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(IRON_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(COAL_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LAPIS_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LAPIS_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(NOTEBLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WEB, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEADBUSH, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(YELLOW_FLOWER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BROWN_MUSHROOM, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RED_MUSHROOM, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GOLD_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(IRON_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BRICK_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BOOKSHELF, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MOSSY_COBBLESTONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(OBSIDIAN, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MOB_SPAWNER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DIAMOND_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DIAMOND_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CRAFTING_TABLE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(REDSTONE_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIT_REDSTONE_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ICE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SNOW, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CLAY, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(JUKEBOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(NETHERRACK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SOUL_SAND, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GLOWSTONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(INVISIBLE_BEDROCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(IRON_BARS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MELON_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MYCELIUM, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WATERLILY, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(NETHER_BRICK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(NETHER_BRICK_FENCE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ENCHANTING_TABLE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(END_PORTAL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(END_STONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DRAGON_EGG, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(REDSTONE_LAMP, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIT_REDSTONE_LAMP, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(EMERALD_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(EMERALD_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BEACON, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(REDSTONE_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(QUARTZ_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SLIME, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SEA_LANTERN, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARDENED_CLAY, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(COAL_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PACKED_ICE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CHEMICAL_HEAT, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GRASS_PATH, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(UNDYED_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(END_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(END_GATEWAY, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ALLOW, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DENY, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MAGMA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(NETHER_WART_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RED_NETHER_BRICK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CHALKBOARD, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CHORUS_PLANT, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLOCK_CAMERA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PODZOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(STONECUTTER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GLOWINGOBSIDIAN, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(NETHERREACTOR, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(INFO_UPDATE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(INFO_UPDATE2, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MOVING_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RESERVED6, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLUE_ICE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DRIED_KELP_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CONDUIT, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BARRIER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SMOOTH_STONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CARTOGRAPHY_TABLE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(FLETCHING_TABLE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SMITHING_TABLE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WITHER_ROSE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HONEY_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HONEYCOMB_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LODESTONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CRIMSON_ROOTS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WARPED_ROOTS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WARPED_WART_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CRIMSON_FUNGUS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WARPED_FUNGUS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SHROOMLIGHT, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CRIMSON_NYLIUM, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WARPED_NYLIUM, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SOUL_SOIL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLOCK_NETHER_SPROUTS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(TARGET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CRIMSON_PLANKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WARPED_PLANKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CRIMSON_FENCE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WARPED_FENCE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(NETHERITE_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ANCIENT_DEBRIS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLACKSTONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(POLISHED_BLACKSTONE_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CHISELED_POLISHED_BLACKSTONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CRACKED_POLISHED_BLACKSTONE_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GILDED_BLACKSTONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(NETHER_GOLD_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CRYING_OBSIDIAN, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(POLISHED_BLACKSTONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CHISELED_NETHER_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CRACKED_NETHER_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(QUARTZ_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(UNKNOWN, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(POWDER_SNOW, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(COPPER_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DRIPSTONE_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DIRT_WITH_ROOTS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HANGING_ROOTS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MOSS_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SPORE_BLOSSOM, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CALCITE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(AMETHYST_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BUDDING_AMETHYST, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(TUFF, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(TINTED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MOSS_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(AZALEA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(FLOWERING_AZALEA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(COPPER_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(EXPOSED_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WEATHERED_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(OXIDIZED_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_EXPOSED_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_WEATHERED_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CUT_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(EXPOSED_CUT_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WEATHERED_CUT_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(OXIDIZED_CUT_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_CUT_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_EXPOSED_CUT_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_WEATHERED_CUT_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SMOOTH_BASALT, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(COBBLED_DEEPSLATE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(POLISHED_DEEPSLATE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEEPSLATE_TILES, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEEPSLATE_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CHISELED_DEEPSLATE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEEPSLATE_LAPIS_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEEPSLATE_IRON_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEEPSLATE_GOLD_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEEPSLATE_REDSTONE_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIT_DEEPSLATE_REDSTONE_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEEPSLATE_DIAMOND_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEEPSLATE_COAL_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEEPSLATE_EMERALD_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEEPSLATE_COPPER_ORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CRACKED_DEEPSLATE_TILES, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CRACKED_DEEPSLATE_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_OXIDIZED_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_OXIDIZED_CUT_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RAW_IRON_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RAW_COPPER_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RAW_GOLD_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SCULK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CLIENT_REQUEST_PLACEHOLDER_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(REINFORCED_DEEPSLATE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(722, LegacyBlockSerializer::deserializeSimple);

        registerDeserializer(MANGROVE_PLANKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MANGROVE_FENCE, LegacyBlockSerializer::deserializeSimple);

        registerDeserializer(STONE, states -> {
            String type = states.getString(STONE_TYPE);
            switch (type) {
                default:
                case STONE_TYPE_STONE:
                    return BlockStone.NORMAL;
                case STONE_TYPE_GRANITE:
                    return BlockStone.GRANITE;
                case STONE_TYPE_GRANITE_SMOOTH:
                    return BlockStone.POLISHED_GRANITE;
                case STONE_TYPE_DIORITE:
                    return BlockStone.DIORITE;
                case STONE_TYPE_DIORITE_SMOOTH:
                    return BlockStone.POLISHED_DIORITE;
                case STONE_TYPE_ANDESITE:
                    return BlockStone.ANDESITE;
                case STONE_TYPE_ANDESITE_SMOOTH:
                    return BlockStone.POLISHED_ANDESITE;
//                default:
//                    throw badValueException(STONE_TYPE, type);
            }
        });

        registerDeserializer(DIRT, states -> {
            String type = states.getString(DIRT_TYPE);
            switch (type) {
                default:
                case DIRT_TYPE_NORMAL:
                    return BlockDirt.NORMAL_DIRT;
                case DIRT_TYPE_COARSE:
                    return BlockDirt.COARSE_DIRT;
//                default:
//                    throw badValueException(DIRT_TYPE, type);
            }
        });

        registerDeserializer(PLANKS, LegacyBlockSerializer::deserializeWoodType);

        registerDeserializer(SAPLING, states -> {
            int meta = deserializeSaplingType(states);
            if (states.getBoolean(AGE_BIT)) {
                meta |= 0b1000;
            }
            return meta;
        });

        registerDeserializer(BEDROCK, states -> states.getBoolean(INFINIBURN_BIT) ? 0b1 : 0);

        registerDeserializer(FLOWING_WATER, LegacyBlockSerializer::deserializeLiquid);
        registerDeserializer(WATER, LegacyBlockSerializer::deserializeLiquid);
        registerDeserializer(FLOWING_LAVA, LegacyBlockSerializer::deserializeLiquid);
        registerDeserializer(LAVA, LegacyBlockSerializer::deserializeLiquid);

        registerDeserializer(SAND, states -> {
            String type = states.getString(SAND_TYPE);
            switch (type) {
                default:
                case SAND_TYPE_NORMAL:
                    return BlockSand.DEFAULT;
                case SAND_TYPE_RED:
                    return BlockSand.RED;
//                default:
//                    throw badValueException(SAND_TYPE, type);
            }
        });

        registerDeserializer(LOG, states -> {
            int meta = deserializePillarAxis(states) << 2;
            String type = states.getString(OLD_LOG_TYPE);
            switch (type) {
                default:
                case OLD_LOG_TYPE_OAK:
                    meta |= BlockWood.OAK;
                    break;
                case OLD_LOG_TYPE_SPRUCE:
                    meta |= BlockWood.SPRUCE;
                    break;
                case OLD_LOG_TYPE_BIRCH:
                    meta |= BlockWood.BIRCH;
                    break;
                case OLD_LOG_TYPE_JUNGLE:
                    meta |= BlockWood.JUNGLE;
                    break;
//                default:
//                    throw badValueException(OLD_LOG_TYPE, type);
            }
            return meta;
        });
        registerDeserializer(LOG2, states -> {
            int meta = deserializePillarAxis(states) << 1;
            String type = states.getString(NEW_LOG_TYPE);
            switch (type) {
                default:
                case NEW_LOG_TYPE_ACACIA:
                    meta |= BlockWood2.ACACIA;
                    break;
                case NEW_LOG_TYPE_DARK_OAK:
                    meta |= BlockWood2.DARK_OAK;
                    break;
//                default:
//                    throw badValueException(NEW_LOG_TYPE, type);
            }
            return meta;
        });
        registerDeserializer(MANGROVE_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_MANGROVE_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(MANGROVE_WOOD, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_MANGROVE_WOOD, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(CRIMSON_STEM, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(WARPED_STEM, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_CRIMSON_STEM, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_WARPED_STEM, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(CRIMSON_HYPHAE, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(WARPED_HYPHAE, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_CRIMSON_HYPHAE, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_WARPED_HYPHAE, LegacyBlockSerializer::deserializePillarAxis);

        registerDeserializer(LEAVES, states -> {
            int meta = deserializeLeaves(states) << 2;
            String type = states.getString(OLD_LEAF_TYPE);
            switch (type) {
                default:
                case OLD_LEAF_TYPE_OAK:
                    meta |= BlockLeaves.OAK;
                    break;
                case OLD_LEAF_TYPE_SPRUCE:
                    meta |= BlockLeaves.SPRUCE;
                    break;
                case OLD_LEAF_TYPE_BIRCH:
                    meta |= BlockLeaves.BIRCH;
                    break;
                case OLD_LEAF_TYPE_JUNGLE:
                    meta |= BlockLeaves.JUNGLE;
                    break;
//                default:
//                    throw badValueException(OLD_LEAF_TYPE, type);
            }
            return meta;
        });
        registerDeserializer(LEAVES2, states -> {
            int meta = deserializeLeaves(states) << 1;
            String type = states.getString(NEW_LEAF_TYPE);
            switch (type) {
                default:
                case NEW_LEAF_TYPE_ACACIA:
                    meta |= BlockLeaves2.ACACIA;
                    break;
                case NEW_LEAF_TYPE_DARK_OAK:
                    meta |= BlockLeaves2.DARK_OAK;
                    break;
//                default:
//                    throw badValueException(NEW_LEAF_TYPE, type);
            }
            return meta;
        });
        registerDeserializer(MANGROVE_LEAVES, LegacyBlockSerializer::deserializeLeaves);

        registerDeserializer(SPONGE, states -> {
            String type = states.getString(SPONGE_TYPE);
            switch (type) {
                default:
                case SPONGE_TYPE_DRY:
                    return BlockSponge.DRY;
                case SPONGE_TYPE_WET:
                    return BlockSponge.WET;
//                default:
//                    throw badValueException(SPONGE_TYPE, type);
            }
        });

        registerDeserializer(DISPENSER, LegacyBlockSerializer::deserializeDispenser);
        registerDeserializer(DROPPER, LegacyBlockSerializer::deserializeDispenser);

        registerDeserializer(SANDSTONE, LegacyBlockSerializer::deserializeSandstone);
        registerDeserializer(RED_SANDSTONE, LegacyBlockSerializer::deserializeSandstone);

        registerDeserializer(BLOCK_BED, states -> {
            int meta = deserializeDirection(states);
            if (states.getBoolean(OCCUPIED_BIT)) {
                meta |= 0b100;
            }
            if (states.getBoolean(HEAD_PIECE_BIT)) {
                meta |= 0b1000;
            }
            return meta;
        });

        registerDeserializer(RAIL, states -> states.getInt(RAIL_DIRECTION) & 0b1111);
        registerDeserializer(GOLDEN_RAIL, LegacyBlockSerializer::deserializeFeatureRail);
        registerDeserializer(DETECTOR_RAIL, LegacyBlockSerializer::deserializeFeatureRail);
        registerDeserializer(ACTIVATOR_RAIL, LegacyBlockSerializer::deserializeFeatureRail);

        registerDeserializer(PISTON, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(PISTON_ARM_COLLISION, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(STICKY_PISTON, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(STICKY_PISTON_ARM_COLLISION, LegacyBlockSerializer::deserializeFacingDirection);

        registerDeserializer(TALLGRASS, states -> {
            String type = states.getString(TALL_GRASS_TYPE);
            switch (type) {
                case TALL_GRASS_TYPE_DEFAULT:
                    return 0;
                default:
                case TALL_GRASS_TYPE_TALL:
                    return 1;
                case TALL_GRASS_TYPE_FERN:
                    return 2;
                case TALL_GRASS_TYPE_SNOW:
                    return 3;
//                default:
//                    throw badValueException(TALL_GRASS_TYPE, type);
            }
        });

        registerDeserializer(WOOL, LegacyBlockSerializer::deserializeColor);
        registerDeserializer(CARPET, LegacyBlockSerializer::deserializeColor);
        registerDeserializer(STAINED_GLASS, LegacyBlockSerializer::deserializeColor);
        registerDeserializer(HARD_STAINED_GLASS, LegacyBlockSerializer::deserializeColor);
        registerDeserializer(STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeColor);
        registerDeserializer(HARD_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeColor);
        registerDeserializer(CONCRETE, LegacyBlockSerializer::deserializeColor);
        registerDeserializer(CONCRETE_POWDER, LegacyBlockSerializer::deserializeColor);
        registerDeserializer(STAINED_HARDENED_CLAY, LegacyBlockSerializer::deserializeColor);
        registerDeserializer(SHULKER_BOX, LegacyBlockSerializer::deserializeColor);

        registerDeserializer(RED_FLOWER, states -> {
            String type = states.getString(FLOWER_TYPE);
            switch (type) {
                default:
                case FLOWER_TYPE_POPPY:
                    return BlockFlower.TYPE_POPPY;
                case FLOWER_TYPE_ORCHID:
                    return BlockFlower.TYPE_BLUE_ORCHID;
                case FLOWER_TYPE_ALLIUM:
                    return BlockFlower.TYPE_ALLIUM;
                case FLOWER_TYPE_HOUSTONIA:
                    return BlockFlower.TYPE_AZURE_BLUET;
                case FLOWER_TYPE_TULIP_RED:
                    return BlockFlower.TYPE_RED_TULIP;
                case FLOWER_TYPE_TULIP_ORANGE:
                    return BlockFlower.TYPE_ORANGE_TULIP;
                case FLOWER_TYPE_TULIP_WHITE:
                    return BlockFlower.TYPE_WHITE_TULIP;
                case FLOWER_TYPE_TULIP_PINK:
                    return BlockFlower.TYPE_PINK_TULIP;
                case FLOWER_TYPE_OXEYE:
                    return BlockFlower.TYPE_OXEYE_DAISY;
                case FLOWER_TYPE_CORNFLOWER:
                    return BlockFlower.TYPE_CORNFLOWER;
                case FLOWER_TYPE_LILY_OF_THE_VALLEY:
                    return BlockFlower.TYPE_LILY_OF_THE_VALLEY;
//                default:
//                    throw badValueException(FLOWER_TYPE, type);
            }
        });

        registerDeserializer(WOODEN_SLAB, LegacyBlockSerializer::deserializeWoodenSlab);
        registerDeserializer(DOUBLE_WOODEN_SLAB, LegacyBlockSerializer::deserializeWoodenSlab);
        registerDeserializer(STONE_SLAB, LegacyBlockSerializer::deserializeStoneSlab1);
        registerDeserializer(DOUBLE_STONE_SLAB, LegacyBlockSerializer::deserializeStoneSlab1);
        registerDeserializer(STONE_SLAB2, LegacyBlockSerializer::deserializeStoneSlab2);
        registerDeserializer(DOUBLE_STONE_SLAB2, LegacyBlockSerializer::deserializeStoneSlab2);
        registerDeserializer(STONE_SLAB3, LegacyBlockSerializer::deserializeStoneSlab3);
        registerDeserializer(DOUBLE_STONE_SLAB3, LegacyBlockSerializer::deserializeStoneSlab3);
        registerDeserializer(STONE_SLAB4, LegacyBlockSerializer::deserializeStoneSlab4);
        registerDeserializer(DOUBLE_STONE_SLAB4, LegacyBlockSerializer::deserializeStoneSlab4);
        registerDeserializer(CRIMSON_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(CRIMSON_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(WARPED_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(WARPED_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(BLACKSTONE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(BLACKSTONE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(POLISHED_BLACKSTONE_BRICK_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(POLISHED_BLACKSTONE_BRICK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(POLISHED_BLACKSTONE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(POLISHED_BLACKSTONE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(MANGROVE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(MANGROVE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);

        registerDeserializer(TNT, states -> {
            int meta = states.getBoolean(EXPLODE_BIT) ? 0b1 : 0;
            if (states.getBoolean(ALLOW_UNDERWATER_BIT)) {
                meta |= 0b10;
            }
            return meta;
        });

        registerDeserializer(TORCH, LegacyBlockSerializer::deserializeTorch);
        registerDeserializer(REDSTONE_TORCH, LegacyBlockSerializer::deserializeTorch);
        registerDeserializer(UNLIT_REDSTONE_TORCH, LegacyBlockSerializer::deserializeTorch);
        registerDeserializer(UNDERWATER_TORCH, LegacyBlockSerializer::deserializeTorch);
        registerDeserializer(SOUL_TORCH, LegacyBlockSerializer::deserializeTorch);
        registerDeserializer(COLORED_TORCH_RG, LegacyBlockSerializer::deserializeColoredTorch);
        registerDeserializer(COLORED_TORCH_BP, LegacyBlockSerializer::deserializeColoredTorch);

        registerDeserializer(FIRE, LegacyBlockSerializer::deserializeFire);
        registerDeserializer(SOUL_FIRE, LegacyBlockSerializer::deserializeFire);

        registerDeserializer(OAK_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(SPRUCE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(BIRCH_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(JUNGLE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(ACACIA_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(DARK_OAK_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(STONE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(MOSSY_COBBLESTONE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(NORMAL_STONE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(GRANITE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(POLISHED_GRANITE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(DIORITE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(POLISHED_DIORITE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(ANDESITE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(POLISHED_ANDESITE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(SANDSTONE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(SMOOTH_SANDSTONE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(RED_SANDSTONE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(SMOOTH_RED_SANDSTONE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(STONE_BRICK_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(MOSSY_STONE_BRICK_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(BRICK_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(NETHER_BRICK_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(RED_NETHER_BRICK_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(QUARTZ_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(SMOOTH_QUARTZ_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(END_BRICK_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(PURPUR_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(PRISMARINE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(DARK_PRISMARINE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(PRISMARINE_BRICKS_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(CRIMSON_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(WARPED_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(BLACKSTONE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(POLISHED_BLACKSTONE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(POLISHED_BLACKSTONE_BRICK_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(CUT_COPPER_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(EXPOSED_CUT_COPPER_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(WEATHERED_CUT_COPPER_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(OXIDIZED_CUT_COPPER_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(WAXED_CUT_COPPER_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(WAXED_EXPOSED_CUT_COPPER_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(WAXED_WEATHERED_CUT_COPPER_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(COBBLED_DEEPSLATE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(POLISHED_DEEPSLATE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(DEEPSLATE_TILE_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(DEEPSLATE_BRICK_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(WAXED_OXIDIZED_CUT_COPPER_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(MUD_BRICK_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(MANGROVE_STAIRS, LegacyBlockSerializer::deserializeStairs);

        registerDeserializer(CHEST, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(TRAPPED_CHEST, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(ENDER_CHEST, LegacyBlockSerializer::deserializeFacingDirection);

        registerDeserializer(REDSTONE_WIRE, LegacyBlockSerializer::deserializeRedstoneSignal);

        registerDeserializer(BLOCK_WHEAT, LegacyBlockSerializer::deserializeGrowth);
        registerDeserializer(BLOCK_BEETROOT, LegacyBlockSerializer::deserializeGrowth);
        registerDeserializer(CARROTS, LegacyBlockSerializer::deserializeGrowth);
        registerDeserializer(POTATOES, LegacyBlockSerializer::deserializeGrowth);
        registerDeserializer(SWEET_BERRY_BUSH, LegacyBlockSerializer::deserializeGrowth);

        registerDeserializer(MELON_STEM, LegacyBlockSerializer::deserializeStem);
        registerDeserializer(PUMPKIN_STEM, LegacyBlockSerializer::deserializeStem);

        registerDeserializer(FARMLAND, states -> states.getInt(MOISTURIZED_AMOUNT) & 0b111);

        registerDeserializer(FURNACE, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(LIT_FURNACE, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(BLAST_FURNACE, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(LIT_BLAST_FURNACE, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(SMOKER, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(LIT_SMOKER, LegacyBlockSerializer::deserializeFacingDirection);

        registerDeserializer(STANDING_SIGN, LegacyBlockSerializer::deserializeStandingSign);
        registerDeserializer(SPRUCE_STANDING_SIGN, LegacyBlockSerializer::deserializeStandingSign);
        registerDeserializer(BIRCH_STANDING_SIGN, LegacyBlockSerializer::deserializeStandingSign);
        registerDeserializer(JUNGLE_STANDING_SIGN, LegacyBlockSerializer::deserializeStandingSign);
        registerDeserializer(ACACIA_STANDING_SIGN, LegacyBlockSerializer::deserializeStandingSign);
        registerDeserializer(DARKOAK_STANDING_SIGN, LegacyBlockSerializer::deserializeStandingSign);
        registerDeserializer(CRIMSON_STANDING_SIGN, LegacyBlockSerializer::deserializeStandingSign);
        registerDeserializer(WARPED_STANDING_SIGN, LegacyBlockSerializer::deserializeStandingSign);
        registerDeserializer(MANGROVE_STANDING_SIGN, LegacyBlockSerializer::deserializeStandingSign);
        registerDeserializer(STANDING_BANNER, LegacyBlockSerializer::deserializeStandingSign);

        registerDeserializer(WALL_SIGN, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(SPRUCE_WALL_SIGN, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(BIRCH_WALL_SIGN, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(JUNGLE_WALL_SIGN, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(ACACIA_WALL_SIGN, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(DARKOAK_WALL_SIGN, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(CRIMSON_WALL_SIGN, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(WARPED_WALL_SIGN, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(MANGROVE_WALL_SIGN, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(WALL_BANNER, LegacyBlockSerializer::deserializeFacingDirection);

        registerDeserializer(BLOCK_IRON_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(BLOCK_WOODEN_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(BLOCK_SPRUCE_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(BLOCK_BIRCH_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(BLOCK_JUNGLE_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(BLOCK_ACACIA_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(BLOCK_DARK_OAK_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(BLOCK_CRIMSON_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(BLOCK_WARPED_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(BLOCK_MANGROVE_DOOR, LegacyBlockSerializer::deserializeDoor);

        registerDeserializer(LADDER, LegacyBlockSerializer::deserializeFacingDirection);

        registerDeserializer(LEVER, states -> {
            int meta = states.getBoolean(OPEN_BIT) ? 0b1000 : 0;
            String direction = states.getString(LEVER_DIRECTION);
            switch (direction) {
                default:
                case LEVER_DIRECTION_DOWN_EAST_WEST:
                    meta |= 0;
                    break;
                case LEVER_DIRECTION_EAST:
                    meta |= 1;
                    break;
                case LEVER_DIRECTION_WEST:
                    meta |= 2;
                    break;
                case LEVER_DIRECTION_SOUTH:
                    meta |= 3;
                    break;
                case LEVER_DIRECTION_NORTH:
                    meta |= 4;
                    break;
                case LEVER_DIRECTION_UP_NORTH_SOUTH:
                    meta |= 5;
                    break;
                case LEVER_DIRECTION_UP_EAST_WEST:
                    meta |= 6;
                    break;
                case LEVER_DIRECTION_DOWN_NORTH_SOUTH:
                    meta |= 7;
                    break;
//                default:
//                    throw badValueException(LEVER_DIRECTION, direction);
            }
            return meta;
        });

        registerDeserializer(LIGHT_WEIGHTED_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);
        registerDeserializer(HEAVY_WEIGHTED_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);
        registerDeserializer(STONE_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);
        registerDeserializer(WOODEN_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);
        registerDeserializer(SPRUCE_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);
        registerDeserializer(BIRCH_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);
        registerDeserializer(JUNGLE_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);
        registerDeserializer(ACACIA_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);
        registerDeserializer(DARK_OAK_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);
        registerDeserializer(CRIMSON_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);
        registerDeserializer(WARPED_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);
        registerDeserializer(POLISHED_BLACKSTONE_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);
        registerDeserializer(MANGROVE_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);

        registerDeserializer(STONE_BUTTON, LegacyBlockSerializer::deserializeButton);
        registerDeserializer(WOODEN_BUTTON, LegacyBlockSerializer::deserializeButton);
        registerDeserializer(SPRUCE_BUTTON, LegacyBlockSerializer::deserializeButton);
        registerDeserializer(BIRCH_BUTTON, LegacyBlockSerializer::deserializeButton);
        registerDeserializer(JUNGLE_BUTTON, LegacyBlockSerializer::deserializeButton);
        registerDeserializer(ACACIA_BUTTON, LegacyBlockSerializer::deserializeButton);
        registerDeserializer(DARK_OAK_BUTTON, LegacyBlockSerializer::deserializeButton);
        registerDeserializer(CRIMSON_BUTTON, LegacyBlockSerializer::deserializeButton);
        registerDeserializer(WARPED_BUTTON, LegacyBlockSerializer::deserializeButton);
        registerDeserializer(POLISHED_BLACKSTONE_BUTTON, LegacyBlockSerializer::deserializeButton);
        registerDeserializer(MANGROVE_BUTTON, LegacyBlockSerializer::deserializeButton);

        registerDeserializer(SNOW_LAYER, states -> {
            int meta = states.getInt(HEIGHT) & 0b111;
            if (states.getBoolean(COVERED_BIT)) {
                meta |= 0b1000;
            }
            return meta;
        });

        registerDeserializer(CACTUS, states -> states.getInt(AGE) & 0b1111);

        registerDeserializer(BLOCK_REEDS, states -> states.getInt(AGE) & 0b1111);

        registerDeserializer(FENCE, LegacyBlockSerializer::deserializeWoodType);

        registerDeserializer(PUMPKIN, LegacyBlockSerializer::deserializeDirection);
        registerDeserializer(LIT_PUMPKIN, LegacyBlockSerializer::deserializeDirection);
        registerDeserializer(CARVED_PUMPKIN, LegacyBlockSerializer::deserializeDirection);

        registerDeserializer(PORTAL, states -> {
            String axis = states.getString(PORTAL_AXIS);
            switch (axis) {
                default:
                case PORTAL_AXIS_UNKNOWN:
                    return BlockNetherPortal.AXIS_UNKNOWN;
                case PORTAL_AXIS_X:
                    return BlockNetherPortal.AXIS_X;
                case PORTAL_AXIS_Z:
                    return BlockNetherPortal.AXIS_Z;
//                default:
//                    throw badValueException(PORTAL_AXIS, axis);
            }
        });

        registerDeserializer(BLOCK_CAKE, states -> states.getInt(BITE_COUNTER) & 0b111);

        registerDeserializer(UNPOWERED_REPEATER, LegacyBlockSerializer::deserializeRepeater);
        registerDeserializer(POWERED_REPEATER, LegacyBlockSerializer::deserializeRepeater);

        registerDeserializer(IRON_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(SPRUCE_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(BIRCH_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(JUNGLE_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(ACACIA_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(DARK_OAK_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(CRIMSON_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(WARPED_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(MANGROVE_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);

        registerDeserializer(MONSTER_EGG, states -> {
            String type = states.getString(MONSTER_EGG_STONE_TYPE);
            switch (type) {
                default:
                case MONSTER_EGG_STONE_TYPE_STONE:
                    return BlockMonsterEgg.STONE;
                case MONSTER_EGG_STONE_TYPE_COBBLESTONE:
                    return BlockMonsterEgg.COBBLESTONE;
                case MONSTER_EGG_STONE_TYPE_STONE_BRICK:
                    return BlockMonsterEgg.STONE_BRICK;
                case MONSTER_EGG_STONE_TYPE_MOSSY_STONE_BRICK:
                    return BlockMonsterEgg.MOSSY_BRICK;
                case MONSTER_EGG_STONE_TYPE_CRACKED_STONE_BRICK:
                    return BlockMonsterEgg.CRACKED_BRICK;
                case MONSTER_EGG_STONE_TYPE_CHISELED_STONE_BRICK:
                    return BlockMonsterEgg.CHISELED_BRICK;
//                default:
//                    throw badValueException(MONSTER_EGG_STONE_TYPE, type);
            }
        });

        registerDeserializer(STONEBRICK, states -> {
            String type = states.getString(STONE_BRICK_TYPE);
            switch (type) {
                default:
                case STONE_BRICK_TYPE_DEFAULT:
                    return BlockBricksStone.NORMAL;
                case STONE_BRICK_TYPE_MOSSY:
                    return BlockBricksStone.MOSSY;
                case STONE_BRICK_TYPE_CRACKED:
                    return BlockBricksStone.CRACKED;
                case STONE_BRICK_TYPE_CHISELED:
                    return BlockBricksStone.CHISELED;
                case STONE_BRICK_TYPE_SMOOTH:
                    return BlockBricksStone.SMOOTH;
//                default:
//                    throw badValueException(STONE_BRICK_TYPE, type);
            }
        });

        registerDeserializer(BROWN_MUSHROOM_BLOCK, LegacyBlockSerializer::deserializeHugeMushroom);
        registerDeserializer(RED_MUSHROOM_BLOCK, LegacyBlockSerializer::deserializeHugeMushroom);

        registerDeserializer(VINE, states -> states.getInt(VINE_DIRECTION_BITS) & 0b1111);

        registerDeserializer(FENCE_GATE, LegacyBlockSerializer::deserializeFenceGate);
        registerDeserializer(SPRUCE_FENCE_GATE, LegacyBlockSerializer::deserializeFenceGate);
        registerDeserializer(BIRCH_FENCE_GATE, LegacyBlockSerializer::deserializeFenceGate);
        registerDeserializer(JUNGLE_FENCE_GATE, LegacyBlockSerializer::deserializeFenceGate);
        registerDeserializer(ACACIA_FENCE_GATE, LegacyBlockSerializer::deserializeFenceGate);
        registerDeserializer(DARK_OAK_FENCE_GATE, LegacyBlockSerializer::deserializeFenceGate);
        registerDeserializer(CRIMSON_FENCE_GATE, LegacyBlockSerializer::deserializeFenceGate);
        registerDeserializer(WARPED_FENCE_GATE, LegacyBlockSerializer::deserializeFenceGate);
        registerDeserializer(MANGROVE_FENCE_GATE, LegacyBlockSerializer::deserializeFenceGate);

        registerDeserializer(BLOCK_NETHER_WART, states -> states.getInt(AGE) & 0b11);

        registerDeserializer(BLOCK_BREWING_STAND, states -> {
            int meta = states.getBoolean(BREWING_STAND_SLOT_A_BIT) ? 0b1 : 0;
            if (states.getBoolean(BREWING_STAND_SLOT_B_BIT)) {
                meta |= 0b10;
            }
            if (states.getBoolean(BREWING_STAND_SLOT_C_BIT)) {
                meta |= 0b100;
            }
            return meta;
        });

        registerDeserializer(BLOCK_CAULDRON, LegacyBlockSerializer::deserializeCauldron);
        registerDeserializer(LAVA_CAULDRON, LegacyBlockSerializer::deserializeCauldron);

        registerDeserializer(END_PORTAL_FRAME, states -> {
            int meta = deserializeDirection(states);
            if (states.getBoolean(END_PORTAL_EYE_BIT)) {
                meta |= 0b100;
            }
            return meta;
        });

        registerDeserializer(COCOA, states -> deserializeDirection(states) | (states.getInt(AGE) & 0b11) << 2);

        registerDeserializer(TRIPWIRE_HOOK, states -> {
            int meta = deserializeDirection(states);
            if (states.getBoolean(ATTACHED_BIT)) {
                meta |= 0b100;
            }
            if (states.getBoolean(POWERED_BIT)) {
                meta |= 0b1000;
            }
            return meta;
        });

        registerDeserializer(TRIP_WIRE, states -> {
            int meta = states.getBoolean(POWERED_BIT) ? 0b1 : 0;
            if (states.getBoolean(SUSPENDED_BIT)) {
                meta |= 0b10;
            }
            if (states.getBoolean(ATTACHED_BIT)) {
                meta |= 0b100;
            }
            if (states.getBoolean(DISARMED_BIT)) {
                meta |= 0b1000;
            }
            return meta;
        });

        registerDeserializer(COMMAND_BLOCK, LegacyBlockSerializer::deserializeCommandBlock);
        registerDeserializer(REPEATING_COMMAND_BLOCK, LegacyBlockSerializer::deserializeCommandBlock);
        registerDeserializer(CHAIN_COMMAND_BLOCK, LegacyBlockSerializer::deserializeCommandBlock);

        registerDeserializer(COBBLESTONE_WALL, states -> {
            int meta = deserializeWall(states) << 4;
            String type = states.getString(WALL_BLOCK_TYPE);
            switch (type) {
                default:
                case WALL_BLOCK_TYPE_COBBLESTONE:
                    meta |= BlockWallCobblestone.NORMAL_COBBLESTONE_WALL;
                    break;
                case WALL_BLOCK_TYPE_MOSSY_COBBLESTONE:
                    meta |= BlockWallCobblestone.MOSSY_COBBLESTONE_WALL;
                    break;
                case WALL_BLOCK_TYPE_GRANITE:
                    meta |= BlockWallCobblestone.GRANITE_WALL;
                    break;
                case WALL_BLOCK_TYPE_DIORITE:
                    meta |= BlockWallCobblestone.DIORITE_WALL;
                    break;
                case WALL_BLOCK_TYPE_ANDESITE:
                    meta |= BlockWallCobblestone.ANDESITE_WALL;
                    break;
                case WALL_BLOCK_TYPE_SANDSTONE:
                    meta |= BlockWallCobblestone.SANDSTONE_WALL;
                    break;
                case WALL_BLOCK_TYPE_BRICK:
                    meta |= BlockWallCobblestone.BRICK_WALL;
                    break;
                case WALL_BLOCK_TYPE_STONE_BRICK:
                    meta |= BlockWallCobblestone.STONE_BRICK_WALL;
                    break;
                case WALL_BLOCK_TYPE_MOSSY_STONE_BRICK:
                    meta |= BlockWallCobblestone.MOSSY_STONE_BRICK_WALL;
                    break;
                case WALL_BLOCK_TYPE_NETHER_BRICK:
                    meta |= BlockWallCobblestone.NETHER_BRICK_WALL;
                    break;
                case WALL_BLOCK_TYPE_END_BRICK:
                    meta |= BlockWallCobblestone.END_BRICK_WALL;
                    break;
                case WALL_BLOCK_TYPE_PRISMARINE:
                    meta |= BlockWallCobblestone.PRISMARINE_WALL;
                    break;
                case WALL_BLOCK_TYPE_RED_SANDSTONE:
                    meta |= BlockWallCobblestone.RED_SANDSTONE_WALL;
                    break;
                case WALL_BLOCK_TYPE_RED_NETHER_BRICK:
                    meta |= BlockWallCobblestone.RED_NETHER_BRICK_WALL;
                    break;
//                default:
//                    throw badValueException(WALL_BLOCK_TYPE, type);
            }
            return meta;
        });
        registerDeserializer(BLACKSTONE_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(POLISHED_BLACKSTONE_BRICK_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(POLISHED_BLACKSTONE_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(COBBLED_DEEPSLATE_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(POLISHED_DEEPSLATE_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(DEEPSLATE_TILE_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(DEEPSLATE_BRICK_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(MUD_BRICK_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(BORDER_BLOCK, LegacyBlockSerializer::deserializeWall);

        registerDeserializer(BLOCK_FLOWER_POT, states -> states.getBoolean(UPDATE_BIT) ? 0b1 : 0);

        registerDeserializer(BLOCK_SKULL, LegacyBlockSerializer::deserializeFacingDirection);

        registerDeserializer(ANVIL, states -> {
            int meta = deserializeDirection(states);
            String damage = states.getString(DAMAGE);
            switch (damage) {
                default:
                case DAMAGE_UNDAMAGED:
                    break;
                case DAMAGE_SLIGHTLY_DAMAGED:
                    meta |= 0b0100;
                    break;
                case DAMAGE_VERY_DAMAGED:
                    meta |= 0b1000;
                    break;
                case DAMAGE_BROKEN:
                    meta |= 0b1100;
                    break;
//                default:
//                    throw badValueException(DAMAGE, damage);
            }
            return meta;
        });

        registerDeserializer(UNPOWERED_COMPARATOR, LegacyBlockSerializer::deserializeComparator);
        registerDeserializer(POWERED_COMPARATOR, LegacyBlockSerializer::deserializeComparator);

        registerDeserializer(DAYLIGHT_DETECTOR, LegacyBlockSerializer::deserializeRedstoneSignal);
        registerDeserializer(DAYLIGHT_DETECTOR_INVERTED, LegacyBlockSerializer::deserializeRedstoneSignal);

        registerDeserializer(BLOCK_HOPPER, states -> {
            int meta = deserializeFacingDirection(states);
            if (states.getBoolean(TOGGLE_BIT)) {
                meta |= 0b1000;
            }
            return meta;
        });

        registerDeserializer(QUARTZ_BLOCK, LegacyBlockSerializer::deserializeChiselPillar);
        registerDeserializer(PURPUR_BLOCK, LegacyBlockSerializer::deserializeChiselPillar);
        registerDeserializer(HAY_BLOCK, LegacyBlockSerializer::deserializeSimplePillar);
        registerDeserializer(BONE_BLOCK, LegacyBlockSerializer::deserializeSimplePillar);

        registerDeserializer(PRISMARINE, states -> {
            String type = states.getString(PRISMARINE_BLOCK_TYPE);
            switch (type) {
                default:
                case PRISMARINE_BLOCK_TYPE_DEFAULT:
                    return BlockPrismarine.NORMAL;
                case PRISMARINE_BLOCK_TYPE_DARK:
                    return BlockPrismarine.DARK;
                case PRISMARINE_BLOCK_TYPE_BRICKS:
                    return BlockPrismarine.BRICKS;
//                default:
//                    throw badValueException(PRISMARINE_BLOCK_TYPE, type);
            }
        });

        registerDeserializer(DOUBLE_PLANT, states -> {
            int meta = states.getBoolean(UPPER_BLOCK_BIT) ? 0b1000 : 0;
            String type = states.getString(DOUBLE_PLANT_TYPE);
            switch (type) {
                default:
                case DOUBLE_PLANT_TYPE_SUNFLOWER:
                    meta |= BlockDoublePlant.SUNFLOWER;
                    break;
                case DOUBLE_PLANT_TYPE_SYRINGA:
                    meta |= BlockDoublePlant.LILAC;
                    break;
                case DOUBLE_PLANT_TYPE_GRASS:
                    meta |= BlockDoublePlant.TALL_GRASS;
                    break;
                case DOUBLE_PLANT_TYPE_FERN:
                    meta |= BlockDoublePlant.LARGE_FERN;
                    break;
                case DOUBLE_PLANT_TYPE_ROSE:
                    meta |= BlockDoublePlant.ROSE_BUSH;
                    break;
                case DOUBLE_PLANT_TYPE_PAEONIA:
                    meta |= BlockDoublePlant.PEONY;
                    break;
//                default:
//                    throw badValueException(DOUBLE_PLANT_TYPE, type);
            }
            return meta;
        });

        registerDeserializer(BLOCK_FRAME, LegacyBlockSerializer::deserializeFrame);
        registerDeserializer(BLOCK_GLOW_FRAME, LegacyBlockSerializer::deserializeFrame);

        registerDeserializer(CHORUS_FLOWER, states -> states.getInt(AGE) & 0b111);

        registerDeserializer(FROSTED_ICE, states -> states.getInt(AGE) & 0b11);

        registerDeserializer(END_ROD, LegacyBlockSerializer::deserializeFacingDirection);

        registerDeserializer(STRUCTURE_VOID, states -> {
            String type = states.getString(STRUCTURE_VOID_TYPE);
            switch (type) {
                default:
                case STRUCTURE_VOID_TYPE_VOID:
                    return BlockStructureVoid.TYPE_VOID;
                case STRUCTURE_VOID_TYPE_AIR:
                    return BlockStructureVoid.TYPE_AIR;
//                default:
//                    throw badValueException(STRUCTURE_VOID_TYPE, type);
            }
        });

        registerDeserializer(PURPLE_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(WHITE_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(ORANGE_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(MAGENTA_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(LIGHT_BLUE_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(YELLOW_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(LIME_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(PINK_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(GRAY_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(SILVER_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(CYAN_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(BLUE_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(BROWN_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(GREEN_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(RED_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(BLACK_GLAZED_TERRACOTTA, LegacyBlockSerializer::deserializeFacingDirection);

        registerDeserializer(CHEMISTRY_TABLE, states -> {
            int meta = deserializeDirection(states);
            String type = states.getString(CHEMISTRY_TABLE_TYPE);
            switch (type) {
                default:
                case CHEMISTRY_TABLE_TYPE_COMPOUND_CREATOR:
                    meta |= BlockChemistryTable.COMPOUND_CREATOR;
                    break;
                case CHEMISTRY_TABLE_TYPE_MATERIAL_REDUCER:
                    meta |= BlockChemistryTable.MATERIAL_REDUCER;
                    break;
                case CHEMISTRY_TABLE_TYPE_ELEMENT_CONSTRUCTOR:
                    meta |= BlockChemistryTable.ELEMENT_CONSTRUCTOR;
                    break;
                case CHEMISTRY_TABLE_TYPE_LAB_TABLE:
                    meta |= BlockChemistryTable.LAB_TABLE;
                    break;
//                default:
//                    throw badValueException(CHEMISTRY_TABLE_TYPE, type);
            }
            return meta;
        });

        registerDeserializer(OBSERVER, states -> {
            int meta = deserializeFacingDirection(states);
            if (states.getBoolean(POWERED_BIT)) {
                meta |= 0b1000;
            }
            return meta;
        });

        registerDeserializer(STRUCTURE_BLOCK, states -> {
            String type = states.getString(STRUCTURE_BLOCK_TYPE);
            switch (type) {
                case STRUCTURE_BLOCK_TYPE_DATA:
                    return BlockStructure.TYPE_DATA;
                case STRUCTURE_BLOCK_TYPE_SAVE:
                    return BlockStructure.TYPE_SAVE;
                case STRUCTURE_BLOCK_TYPE_LOAD:
                    return BlockStructure.TYPE_LOAD;
                case STRUCTURE_BLOCK_TYPE_CORNER:
                    return BlockStructure.TYPE_CORNER;
                default:
                case STRUCTURE_BLOCK_TYPE_INVALID:
                    return BlockStructure.TYPE_INVALID;
                case STRUCTURE_BLOCK_TYPE_EXPORT:
                    return BlockStructure.TYPE_EXPORT;
//                default:
//                    throw badValueException(STRUCTURE_BLOCK_TYPE, type);
            }
        });

        registerDeserializer(STRIPPED_OAK_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_SPRUCE_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_BIRCH_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_JUNGLE_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_ACACIA_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_DARK_OAK_LOG, LegacyBlockSerializer::deserializePillarAxis);

        registerDeserializer(SEAGRASS, states -> {
            String type = states.getString(SEA_GRASS_TYPE);
            switch (type) {
                default:
                case SEA_GRASS_TYPE_DEFAULT:
                    return BlockSeagrass.DEFAULT_SEAGRASS;
                case SEA_GRASS_TYPE_DOUBLE_TOP:
                    return BlockSeagrass.DOUBLE_SEAGRASS_TOP;
                case SEA_GRASS_TYPE_DOUBLE_BOT:
                    return BlockSeagrass.DOUBLE_SEAGRASS_BOTTOM;
//                default:
//                    throw badValueException(SEA_GRASS_TYPE, type);
            }
        });

        registerDeserializer(CORAL, LegacyBlockSerializer::deserializeCoral);
        registerDeserializer(CORAL_BLOCK, LegacyBlockSerializer::deserializeCoral);

        registerDeserializer(CORAL_FAN, LegacyBlockSerializer::deserializeCoralFan);
        registerDeserializer(CORAL_FAN_DEAD, LegacyBlockSerializer::deserializeCoralFan);

        registerDeserializer(CORAL_FAN_HANG, LegacyBlockSerializer::deserializeHangCoralFan);
        registerDeserializer(CORAL_FAN_HANG2, LegacyBlockSerializer::deserializeHangCoralFan);
        registerDeserializer(CORAL_FAN_HANG3, LegacyBlockSerializer::deserializeHangCoralFan);

        registerDeserializer(BLOCK_KELP, LegacyBlockSerializer::deserializeGrowthPlant);
        registerDeserializer(WEEPING_VINES, LegacyBlockSerializer::deserializeGrowthPlant);
        registerDeserializer(TWISTING_VINES, LegacyBlockSerializer::deserializeGrowthPlant);

        registerDeserializer(SEA_PICKLE, states -> {
            int meta = states.getInt(CLUSTER_COUNT) & 0b11;
            if (states.getBoolean(DEAD_BIT)) {
                meta |= 0b100;
            }
            return meta;
        });

        registerDeserializer(TURTLE_EGG, states -> {
            int meta;
            String count = states.getString(TURTLE_EGG_COUNT);
            switch (count) {
                default:
                case TURTLE_EGG_COUNT_ONE_EGG:
                    meta = 0;
                    break;
                case TURTLE_EGG_COUNT_TWO_EGG:
                    meta = 1;
                    break;
                case TURTLE_EGG_COUNT_THREE_EGG:
                    meta = 2;
                    break;
                case TURTLE_EGG_COUNT_FOUR_EGG:
                    meta = 3;
                    break;
//                default:
//                    throw badValueException(TURTLE_EGG_COUNT, count);
            }
            String type = states.getString(CRACKED_STATE);
            switch (type) {
                default:
                case CRACKED_STATE_NO_CRACKS:
                    break;
                case CRACKED_STATE_CRACKED:
                    meta |= 0b0100;
                    break;
                case CRACKED_STATE_MAX_CRACKED:
                    meta |= 0b1000;
                    break;
//                default:
//                    throw badValueException(CRACKED_STATE, count);
            }
            return meta;
        });

        registerDeserializer(BUBBLE_COLUMN, states -> states.getBoolean(DRAG_DOWN) ? 0b1 : 0);

        registerDeserializer(BAMBOO, states -> {
            int meta = states.getBoolean(AGE_BIT) ? 0b1000 : 0;
            String count = states.getString(BAMBOO_STALK_THICKNESS);
            switch (count) {
                default:
                case BAMBOO_STALK_THICKNESS_THIN:
                    break;
                case BAMBOO_STALK_THICKNESS_THICK:
                    meta |= 0b1;
                    break;
//                default:
//                    throw badValueException(BAMBOO_STALK_THICKNESS, count);
            }
            String type = states.getString(BAMBOO_LEAF_SIZE);
            switch (type) {
                default:
                case BAMBOO_LEAF_SIZE_NO_LEAVES:
                    break;
                case BAMBOO_LEAF_SIZE_SMALL_LEAVES:
                    meta |= 0b010;
                    break;
                case BAMBOO_LEAF_SIZE_LARGE_LEAVES:
                    meta |= 0b100;
                    break;
//                default:
//                    throw badValueException(BAMBOO_LEAF_SIZE, count);
            }
            return meta;
        });

        registerDeserializer(BAMBOO_SAPLING, states -> {
            int meta = deserializeSaplingType(states) << 1;
            if (states.getBoolean(AGE_BIT)) {
                meta |= 0b1;
            }
            return meta;
        });

        registerDeserializer(SCAFFOLDING, states -> {
            int meta = states.getInt(STABILITY) & 0b111;
            if (states.getBoolean(STABILITY_CHECK)) {
                meta |= 0b1000;
            }
            return meta;
        });

        registerDeserializer(LECTERN, states -> {
            int meta = deserializeDirection(states);
            if (states.getBoolean(POWERED_BIT)) {
                meta |= 0b100;
            }
            return meta;
        });

        registerDeserializer(GRINDSTONE, states -> deserializeDirection(states) | deserializeAttachment(states) << 2);

        registerDeserializer(STONECUTTER_BLOCK, LegacyBlockSerializer::deserializeFacingDirection);

        registerDeserializer(BARREL, states -> {
            int meta = deserializeFacingDirection(states);
            if (states.getBoolean(OPEN_BIT)) {
                meta |= 0b1000;
            }
            return meta;
        });

        registerDeserializer(LOOM, LegacyBlockSerializer::deserializeDirection);

        registerDeserializer(BELL, states -> {
            int meta = deserializeDirection(states) | deserializeAttachment(states) << 2;
            if (states.getBoolean(TOGGLE_BIT)) {
                meta |= 0b10000;
            }
            return meta;
        });

        registerDeserializer(LANTERN, LegacyBlockSerializer::deserializeLantern);
        registerDeserializer(SOUL_LANTERN, LegacyBlockSerializer::deserializeLantern);

        registerDeserializer(BLOCK_CAMPFIRE, LegacyBlockSerializer::deserializeCampfire);
        registerDeserializer(BLOCK_SOUL_CAMPFIRE, LegacyBlockSerializer::deserializeCampfire);

        registerDeserializer(JIGSAW, states -> deserializeFacingDirection(states) | (states.getInt(ROTATION) & 0b11) << 3);

        registerDeserializer(WOOD, states -> {
            int meta = deserializeWoodType(states) | deserializePillarAxis(states) << 4;
            if (states.getBoolean(STRIPPED_BIT)) {
                meta |= 0b1000;
            }
            return meta;
        });

        registerDeserializer(COMPOSTER, states -> states.getInt(COMPOSTER_FILL_LEVEL) & 0b1111);

        registerDeserializer(LIGHT_BLOCK, states -> states.getInt(BLOCK_LIGHT_LEVEL) & 0b1111);

        registerDeserializer(BEEHIVE, LegacyBlockSerializer::deserializeBeehive);
        registerDeserializer(BEE_NEST, LegacyBlockSerializer::deserializeBeehive);

        //TODO: 1.16+

        registerDeserializer(BASALT, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(POLISHED_BASALT, LegacyBlockSerializer::deserializePillarAxis);

        registerDeserializer(RESPAWN_ANCHOR, states -> states.getInt(RESPAWN_ANCHOR_CHARGE) & 0b111);

        registerDeserializer(BLOCK_CHAIN, LegacyBlockSerializer::deserializePillarAxis);

        registerDeserializer(ELEMENT_0, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_1, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_2, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_3, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_4, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_5, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_6, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_7, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_8, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_9, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_10, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_11, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_12, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_13, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_14, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_15, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_16, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_17, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_18, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_19, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_20, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_21, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_22, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_23, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_24, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_25, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_26, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_27, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_28, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_29, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_30, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_31, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_32, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_33, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_34, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_35, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_36, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_37, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_38, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_39, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_40, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_41, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_42, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_43, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_44, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_45, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_46, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_47, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_48, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_49, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_50, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_51, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_52, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_53, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_54, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_55, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_56, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_57, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_58, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_59, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_60, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_61, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_62, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_63, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_64, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_65, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_66, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_67, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_68, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_69, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_70, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_71, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_72, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_73, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_74, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_75, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_76, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_77, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_78, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_79, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_80, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_81, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_82, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_83, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_84, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_85, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_86, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_87, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_88, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_89, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_90, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_91, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_92, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_93, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_94, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_95, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_96, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_97, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_98, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_99, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_100, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_101, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_102, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_103, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_104, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_105, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_106, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_107, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_108, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_109, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_110, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_111, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_112, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_113, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_114, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_115, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_116, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_117, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ELEMENT_118, LegacyBlockSerializer::deserializeSimple);

        BlockSerializer.setRuntimeSerializer(new RuntimeBlockSerializer() {
            @Override
            public CompoundTag serialize(int fullId) {
                return LegacyBlockSerializer.serialize(fullId);
            }

            @Override
            public int deserialize(CompoundTag tag) {
                return LegacyBlockSerializer.deserialize(tag);
            }
        });
    }

    private static int deserializeSimple(CompoundTag states) {
        return 0;
    }

    private static int deserializeLiquid(CompoundTag states) {
        return states.getInt(LIQUID_DEPTH) & 0b1111;
    }

    private static int deserializeDispenser(CompoundTag states) {
        int meta = deserializeFacingDirection(states);
        if (states.getBoolean(TRIGGERED_BIT)) {
            meta |= 0b1000;
        }
        return meta;
    }

    private static int deserializeSandstone(CompoundTag states) {
        String type = states.getString(SAND_STONE_TYPE);
        switch (type) {
            default:
            case SAND_STONE_TYPE_DEFAULT:
                return BlockSandstone.NORMAL;
            case SAND_STONE_TYPE_HEIROGLYPHS:
                return BlockSandstone.CHISELED;
            case SAND_STONE_TYPE_CUT:
                return BlockSandstone.CUT;
            case SAND_STONE_TYPE_SMOOTH:
                return BlockSandstone.SMOOTH;
//            default:
//                throw badValueException(SAND_STONE_TYPE, type);
        }
    }

    private static int deserializeFeatureRail(CompoundTag states) {
        int meta = states.getInt(RAIL_DIRECTION) & 0b111;
        if (states.getBoolean(RAIL_DATA_BIT)) {
            meta |= 0b1000;
        }
        return meta;
    }

    private static int deserializeTorch(CompoundTag states) {
        String direction = states.getString(TORCH_FACING_DIRECTION);
        switch (direction) {
            default:
            case TORCH_FACING_DIRECTION_UNKNOWN:
                return 0;
            case TORCH_FACING_DIRECTION_WEST:
                return 1;
            case TORCH_FACING_DIRECTION_EAST:
                return 2;
            case TORCH_FACING_DIRECTION_NORTH:
                return 3;
            case TORCH_FACING_DIRECTION_SOUTH:
                return 4;
            case TORCH_FACING_DIRECTION_TOP:
                return 5;
//            default:
//                throw badValueException(TORCH_FACING_DIRECTION, direction);
        }
    }

    private static int deserializeColoredTorch(CompoundTag states) {
        int meta = deserializeTorch(states);
        if (states.getBoolean(COLOR_BIT)) {
            meta |= 0b1000;
        }
        return meta;
    }

    private static int deserializeSlab(CompoundTag states) {
        return states.getBoolean(TOP_SLOT_BIT) ? 0b1 : 0;
    }

    private static int deserializeWoodenSlab(CompoundTag states) {
        return (deserializeSlab(states) << 3) | deserializeWoodType(states);
    }

    private static int deserializeStoneSlab1(CompoundTag states) {
        int meta = deserializeSlab(states) << 3;
        String type = states.getString(STONE_SLAB_TYPE);
        switch (type) {
            default:
            case STONE_SLAB_TYPE_SMOOTH_STONE:
                meta |= BlockSlabStone.SMOOTH_STONE;
                break;
            case STONE_SLAB_TYPE_SANDSTONE:
                meta |= BlockSlabStone.SANDSTONE;
                break;
            case STONE_SLAB_TYPE_WOOD:
                meta |= BlockSlabStone.WOOD;
                break;
            case STONE_SLAB_TYPE_COBBLESTONE:
                meta |= BlockSlabStone.COBBLESTONE;
                break;
            case STONE_SLAB_TYPE_BRICK:
                meta |= BlockSlabStone.BRICK;
                break;
            case STONE_SLAB_TYPE_STONE_BRICK:
                meta |= BlockSlabStone.STONE_BRICK;
                break;
            case STONE_SLAB_TYPE_QUARTZ:
                meta |= BlockSlabStone.QUARTZ;
                break;
            case STONE_SLAB_TYPE_NETHER_BRICK:
                meta |= BlockSlabStone.NETHER_BRICK;
                break;
//            default:
//                throw badValueException(STONE_SLAB_TYPE, type);
        }
        return meta;
    }

    private static int deserializeStoneSlab2(CompoundTag states) {
        int meta = deserializeSlab(states) << 3;
        String type = states.getString(STONE_SLAB_TYPE_2);
        switch (type) {
            default:
            case STONE_SLAB_TYPE_2_RED_SANDSTONE:
                meta |= BlockSlabRedSandstone.RED_SANDSTONE;
                break;
            case STONE_SLAB_TYPE_2_PURPUR:
                meta |= BlockSlabRedSandstone.PURPUR;
                break;
            case STONE_SLAB_TYPE_2_PRISMARINE_ROUGH:
                meta |= BlockSlabRedSandstone.PRISMARINE_ROUGH;
                break;
            case STONE_SLAB_TYPE_2_PRISMARINE_DARK:
                meta |= BlockSlabRedSandstone.PRISMARINE_DARK;
                break;
            case STONE_SLAB_TYPE_2_PRISMARINE_BRICK:
                meta |= BlockSlabRedSandstone.PRISMARINE_BRICK;
                break;
            case STONE_SLAB_TYPE_2_MOSSY_COBBLESTONE:
                meta |= BlockSlabRedSandstone.MOSSY_COBBLESTONE;
                break;
            case STONE_SLAB_TYPE_2_SMOOTH_SANDSTONE:
                meta |= BlockSlabRedSandstone.SMOOTH_SANDSTONE;
                break;
            case STONE_SLAB_TYPE_2_RED_NETHER_BRICK:
                meta |= BlockSlabRedSandstone.RED_NETHER_BRICK;
                break;
//            default:
//                throw badValueException(STONE_SLAB_TYPE_2, type);
        }
        return meta;
    }

    private static int deserializeStoneSlab3(CompoundTag states) {
        int meta = deserializeSlab(states) << 3;
        String type = states.getString(STONE_SLAB_TYPE_3);
        switch (type) {
            default:
            case STONE_SLAB_TYPE_3_END_STONE_BRICK:
                meta |= BlockSlabStone3.END_STONE_BRICK;
                break;
            case STONE_SLAB_TYPE_3_SMOOTH_RED_SANDSTONE:
                meta |= BlockSlabStone3.SMOOTH_RED_SANDSTONE;
                break;
            case STONE_SLAB_TYPE_3_POLISHED_ANDESITE:
                meta |= BlockSlabStone3.POLISHED_ANDESITE;
                break;
            case STONE_SLAB_TYPE_3_ANDESITE:
                meta |= BlockSlabStone3.ANDESITE;
                break;
            case STONE_SLAB_TYPE_3_DIORITE:
                meta |= BlockSlabStone3.DIORITE;
                break;
            case STONE_SLAB_TYPE_3_POLISHED_DIORITE:
                meta |= BlockSlabStone3.POLISHED_DIORITE;
                break;
            case STONE_SLAB_TYPE_3_GRANITE:
                meta |= BlockSlabStone3.GRANITE;
                break;
            case STONE_SLAB_TYPE_3_POLISHED_GRANITE:
                meta |= BlockSlabStone3.POLISHED_GRANITE;
                break;
//            default:
//                throw badValueException(STONE_SLAB_TYPE_3, type);
        }
        return meta;
    }

    private static int deserializeStoneSlab4(CompoundTag states) {
        int meta = deserializeSlab(states) << 3;
        String type = states.getString(STONE_SLAB_TYPE_4);
        switch (type) {
            default:
            case STONE_SLAB_TYPE_4_MOSSY_STONE_BRICK:
                meta |= BlockSlabStone4.MOSSY_STONE_BRICK;
                break;
            case STONE_SLAB_TYPE_4_SMOOTH_QUARTZ:
                meta |= BlockSlabStone4.SMOOTH_QUARTZ;
                break;
            case STONE_SLAB_TYPE_4_STONE:
                meta |= BlockSlabStone4.STONE;
                break;
            case STONE_SLAB_TYPE_4_CUT_SANDSTONE:
                meta |= BlockSlabStone4.CUT_SANDSTONE;
                break;
            case STONE_SLAB_TYPE_4_CUT_RED_SANDSTONE:
                meta |= BlockSlabStone4.CUT_RED_SANDSTONE;
                break;
//            default:
//                throw badValueException(STONE_SLAB_TYPE_4, type);
        }
        return meta;
    }

    private static int deserializeStairs(CompoundTag states) {
        int meta = states.getInt(WEIRDO_DIRECTION) & 0b11;
        if (states.getBoolean(UPSIDE_DOWN_BIT)) {
            meta |= 0b100;
        }
        return meta;
    }

    private static int deserializeStandingSign(CompoundTag states) {
        return states.getInt(GROUND_SIGN_DIRECTION) & 0b1111;
    }

    private static int deserializeFenceGate(CompoundTag states) {
        int meta = deserializeDirection(states);
        if (states.getBoolean(OPEN_BIT)) {
            meta |= 0b100;
        }
        if (states.getBoolean(IN_WALL_BIT)) {
            meta |= 0b1000;
        }
        return meta;
    }

    private static int deserializeDoor(CompoundTag states) {
        int meta = deserializeDirection(states);
        if (states.getBoolean(OPEN_BIT)) {
            meta |= 0b100;
        }
        if (states.getBoolean(UPPER_BLOCK_BIT)) {
            meta |= 0b1000;
        }
        if (states.getBoolean(DOOR_HINGE_BIT)) {
            meta |= 0b10000;
        }
        return meta;
    }

    private static int deserializeTrapdoor(CompoundTag states) {
        int meta = deserializeDirection(states);
        if (states.getBoolean(UPSIDE_DOWN_BIT)) {
            meta |= 0b100;
        }
        if (states.getBoolean(OPEN_BIT)) {
            meta |= 0b1000;
        }
        return meta;
    }

    private static int deserializeButton(CompoundTag states) {
        int meta = deserializeFacingDirection(states);
        if (states.getBoolean(BUTTON_PRESSED_BIT)) {
            meta |= 0b1000;
        }
        return meta;
    }

    private static int deserializeRepeater(CompoundTag states) {
        return deserializeDirection(states) | (states.getInt(REPEATER_DELAY) & 0b11) << 2;
    }

    private static int deserializeComparator(CompoundTag states) {
        int meta = deserializeDirection(states);
        if (states.getBoolean(OUTPUT_SUBTRACT_BIT)) {
            meta |= 0b100;
        }
        if (states.getBoolean(OUTPUT_LIT_BIT)) {
            meta |= 0b1000;
        }
        return meta;
    }

    private static int deserializeLeaves(CompoundTag states) {
        int meta = states.getBoolean(UPDATE_BIT) ? 0b1 : 0;
        if (states.getBoolean(PERSISTENT_BIT)) {
            meta |= 0b10;
        }
        return meta;
    }

    private static int deserializeHugeMushroom(CompoundTag states) {
        return states.getInt(HUGE_MUSHROOM_BITS) & 0b1111;
    }

    private static int deserializeStem(CompoundTag states) {
        return deserializeGrowth(states) | deserializeFacingDirection(states) << 3;
    }

    private static int deserializeCauldron(CompoundTag states) {
        int meta = states.getInt(FILL_LEVEL) & 0b111;
        String type = states.getString(CAULDRON_LIQUID);
        switch (type) {
            default:
            case CAULDRON_LIQUID_WATER:
                break;
            case CAULDRON_LIQUID_LAVA:
                meta |= 0b01000;
                break;
            case CAULDRON_LIQUID_POWDER_SNOW:
                meta |= 0b10000;
                break;
//            default:
//                throw badValueException(CAULDRON_LIQUID, type);
        }
        return meta;
    }

    private static int deserializeCommandBlock(CompoundTag states) {
        int meta = deserializeFacingDirection(states);
        if (states.getBoolean(CONDITIONAL_BIT)) {
            meta |= 0b1000;
        }
        return meta;
    }

    private static int deserializeFrame(CompoundTag states) {
        int meta = deserializeFacingDirection(states);
        if (states.getBoolean(ITEM_FRAME_MAP_BIT)) {
            meta |= 0b1000;
        }
        if (states.getBoolean(ITEM_FRAME_PHOTO_BIT)) {
            meta |= 0b10000;
        }
        return meta;
    }

    private static int deserializeCampfire(CompoundTag states) {
        int meta = deserializeDirection(states);
        if (states.getBoolean(EXTINGUISHED)) {
            meta |= 0b100;
        }
        return meta;
    }

    private static int deserializeFire(CompoundTag states) {
        return states.getInt(AGE) & 0b1111;
    }

    private static int deserializeLantern(CompoundTag states) {
        return states.getBoolean(HANGING) ? 0b1 : 0;
    }

    private static int deserializeBeehive(CompoundTag states) {
        return deserializeDirection(states) | (states.getInt(HONEY_LEVEL) & 0b111) << 2;
    }

    private static int deserializeChiselPillar(CompoundTag states) {
        int meta = deserializePillarAxis(states) << 2;
        String type = states.getString(CHISEL_TYPE);
        switch (type) {
            default:
            case CHISEL_TYPE_DEFAULT:
                break;
            case CHISEL_TYPE_CHISELED:
                meta |= 0b01;
                break;
            case CHISEL_TYPE_LINES:
                meta |= 0b10;
                break;
            case CHISEL_TYPE_SMOOTH:
                meta |= 0b11;
                break;
//            default:
//                throw badValueException(CHISEL_TYPE, type);
        }
        return meta;
    }

    private static int deserializeSimplePillar(CompoundTag states) {
        // discard "deprecated" state
        return /*(states.getInt(DEPRECATED) & 0b11) |*/ deserializePillarAxis(states) << 2;
    }

    private static int deserializeDirection(CompoundTag states) {
        return states.getInt(DIRECTION) & 0b11;
    }

    private static int deserializeFacingDirection(CompoundTag states) {
        return states.getInt(FACING_DIRECTION) & 0b111;
    }

    private static int deserializePillarAxis(CompoundTag states) {
        String axis = states.getString(PILLAR_AXIS);
        switch (axis) {
            default:
            case PILLAR_AXIS_Y:
                return 0b00;
            case PILLAR_AXIS_X:
                return 0b01;
            case PILLAR_AXIS_Z:
                return 0b10;
//            default:
//                throw badValueException(PILLAR_AXIS, axis);
        }
    }

    private static int deserializeColor(CompoundTag states) {
        String color = states.getString(COLOR);
        switch (color) {
            default:
            case COLOR_WHITE:
                return 0;
            case COLOR_ORANGE:
                return 1;
            case COLOR_MAGENTA:
                return 2;
            case COLOR_LIGHT_BLUE:
                return 3;
            case COLOR_YELLOW:
                return 4;
            case COLOR_LIME:
                return 5;
            case COLOR_PINK:
                return 6;
            case COLOR_GRAY:
                return 7;
            case COLOR_SILVER:
                return 8;
            case COLOR_CYAN:
                return 9;
            case COLOR_PURPLE:
                return 10;
            case COLOR_BLUE:
                return 11;
            case COLOR_BROWN:
                return 12;
            case COLOR_GREEN:
                return 13;
            case COLOR_RED:
                return 14;
            case COLOR_BLACK:
                return 15;
//            default:
//                throw badValueException(COLOR, color);
        }
    }

    private static int deserializeCoralColor(CompoundTag states) {
        String color = states.getString(CORAL_COLOR);
        switch (color) {
            default:
            case CORAL_COLOR_BLUE:
                return BlockCoral.BLUE;
            case CORAL_COLOR_PINK:
                return BlockCoral.PINK;
            case CORAL_COLOR_PURPLE:
                return BlockCoral.PURPLE;
            case CORAL_COLOR_RED:
                return BlockCoral.RED;
            case CORAL_COLOR_YELLOW:
                return BlockCoral.YELLOW;
//            default:
//                throw badValueException(CORAL_COLOR, color);
        }
    }

    private static int deserializeCoral(CompoundTag states) {
        int meta = deserializeCoralColor(states);
        if (states.getBoolean(DEAD_BIT)) {
            meta |= 0b1000;
        }
        return meta;
    }

    private static int deserializeCoralFan(CompoundTag states) {
        return deserializeCoralColor(states) | (states.getInt(CORAL_FAN_DIRECTION) & 0b11) << 3;
    }

    private static int deserializeHangCoralFan(CompoundTag states) {
        int meta = (states.getInt(CORAL_FAN_DIRECTION) & 0b11) << 2;
        if (states.getBoolean(CORAL_HANG_TYPE_BIT)) {
            meta |= 0b1;
        }
        if (states.getBoolean(DEAD_BIT)) {
            meta |= 0b10;
        }
        return meta;
    }

    private static int deserializeWoodType(CompoundTag states) {
        String type = states.getString(WOOD_TYPE);
        switch (type) {
            default:
            case WOOD_TYPE_OAK:
                return BlockPlanks.OAK;
            case WOOD_TYPE_SPRUCE:
                return BlockPlanks.SPRUCE;
            case WOOD_TYPE_BIRCH:
                return BlockPlanks.BIRCH;
            case WOOD_TYPE_JUNGLE:
                return BlockPlanks.JUNGLE;
            case WOOD_TYPE_ACACIA:
                return BlockPlanks.ACACIA;
            case WOOD_TYPE_DARK_OAK:
                return BlockPlanks.DARK_OAK;
//            default:
//                throw badValueException(WOOD_TYPE, type);
        }
    }

    private static int deserializeRedstoneSignal(CompoundTag states) {
        return states.getInt(REDSTONE_SIGNAL) & 0b1111;
    }

    private static int deserializeGrowth(CompoundTag states) {
        return states.getInt(GROWTH) & 0b111;
    }

    private static int deserializeSaplingType(CompoundTag states) {
        String type = states.getString(SAPLING_TYPE);
        switch (type) {
            default:
            case SAPLING_TYPE_OAK:
                return BlockSapling.OAK;
            case SAPLING_TYPE_SPRUCE:
                return BlockSapling.SPRUCE;
            case SAPLING_TYPE_BIRCH:
                return BlockSapling.BIRCH;
            case SAPLING_TYPE_JUNGLE:
                return BlockSapling.JUNGLE;
            case SAPLING_TYPE_ACACIA:
                return BlockSapling.ACACIA;
            case SAPLING_TYPE_DARK_OAK:
                return BlockSapling.DARK_OAK;
//            default:
//                throw badValueException(SAPLING_TYPE, type);
        }
    }

    private static int deserializeAttachment(CompoundTag states) {
        String type = states.getString(ATTACHMENT);
        switch (type) {
            default:
            case ATTACHMENT_STANDING:
                return 0b00;
            case ATTACHMENT_HANGING:
                return 0b01;
            case ATTACHMENT_SIDE:
                return 0b10;
            case ATTACHMENT_MULTIPLE:
                return 0b11;
//            default:
//                throw badValueException(ATTACHMENT, type);
        }
    }

    private static int deserializeWall(CompoundTag states) {
        int meta = states.getBoolean(WALL_POST_BIT) ? 0b1 : 0;
        meta |= deserializeWallConnectionType(states, WALL_CONNECTION_TYPE_NORTH) << 1;
        meta |= deserializeWallConnectionType(states, WALL_CONNECTION_TYPE_EAST) << 3;
        meta |= deserializeWallConnectionType(states, WALL_CONNECTION_TYPE_SOUTH) << 5;
        meta |= deserializeWallConnectionType(states, WALL_CONNECTION_TYPE_WEST) << 7;
        return meta;
    }

    private static int deserializeWallConnectionType(CompoundTag states, String direction) {
        String type = states.getString(direction);
        switch (type) {
            default:
            case WALL_CONNECTION_TYPE_NORTH_NONE:
                return 0b00;
            case WALL_CONNECTION_TYPE_NORTH_SHORT:
                return 0b01;
            case WALL_CONNECTION_TYPE_NORTH_TALL:
                return 0b10;
//            default:
//                throw badValueException(direction, type);
        }
    }

    private static int deserializeGrowthPlant(CompoundTag states) {
        return states.getInt(AGE) & 0b11111;
    }

    public static void initialize() {
    }

    private LegacyBlockSerializer() {
        throw new IllegalStateException();
    }

    @FunctionalInterface
    private interface DeserializationFunction {
        int deserialize(CompoundTag states);
    }
}

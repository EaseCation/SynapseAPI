package org.itxtech.synapseapi.multiprotocol.utils.block;

import cn.nukkit.block.*;
import cn.nukkit.block.BlockSerializer.RuntimeBlockSerializer;
import cn.nukkit.block.edu.*;
import cn.nukkit.block.state.BlockLegacy;
import cn.nukkit.block.state.BlockTypes;
import cn.nukkit.nbt.tag.CompoundTag;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.BiomeDefinitions;
import org.itxtech.synapseapi.multiprotocol.utils.CraftingPacketManager;
import org.itxtech.synapseapi.multiprotocol.utils.CreativeItemsPalette;

import java.util.function.IntFunction;

import static cn.nukkit.GameVersion.*;
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
        return FORWARD_SERIALIZER.get(fullId).clone();
    }

    public static int deserialize(CompoundTag tag) {
        String name = tag.getString("name");
        CompoundTag states = tag.getCompound("states");

        int id = Blocks.getIdByBlockName(name, true);
        if (id == -1) {
            log.warn("Unmapped block name: {}", name);
            return Block.UNKNOWN << Block.BLOCK_META_BITS;
        }

        if (states.isEmpty()) {
            return id << Block.BLOCK_META_BITS;
        }

        DeserializationFunction deserializer = DESERIALIZERS[id];
        if (deserializer == null) {
            log.warn("Missing block state deserializer: {}", name);
            return Block.UNKNOWN << Block.BLOCK_META_BITS;
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

    static {
        log.debug("Loading block serializer...");

        registerDeserializer(AIR, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GRASS_BLOCK, LegacyBlockSerializer::deserializeSimple);
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
        registerDeserializer(DANDELION, LegacyBlockSerializer::deserializeSimple);
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
        //registerDeserializer(MYSTERIOUS_FRAME_SLOT, LegacyBlockSerializer::deserializeSimple); //TODO: 1.18.30 remove me
        registerDeserializer(FROG_SPAWN, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MUD, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MUD_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PACKED_MUD, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MANGROVE_ROOTS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MANGROVE_PLANKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MANGROVE_FENCE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BAMBOO_MOSAIC, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BAMBOO_PLANKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BAMBOO_FENCE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CHERRY_PLANKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CHERRY_FENCE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(TORCHFLOWER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(POLISHED_TUFF, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CHISELED_TUFF, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(TUFF_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CHISELED_TUFF_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CHISELED_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(EXPOSED_CHISELED_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WEATHERED_CHISELED_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(OXIDIZED_CHISELED_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_CHISELED_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_EXPOSED_CHISELED_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_OXIDIZED_CHISELED_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_WEATHERED_CHISELED_COPPER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(COPPER_GRATE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(EXPOSED_COPPER_GRATE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WEATHERED_COPPER_GRATE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(OXIDIZED_COPPER_GRATE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_COPPER_GRATE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_EXPOSED_COPPER_GRATE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_WEATHERED_COPPER_GRATE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WAXED_OXIDIZED_COPPER_GRATE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HEAVY_CORE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PALE_OAK_FENCE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PALE_OAK_PLANKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PALE_MOSS_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RESIN_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RESIN_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CHISELED_RESIN_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(OPEN_EYEBLOSSOM, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CLOSED_EYEBLOSSOM, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BUSH, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(FIREFLY_BUSH, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SHORT_DRY_GRASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(TALL_DRY_GRASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CACTUS_FLOWER, LegacyBlockSerializer::deserializeSimple);

        registerDeserializer(STONE, states -> {
            String type = states.getString(STONE_TYPE);
            switch (type) {
                default:
                case STONE_TYPE_STONE:
                    return BlockStone.TYPE_NORMAL;
                case STONE_TYPE_GRANITE:
                    return BlockStone.TYPE_GRANITE;
                case STONE_TYPE_GRANITE_SMOOTH:
                    return BlockStone.TYPE_POLISHED_GRANITE;
                case STONE_TYPE_DIORITE:
                    return BlockStone.TYPE_DIORITE;
                case STONE_TYPE_DIORITE_SMOOTH:
                    return BlockStone.TYPE_POLISHED_DIORITE;
                case STONE_TYPE_ANDESITE:
                    return BlockStone.TYPE_ANDESITE;
                case STONE_TYPE_ANDESITE_SMOOTH:
                    return BlockStone.TYPE_POLISHED_ANDESITE;
            }
        });

//        registerDeserializer(STONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GRANITE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(POLISHED_GRANITE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DIORITE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(POLISHED_DIORITE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ANDESITE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(POLISHED_ANDESITE, LegacyBlockSerializer::deserializeSimple);

        registerDeserializer(DIRT, states -> {
            String type = states.getString(DIRT_TYPE);
            switch (type) {
                default:
                case DIRT_TYPE_NORMAL:
                    return BlockDirt.TYPE_NORMAL_DIRT;
                case DIRT_TYPE_COARSE:
                    return BlockDirt.TYPE_COARSE_DIRT;
            }
        });

//        registerDeserializer(DIRT, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(COARSE_DIRT, LegacyBlockSerializer::deserializeSimple);

        registerDeserializer(PLANKS, LegacyBlockSerializer::deserializeWoodType);

//        registerDeserializer(OAK_PLANKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SPRUCE_PLANKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BIRCH_PLANKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(JUNGLE_PLANKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ACACIA_PLANKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DARK_OAK_PLANKS, LegacyBlockSerializer::deserializeSimple);

        registerDeserializer(SAPLING, states -> {
            int meta = deserializeSaplingType(states);
            if (states.getBoolean(AGE_BIT)) {
                meta |= 0b1000;
            }
            return meta;
        });

//        registerDeserializer(OAK_SAPLING, LegacyBlockSerializer::deserializeSapling);
        registerDeserializer(SPRUCE_SAPLING, LegacyBlockSerializer::deserializeSapling);
        registerDeserializer(BIRCH_SAPLING, LegacyBlockSerializer::deserializeSapling);
        registerDeserializer(JUNGLE_SAPLING, LegacyBlockSerializer::deserializeSapling);
        registerDeserializer(ACACIA_SAPLING, LegacyBlockSerializer::deserializeSapling);
        registerDeserializer(DARK_OAK_SAPLING, LegacyBlockSerializer::deserializeSapling);

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
            }
        });

//        registerDeserializer(SAND, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RED_SAND, LegacyBlockSerializer::deserializeSimple);
        /*
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
            }
            return meta;
        });
        */
        registerDeserializer(MANGROVE_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_MANGROVE_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_MANGROVE_WOOD, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(CRIMSON_STEM, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(WARPED_STEM, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_CRIMSON_STEM, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_WARPED_STEM, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(CRIMSON_HYPHAE, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(WARPED_HYPHAE, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_CRIMSON_HYPHAE, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_WARPED_HYPHAE, LegacyBlockSerializer::deserializePillarAxis);

        registerDeserializer(OAK_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(SPRUCE_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(BIRCH_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(JUNGLE_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(ACACIA_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(DARK_OAK_LOG, LegacyBlockSerializer::deserializePillarAxis);

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
            }
            return meta;
        });
        registerDeserializer(AZALEA_LEAVES, LegacyBlockSerializer::deserializeLeaves);
        registerDeserializer(AZALEA_LEAVES_FLOWERED, LegacyBlockSerializer::deserializeLeaves);
        registerDeserializer(MANGROVE_LEAVES, LegacyBlockSerializer::deserializeLeaves);
        registerDeserializer(CHERRY_LEAVES, LegacyBlockSerializer::deserializeLeaves);
        registerDeserializer(PALE_OAK_LEAVES, LegacyBlockSerializer::deserializeLeaves);

//        registerDeserializer(OAK_LEAVES, LegacyBlockSerializer::deserializeLeaves);
        registerDeserializer(SPRUCE_LEAVES, LegacyBlockSerializer::deserializeLeaves);
        registerDeserializer(BIRCH_LEAVES, LegacyBlockSerializer::deserializeLeaves);
        registerDeserializer(JUNGLE_LEAVES, LegacyBlockSerializer::deserializeLeaves);
//        registerDeserializer(ACACIA_LEAVES, LegacyBlockSerializer::deserializeLeaves);
        registerDeserializer(DARK_OAK_LEAVES, LegacyBlockSerializer::deserializeLeaves);

        registerDeserializer(SPONGE, states -> {
            String type = states.getString(SPONGE_TYPE);
            switch (type) {
                default:
                case SPONGE_TYPE_DRY:
                    return BlockSponge.DRY;
                case SPONGE_TYPE_WET:
                    return BlockSponge.WET;
            }
        });

//        registerDeserializer(SPONGE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WET_SPONGE, LegacyBlockSerializer::deserializeSimple);

        registerDeserializer(DISPENSER, LegacyBlockSerializer::deserializeDispenser);
        registerDeserializer(DROPPER, LegacyBlockSerializer::deserializeDispenser);

        registerDeserializer(SANDSTONE, LegacyBlockSerializer::deserializeSandstone);
        registerDeserializer(RED_SANDSTONE, LegacyBlockSerializer::deserializeSandstone);

//        registerDeserializer(SANDSTONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CHISELED_SANDSTONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CUT_SANDSTONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SMOOTH_SANDSTONE, LegacyBlockSerializer::deserializeSimple);

//        registerDeserializer(RED_SANDSTONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CHISELED_RED_SANDSTONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CUT_RED_SANDSTONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SMOOTH_RED_SANDSTONE, LegacyBlockSerializer::deserializeSimple);

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

        registerDeserializer(SHORT_GRASS, states -> {
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
            }
        });

//        registerDeserializer(SHORT_GRASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(FERN, LegacyBlockSerializer::deserializeSimple);

        //registerDeserializer(WOOL, LegacyBlockSerializer::deserializeColor);
        //registerDeserializer(CARPET, LegacyBlockSerializer::deserializeColor);
        registerDeserializer(STAINED_GLASS, LegacyBlockSerializer::deserializeColor);
        registerDeserializer(HARD_STAINED_GLASS, LegacyBlockSerializer::deserializeColor);
        registerDeserializer(STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeColor);
        registerDeserializer(HARD_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeColor);
        //registerDeserializer(CONCRETE, LegacyBlockSerializer::deserializeColor);
        registerDeserializer(CONCRETE_POWDER, LegacyBlockSerializer::deserializeColor);
        registerDeserializer(STAINED_HARDENED_CLAY, LegacyBlockSerializer::deserializeColor);
        //registerDeserializer(SHULKER_BOX, LegacyBlockSerializer::deserializeColor);

        registerDeserializer(WHITE_WOOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_GRAY_WOOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GRAY_WOOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLACK_WOOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BROWN_WOOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RED_WOOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ORANGE_WOOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(YELLOW_WOOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIME_WOOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GREEN_WOOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CYAN_WOOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLUE_WOOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLUE_WOOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PURPLE_WOOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MAGENTA_WOOL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PINK_WOOL, LegacyBlockSerializer::deserializeSimple);

        registerDeserializer(WHITE_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_GRAY_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GRAY_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLACK_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BROWN_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RED_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ORANGE_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(YELLOW_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIME_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GREEN_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CYAN_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLUE_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLUE_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PURPLE_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MAGENTA_CARPET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PINK_CARPET, LegacyBlockSerializer::deserializeSimple);

//        registerDeserializer(WHITE_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_GRAY_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GRAY_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLACK_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BROWN_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RED_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ORANGE_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(YELLOW_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIME_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GREEN_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CYAN_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLUE_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLUE_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PURPLE_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MAGENTA_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PINK_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);

//        registerDeserializer(HARD_WHITE_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_LIGHT_GRAY_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_GRAY_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_BLACK_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_BROWN_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_RED_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_ORANGE_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_YELLOW_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_LIME_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_GREEN_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_CYAN_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_LIGHT_BLUE_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_BLUE_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_PURPLE_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_MAGENTA_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_PINK_STAINED_GLASS, LegacyBlockSerializer::deserializeSimple);

//        registerDeserializer(WHITE_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_GRAY_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GRAY_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLACK_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BROWN_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RED_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ORANGE_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(YELLOW_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIME_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GREEN_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CYAN_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLUE_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLUE_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PURPLE_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MAGENTA_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PINK_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);

//        registerDeserializer(HARD_WHITE_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_LIGHT_GRAY_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_GRAY_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_BLACK_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_BROWN_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_RED_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_ORANGE_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_YELLOW_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_LIME_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_GREEN_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_CYAN_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_LIGHT_BLUE_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_BLUE_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_PURPLE_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_MAGENTA_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HARD_PINK_STAINED_GLASS_PANE, LegacyBlockSerializer::deserializeSimple);

        registerDeserializer(WHITE_CONCRETE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_GRAY_CONCRETE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GRAY_CONCRETE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLACK_CONCRETE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BROWN_CONCRETE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RED_CONCRETE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ORANGE_CONCRETE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(YELLOW_CONCRETE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIME_CONCRETE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GREEN_CONCRETE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CYAN_CONCRETE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLUE_CONCRETE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLUE_CONCRETE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PURPLE_CONCRETE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MAGENTA_CONCRETE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PINK_CONCRETE, LegacyBlockSerializer::deserializeSimple);

//        registerDeserializer(WHITE_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_GRAY_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GRAY_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLACK_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BROWN_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RED_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ORANGE_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(YELLOW_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIME_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GREEN_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CYAN_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLUE_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLUE_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PURPLE_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MAGENTA_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PINK_CONCRETE_POWDER, LegacyBlockSerializer::deserializeSimple);

//        registerDeserializer(WHITE_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_GRAY_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GRAY_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLACK_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BROWN_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RED_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ORANGE_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(YELLOW_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIME_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GREEN_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CYAN_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLUE_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLUE_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PURPLE_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MAGENTA_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PINK_TERRACOTTA, LegacyBlockSerializer::deserializeSimple);

        registerDeserializer(WHITE_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_GRAY_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GRAY_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLACK_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BROWN_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RED_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ORANGE_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(YELLOW_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIME_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(GREEN_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CYAN_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLUE_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLUE_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PURPLE_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MAGENTA_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PINK_SHULKER_BOX, LegacyBlockSerializer::deserializeSimple);

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
            }
        });

//        registerDeserializer(POPPY, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BLUE_ORCHID, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ALLIUM, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(AZURE_BLUET, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(RED_TULIP, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ORANGE_TULIP, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(WHITE_TULIP, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PINK_TULIP, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(OXEYE_DAISY, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CORNFLOWER, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LILY_OF_THE_VALLEY, LegacyBlockSerializer::deserializeSimple);

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
        registerDeserializer(CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(DOUBLE_CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(EXPOSED_CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(EXPOSED_DOUBLE_CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(WEATHERED_CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(WEATHERED_DOUBLE_CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(OXIDIZED_CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(OXIDIZED_DOUBLE_CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(WAXED_CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(WAXED_DOUBLE_CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(WAXED_EXPOSED_CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(WAXED_EXPOSED_DOUBLE_CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(WAXED_WEATHERED_CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(WAXED_WEATHERED_DOUBLE_CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(WAXED_OXIDIZED_CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(WAXED_OXIDIZED_DOUBLE_CUT_COPPER_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(COBBLED_DEEPSLATE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(COBBLED_DEEPSLATE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(POLISHED_DEEPSLATE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(POLISHED_DEEPSLATE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(DEEPSLATE_TILE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(DEEPSLATE_TILE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(DEEPSLATE_BRICK_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(DEEPSLATE_BRICK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(MUD_BRICK_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(MUD_BRICK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(MANGROVE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(MANGROVE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(BAMBOO_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(BAMBOO_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(BAMBOO_MOSAIC_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(BAMBOO_MOSAIC_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(CHERRY_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(CHERRY_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(TUFF_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(TUFF_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(POLISHED_TUFF_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(POLISHED_TUFF_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(TUFF_BRICK_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(TUFF_BRICK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(PALE_OAK_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(PALE_OAK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(RESIN_BRICK_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(RESIN_BRICK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);

//        registerDeserializer(OAK_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(SPRUCE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(BIRCH_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(JUNGLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(ACACIA_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(DARK_OAK_SLAB, LegacyBlockSerializer::deserializeSlab);

//        registerDeserializer(OAK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(SPRUCE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(BIRCH_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(JUNGLE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(ACACIA_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(DARK_OAK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);

//        registerDeserializer(SMOOTH_STONE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(SANDSTONE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(PETRIFIED_OAK_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(COBBLESTONE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(BRICK_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(STONE_BRICK_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(QUARTZ_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(NETHER_BRICK_SLAB, LegacyBlockSerializer::deserializeSlab);

//        registerDeserializer(SMOOTH_STONE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(SANDSTONE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(PETRIFIED_OAK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(COBBLESTONE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(BRICK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(STONE_BRICK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(QUARTZ_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(NETHER_BRICK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);

//        registerDeserializer(RED_SANDSTONE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(PURPUR_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(PRISMARINE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(DARK_PRISMARINE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(PRISMARINE_BRICK_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(MOSSY_COBBLESTONE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(SMOOTH_SANDSTONE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(RED_NETHER_BRICK_SLAB, LegacyBlockSerializer::deserializeSlab);

//        registerDeserializer(RED_SANDSTONE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(PURPUR_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(PRISMARINE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(DARK_PRISMARINE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(PRISMARINE_BRICK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(MOSSY_COBBLESTONE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(SMOOTH_SANDSTONE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(RED_NETHER_BRICK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);

//        registerDeserializer(END_STONE_BRICK_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(SMOOTH_RED_SANDSTONE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(POLISHED_ANDESITE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(ANDESITE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(DIORITE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(POLISHED_DIORITE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(GRANITE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(POLISHED_GRANITE_SLAB, LegacyBlockSerializer::deserializeSlab);

//        registerDeserializer(END_STONE_BRICK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(SMOOTH_RED_SANDSTONE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(POLISHED_ANDESITE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(ANDESITE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(DIORITE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(POLISHED_DIORITE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(GRANITE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(POLISHED_GRANITE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);

//        registerDeserializer(MOSSY_STONE_BRICK_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(SMOOTH_QUARTZ_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(NORMAL_STONE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(CUT_SANDSTONE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(CUT_RED_SANDSTONE_SLAB, LegacyBlockSerializer::deserializeSlab);

//        registerDeserializer(MOSSY_STONE_BRICK_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(SMOOTH_QUARTZ_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(NORMAL_STONE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(CUT_SANDSTONE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);
        registerDeserializer(CUT_RED_SANDSTONE_DOUBLE_SLAB, LegacyBlockSerializer::deserializeSlab);

        registerDeserializer(TNT, states -> {
            int meta = deserializeExplode(states);
            if (states.getBoolean(ALLOW_UNDERWATER_BIT)) {
                meta |= 0b10;
            }
            return meta;
        });

//        registerDeserializer(TNT, LegacyBlockSerializer::deserializeExplode);
        registerDeserializer(UNDERWATER_TNT, LegacyBlockSerializer::deserializeExplode);

        registerDeserializer(TORCH, LegacyBlockSerializer::deserializeTorch);
        registerDeserializer(REDSTONE_TORCH, LegacyBlockSerializer::deserializeTorch);
        registerDeserializer(UNLIT_REDSTONE_TORCH, LegacyBlockSerializer::deserializeTorch);
        registerDeserializer(UNDERWATER_TORCH, LegacyBlockSerializer::deserializeTorch);
        registerDeserializer(SOUL_TORCH, LegacyBlockSerializer::deserializeTorch);
        registerDeserializer(COLORED_TORCH_RG, LegacyBlockSerializer::deserializeColoredTorch);
        registerDeserializer(COLORED_TORCH_BP, LegacyBlockSerializer::deserializeColoredTorch);

//        registerDeserializer(COLORED_TORCH_RED, LegacyBlockSerializer::deserializeTorch);
        registerDeserializer(COLORED_TORCH_GREEN, LegacyBlockSerializer::deserializeTorch);

//        registerDeserializer(COLORED_TORCH_BLUE, LegacyBlockSerializer::deserializeTorch);
        registerDeserializer(COLORED_TORCH_PURPLE, LegacyBlockSerializer::deserializeTorch);

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
        registerDeserializer(BAMBOO_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(BAMBOO_MOSAIC_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(CHERRY_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(TUFF_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(POLISHED_TUFF_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(TUFF_BRICK_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(PALE_OAK_STAIRS, LegacyBlockSerializer::deserializeStairs);
        registerDeserializer(RESIN_BRICK_STAIRS, LegacyBlockSerializer::deserializeStairs);

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
        registerDeserializer(BAMBOO_STANDING_SIGN, LegacyBlockSerializer::deserializeStandingSign);
        registerDeserializer(CHERRY_STANDING_SIGN, LegacyBlockSerializer::deserializeStandingSign);
        registerDeserializer(PALE_OAK_STANDING_SIGN, LegacyBlockSerializer::deserializeStandingSign);
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
        registerDeserializer(BAMBOO_WALL_SIGN, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(CHERRY_WALL_SIGN, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(PALE_OAK_WALL_SIGN, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(WALL_BANNER, LegacyBlockSerializer::deserializeFacingDirection);

        registerDeserializer(OAK_HANGING_SIGN, LegacyBlockSerializer::deserializeHangingSign);
        registerDeserializer(SPRUCE_HANGING_SIGN, LegacyBlockSerializer::deserializeHangingSign);
        registerDeserializer(BIRCH_HANGING_SIGN, LegacyBlockSerializer::deserializeHangingSign);
        registerDeserializer(JUNGLE_HANGING_SIGN, LegacyBlockSerializer::deserializeHangingSign);
        registerDeserializer(ACACIA_HANGING_SIGN, LegacyBlockSerializer::deserializeHangingSign);
        registerDeserializer(DARK_OAK_HANGING_SIGN, LegacyBlockSerializer::deserializeHangingSign);
        registerDeserializer(CRIMSON_HANGING_SIGN, LegacyBlockSerializer::deserializeHangingSign);
        registerDeserializer(WARPED_HANGING_SIGN, LegacyBlockSerializer::deserializeHangingSign);
        registerDeserializer(MANGROVE_HANGING_SIGN, LegacyBlockSerializer::deserializeHangingSign);
        registerDeserializer(BAMBOO_HANGING_SIGN, LegacyBlockSerializer::deserializeHangingSign);
        registerDeserializer(CHERRY_HANGING_SIGN, LegacyBlockSerializer::deserializeHangingSign);
        registerDeserializer(PALE_OAK_HANGING_SIGN, LegacyBlockSerializer::deserializeHangingSign);

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
        registerDeserializer(BAMBOO_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(CHERRY_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(COPPER_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(EXPOSED_COPPER_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(WEATHERED_COPPER_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(OXIDIZED_COPPER_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(WAXED_COPPER_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(WAXED_EXPOSED_COPPER_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(WAXED_WEATHERED_COPPER_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(WAXED_OXIDIZED_COPPER_DOOR, LegacyBlockSerializer::deserializeDoor);
        registerDeserializer(PALE_OAK_DOOR, LegacyBlockSerializer::deserializeDoor);

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
        registerDeserializer(BAMBOO_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);
        registerDeserializer(CHERRY_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);
        registerDeserializer(PALE_OAK_PRESSURE_PLATE, LegacyBlockSerializer::deserializeRedstoneSignal);

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
        registerDeserializer(BAMBOO_BUTTON, LegacyBlockSerializer::deserializeButton);
        registerDeserializer(CHERRY_BUTTON, LegacyBlockSerializer::deserializeButton);
        registerDeserializer(PALE_OAK_BUTTON, LegacyBlockSerializer::deserializeButton);

        registerDeserializer(SNOW_LAYER, states -> {
            int meta = states.getInt(HEIGHT) & 0b111;
            if (states.getBoolean(COVERED_BIT)) {
                meta |= 0b1000;
            }
            return meta;
        });

        registerDeserializer(CACTUS, states -> states.getInt(AGE) & 0b1111);

        registerDeserializer(BLOCK_REEDS, states -> states.getInt(AGE) & 0b1111);

        //registerDeserializer(FENCE, LegacyBlockSerializer::deserializeWoodType);

        registerDeserializer(OAK_FENCE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(SPRUCE_FENCE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BIRCH_FENCE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(JUNGLE_FENCE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(ACACIA_FENCE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DARK_OAK_FENCE, LegacyBlockSerializer::deserializeSimple);

        registerDeserializer(PUMPKIN, LegacyBlockSerializer::deserializeCardinalDirection);
        registerDeserializer(LIT_PUMPKIN, LegacyBlockSerializer::deserializeCardinalDirection);
        registerDeserializer(CARVED_PUMPKIN, LegacyBlockSerializer::deserializeCardinalDirection);

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
        registerDeserializer(BAMBOO_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(CHERRY_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(COPPER_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(EXPOSED_COPPER_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(WEATHERED_COPPER_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(OXIDIZED_COPPER_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(WAXED_COPPER_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(WAXED_EXPOSED_COPPER_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(WAXED_WEATHERED_COPPER_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(WAXED_OXIDIZED_COPPER_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);
        registerDeserializer(PALE_OAK_TRAPDOOR, LegacyBlockSerializer::deserializeTrapdoor);

        registerDeserializer(MONSTER_EGG, states -> {
            String type = states.getString(MONSTER_EGG_STONE_TYPE);
            switch (type) {
                default:
                case MONSTER_EGG_STONE_TYPE_STONE:
                    return BlockMonsterEgg.TYPE_STONE;
                case MONSTER_EGG_STONE_TYPE_COBBLESTONE:
                    return BlockMonsterEgg.TYPE_COBBLESTONE;
                case MONSTER_EGG_STONE_TYPE_STONE_BRICK:
                    return BlockMonsterEgg.TYPE_STONE_BRICK;
                case MONSTER_EGG_STONE_TYPE_MOSSY_STONE_BRICK:
                    return BlockMonsterEgg.TYPE_MOSSY_BRICK;
                case MONSTER_EGG_STONE_TYPE_CRACKED_STONE_BRICK:
                    return BlockMonsterEgg.TYPE_CRACKED_BRICK;
                case MONSTER_EGG_STONE_TYPE_CHISELED_STONE_BRICK:
                    return BlockMonsterEgg.TYPE_CHISELED_BRICK;
            }
        });

//        registerDeserializer(INFESTED_STONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(INFESTED_COBBLESTONE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(INFESTED_STONE_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(INFESTED_MOSSY_STONE_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(INFESTED_CRACKED_STONE_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(INFESTED_CHISELED_STONE_BRICKS, LegacyBlockSerializer::deserializeSimple);

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
            }
        });

//        registerDeserializer(STONE_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(MOSSY_STONE_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CRACKED_STONE_BRICKS, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(CHISELED_STONE_BRICKS, LegacyBlockSerializer::deserializeSimple);

        registerDeserializer(BROWN_MUSHROOM_BLOCK, LegacyBlockSerializer::deserializeHugeMushroom);
        registerDeserializer(RED_MUSHROOM_BLOCK, LegacyBlockSerializer::deserializeHugeMushroom);
        registerDeserializer(MUSHROOM_STEM, LegacyBlockSerializer::deserializeHugeMushroom);

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
        registerDeserializer(BAMBOO_FENCE_GATE, LegacyBlockSerializer::deserializeFenceGate);
        registerDeserializer(CHERRY_FENCE_GATE, LegacyBlockSerializer::deserializeFenceGate);
        registerDeserializer(PALE_OAK_FENCE_GATE, LegacyBlockSerializer::deserializeFenceGate);

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
        //registerDeserializer(LAVA_CAULDRON, LegacyBlockSerializer::deserializeCauldron); //TODO: 1.20.0 remove me

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
                    meta |= BlockWallCobblestone.TYPE_COBBLESTONE;
                    break;
                case WALL_BLOCK_TYPE_MOSSY_COBBLESTONE:
                    meta |= BlockWallCobblestone.TYPE_MOSSY_COBBLESTONE;
                    break;
                case WALL_BLOCK_TYPE_GRANITE:
                    meta |= BlockWallCobblestone.TYPE_GRANITE;
                    break;
                case WALL_BLOCK_TYPE_DIORITE:
                    meta |= BlockWallCobblestone.TYPE_DIORITE;
                    break;
                case WALL_BLOCK_TYPE_ANDESITE:
                    meta |= BlockWallCobblestone.TYPE_ANDESITE;
                    break;
                case WALL_BLOCK_TYPE_SANDSTONE:
                    meta |= BlockWallCobblestone.TYPE_SANDSTONE;
                    break;
                case WALL_BLOCK_TYPE_BRICK:
                    meta |= BlockWallCobblestone.TYPE_BRICK;
                    break;
                case WALL_BLOCK_TYPE_STONE_BRICK:
                    meta |= BlockWallCobblestone.TYPE_STONE_BRICK;
                    break;
                case WALL_BLOCK_TYPE_MOSSY_STONE_BRICK:
                    meta |= BlockWallCobblestone.TYPE_MOSSY_STONE_BRICK;
                    break;
                case WALL_BLOCK_TYPE_NETHER_BRICK:
                    meta |= BlockWallCobblestone.TYPE_NETHER_BRICK;
                    break;
                case WALL_BLOCK_TYPE_END_BRICK:
                    meta |= BlockWallCobblestone.TYPE_END_BRICK;
                    break;
                case WALL_BLOCK_TYPE_PRISMARINE:
                    meta |= BlockWallCobblestone.TYPE_PRISMARINE;
                    break;
                case WALL_BLOCK_TYPE_RED_SANDSTONE:
                    meta |= BlockWallCobblestone.TYPE_RED_SANDSTONE;
                    break;
                case WALL_BLOCK_TYPE_RED_NETHER_BRICK:
                    meta |= BlockWallCobblestone.TYPE_RED_NETHER_BRICK;
                    break;
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
        registerDeserializer(TUFF_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(POLISHED_TUFF_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(TUFF_BRICK_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(RESIN_BRICK_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(BORDER_BLOCK, LegacyBlockSerializer::deserializeWall);

//        registerDeserializer(COBBLESTONE_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(MOSSY_COBBLESTONE_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(GRANITE_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(DIORITE_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(ANDESITE_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(SANDSTONE_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(BRICK_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(STONE_BRICK_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(MOSSY_STONE_BRICK_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(NETHER_BRICK_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(END_STONE_BRICK_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(PRISMARINE_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(RED_SANDSTONE_WALL, LegacyBlockSerializer::deserializeWall);
        registerDeserializer(RED_NETHER_BRICK_WALL, LegacyBlockSerializer::deserializeWall);

        registerDeserializer(BLOCK_FLOWER_POT, states -> states.getBoolean(UPDATE_BIT) ? 0b1 : 0);

        registerDeserializer(BLOCK_SKULL, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(WITHER_SKELETON_SKULL, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(ZOMBIE_HEAD, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(PLAYER_HEAD, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(CREEPER_HEAD, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(DRAGON_HEAD, LegacyBlockSerializer::deserializeFacingDirection);
        registerDeserializer(PIGLIN_HEAD, LegacyBlockSerializer::deserializeFacingDirection);

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
            }
            return meta;
        });

//        registerDeserializer(ANVIL, LegacyBlockSerializer::deserializeDirection);
        registerDeserializer(CHIPPED_ANVIL, LegacyBlockSerializer::deserializeDirection);
        registerDeserializer(DAMAGED_ANVIL, LegacyBlockSerializer::deserializeDirection);
        registerDeserializer(DEPRECATED_ANVIL, LegacyBlockSerializer::deserializeDirection);

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

//        registerDeserializer(QUARTZ_BLOCK, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(CHISELED_QUARTZ_BLOCK, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(QUARTZ_PILLAR, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(SMOOTH_QUARTZ, LegacyBlockSerializer::deserializePillarAxis);

//        registerDeserializer(PURPUR_BLOCK, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(DEPRECATED_PURPUR_BLOCK_1, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(PURPUR_PILLAR, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(DEPRECATED_PURPUR_BLOCK_2, LegacyBlockSerializer::deserializePillarAxis);

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
            }
        });

//        registerDeserializer(PRISMARINE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DARK_PRISMARINE, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(PRISMARINE_BRICKS, LegacyBlockSerializer::deserializeSimple);

        registerDeserializer(DOUBLE_PLANT, states -> {
            int meta = states.getBoolean(UPPER_BLOCK_BIT) ? 0b1000 : 0;
            String type = states.getString(DOUBLE_PLANT_TYPE);
            switch (type) {
                default:
                case DOUBLE_PLANT_TYPE_SUNFLOWER:
                    meta |= BlockDoublePlant.TYPE_SUNFLOWER;
                    break;
                case DOUBLE_PLANT_TYPE_SYRINGA:
                    meta |= BlockDoublePlant.TYPE_LILAC;
                    break;
                case DOUBLE_PLANT_TYPE_GRASS:
                    meta |= BlockDoublePlant.TYPE_TALL_GRASS;
                    break;
                case DOUBLE_PLANT_TYPE_FERN:
                    meta |= BlockDoublePlant.TYPE_LARGE_FERN;
                    break;
                case DOUBLE_PLANT_TYPE_ROSE:
                    meta |= BlockDoublePlant.TYPE_ROSE_BUSH;
                    break;
                case DOUBLE_PLANT_TYPE_PAEONIA:
                    meta |= BlockDoublePlant.TYPE_PEONY;
                    break;
            }
            return meta;
        });

//        registerDeserializer(SUNFLOWER, states -> states.getBoolean(UPPER_BLOCK_BIT) ? 0b1 : 0);
        registerDeserializer(LILAC, states -> states.getBoolean(UPPER_BLOCK_BIT) ? 0b1 : 0);
        registerDeserializer(TALL_GRASS, states -> states.getBoolean(UPPER_BLOCK_BIT) ? 0b1 : 0);
        registerDeserializer(LARGE_FERN, states -> states.getBoolean(UPPER_BLOCK_BIT) ? 0b1 : 0);
        registerDeserializer(ROSE_BUSH, states -> states.getBoolean(UPPER_BLOCK_BIT) ? 0b1 : 0);
        registerDeserializer(PEONY, states -> states.getBoolean(UPPER_BLOCK_BIT) ? 0b1 : 0);

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
                    //TODO: 1.21.30 remove me
                    return BlockStructureVoid.TYPE_AIR;
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
                    meta |= BlockChemistryTable.TYPE_COMPOUND_CREATOR;
                    break;
                case CHEMISTRY_TABLE_TYPE_MATERIAL_REDUCER:
                    meta |= BlockChemistryTable.TYPE_MATERIAL_REDUCER;
                    break;
                case CHEMISTRY_TABLE_TYPE_ELEMENT_CONSTRUCTOR:
                    meta |= BlockChemistryTable.TYPE_ELEMENT_CONSTRUCTOR;
                    break;
                case CHEMISTRY_TABLE_TYPE_LAB_TABLE:
                    meta |= BlockChemistryTable.TYPE_LAB_TABLE;
                    break;
            }
            return meta;
        });

//        registerDeserializer(COMPOUND_CREATOR, LegacyBlockSerializer::deserializeDirection);
        registerDeserializer(MATERIAL_REDUCER, LegacyBlockSerializer::deserializeDirection);
        registerDeserializer(ELEMENT_CONSTRUCTOR, LegacyBlockSerializer::deserializeDirection);
        registerDeserializer(LAB_TABLE, LegacyBlockSerializer::deserializeDirection);

        registerDeserializer(OBSERVER, states -> {
            int meta = deserializeNewFacingDirection(states);
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
            }
        });

        //registerDeserializer(CORAL, LegacyBlockSerializer::deserializeCoral);
        registerDeserializer(CORAL_BLOCK, LegacyBlockSerializer::deserializeCoral);

        registerDeserializer(TUBE_CORAL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BRAIN_CORAL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BUBBLE_CORAL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(FIRE_CORAL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HORN_CORAL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEAD_TUBE_CORAL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEAD_BRAIN_CORAL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEAD_BUBBLE_CORAL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEAD_FIRE_CORAL, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEAD_HORN_CORAL, LegacyBlockSerializer::deserializeSimple);

//        registerDeserializer(TUBE_CORAL_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BRAIN_CORAL_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(BUBBLE_CORAL_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(FIRE_CORAL_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(HORN_CORAL_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEAD_TUBE_CORAL_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEAD_BRAIN_CORAL_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEAD_BUBBLE_CORAL_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEAD_FIRE_CORAL_BLOCK, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(DEAD_HORN_CORAL_BLOCK, LegacyBlockSerializer::deserializeSimple);

        registerDeserializer(CORAL_FAN, LegacyBlockSerializer::deserializeCoralFan);
        registerDeserializer(CORAL_FAN_DEAD, LegacyBlockSerializer::deserializeCoralFan);

//        registerDeserializer(TUBE_CORAL_FAN, LegacyBlockSerializer::deserializeCoralFanDirection);
        registerDeserializer(BRAIN_CORAL_FAN, LegacyBlockSerializer::deserializeCoralFanDirection);
        registerDeserializer(BUBBLE_CORAL_FAN, LegacyBlockSerializer::deserializeCoralFanDirection);
        registerDeserializer(FIRE_CORAL_FAN, LegacyBlockSerializer::deserializeCoralFanDirection);
        registerDeserializer(HORN_CORAL_FAN, LegacyBlockSerializer::deserializeCoralFanDirection);

//        registerDeserializer(DEAD_TUBE_CORAL_FAN, LegacyBlockSerializer::deserializeCoralFanDirection);
        registerDeserializer(DEAD_BRAIN_CORAL_FAN, LegacyBlockSerializer::deserializeCoralFanDirection);
        registerDeserializer(DEAD_BUBBLE_CORAL_FAN, LegacyBlockSerializer::deserializeCoralFanDirection);
        registerDeserializer(DEAD_FIRE_CORAL_FAN, LegacyBlockSerializer::deserializeCoralFanDirection);
        registerDeserializer(DEAD_HORN_CORAL_FAN, LegacyBlockSerializer::deserializeCoralFanDirection);

        registerDeserializer(CORAL_FAN_HANG, LegacyBlockSerializer::deserializeHangCoralFan);
        registerDeserializer(CORAL_FAN_HANG2, LegacyBlockSerializer::deserializeHangCoralFan);
        registerDeserializer(CORAL_FAN_HANG3, LegacyBlockSerializer::deserializeHangCoralFan);

//        registerDeserializer(TUBE_CORAL_WALL_FAN, LegacyBlockSerializer::deserializeCoralDirection);
        registerDeserializer(BRAIN_CORAL_WALL_FAN, LegacyBlockSerializer::deserializeCoralDirection);
        registerDeserializer(DEAD_TUBE_CORAL_WALL_FAN, LegacyBlockSerializer::deserializeCoralDirection);
        registerDeserializer(DEAD_BRAIN_CORAL_WALL_FAN, LegacyBlockSerializer::deserializeCoralDirection);

//        registerDeserializer(BUBBLE_CORAL_WALL_FAN, LegacyBlockSerializer::deserializeCoralDirection);
        registerDeserializer(FIRE_CORAL_WALL_FAN, LegacyBlockSerializer::deserializeCoralDirection);
        registerDeserializer(DEAD_BUBBLE_CORAL_WALL_FAN, LegacyBlockSerializer::deserializeCoralDirection);
        registerDeserializer(DEAD_FIRE_CORAL_WALL_FAN, LegacyBlockSerializer::deserializeCoralDirection);

//        registerDeserializer(HORN_CORAL_WALL_FAN, LegacyBlockSerializer::deserializeCoralDirection);
        registerDeserializer(DEAD_HORN_CORAL_WALL_FAN, LegacyBlockSerializer::deserializeCoralDirection);

        registerDeserializer(BLOCK_KELP, states -> states.getInt(KELP_AGE) & 0b11111);

        registerDeserializer(WEEPING_VINES, states -> states.getInt(WEEPING_VINES_AGE) & 0b11111);
        registerDeserializer(TWISTING_VINES, states -> states.getInt(TWISTING_VINES_AGE) & 0b11111);

        registerDeserializer(SEA_PICKLE, states -> {
            int meta = states.getInt(CLUSTER_COUNT) & 0b11;
            if (states.getBoolean(DEAD_BIT)) {
                meta |= 0b100;
            }
            return meta;
        });

        registerDeserializer(TURTLE_EGG, states -> {
            int meta = deserializeCrackedState(states) << 2;
            String count = states.getString(TURTLE_EGG_COUNT);
            switch (count) {
                default:
                case TURTLE_EGG_COUNT_ONE_EGG:
                    break;
                case TURTLE_EGG_COUNT_TWO_EGG:
                    meta |= 1;
                    break;
                case TURTLE_EGG_COUNT_THREE_EGG:
                    meta |= 2;
                    break;
                case TURTLE_EGG_COUNT_FOUR_EGG:
                    meta |= 3;
                    break;
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

//        registerDeserializer(OAK_WOOD, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(SPRUCE_WOOD, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(BIRCH_WOOD, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(JUNGLE_WOOD, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(ACACIA_WOOD, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(DARK_OAK_WOOD, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_OAK_WOOD, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_SPRUCE_WOOD, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_BIRCH_WOOD, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_JUNGLE_WOOD, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_ACACIA_WOOD, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_DARK_OAK_WOOD, LegacyBlockSerializer::deserializePillarAxis);

        registerDeserializer(COMPOSTER, states -> states.getInt(COMPOSTER_FILL_LEVEL) & 0b1111);

        registerDeserializer(LIGHT_BLOCK, states -> states.getInt(BLOCK_LIGHT_LEVEL) & 0b1111);

//        registerDeserializer(LIGHT_BLOCK_0, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLOCK_1, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLOCK_2, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLOCK_3, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLOCK_4, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLOCK_5, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLOCK_6, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLOCK_7, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLOCK_8, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLOCK_9, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLOCK_10, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLOCK_11, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLOCK_12, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLOCK_13, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLOCK_14, LegacyBlockSerializer::deserializeSimple);
        registerDeserializer(LIGHT_BLOCK_15, LegacyBlockSerializer::deserializeSimple);

        registerDeserializer(BEEHIVE, LegacyBlockSerializer::deserializeBeehive);
        registerDeserializer(BEE_NEST, LegacyBlockSerializer::deserializeBeehive);

        registerDeserializer(BASALT, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(POLISHED_BASALT, LegacyBlockSerializer::deserializePillarAxis);

        registerDeserializer(RESPAWN_ANCHOR, states -> states.getInt(RESPAWN_ANCHOR_CHARGE) & 0b111);

        registerDeserializer(BLOCK_CHAIN, LegacyBlockSerializer::deserializePillarAxis);

        registerDeserializer(POINTED_DRIPSTONE, states -> {
            int meta = states.getBoolean(HANGING) ? 0b1000 : 0;
            String thickness = states.getString(DRIPSTONE_THICKNESS);
            switch (thickness) {
                default:
                case DRIPSTONE_THICKNESS_TIP:
                    break;
                case DRIPSTONE_THICKNESS_FRUSTUM:
                    meta |= 0b1;
                    break;
                case DRIPSTONE_THICKNESS_MIDDLE:
                    meta |= 0b10;
                    break;
                case DRIPSTONE_THICKNESS_BASE:
                    meta |= 0b11;
                    break;
                case DRIPSTONE_THICKNESS_MERGE:
                    meta |= 0b100;
                    break;
            }
            return meta;
        });

        registerDeserializer(LIGHTNING_ROD, LegacyBlockSerializer::deserializeFacingDirection);

        registerDeserializer(CAVE_VINES, LegacyBlockSerializer::deserializeGrowingPlant);
        registerDeserializer(CAVE_VINES_BODY_WITH_BERRIES, LegacyBlockSerializer::deserializeGrowingPlant);
        registerDeserializer(CAVE_VINES_HEAD_WITH_BERRIES, LegacyBlockSerializer::deserializeGrowingPlant);

        registerDeserializer(BIG_DRIPLEAF, states -> {
            int meta = deserializeDirection(states) << 3;
            if (states.getBoolean(BIG_DRIPLEAF_HEAD)) {
                meta |= 0b100;
            }
            String tilt = states.getString(BIG_DRIPLEAF_TILT);
            switch (tilt) {
                default:
                case BIG_DRIPLEAF_TILT_NONE:
                    break;
                case BIG_DRIPLEAF_TILT_UNSTABLE:
                    meta |= 0b1;
                    break;
                case BIG_DRIPLEAF_TILT_PARTIAL_TILT:
                    meta |= 0b10;
                    break;
                case BIG_DRIPLEAF_TILT_FULL_TILT:
                    meta |= 0b11;
                    break;
            }
            return meta;
        });

        registerDeserializer(SMALL_DRIPLEAF_BLOCK, states -> {
            int meta = deserializeDirection(states) << 1;
            if (states.getBoolean(UPPER_BLOCK_BIT)) {
                meta |= 0b1;
            }
            return meta;
        });

        if (V1_20_30.isAvailable()) {
            registerDeserializer(AMETHYST_CLUSTER, LegacyBlockSerializer::deserializeBlockFace);
            registerDeserializer(LARGE_AMETHYST_BUD, LegacyBlockSerializer::deserializeBlockFace);
            registerDeserializer(MEDIUM_AMETHYST_BUD, LegacyBlockSerializer::deserializeBlockFace);
            registerDeserializer(SMALL_AMETHYST_BUD, LegacyBlockSerializer::deserializeBlockFace);
        } else {
            registerDeserializer(AMETHYST_CLUSTER, LegacyBlockSerializer::deserializeFacingDirection);
            registerDeserializer(LARGE_AMETHYST_BUD, LegacyBlockSerializer::deserializeFacingDirection);
            registerDeserializer(MEDIUM_AMETHYST_BUD, LegacyBlockSerializer::deserializeFacingDirection);
            registerDeserializer(SMALL_AMETHYST_BUD, LegacyBlockSerializer::deserializeFacingDirection);
        }

        registerDeserializer(DEEPSLATE, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(INFESTED_DEEPSLATE, LegacyBlockSerializer::deserializePillarAxis);

        registerDeserializer(GLOW_LICHEN, LegacyBlockSerializer::deserializeMultiFaceDirection);
        registerDeserializer(SCULK_VEIN, LegacyBlockSerializer::deserializeMultiFaceDirection);
        registerDeserializer(RESIN_CLUMP, LegacyBlockSerializer::deserializeMultiFaceDirection);

        registerDeserializer(CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(WHITE_CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(ORANGE_CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(MAGENTA_CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(LIGHT_BLUE_CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(YELLOW_CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(LIME_CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(PINK_CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(GRAY_CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(LIGHT_GRAY_CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(CYAN_CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(PURPLE_CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(BLUE_CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(BROWN_CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(GREEN_CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(RED_CANDLE, LegacyBlockSerializer::deserializeCandle);
        registerDeserializer(BLACK_CANDLE, LegacyBlockSerializer::deserializeCandle);

        registerDeserializer(CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(WHITE_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(ORANGE_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(MAGENTA_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(LIGHT_BLUE_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(YELLOW_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(LIME_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(PINK_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(GRAY_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(LIGHT_GRAY_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(CYAN_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(PURPLE_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(BLUE_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(BROWN_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(GREEN_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(RED_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);
        registerDeserializer(BLACK_CANDLE_CAKE, LegacyBlockSerializer::deserializeLit);

        registerDeserializer(SCULK_SENSOR, states -> states.getInt(SCULK_SENSOR_PHASE) & 0b11);
        registerDeserializer(CALIBRATED_SCULK_SENSOR, states -> (states.getInt(SCULK_SENSOR_PHASE) & 0b11) | deserializeDirection(states) << 2);

        registerDeserializer(SCULK_CATALYST, states -> states.getBoolean(BLOOM) ? 0b1 : 0);

        registerDeserializer(SCULK_SHRIEKER, states -> {
            int meta = states.getBoolean(ACTIVE) ? 0b1 : 0;
            if (states.getBoolean(CAN_SUMMON)) {
                meta |= 0b10;
            }
            return meta;
        });

        registerDeserializer(PEARLESCENT_FROGLIGHT, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(VERDANT_FROGLIGHT, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(OCHRE_FROGLIGHT, LegacyBlockSerializer::deserializePillarAxis);

        registerDeserializer(MANGROVE_PROPAGULE, states -> {
            int meta = states.getInt(PROPAGULE_STAGE) & 0b111;
            if (states.getBoolean(HANGING)) {
                meta |= 0b1000;
            }
            return meta;
        });

        registerDeserializer(MUDDY_MANGROVE_ROOTS, LegacyBlockSerializer::deserializePillarAxis);

        registerDeserializer(BAMBOO_BLOCK, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_BAMBOO_BLOCK, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(CHERRY_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_CHERRY_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_CHERRY_WOOD, LegacyBlockSerializer::deserializePillarAxis);

        registerDeserializer(MANGROVE_WOOD, LegacyBlockSerializer::deserializeWood);
//        registerDeserializer(MANGROVE_WOOD, LegacyBlockSerializer::deserializePillarAxis); //TODO: 1.21.40
        registerDeserializer(CHERRY_WOOD, LegacyBlockSerializer::deserializeWood);
//        registerDeserializer(CHERRY_WOOD, LegacyBlockSerializer::deserializePillarAxis); //TODO: 1.21.40

        registerDeserializer(CHERRY_SAPLING, LegacyBlockSerializer::deserializeSapling);

        registerDeserializer(PINK_PETALS, states -> (states.getInt(GROWTH) & 0b111) | deserializeDirection(states) << 3);
        registerDeserializer(WILDFLOWERS, states -> (states.getInt(GROWTH) & 0b111) | deserializeCardinalDirection(states) << 3);
        registerDeserializer(LEAF_LITTER, states -> (states.getInt(GROWTH) & 0b111) | deserializeCardinalDirection(states) << 3);

        registerDeserializer(TORCHFLOWER_CROP, states -> states.getInt(GROWTH) & 0b111);

        registerDeserializer(PITCHER_CROP, states -> {
            int meta = (states.getInt(GROWTH) & 0b111) << 1;
            if (states.getBoolean(UPPER_BLOCK_BIT)) {
                meta |= 0b1;
            }
            return meta;
        });

        registerDeserializer(PITCHER_PLANT, states -> states.getBoolean(UPPER_BLOCK_BIT) ? 0b1 : 0);

        registerDeserializer(DECORATED_POT, LegacyBlockSerializer::deserializeDirection);

        registerDeserializer(SNIFFER_EGG, LegacyBlockSerializer::deserializeCrackedState);

        registerDeserializer(SUSPICIOUS_SAND, LegacyBlockSerializer::deserializeBrushable);
        registerDeserializer(SUSPICIOUS_GRAVEL, LegacyBlockSerializer::deserializeBrushable);

        registerDeserializer(CHISELED_BOOKSHELF, states -> deserializeDirection(states) | (states.getInt(BOOKS_STORED) & 0b111111) << 2);

        registerDeserializer(COPPER_BULB, LegacyBlockSerializer::deserializeBulb);
        registerDeserializer(EXPOSED_COPPER_BULB, LegacyBlockSerializer::deserializeBulb);
        registerDeserializer(WEATHERED_COPPER_BULB, LegacyBlockSerializer::deserializeBulb);
        registerDeserializer(OXIDIZED_COPPER_BULB, LegacyBlockSerializer::deserializeBulb);
        registerDeserializer(WAXED_COPPER_BULB, LegacyBlockSerializer::deserializeBulb);
        registerDeserializer(WAXED_EXPOSED_COPPER_BULB, LegacyBlockSerializer::deserializeBulb);
        registerDeserializer(WAXED_WEATHERED_COPPER_BULB, LegacyBlockSerializer::deserializeBulb);
        registerDeserializer(WAXED_OXIDIZED_COPPER_BULB, LegacyBlockSerializer::deserializeBulb);

        registerDeserializer(CRAFTER, states -> {
            int meta;
            String orientation = states.getString(ORIENTATION);
            switch (orientation) {
                default:
                case ORIENTATION_DOWN_EAST:
                    meta = 0;
                    break;
                case ORIENTATION_DOWN_NORTH:
                    meta = 1;
                    break;
                case ORIENTATION_DOWN_SOUTH:
                    meta = 2;
                    break;
                case ORIENTATION_DOWN_WEST:
                    meta = 3;
                    break;
                case ORIENTATION_UP_EAST:
                    meta = 4;
                    break;
                case ORIENTATION_UP_NORTH:
                    meta = 5;
                    break;
                case ORIENTATION_UP_SOUTH:
                    meta = 6;
                    break;
                case ORIENTATION_UP_WEST:
                    meta = 7;
                    break;
                case ORIENTATION_WEST_UP:
                    meta = 8;
                    break;
                case ORIENTATION_EAST_UP:
                    meta = 9;
                    break;
                case ORIENTATION_NORTH_UP:
                    meta = 10;
                    break;
                case ORIENTATION_SOUTH_UP:
                    meta = 11;
                    break;
            }
            if (states.getBoolean(TRIGGERED_BIT)) {
                meta |= 0b10000;
            }
            if (states.getBoolean(CRAFTING)) {
                meta |= 0b100000;
            }
            return meta;
        });

        registerDeserializer(VAULT, states -> {
            int meta = deserializeCardinalDirection(states);
            String state = states.getString(VAULT_STATE);
            switch (state) {
                default:
                case VAULT_STATE_INACTIVE:
                    break;
                case VAULT_STATE_ACTIVE:
                    meta |= 0b100;
                    break;
                case VAULT_STATE_UNLOCKING:
                    meta |= 0b1000;
                    break;
                case VAULT_STATE_EJECTING:
                    meta |= 0b1100;
                    break;
            }
            if (states.getBoolean(OMINOUS)) {
                meta |= 0b10000;
            }
            return meta;
        });

        registerDeserializer(TRIAL_SPAWNER, states -> {
            int meta = states.getInt(TRIAL_SPAWNER_STATE) & 0b111;
            if (states.getBoolean(OMINOUS)) {
                meta |= 0b1000;
            }
            return meta;
        });

        registerDeserializer(PALE_OAK_LOG, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_PALE_OAK_LOG, LegacyBlockSerializer::deserializePillarAxis);

        registerDeserializer(PALE_OAK_WOOD, LegacyBlockSerializer::deserializePillarAxis);
        registerDeserializer(STRIPPED_PALE_OAK_WOOD, LegacyBlockSerializer::deserializePillarAxis);

        registerDeserializer(PALE_OAK_SAPLING, LegacyBlockSerializer::deserializeSapling);

        registerDeserializer(PALE_HANGING_MOSS, states -> states.getBoolean(TIP) ? 0b1 : 0);

        registerDeserializer(CREAKING_HEART, states -> {
            int meta = deserializePillarAxis(states);
            //TODO: 1.21.60 CREAKING_HEART_STATE
            if (states.getBoolean(ACTIVE)) {
                meta |= 0b100;
            }
            if (states.getBoolean(NATURAL)) {
                meta |= 0b1000;
            }
            return meta;
        });

        registerDeserializer(DRIED_GHAST, states -> deserializeCardinalDirection(states) | (states.getInt(REHYDRATION_LEVEL) & 0b11) << 2);

        registerDeserializer(CHALKBOARD, states -> states.getInt(DIRECTION) & 0b1111);

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

            @Override
            public void registerCustomBlock(String name, int id, IntFunction<CompoundTag> definitionSupplier) {
                BlockLegacy legacyBlock = BlockTypes.getBlockRegistry().getBlock(id);
                registerDeserializer(id, legacyBlock.getVariantCount() == 1 ? LegacyBlockSerializer::deserializeSimple : legacyBlock::deserialize);

                RuntimeBlockMapper.registerCustomBlock(name, id, definitionSupplier);
            }

            @Override
            public void rebuildPalette() {
                AdvancedGlobalBlockPalette.rebuildStaticPalettes();

                CraftingPacketManager.rebuildPacket();
                CreativeItemsPalette.cachePackets();
                BiomeDefinitions.rebuildPackets();
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
        }
    }

    private static int deserializeColoredTorch(CompoundTag states) {
        int meta = deserializeTorch(states);
        if (states.getBoolean(COLOR_BIT)) {
            meta |= 0b1000;
        }
        return meta;
    }

    private static int deserializeVerticalHalf(CompoundTag states) {
        String direction = states.getString(MINECRAFT_VERTICAL_HALF);
        switch (direction) {
            default:
            case MINECRAFT_VERTICAL_HALF_BOTTOM:
                return 0;
            case MINECRAFT_VERTICAL_HALF_TOP:
                return 1;
        }
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
                meta |= BlockSlabStone.TYPE_SMOOTH_STONE;
                break;
            case STONE_SLAB_TYPE_SANDSTONE:
                meta |= BlockSlabStone.TYPE_SANDSTONE;
                break;
            case STONE_SLAB_TYPE_WOOD:
                meta |= BlockSlabStone.TYPE_WOOD;
                break;
            case STONE_SLAB_TYPE_COBBLESTONE:
                meta |= BlockSlabStone.TYPE_COBBLESTONE;
                break;
            case STONE_SLAB_TYPE_BRICK:
                meta |= BlockSlabStone.TYPE_BRICK;
                break;
            case STONE_SLAB_TYPE_STONE_BRICK:
                meta |= BlockSlabStone.TYPE_STONE_BRICK;
                break;
            case STONE_SLAB_TYPE_QUARTZ:
                meta |= BlockSlabStone.TYPE_QUARTZ;
                break;
            case STONE_SLAB_TYPE_NETHER_BRICK:
                meta |= BlockSlabStone.TYPE_NETHER_BRICK;
                break;
        }
        return meta;
    }

    private static int deserializeStoneSlab2(CompoundTag states) {
        int meta = deserializeSlab(states) << 3;
        String type = states.getString(STONE_SLAB_TYPE_2);
        switch (type) {
            default:
            case STONE_SLAB_TYPE_2_RED_SANDSTONE:
                meta |= BlockSlabRedSandstone.TYPE_RED_SANDSTONE;
                break;
            case STONE_SLAB_TYPE_2_PURPUR:
                meta |= BlockSlabRedSandstone.TYPE_PURPUR;
                break;
            case STONE_SLAB_TYPE_2_PRISMARINE_ROUGH:
                meta |= BlockSlabRedSandstone.TYPE_PRISMARINE_ROUGH;
                break;
            case STONE_SLAB_TYPE_2_PRISMARINE_DARK:
                meta |= BlockSlabRedSandstone.TYPE_PRISMARINE_DARK;
                break;
            case STONE_SLAB_TYPE_2_PRISMARINE_BRICK:
                meta |= BlockSlabRedSandstone.TYPE_PRISMARINE_BRICK;
                break;
            case STONE_SLAB_TYPE_2_MOSSY_COBBLESTONE:
                meta |= BlockSlabRedSandstone.TYPE_MOSSY_COBBLESTONE;
                break;
            case STONE_SLAB_TYPE_2_SMOOTH_SANDSTONE:
                meta |= BlockSlabRedSandstone.TYPE_SMOOTH_SANDSTONE;
                break;
            case STONE_SLAB_TYPE_2_RED_NETHER_BRICK:
                meta |= BlockSlabRedSandstone.TYPE_RED_NETHER_BRICK;
                break;
        }
        return meta;
    }

    private static int deserializeStoneSlab3(CompoundTag states) {
        int meta = deserializeSlab(states) << 3;
        String type = states.getString(STONE_SLAB_TYPE_3);
        switch (type) {
            default:
            case STONE_SLAB_TYPE_3_END_STONE_BRICK:
                meta |= BlockSlabStone3.TYPE_END_STONE_BRICK;
                break;
            case STONE_SLAB_TYPE_3_SMOOTH_RED_SANDSTONE:
                meta |= BlockSlabStone3.TYPE_SMOOTH_RED_SANDSTONE;
                break;
            case STONE_SLAB_TYPE_3_POLISHED_ANDESITE:
                meta |= BlockSlabStone3.TYPE_POLISHED_ANDESITE;
                break;
            case STONE_SLAB_TYPE_3_ANDESITE:
                meta |= BlockSlabStone3.TYPE_ANDESITE;
                break;
            case STONE_SLAB_TYPE_3_DIORITE:
                meta |= BlockSlabStone3.TYPE_DIORITE;
                break;
            case STONE_SLAB_TYPE_3_POLISHED_DIORITE:
                meta |= BlockSlabStone3.TYPE_POLISHED_DIORITE;
                break;
            case STONE_SLAB_TYPE_3_GRANITE:
                meta |= BlockSlabStone3.TYPE_GRANITE;
                break;
            case STONE_SLAB_TYPE_3_POLISHED_GRANITE:
                meta |= BlockSlabStone3.TYPE_POLISHED_GRANITE;
                break;
        }
        return meta;
    }

    private static int deserializeStoneSlab4(CompoundTag states) {
        int meta = deserializeSlab(states) << 3;
        String type = states.getString(STONE_SLAB_TYPE_4);
        switch (type) {
            default:
            case STONE_SLAB_TYPE_4_MOSSY_STONE_BRICK:
                meta |= BlockSlabStone4.TYPE_MOSSY_STONE_BRICK;
                break;
            case STONE_SLAB_TYPE_4_SMOOTH_QUARTZ:
                meta |= BlockSlabStone4.TYPE_SMOOTH_QUARTZ;
                break;
            case STONE_SLAB_TYPE_4_STONE:
                meta |= BlockSlabStone4.TYPE_STONE;
                break;
            case STONE_SLAB_TYPE_4_CUT_SANDSTONE:
                meta |= BlockSlabStone4.TYPE_CUT_SANDSTONE;
                break;
            case STONE_SLAB_TYPE_4_CUT_RED_SANDSTONE:
                meta |= BlockSlabStone4.TYPE_CUT_RED_SANDSTONE;
                break;
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

    private static int deserializeHangingSign(CompoundTag states) {
        int meta = deserializeFacingDirection(states) | deserializeStandingSign(states) << 3;
        if (states.getBoolean(HANGING)) {
            meta |= 0b1_0000_000;
        }
        if (states.getBoolean(ATTACHED_BIT)) {
            meta |= 0b10_0000_000;
        }
        return meta;
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

    private static int deserializeCandle(CompoundTag states) {
        return (states.getInt(CANDLES) & 0b11) | deserializeLit(states) << 2;
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
        }
        return meta;
    }

    private static int deserializeSimplePillar(CompoundTag states) {
        // discard "deprecated" state
        return /*(states.getInt(DEPRECATED) & 0b11) |*/ deserializePillarAxis(states) << 2;
    }

    private static int deserializeCardinalDirection(CompoundTag states) {
        String direction = states.getString(MINECRAFT_CARDINAL_DIRECTION);
        switch (direction) {
            default:
            case MINECRAFT_CARDINAL_DIRECTION_SOUTH:
                return 0;
            case MINECRAFT_CARDINAL_DIRECTION_WEST:
                return 1;
            case MINECRAFT_CARDINAL_DIRECTION_NORTH:
                return 2;
            case MINECRAFT_CARDINAL_DIRECTION_EAST:
                return 3;
        }
    }

    private static int deserializeDirection(CompoundTag states) {
        return states.getInt(DIRECTION) & 0b11;
    }

    private static int deserializeBlockFace(CompoundTag states) {
        String direction = states.getString(MINECRAFT_BLOCK_FACE);
        switch (direction) {
            default:
            case MINECRAFT_BLOCK_FACE_DOWN:
                return 0;
            case MINECRAFT_BLOCK_FACE_UP:
                return 1;
            case MINECRAFT_BLOCK_FACE_NORTH:
                return 2;
            case MINECRAFT_BLOCK_FACE_SOUTH:
                return 3;
            case MINECRAFT_BLOCK_FACE_WEST:
                return 4;
            case MINECRAFT_BLOCK_FACE_EAST:
                return 5;
        }
    }

    private static int deserializeNewFacingDirection(CompoundTag states) {
        String direction = states.getString(MINECRAFT_FACING_DIRECTION);
        switch (direction) {
            default:
            case MINECRAFT_FACING_DIRECTION_DOWN:
                return 0;
            case MINECRAFT_FACING_DIRECTION_UP:
                return 1;
            case MINECRAFT_FACING_DIRECTION_NORTH:
                return 2;
            case MINECRAFT_FACING_DIRECTION_SOUTH:
                return 3;
            case MINECRAFT_FACING_DIRECTION_WEST:
                return 4;
            case MINECRAFT_FACING_DIRECTION_EAST:
                return 5;
        }
    }

    private static int deserializeFacingDirection(CompoundTag states) {
        return states.getInt(FACING_DIRECTION) & 0b111;
    }

    private static int deserializeMultiFaceDirection(CompoundTag states) {
        return states.getInt(MULTI_FACE_DIRECTION_BITS) & 0b111111;
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
        }
    }

    private static int deserializeCoral(CompoundTag states) {
        int meta = deserializeCoralColor(states);
        if (states.getBoolean(DEAD_BIT)) {
            meta |= 0b1000;
        }
        return meta;
    }

    private static int deserializeCoralFanDirection(CompoundTag states) {
        return states.getInt(CORAL_FAN_DIRECTION) & 0b11;
    }

    private static int deserializeCoralFan(CompoundTag states) {
        return deserializeCoralColor(states) | deserializeCoralFanDirection(states) << 3;
    }

    private static int deserializeCoralDirection(CompoundTag states) {
        return states.getInt(CORAL_DIRECTION) & 0b11;
    }

    private static int deserializeHangCoralFan(CompoundTag states) {
        int meta = deserializeCoralDirection(states) << 2;
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
        }
    }

    private static int deserializeGrowingPlant(CompoundTag states) {
        return states.getInt(GROWING_PLANT_AGE) & 0b11111;
    }

    private static int deserializeLit(CompoundTag states) {
        return states.getBoolean(LIT) ? 0b1 : 0;
    }

    private static int deserializeWood(CompoundTag states) {
        int meta = deserializePillarAxis(states) << 1;
        if (states.getBoolean(STRIPPED_BIT)) {
            meta |= 0b1;
        }
        return meta;
    }

    private static int deserializeSapling(CompoundTag states) {
        return states.getBoolean(AGE_BIT) ? 0b1 : 0;
    }

    private static int deserializeCrackedState(CompoundTag states) {
        String state = states.getString(CRACKED_STATE);
        switch (state) {
            default:
            case CRACKED_STATE_NO_CRACKS:
                return 0b00;
            case CRACKED_STATE_CRACKED:
                return 0b01;
            case CRACKED_STATE_MAX_CRACKED:
                return 0b10;
        }
    }

    private static int deserializeBrushable(CompoundTag states) {
        int meta = (states.getInt(BRUSHED_PROGRESS) & 0b11) << 1;
        if (states.getBoolean(HANGING)) {
            meta |= 0b1;
        }
        return meta;
    }

    private static int deserializeBulb(CompoundTag states) {
        int meta = deserializeLit(states);
        if (states.getBoolean(POWERED_BIT)) {
            meta |= 0b10;
        }
        return meta;
    }

    private static int deserializeExplode(CompoundTag states) {
        return states.getBoolean(EXPLODE_BIT) ? 0b1 : 0;
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

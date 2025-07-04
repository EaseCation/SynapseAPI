package org.itxtech.synapseapi.multiprotocol.utils.block;

import cn.nukkit.GameVersion;
import cn.nukkit.block.state.BlockInstance;
import cn.nukkit.block.state.BlockLegacy;
import cn.nukkit.nbt.tag.CompoundTag;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockPalette.BlockData;
import org.itxtech.synapseapi.multiprotocol.utils.block.state.BlockTypes;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;

import static cn.nukkit.GameVersion.*;

@Log4j2
public final class RuntimeBlockMapper {
    public static final Map<AbstractProtocol, BlockPalette[]> PALETTES = new EnumMap<>(AbstractProtocol.class);

    private static RuntimeBlockSerializer RUNTIME_BLOCK_SERIALIZER;

    public static void initialize() {
        log.debug("Loading runtime block mapper...");

        VanillaBlockUpgrader.initialize();
/*
        // 实现这个功能时使用1.17及更早版本客户端的玩家基本没了, 所以之前的数据没必要做了 -- 10/23/2022
        BlockPalette palette11740 = BlockPalette.fromNBT("block_state_list_11740.dat");
        BlockPalette palette118N = BlockPalette.fromJson("block_state_list_118_netease.json");
        BlockPalette palette11810 = BlockPalette.fromNBT("block_state_list_11810.dat");
        BlockPalette palette11830 = BlockPalette.fromNBT("block_state_list_11830.nbt");
//        BlockPalette palette11830N = BlockPalette.fromJson("block_state_list_11830_netease.json");
        BlockPalette palette11830N = BlockPalette.fromNBT("block_state_list_11830.nbt").toNetease();
        BlockPalette palette119 = BlockPalette.fromNBT("block_state_list_119.nbt");
        BlockPalette palette11920 = BlockPalette.fromNBT("block_state_list_11920.nbt");
        BlockPalette palette11950 = BlockPalette.fromNBT("block_state_list_11950.nbt");
        BlockPalette palette11960 = BlockPalette.fromNBT("block_state_list_11960.nbt");
        BlockPalette palette11970 = BlockPalette.fromNBT("block_state_list_11970.nbt");
        BlockPalette palette11980 = BlockPalette.fromNBT("block_state_list_11980.nbt");
        BlockPalette palette120 = BlockPalette.fromNBT("block_state_list_120.nbt");
*/
//        BlockPalette palette12010 = BlockPalette.fromNBT("block_state_list_12010.nbt");
        BlockPalette palette12010 = BlockPalette.fromJson("block_state_list_12010.json");
        BlockPalette palette12010N = BlockPalette.fromJson("block_state_list_12010.json").toNetease();
        BlockPalette palette12030 = BlockPalette.fromNBT("block_state_list_12030.nbt");
        BlockPalette palette12040 = BlockPalette.fromNBT("block_state_list_12040.nbt");
        BlockPalette palette12050 = BlockPalette.fromNBT("block_state_list_12050.nbt");
//        BlockPalette palette12050 = BlockPalette.fromVanillaNBT("block_state_list_12050_raw.nbt", true);
        BlockPalette palette12050N = BlockPalette.fromNBT("block_state_list_12050.nbt").toNetease();
        BlockPalette palette12060 = BlockPalette.fromNBT("block_state_list_12060.nbt");
        BlockPalette palette12070 = BlockPalette.fromNBT("block_state_list_12070.nbt");
        BlockPalette palette12080 = BlockPalette.fromNBT("block_state_list_12080.nbt");
        BlockPalette palette121 = BlockPalette.fromNBT("block_state_list_121.nbt");
        BlockPalette palette121N = BlockPalette.fromNBT("block_state_list_121.nbt").toNetease();
//        BlockPalette palette12120 = BlockPalette.fromNBT("block_state_list_12120.nbt");
        BlockPalette palette12120 = BlockTypes.V1_21_20.getBlockRegistry().createBlockPalette();
        BlockPalette palette12130 = BlockTypes.V1_21_30.getBlockRegistry().createBlockPalette();
        BlockPalette palette12140 = BlockTypes.V1_21_40.getBlockRegistry().createBlockPalette();
        BlockPalette palette12150 = BlockTypes.V1_21_50.getBlockRegistry().createBlockPalette();
        BlockPalette palette12160 = BlockTypes.V1_21_60.getBlockRegistry().createBlockPalette();
        BlockPalette palette12170 = BlockTypes.V1_21_70.getBlockRegistry().createBlockPalette();
        BlockPalette palette12180 = BlockTypes.V1_21_80.getBlockRegistry().createBlockPalette();

/*
        PALETTES.put(AbstractProtocol.PROTOCOL_117_40, new BlockPalette[]{palette11740, palette118N});
        PALETTES.put(AbstractProtocol.PROTOCOL_118, new BlockPalette[]{palette11740, palette118N});
        PALETTES.put(AbstractProtocol.PROTOCOL_118_10, new BlockPalette[]{palette11810, palette11810});
        PALETTES.put(AbstractProtocol.PROTOCOL_118_30, new BlockPalette[]{palette11830, palette11830N});
        PALETTES.put(AbstractProtocol.PROTOCOL_118_30_NE, new BlockPalette[]{palette11830, palette11830N});
        PALETTES.put(AbstractProtocol.PROTOCOL_119, new BlockPalette[]{palette119, palette119});
        PALETTES.put(AbstractProtocol.PROTOCOL_119_10, new BlockPalette[]{palette119, palette119});
        PALETTES.put(AbstractProtocol.PROTOCOL_119_20, new BlockPalette[]{palette11920, palette11920});
        PALETTES.put(AbstractProtocol.PROTOCOL_119_21, new BlockPalette[]{palette11920, palette11920});
        PALETTES.put(AbstractProtocol.PROTOCOL_119_30, new BlockPalette[]{palette11920, palette11920});
        PALETTES.put(AbstractProtocol.PROTOCOL_119_40, new BlockPalette[]{palette11920, palette11920});
        PALETTES.put(AbstractProtocol.PROTOCOL_119_50, new BlockPalette[]{palette11950, palette11950});
        PALETTES.put(AbstractProtocol.PROTOCOL_119_60, new BlockPalette[]{palette11960, palette11960});
        PALETTES.put(AbstractProtocol.PROTOCOL_119_63, new BlockPalette[]{palette11960, palette11960});
        PALETTES.put(AbstractProtocol.PROTOCOL_119_70, new BlockPalette[]{palette11970, palette11970});
        PALETTES.put(AbstractProtocol.PROTOCOL_119_80, new BlockPalette[]{palette11980, palette11980});
        PALETTES.put(AbstractProtocol.PROTOCOL_120, new BlockPalette[]{palette120, palette120});
*/
        PALETTES.put(AbstractProtocol.PROTOCOL_120_10, new BlockPalette[]{palette12010, palette12010N});
        PALETTES.put(AbstractProtocol.PROTOCOL_120_30, new BlockPalette[]{palette12030, palette12030});
        PALETTES.put(AbstractProtocol.PROTOCOL_120_40, new BlockPalette[]{palette12040, palette12040});
        PALETTES.put(AbstractProtocol.PROTOCOL_120_50, new BlockPalette[]{palette12050, palette12050N});
        PALETTES.put(AbstractProtocol.PROTOCOL_120_60, new BlockPalette[]{palette12060, palette12060});
        PALETTES.put(AbstractProtocol.PROTOCOL_120_70, new BlockPalette[]{palette12070, palette12070});
        PALETTES.put(AbstractProtocol.PROTOCOL_120_80, new BlockPalette[]{palette12080, palette12080});
        PALETTES.put(AbstractProtocol.PROTOCOL_121, new BlockPalette[]{palette121, palette121N});
        PALETTES.put(AbstractProtocol.PROTOCOL_121_2, new BlockPalette[]{palette121, palette121N});
        PALETTES.put(AbstractProtocol.PROTOCOL_121_20, new BlockPalette[]{palette12120, palette12120});
        PALETTES.put(AbstractProtocol.PROTOCOL_121_30, new BlockPalette[]{palette12130, palette12130});
        PALETTES.put(AbstractProtocol.PROTOCOL_121_40, new BlockPalette[]{palette12140, palette12140});
        PALETTES.put(AbstractProtocol.PROTOCOL_121_50, new BlockPalette[]{palette12150, palette12150});
        PALETTES.put(AbstractProtocol.PROTOCOL_121_60, new BlockPalette[]{palette12160, palette12160});
        PALETTES.put(AbstractProtocol.PROTOCOL_121_70, new BlockPalette[]{palette12170, palette12170});
        PALETTES.put(AbstractProtocol.PROTOCOL_121_80, new BlockPalette[]{palette12180, palette12180});
        PALETTES.put(AbstractProtocol.PROTOCOL_121_90, new BlockPalette[]{palette12180, palette12180});
        PALETTES.put(AbstractProtocol.PROTOCOL_121_93, new BlockPalette[]{palette12180, palette12180});

        GameVersion baseVersion = V1_20_10;
        BlockPalette basePalette = palette12010;
        log.debug("Base runtime block palette version: {}", baseVersion);

        CompletableFuture.allOf(
/*
                CompletableFuture.runAsync(() -> map(V1_17_40, palette11740, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_18_0, palette118N, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_18_10, palette11810, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_18_30, palette11830, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_18_30, palette11830N, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_19_0, palette119, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_19_20, palette11920, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_19_50, palette11950, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_19_60, palette11960, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_19_70, palette11970, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_19_80, palette11980, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_20_0, palette120, basePalette)),
*/
//                CompletableFuture.runAsync(() -> map(V1_20_10, palette12010, basePalette)),
//                CompletableFuture.runAsync(() -> map(V1_20_10, palette12010N, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_20_30, palette12030, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_20_40, palette12040, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_20_50, palette12050, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_20_50, palette12050N, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_20_60, palette12060, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_20_70, palette12070, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_20_80, palette12080, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_21_0, palette121, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_21_0, palette121N, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_21_20, palette12120, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_21_30, palette12130, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_21_40, palette12140, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_21_50, palette12150, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_21_60, palette12160, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_21_70, palette12170, basePalette)),
                CompletableFuture.runAsync(() -> map(V1_21_80, palette12180, basePalette))
        ).join();

        RUNTIME_BLOCK_SERIALIZER = new RuntimeBlockSerializer(basePalette);
        LegacyBlockSerializer.setSerializer(RUNTIME_BLOCK_SERIALIZER::serialize);

        BlockUtil.initialize();
    }

    private static void map(GameVersion version, BlockPalette target, BlockPalette base) {
        log.debug("Mapping runtime block palette data to {}", version);
        List<BlockUpgradeSchema> schemas = VanillaBlockUpgrader.getSchemas(ver -> ver.ordinal() >= V1_17_40.ordinal() && ver.ordinal() <= version.ordinal());
        base.palette.forEach(block -> map(version, block, target, schemas));
    }

    @Nullable
    private static BlockData map(GameVersion version, BlockData block, BlockPalette target, List<BlockUpgradeSchema> schemas) {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", block.name);
        tag.putCompound("states", block.states.clone());
        tag.putInt("version", VanillaBlockUpgrader.getCurrentVersion());

        VanillaBlockUpgrader.upgrade(tag, schemas);

        String name = tag.getString("name");
        CompoundTag states = tag.getCompound("states");

        Optional<BlockData> match = target.palette.stream()
                .filter(data -> name.equals(data.name) && states.equalsTags(data.states))
                .findFirst();

        if (match.isPresent()) {
            BlockData data = match.get();

            if (data.id != -1) {
                log.trace("{} | already mapped block {}: {}", version, block, data);
                return data;
            }

            data.id = block.id;
            data.val = block.val;
            return data;
        }

        log.debug("{} | No corresponding block found: {}", version, block);
        return null;
    }

    static void registerCustomBlock(String name, int id, IntFunction<CompoundTag> definitionSupplier) {
        BlockLegacy legacyBlock = cn.nukkit.block.state.BlockTypes.getBlockRegistry().getBlock(id);

        int variantCount = legacyBlock.getVariantCount();
        CompoundTag[] variants = new CompoundTag[variantCount];
        for (int meta = 0; meta < variantCount; meta++) {
            BlockInstance block = legacyBlock.getBlock(meta);
            if (block == null) {
                block = legacyBlock.getDefaultState();
            }
            variants[meta] = new CompoundTag()
                    .putString("name", name)
                    .putCompound("states", block.getStatesTag())
                    .putInt("version", VanillaBlockUpgrader.getCurrentVersion());
        }
        RUNTIME_BLOCK_SERIALIZER.idMetaToTag[id] = variants;

        Set<BlockPalette> finished = new ObjectOpenHashSet<>();
        for (Entry<AbstractProtocol, BlockPalette[]> entry : PALETTES.entrySet()) {
            AbstractProtocol protocol = entry.getKey();
            BlockPalette[] pair = entry.getValue();
            for (int i = 0; i < 2; i++) {
                BlockPalette palette = pair[i];
                if (!finished.add(palette)) {
                    continue;
                }

                for (int meta = 0; meta < variantCount; meta++) {
                    BlockInstance block = legacyBlock.getBlock(meta);
                    if (block == null) {
                        continue;
                    }
                    palette.palette.add(new BlockData(name, variants[meta].getCompound("states"), id, meta));
                }

                palette.properties.add(new BlockProperty(name, definitionSupplier.apply(protocol.getProtocolStart())));
            }
        }
    }

    private RuntimeBlockMapper() {
        throw new IllegalStateException();
    }
}

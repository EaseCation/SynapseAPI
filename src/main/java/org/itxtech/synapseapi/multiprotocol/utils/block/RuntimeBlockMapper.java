package org.itxtech.synapseapi.multiprotocol.utils.block;

import cn.nukkit.GameVersion;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static cn.nukkit.GameVersion.*;

@Log4j2
public final class RuntimeBlockMapper {
    public static final Map<AbstractProtocol, BlockPalette[]> PALETTES = new EnumMap<>(AbstractProtocol.class);

    private static RuntimeBlockSerializer RUNTIME_BLOCK_SERIALIZER;

    public static void initialize() {
        log.debug("Loading runtime block mapper...");

        VanillaBlockUpgrader.initialize();

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
        BlockPalette palette12010 = BlockPalette.fromNBT("block_state_list_12010.nbt");
        BlockPalette palette12010N = BlockPalette.fromNBT("block_state_list_12010.nbt").toNetease();
        BlockPalette palette12030 = BlockPalette.fromNBT("block_state_list_12030.nbt");
        BlockPalette palette12040 = BlockPalette.fromNBT("block_state_list_12040.nbt");
        BlockPalette palette12050 = BlockPalette.fromNBT("block_state_list_12050.nbt");
//        BlockPalette palette12050 = BlockPalette.fromVanillaNBT("block_state_list_12050_raw.nbt", true);
        BlockPalette palette12060 = BlockPalette.fromNBT("block_state_list_12060.nbt");
        BlockPalette palette12070 = BlockPalette.fromNBT("block_state_list_12070.nbt");
        BlockPalette palette12080 = BlockPalette.fromNBT("block_state_list_12080.nbt");
        BlockPalette palette121 = BlockPalette.fromNBT("block_state_list_121.nbt");
//        BlockPalette palette12120 = BlockPalette.fromNBT("block_state_list_12120.nbt");
        BlockPalette palette12120 = BlockTypes.V1_21_20.getBlockRegistry().createBlockPalette();

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
        PALETTES.put(AbstractProtocol.PROTOCOL_120_10, new BlockPalette[]{palette12010, palette12010N});
        PALETTES.put(AbstractProtocol.PROTOCOL_120_30, new BlockPalette[]{palette12030, palette12030});
        PALETTES.put(AbstractProtocol.PROTOCOL_120_40, new BlockPalette[]{palette12040, palette12040});
        PALETTES.put(AbstractProtocol.PROTOCOL_120_50, new BlockPalette[]{palette12050, palette12050});
        PALETTES.put(AbstractProtocol.PROTOCOL_120_60, new BlockPalette[]{palette12060, palette12060});
        PALETTES.put(AbstractProtocol.PROTOCOL_120_70, new BlockPalette[]{palette12070, palette12070});
        PALETTES.put(AbstractProtocol.PROTOCOL_120_80, new BlockPalette[]{palette12080, palette12080});
        PALETTES.put(AbstractProtocol.PROTOCOL_121, new BlockPalette[]{palette121, palette121});
        PALETTES.put(AbstractProtocol.PROTOCOL_121_2, new BlockPalette[]{palette121, palette121});
        PALETTES.put(AbstractProtocol.PROTOCOL_121_20, new BlockPalette[]{palette12120, palette12120});

        GameVersion baseVersion = V1_18_0;
        BlockPalette basePalette = palette118N;
        log.debug("Base runtime block palette version: {}", baseVersion);

        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> map(V1_17_40, basePalette, palette11740, ver -> ver.ordinal() >= V1_17_40.ordinal() && ver.ordinal() <= V1_18_0.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_18_10, basePalette, palette11810, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_18_10.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_18_30, basePalette, palette11830, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_18_30.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_18_30, basePalette, palette11830N, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_18_30.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_19_0, basePalette, palette119, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_19_0.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_19_20, basePalette, palette11920, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_19_20.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_19_50, basePalette, palette11950, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_19_50.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_19_60, basePalette, palette11960, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_19_60.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_19_70, basePalette, palette11970, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_19_70.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_19_80, basePalette, palette11980, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_19_80.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_20_0, basePalette, palette120, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_20_0.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_20_10, basePalette, palette12010, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_20_10.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_20_10, basePalette, palette12010N, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_20_10.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_20_30, basePalette, palette12030, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_20_30.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_20_40, basePalette, palette12040, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_20_40.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_20_50, basePalette, palette12050, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_20_50.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_20_60, basePalette, palette12060, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_20_60.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_20_70, basePalette, palette12070, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_20_70.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_20_80, basePalette, palette12080, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_20_80.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_21_0, basePalette, palette121, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_21_0.ordinal())),
                CompletableFuture.runAsync(() -> map(V1_21_20, basePalette, palette12120, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_21_20.ordinal()))
        ).join();

        RUNTIME_BLOCK_SERIALIZER = new RuntimeBlockSerializer(basePalette);
        LegacyBlockSerializer.setSerializer(RUNTIME_BLOCK_SERIALIZER::serialize);

        BlockUtil.initialize();
    }

    private static void map(GameVersion version, BlockPalette base, BlockPalette target, Predicate<GameVersion> upgradeFilter) {
        log.debug("Mapping runtime block palette data to {}", version);
        List<BlockUpgradeSchema> schemas = VanillaBlockUpgrader.getSchemas(upgradeFilter);
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

    static void registerCustomBlock(String name, int id, CompoundTag definition) {
        RUNTIME_BLOCK_SERIALIZER.idMetaToTag[id] = new CompoundTag[]{
                new CompoundTag()
                        .putString("name", name)
                        .putCompound("states", new CompoundTag("states"))
                        .putInt("version", VanillaBlockUpgrader.getCurrentVersion())
        };

        Set<BlockPalette> finished = new ObjectOpenHashSet<>();
        for (BlockPalette[] pair : PALETTES.values()) {
            for (int i = 0; i < 2; i++) {
                BlockPalette palette = pair[i];
                if (!finished.add(palette)) {
                    continue;
                }

                palette.palette.add(new BlockData(name, id));
                palette.properties.add(new BlockProperty(name, definition));
            }
        }
    }

    private RuntimeBlockMapper() {
        throw new IllegalStateException();
    }
}

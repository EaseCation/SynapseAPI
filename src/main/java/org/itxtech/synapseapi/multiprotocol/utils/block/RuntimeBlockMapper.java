package org.itxtech.synapseapi.multiprotocol.utils.block;

import cn.nukkit.GameVersion;
import cn.nukkit.block.Block;
import cn.nukkit.nbt.tag.CompoundTag;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockPalette.BlockData;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static cn.nukkit.GameVersion.*;

@Log4j2
public final class RuntimeBlockMapper {
    public static final Map<AbstractProtocol, BlockPalette[]> PALETTES = new EnumMap<>(AbstractProtocol.class);

    static {
        log.debug("Loading runtime block mapper...");

        VanillaBlockUpgrader.initialize();

        // 实现这个功能时使用1.17及更早版本客户端的玩家基本没了, 所以之前的数据没必要做了 -- 10/23/2022
        BlockPalette palette11740 = BlockPalette.fromNBT("block_state_list_11740.dat");
        BlockPalette palette118N = BlockPalette.fromJson("block_state_list_118_netease.json");
        BlockPalette palette11810 = BlockPalette.fromNBT("block_state_list_11810.dat");
        BlockPalette palette11830 = BlockPalette.fromNBT("block_state_list_11830.nbt");
        BlockPalette palette119 = BlockPalette.fromNBT("block_state_list_119.nbt");
        BlockPalette palette11920 = BlockPalette.fromNBT("block_state_list_11920.nbt");

        PALETTES.put(AbstractProtocol.PROTOCOL_117_40, new BlockPalette[]{palette11740, palette118N});
        PALETTES.put(AbstractProtocol.PROTOCOL_118, new BlockPalette[]{palette11740, palette118N});
        PALETTES.put(AbstractProtocol.PROTOCOL_118_10, new BlockPalette[]{palette11810, palette11810});
        PALETTES.put(AbstractProtocol.PROTOCOL_118_30, new BlockPalette[]{palette11830, palette11830});
        PALETTES.put(AbstractProtocol.PROTOCOL_119, new BlockPalette[]{palette119, palette119});
        PALETTES.put(AbstractProtocol.PROTOCOL_119_10, new BlockPalette[]{palette119, palette119});
        PALETTES.put(AbstractProtocol.PROTOCOL_119_20, new BlockPalette[]{palette11920, palette11920});
        PALETTES.put(AbstractProtocol.PROTOCOL_119_21, new BlockPalette[]{palette11920, palette11920});
        PALETTES.put(AbstractProtocol.PROTOCOL_119_30, new BlockPalette[]{palette11920, palette11920});
        PALETTES.put(AbstractProtocol.PROTOCOL_119_40, new BlockPalette[]{palette11920, palette11920});

        GameVersion baseVersion = V1_18_0;
        BlockPalette basePalette = palette118N;
        log.debug("Base runtime block palette version: {}", baseVersion);

        map(V1_17_40, basePalette, palette11740, ver -> ver.ordinal() >= V1_17_40.ordinal() && ver.ordinal() <= V1_18_0.ordinal());
        map(V1_18_10, basePalette, palette11810, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_18_10.ordinal());
        map(V1_18_30, basePalette, palette11830, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_18_30.ordinal());
        map(V1_19_0, basePalette, palette119, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_19_0.ordinal());
        map(V1_19_20, basePalette, palette11920, ver -> ver.ordinal() >= baseVersion.ordinal() && ver.ordinal() <= V1_19_20.ordinal());

        setupRuntimeBlockSerializer(basePalette);
    }

    private static void map(GameVersion version, BlockPalette base, BlockPalette target, Predicate<GameVersion> upgradeFilter) {
        log.debug("Mapping runtime block palette data to {}", version);
        List<BlockUpgradeSchema> schemas = VanillaBlockUpgrader.getSchemas(upgradeFilter);
        base.palette.forEach(block -> map(block, target, schemas));
    }

    @Nullable
    private static BlockData map(BlockData block, BlockPalette target, List<BlockUpgradeSchema> schemas) {
        Optional<BlockData> match = target.palette.stream().filter(data -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("name", block.name);
            tag.putCompound("states", block.states.clone());
            tag.putInt("version", VanillaBlockUpgrader.getCurrentVersion());
            VanillaBlockUpgrader.upgrade(tag, schemas);
            return tag.getString("name").equals(data.name) && tag.getCompound("states").equals(data.states);
        }).findFirst();

        if (match.isPresent()) {
            BlockData data = match.get();
            data.id = block.id;
            data.val = block.val;
            return data;
        }

        log.debug("No corresponding block found: {}", block);
        return null;
    }

    private static void setupRuntimeBlockSerializer(BlockPalette basePalette) {
        List<CompoundTag>[] mapping = new List[Block.UNDEFINED];
        for (BlockData block : basePalette.palette) {
            int id = block.id;
            if (block.id == -1) {
                continue;
            }
            if (id >= Block.UNDEFINED) {
                log.debug("Skip unsupported block: {}", id);
                continue;
            }
            int meta = block.val;

            List<CompoundTag> tags = mapping[id];
            if (tags == null) {
                tags = new ObjectArrayList<>();
                mapping[id] = tags;
            }

            while (tags.size() <= meta) {
                tags.add(null);
            }
            tags.set(meta, new CompoundTag()
                    .putString("name", block.name)
                    .putCompound("states", block.states)
                    .putInt("version", VanillaBlockUpgrader.getCurrentVersion()));
        }

        CompoundTag[][] idMetaToTag = new CompoundTag[Block.UNDEFINED][];
        for (int id = 0; id < Block.UNDEFINED; id++) {
            List<CompoundTag> tags = mapping[id];
            if (tags == null) {
                log.trace("Skip unmapped block id: {}", id);
                continue;
            }
            if (tags.isEmpty()) {
                log.trace("Skip empty block states. id {}", id);
                continue;
            }
            CompoundTag first = tags.get(0);
            if (first == null) {
                log.debug("First block state undefined. id {}", id);
            }
            for (int meta = 0; meta < tags.size(); meta++) {
                CompoundTag tag = tags.get(meta);
                if (tag != null) {
                    continue;
                }
//                log.trace("Mapping undefined block meta to first state. id {} meta {}", id, meta);
                tags.set(meta, first);
            }
            idMetaToTag[id] = tags.toArray(new CompoundTag[0]);
        }

        CompoundTag infoUpdateBlock = idMetaToTag[Block.INFO_UPDATE][0];

        LegacyBlockSerializer.setSerializer(fullId -> {
            int id = fullId >> Block.BLOCK_META_BITS;
            if (id < 0 || id >= Block.UNDEFINED) {
                log.warn("Invalid block id: {}", id);
                return infoUpdateBlock;
            }

            CompoundTag[] tags = idMetaToTag[id];
            if (tags == null) {
                log.warn("Unmapped block id: {}", id);
                return infoUpdateBlock;
            }

            int meta = fullId & Block.BLOCK_META_MASK;
            if (meta < 0 || meta >= tags.length) {
                log.warn("Invalid block meta: id {} val {}", id, meta);
                return infoUpdateBlock;
            }
            return tags[meta];
        });
    }

    public static void initialize() {
    }

    private RuntimeBlockMapper() {
        throw new IllegalStateException();
    }
}

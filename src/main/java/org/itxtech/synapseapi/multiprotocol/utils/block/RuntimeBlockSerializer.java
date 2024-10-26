package org.itxtech.synapseapi.multiprotocol.utils.block;

import cn.nukkit.block.Block;
import cn.nukkit.nbt.tag.CompoundTag;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockPalette.BlockData;

import java.util.List;

@Log4j2
public class RuntimeBlockSerializer {
    final CompoundTag[][] idMetaToTag = new CompoundTag[Block.BLOCK_ID_COUNT][];
    private final CompoundTag unknownBlock;

    public RuntimeBlockSerializer(BlockPalette palette) {
        List<CompoundTag>[] mapping = new List[Block.BLOCK_ID_COUNT];
        for (BlockData block : palette.palette) {
            int id = block.id;
            if (block.id == -1) {
                continue;
            }
            if (id >= Block.BLOCK_ID_COUNT) {
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

        for (int id = 0; id < Block.BLOCK_ID_COUNT; id++) {
            List<CompoundTag> tags = mapping[id];
            if (tags == null) {
                if (id < Block.UNDEFINED) {
                    log.trace("Skip unmapped block id: {}", id);
                }
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

        unknownBlock = idMetaToTag[Block.UNKNOWN][0];
    }

    public CompoundTag serialize(int fullId) {
        int id = fullId >> Block.BLOCK_META_BITS;
        if (id < 0 || id >= Block.BLOCK_ID_COUNT) {
            log.warn("Invalid block id: {}", id);
            return unknownBlock;
        }

        CompoundTag[] tags = idMetaToTag[id];
        if (tags == null) {
            log.warn("Unmapped block id: {}", id);
            return unknownBlock;
        }

        int meta = fullId & Block.BLOCK_META_MASK;
        int count = tags.length;
        if (meta < 0 || meta >= count) {
            log.warn("Invalid block meta: id {} val {}", id, meta);
            if (count == 0) {
                return unknownBlock;
            }
            return tags[0];
        }
        return tags[meta];
    }
}

package org.itxtech.synapseapi.multiprotocol.utils.blockpalette;

import cn.nukkit.block.Block;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.BinaryStream;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPaletteInterface;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockPalette.BlockData;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockProperty;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockData;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockTable;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Log4j2
public class GlobalBlockPaletteNBT implements AdvancedGlobalBlockPaletteInterface {

    final int[][] idMetaToRuntimeId = new int[Block.BLOCK_ID_COUNT][];
    final int[] runtimeIdToLegacy;
    final CompoundTag[] runtimeIdToState;
    final Int2IntMap unknownToRuntimeId = new Int2IntOpenHashMap();
    final IntList runtimeIdToUnknown = new IntArrayList();
    final AtomicInteger runtimeIdAllocator = new AtomicInteger(0);
    final int totalRuntimeIds;
    final boolean allowUnknownBlock; // Show 'Update Game!' block
    final byte[] compiledTable;
    final byte[] blockProperties;
    final byte[] itemDataPalette;

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    public GlobalBlockPaletteNBT(AbstractProtocol protocol, PaletteBlockTable blockTable, String itemDataPaletteJsonFile, boolean allowUnknownBlock) {
        log.debug("Loading Advanced Global Block Palette from PaletteBlockTable(nbt) for {}. totalRuntimeIds {} remapSize {}", protocol, blockTable.totalRuntimeIds, blockTable.size());

        totalRuntimeIds = Math.max(blockTable.totalRuntimeIds, blockTable.size());
        runtimeIdToLegacy = new int[totalRuntimeIds];
        runtimeIdToState = new CompoundTag[totalRuntimeIds];
        Arrays.fill(runtimeIdToLegacy, -1);
        unknownToRuntimeId.defaultReturnValue(-1);

        this.allowUnknownBlock = allowUnknownBlock;

        IntList[] idMetaToRuntimeId = new IntList[Block.BLOCK_ID_COUNT];
        try {
            compiledTable = NBTIO.write(blockTable.toTag(), ByteOrder.LITTLE_ENDIAN, true);
            for (int i = 0; i < blockTable.size(); i++, runtimeIdAllocator.incrementAndGet()) {
                PaletteBlockData data = blockTable.get(i);
                runtimeIdToState[i] = data.block.getStatesTag();

                if (data.legacyStates != null && data.legacyStates.length > 0) {
                    int legacyIdNoMeta = data.legacyStates[data.legacyIndex].id << BLOCK_META_BITS;
                    int legacyId = legacyIdNoMeta | data.legacyStates[data.legacyIndex].val;
                    runtimeIdToLegacy[i] = legacyId;
                    for (PaletteBlockData.LegacyStates legacyState : data.legacyStates) {
                        int id = legacyState.id;
                        if (id >= Block.BLOCK_ID_COUNT) {
                            log.debug("Skip unsupported block: {}", data);
                            continue;
                        }

                        int meta = legacyState.val;
//                        legacyIdNoMeta = id << BLOCK_META_BITS;
//                        legacyId = legacyIdNoMeta | meta;
//                        if (legacyToRuntimeId[legacyId] == -1) {
//                            legacyToRuntimeId[legacyId] = i;
//                        }
                        IntList metaToRuntimeId = idMetaToRuntimeId[id];
                        if (metaToRuntimeId == null) {
                            metaToRuntimeId = new IntArrayList();
                            idMetaToRuntimeId[id] = metaToRuntimeId;
                        }
                        while (metaToRuntimeId.size() <= meta) {
                            metaToRuntimeId.add(-1);
                        }
                        if (metaToRuntimeId.getInt(meta) == -1) {
                            metaToRuntimeId.set(meta, i);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new AssertionError("Unable to write block palette", e);
        }

        for (int id = 0; id < Block.BLOCK_ID_COUNT; id++) {
            IntList metaToRuntimeId = idMetaToRuntimeId[id];
            if (metaToRuntimeId == null) {
                continue;
            }
            int firstRuntimeId = metaToRuntimeId.getInt(0);
            if (firstRuntimeId == -1) {
                log.debug("First block runtimeId undefined. id {}", id);
            }
            for (int meta = 0; meta < metaToRuntimeId.size(); meta++) {
                int runtimeId = metaToRuntimeId.getInt(meta);
                if (runtimeId != -1) {
                    continue;
                }
//                log.trace("Mapping undefined block meta to first runtimeId: id {} meta {} -> runtimeId {}", id, meta, firstRuntimeId);
                metaToRuntimeId.set(meta, firstRuntimeId);
            }
            this.idMetaToRuntimeId[id] = metaToRuntimeId.toIntArray();
        }

        blockProperties = new byte[1]; // BinaryStream::putUnsignedVarInt(0);

        itemDataPalette = loadItemDataPalette(itemDataPaletteJsonFile);
    }

    public GlobalBlockPaletteNBT(AbstractProtocol protocol, List<BlockData> palette) {
        this(protocol, palette, Collections.emptyList());
    }

    public GlobalBlockPaletteNBT(AbstractProtocol protocol, List<BlockData> palette, List<BlockProperty> properties) {
        this.allowUnknownBlock = true;
        totalRuntimeIds = palette.size();
        log.debug("Loading Advanced Global Block Palette from runtime mapping for {}. total {}", protocol, totalRuntimeIds);
        runtimeIdToLegacy = new int[totalRuntimeIds];
        runtimeIdToState = new CompoundTag[totalRuntimeIds];
        Arrays.fill(runtimeIdToLegacy, -1);
        unknownToRuntimeId.defaultReturnValue(-1);

        IntList[] idMetaToRuntimeId = new IntList[Block.BLOCK_ID_COUNT];
        for (int i = 0; i < palette.size(); i++, runtimeIdAllocator.incrementAndGet()) {
            BlockData data = palette.get(i);
            runtimeIdToState[i] = data.states;
            int id = data.id;
            if (id == -1) {
                continue;
            }
            if (id >= Block.BLOCK_ID_COUNT) {
                log.debug("Skip unsupported block: {}", data);
                continue;
            }
            int meta = data.val;
            int legacyIdNoMeta = id << BLOCK_META_BITS;
            int legacyId = legacyIdNoMeta | meta;
            runtimeIdToLegacy[i] = legacyId;

//            if (legacyToRuntimeId[legacyId] == -1) {
//                legacyToRuntimeId[legacyId] = i;
//            }
            IntList metaToRuntimeId = idMetaToRuntimeId[id];
            if (metaToRuntimeId == null) {
                metaToRuntimeId = new IntArrayList();
                idMetaToRuntimeId[id] = metaToRuntimeId;
            }
            while (metaToRuntimeId.size() <= meta) {
                metaToRuntimeId.add(-1);
            }
            if (metaToRuntimeId.getInt(meta) == -1) {
                metaToRuntimeId.set(meta, i);
            }
        }

        for (int id = 0; id < Block.BLOCK_ID_COUNT; id++) {
            IntList metaToRuntimeId = idMetaToRuntimeId[id];
            if (metaToRuntimeId == null) {
                if (id == Block.LAVA_CAULDRON) {
                    final int toId = Block.BLOCK_CAULDRON;
                    log.debug("REMOVE ME IN THE FUTURE! (1.20.0) Manual mapping block ID: {} => {} ({})", id, toId, protocol);
                    this.idMetaToRuntimeId[id] = idMetaToRuntimeId[toId].toIntArray();
                }
                continue;
            }
            int firstRuntimeId = metaToRuntimeId.getInt(0);
            if (firstRuntimeId == -1) {
                log.debug("First block runtimeId undefined. id {}", id);
            }
            for (int meta = 0; meta < metaToRuntimeId.size(); meta++) {
                int runtimeId = metaToRuntimeId.getInt(meta);
                if (runtimeId != -1) {
                    continue;
                }
//                log.trace("Mapping undefined block meta to first runtimeId: id {} meta {} -> runtimeId {}", id, meta, firstRuntimeId);
                metaToRuntimeId.set(meta, firstRuntimeId);
            }
            this.idMetaToRuntimeId[id] = metaToRuntimeId.toIntArray();
        }

        compiledTable = null;

        BinaryStream stream = new BinaryStream();
        stream.putUnsignedVarInt(properties.size());
        for (BlockProperty property : properties) {
            stream.putString(property.name());
            byte[] bytes;
            try {
                bytes = NBTIO.writeNetwork(property.definition());
            } catch (IOException e) {
                throw new AssertionError("Unable to write block property definition", e);
            }
            stream.put(bytes);
        }
        blockProperties = stream.getBuffer();

        itemDataPalette = null;
    }

    @Override
    public int getOrCreateRuntimeId(int id, int meta) {
        if (id < 0) {
            log.warn("Block ID must be positive: {}", id, new Throwable("debug trace"));
            id = 0xff - id;
        }

        int[] metaToRuntimeId;
        if (id >= Block.BLOCK_ID_COUNT || (metaToRuntimeId = idMetaToRuntimeId[id]) == null) {
            if (!this.allowUnknownBlock) {
                throw new NoSuchElementException("Unmapped block registered id:" + id + " meta:" + meta);
            }
            //TODO: 1.20.0 测试疑似不再生效, 无效方块在客户端变为空气而不是未知方块 -- 07/07/2023

            int legacyIdNoMeta = id << BLOCK_META_BITS;
            int legacyId = legacyIdNoMeta | meta;

            // First, try to get the runtimeId with only a read lock.
            int runtimeId;
            readLock.lock();
            try {
                runtimeId = unknownToRuntimeId.get(legacyId);
                if (runtimeId == -1) {
                    runtimeId = unknownToRuntimeId.get(legacyIdNoMeta);
                }
            } finally {
                readLock.unlock();
            }

            // If runtimeId is still -1, then we need to do write, so acquire the write lock.
            if (runtimeId == -1) {
                writeLock.lock();
                try {
                    // Re-check the condition, as it might have been changed by another thread.
                    runtimeId = unknownToRuntimeId.get(legacyId);
                    if (runtimeId == -1) {
                        runtimeId = unknownToRuntimeId.get(legacyIdNoMeta);
                        if (runtimeId == -1) {
                            log.warn("Creating new runtime ID for unknown block: id {} meta {}", id, meta, new Throwable("debug trace"));
                            runtimeId = runtimeIdAllocator.getAndIncrement();
                            unknownToRuntimeId.put(legacyIdNoMeta, runtimeId);
                            runtimeIdToUnknown.add(legacyIdNoMeta);
                        }
                    }
                } finally {
                    writeLock.unlock();
                }
            }

            return runtimeId;
        }

        if (meta >= metaToRuntimeId.length) {
            return metaToRuntimeId[0];
        }
        return metaToRuntimeId[meta];
    }

    @Override
    public int getOrCreateRuntimeId(int legacyId) throws NoSuchElementException {
        return getOrCreateRuntimeId(legacyId >> Block.BLOCK_META_BITS, legacyId & Block.BLOCK_META_MASK);
    }

    @Override
    public int getRuntimeId(int id, int meta) {
        int[] metaToRuntimeId;
        if (id >= Block.BLOCK_ID_COUNT || (metaToRuntimeId = idMetaToRuntimeId[id]) == null) {
            return Integer.MIN_VALUE;
        }

        if (meta >= metaToRuntimeId.length) {
            return -metaToRuntimeId[0];
        }
        return metaToRuntimeId[meta];
    }

    @Override
    public int getLegacyId(int runtimeId) {
        if (runtimeId < 0) {
            return -1;
        }

        if (runtimeId >= totalRuntimeIds) {
            int unknownIndex = runtimeId - totalRuntimeIds;
            try {
                readLock.lock();

                if (unknownIndex >= runtimeIdToUnknown.size()) {
                    return -1;
                }
                return runtimeIdToUnknown.getInt(unknownIndex);
            } finally {
                readLock.unlock();
            }
        }

        return runtimeIdToLegacy[runtimeId];
    }

    public CompoundTag getState(int runtimeId) {
        if (runtimeId >= totalRuntimeIds) {
            return UNKNOWN_BLOCK_STATE;
        }
        return runtimeIdToState[runtimeId];
    }

    @Override
    public byte[] getCompiledTable() {
        return compiledTable;
    }

    @Override
    public byte[] getCompiledBlockProperties() {
        return blockProperties;
    }

    @Override
    public byte[] getItemDataPalette() {
        return itemDataPalette;
    }
}

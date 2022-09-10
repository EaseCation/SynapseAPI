package org.itxtech.synapseapi.multiprotocol.utils.blockpalette;

import cn.nukkit.block.Block;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPaletteInterface;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockData;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockTable;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Log4j2
public class GlobalBlockPaletteNBT implements AdvancedGlobalBlockPaletteInterface {

    final int[] legacyToRuntimeId = new int[FULL_BLOCK_COUNT]; //TODO: 2D array
    final int[] runtimeIdToLegacy;
    final CompoundTag[] runtimeIdToState;
    final Int2IntMap unknownToRuntimeId = new Int2IntOpenHashMap();
    final IntList runtimeIdToUnknown = new IntArrayList();
    final AtomicInteger runtimeIdAllocator = new AtomicInteger(0);
    final int totalRuntimeIds;
    final boolean allowUnknownBlock; // Show 'Update Game!' block
    final byte[] compiledTable;
    final byte[] itemDataPalette;

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    public GlobalBlockPaletteNBT(AbstractProtocol protocol, PaletteBlockTable blockTable, String itemDataPaletteJsonFile, boolean allowUnknownBlock) {
        log.info("Loading Advanced Global Block Palette from PaletteBlockTable(nbt) for {}. totalRuntimeIds {} remapSize {}", protocol, blockTable.totalRuntimeIds, blockTable.size());

        totalRuntimeIds = Math.max(blockTable.totalRuntimeIds, blockTable.size());
        runtimeIdToLegacy = new int[totalRuntimeIds];
        runtimeIdToState = new CompoundTag[totalRuntimeIds];
        Arrays.fill(legacyToRuntimeId, -1);
        Arrays.fill(runtimeIdToLegacy, -1);
        unknownToRuntimeId.defaultReturnValue(-1);

        this.allowUnknownBlock = allowUnknownBlock;

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
                        legacyIdNoMeta = id << BLOCK_META_BITS;
                        legacyId = legacyIdNoMeta | meta;
                        if (legacyToRuntimeId[legacyId] == -1) {
                            legacyToRuntimeId[legacyId] = i;
                        }

//                        if (meta > 0x3f) log.trace("block meta > 63! id: {}, meta: {}", id, meta);
                    }
                }
            }
        } catch (IOException e) {
            throw new AssertionError("Unable to write block palette", e);
        }
        itemDataPalette = loadItemDataPalette(itemDataPaletteJsonFile);
    }

    @Override
    public int getOrCreateRuntimeId(int id, int meta) {
        if (id < 0) {
            log.warn("Block ID must be positive: {}", id, new Throwable("debug trace"));
            id = 0xff - id;
        }

        int legacyIdNoMeta = id << BLOCK_META_BITS;
        int legacyId = legacyIdNoMeta | meta;

        if (legacyId >= FULL_BLOCK_COUNT) {
            if (!this.allowUnknownBlock) {
                throw new NoSuchElementException("Unmapped block registered id:" + id + " meta:" + meta);
            }
            try {
                readLock.lock();

                int runtimeId = unknownToRuntimeId.get(legacyId);
                if (runtimeId == -1) {
                    runtimeId = unknownToRuntimeId.get(legacyIdNoMeta);
                    if (runtimeId == -1) {
                        log.warn("Creating new runtime ID for unknown block {}", id, new Throwable("debug trace"));
                        try {
                            writeLock.lock();

                            runtimeId = runtimeIdAllocator.getAndIncrement();
                            unknownToRuntimeId.put(legacyIdNoMeta, runtimeId);
                            runtimeIdToUnknown.add(legacyIdNoMeta);
                        } finally {
                            writeLock.unlock();
                        }
                    }
                }
                return runtimeId;
            } finally {
                readLock.unlock();
            }
        }

        int runtimeId = legacyToRuntimeId[legacyId];
        if (runtimeId == -1) {
            runtimeId = legacyToRuntimeId[legacyIdNoMeta];
            if (runtimeId == -1) {
                if (!this.allowUnknownBlock) {
                    throw new NoSuchElementException("Unmapped block registered id:" + id + " meta:" + meta);
                }
                log.warn("Creating new runtime ID for unknown block {}", id, new Throwable("debug trace"));
                try {
                    writeLock.lock();

                    runtimeId = runtimeIdAllocator.getAndIncrement();
                    legacyToRuntimeId[legacyIdNoMeta] = runtimeId;
                    runtimeIdToLegacy[runtimeId] = legacyIdNoMeta;
                } finally {
                    writeLock.unlock();
                }
            }
        }
        return runtimeId;
    }

    @Override
    public int getOrCreateRuntimeId(int legacyId) throws NoSuchElementException {
        return getOrCreateRuntimeId(legacyId >> Block.BLOCK_META_BITS, legacyId & Block.BLOCK_META_MASK);
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
    public byte[] getItemDataPalette() {
        return itemDataPalette;
    }
}

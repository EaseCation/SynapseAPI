package org.itxtech.synapseapi.multiprotocol.utils.blockpalette;

import cn.nukkit.block.Block;
import cn.nukkit.nbt.NBTIO;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPaletteInterface;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockData;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockTable;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class GlobalBlockPaletteNBTOld implements AdvancedGlobalBlockPaletteInterface {

    final Int2IntMap legacyToRuntimeId = new Int2IntOpenHashMap();
    final Int2IntMap runtimeIdToLegacy = new Int2IntOpenHashMap();
    final AtomicInteger runtimeIdAllocator = new AtomicInteger(0);
    final byte[] compiledTable;
    final byte[] itemDataPalette;

    public GlobalBlockPaletteNBTOld(AbstractProtocol protocol, PaletteBlockTable blockTable, String itemDataPaletteJsonFile) {
        log.info("Loading Advanced Global Block Palette from PaletteBlockTable(old nbt) for {}", protocol);
        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);

        try {
            compiledTable = NBTIO.write(blockTable.toTag(), ByteOrder.LITTLE_ENDIAN, true);
            for (int i = 0; i < blockTable.size(); i++) {
                PaletteBlockData data = blockTable.get(i);

                if (data.legacyStates != null && data.legacyStates.length > 0) {
                    runtimeIdToLegacy.put(i, data.legacyStates[0].id << 6 | (short) data.legacyStates[0].val);
                    for (PaletteBlockData.LegacyStates legacyState : data.legacyStates) {
                        int legacyId = legacyState.id << 6 | (short) legacyState.val;
                        legacyToRuntimeId.putIfAbsent(legacyId, i);

//                        if (legacyState.val > 0x3f) log.trace("block meta > 63! id: {}, meta: {}", legacyState.id, legacyState.val);
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
        int legacyId = id << 6 | meta;
        int runtimeId = legacyToRuntimeId.get(legacyId);
        if (runtimeId == -1) {
            //runtimeId = registerMapping(runtimeIdAllocator.incrementAndGet(), legacyId);
            throw new NoSuchElementException("Unmapped block registered id:" + id + " meta:" + meta);
        }
        return runtimeId;
    }

    @Override
    public int getOrCreateRuntimeId(int legacyId) throws NoSuchElementException {
        return getOrCreateRuntimeId(legacyId >> Block.BLOCK_META_BITS, legacyId & Block.BLOCK_META_MASK);
    }

    @Override
    public int getLegacyId(int runtimeId) {
        return runtimeIdToLegacy.get(runtimeId);
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

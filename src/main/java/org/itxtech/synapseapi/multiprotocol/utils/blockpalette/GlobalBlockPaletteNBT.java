package org.itxtech.synapseapi.multiprotocol.utils.blockpalette;

import cn.nukkit.Server;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import com.google.common.io.ByteStreams;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPaletteInterface;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockData;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockTable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class GlobalBlockPaletteNBT implements AdvancedGlobalBlockPaletteInterface {

    final Int2IntMap legacyToRuntimeId = new Int2IntOpenHashMap();
    final Int2IntMap runtimeIdToLegacy = new Int2IntOpenHashMap();
    final Int2ObjectMap<CompoundTag> runtimeIdToState = new Int2ObjectOpenHashMap<>();
    final AtomicInteger runtimeIdAllocator = new AtomicInteger(0);
    final boolean allowUnknownBlock; // Show 'Update Game!' block
    final byte[] compiledTable;
    final byte[] itemDataPalette;

    public GlobalBlockPaletteNBT(AbstractProtocol protocol, String blockPaletteFile) {
        this(protocol, blockPaletteFile, null);
    }

    public GlobalBlockPaletteNBT(AbstractProtocol protocol, PaletteBlockTable blockTable, String itemDataPaletteJsonFile) {
        this(protocol, blockTable, itemDataPaletteJsonFile, false);
    }

    public GlobalBlockPaletteNBT(AbstractProtocol protocol, PaletteBlockTable blockTable, String itemDataPaletteJsonFile, boolean allowUnknownBlock) {
        Server.getInstance().getLogger().info("Loading Advanced Global Block Palette from PaletteBlockTable(nbt)");
        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);
        this.allowUnknownBlock = allowUnknownBlock;

        try {
            compiledTable = NBTIO.write(blockTable.toTag(), ByteOrder.LITTLE_ENDIAN, true);
            for (int i = 0; i < blockTable.size(); i++, runtimeIdAllocator.incrementAndGet()) {
                PaletteBlockData data = blockTable.get(i);
                runtimeIdToState.put(i, data.block.getStatesTag());

                if (data.legacyStates != null && data.legacyStates.length > 0) {
                    int legacyIdNoMeta = data.legacyStates[0].id << 14;
                    int legacyId = legacyIdNoMeta | data.legacyStates[0].val;
                    runtimeIdToLegacy.put(i, legacyId);
                    for (PaletteBlockData.LegacyStates legacyState : data.legacyStates) {
                        legacyIdNoMeta = legacyState.id << 14;
                        legacyId = legacyIdNoMeta | legacyState.val;
                        legacyToRuntimeId.putIfAbsent(legacyId, i);
                    }
                }
            }
        } catch (IOException e) {
            throw new AssertionError("Unable to write block palette", e);
        }
        itemDataPalette = loadItemDataPalette(itemDataPaletteJsonFile);
    }

    public GlobalBlockPaletteNBT(AbstractProtocol protocol, String blockPaletteFile, String itemDataPaletteJsonFile) {
        this(protocol, blockPaletteFile, itemDataPaletteJsonFile, false);
    }

    public GlobalBlockPaletteNBT(AbstractProtocol protocol, String blockPaletteFile, String itemDataPaletteJsonFile, boolean allowUnknownBlock) {
        Server.getInstance().getLogger().info("Loading Advanced Global Block Palette from " + blockPaletteFile);
        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);
        this.allowUnknownBlock = allowUnknownBlock;

        compiledTable = loadBlockPaletteNBT(protocol, blockPaletteFile);
        itemDataPalette = loadItemDataPalette(itemDataPaletteJsonFile);
    }

    private byte[] loadBlockPaletteNBT(AbstractProtocol protocol, String file) {
        ListTag<CompoundTag> tag;
        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(file)) {
            if (stream == null) {
                throw new AssertionError("Unable to locate block state nbt");
            }
            //noinspection unchecked
            tag = (ListTag<CompoundTag>) NBTIO.readTag(new ByteArrayInputStream(ByteStreams.toByteArray(stream)), ByteOrder.LITTLE_ENDIAN, false);
        } catch (IOException e) {
            throw new AssertionError("Unable to load block palette", e);
        }

        for (CompoundTag state : tag.getAll()) {
            int runtimeId = runtimeIdAllocator.getAndIncrement();
            runtimeIdToState.put(runtimeId, state);

            String name = state.getCompound("block").getString("name");

            if (!state.contains("LegacyStates")) continue;

            List<CompoundTag> legacyStates = state.getList("LegacyStates", CompoundTag.class).getAll();

            // Resolve to first legacy id
            CompoundTag firstState = legacyStates.get(0);
            runtimeIdToLegacy.put(runtimeId, firstState.getInt("id") << 14 | firstState.getShort("val"));

            for (CompoundTag legacyState : legacyStates) {
                int legacyId = legacyState.getInt("id") << 14 | legacyState.getShort("val");
                legacyToRuntimeId.putIfAbsent(legacyId, runtimeId);
            }
            //state.remove("LegacyStates"); // No point in sending this since the client doesn't use it.
        }

        try {
            return NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException e) {
            throw new AssertionError("Unable to write block palette", e);
        }
    }

    @Override
    public int getOrCreateRuntimeId(int id, int meta) {
        int legacyIdNoMeta = id << 14;
        int legacyId = legacyIdNoMeta | meta;
        int runtimeId = legacyToRuntimeId.get(legacyId);
        if (runtimeId == -1) {
            runtimeId = legacyToRuntimeId.get(legacyIdNoMeta);
            if (runtimeId == -1) {
                if (!this.allowUnknownBlock) {
                    throw new NoSuchElementException("Unmapped block registered id:" + id + " meta:" + meta);
                }
                log.info("Creating new runtime ID for unknown block {}", id);
                runtimeId = runtimeIdAllocator.getAndIncrement();
                legacyToRuntimeId.put(legacyIdNoMeta, runtimeId);
                runtimeIdToLegacy.put(runtimeId, legacyIdNoMeta);
            }
        }
        return runtimeId;
    }

    @Override
    public int getOrCreateRuntimeId(int legacyId) throws NoSuchElementException {
        return getOrCreateRuntimeId(legacyId >> 4, legacyId & 0xf);
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

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
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPaletteInterface;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockData;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockTable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalBlockPaletteNBTOld implements AdvancedGlobalBlockPaletteInterface {

    final Int2IntMap legacyToRuntimeId = new Int2IntOpenHashMap();
    final Int2IntMap runtimeIdToLegacy = new Int2IntOpenHashMap();
    final AtomicInteger runtimeIdAllocator = new AtomicInteger(0);
    final byte[] compiledTable;
    final byte[] itemDataPalette;

    private final Int2ObjectMap<String> legacyIdToString = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<String> stringToLegacyId = new Object2IntOpenHashMap<>();
    private final Int2ObjectMap<String> runtimeIdToString = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<String> stringToRuntimeId = new Object2IntOpenHashMap<>();

    public GlobalBlockPaletteNBTOld(AbstractProtocol protocol, String blockPaletteFile) {
        this(protocol, blockPaletteFile, null);
    }

    public GlobalBlockPaletteNBTOld(AbstractProtocol protocol, PaletteBlockTable blockTable, String itemDataPaletteJsonFile) {
        Server.getInstance().getLogger().info("Loading Advanced Global Block Palette from PaletteBlockTable(old nbt)");
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
                    }
                }
            }
        } catch (IOException e) {
            throw new AssertionError("Unable to write block palette", e);
        }
        itemDataPalette = loadItemDataPalette(itemDataPaletteJsonFile);
    }

    public GlobalBlockPaletteNBTOld(AbstractProtocol protocol, String blockPaletteFile, String itemDataPaletteJsonFile) {
        Server.getInstance().getLogger().info("Loading Advanced Global Block Palette from " + blockPaletteFile);
        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);

        compiledTable = loadBlockPaletteNBT(protocol, blockPaletteFile);
        itemDataPalette = loadItemDataPalette(itemDataPaletteJsonFile);
    }


    private byte[] loadBlockPaletteNBT(AbstractProtocol protocol, String file) {
        InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(file);
        if (stream == null) {
            throw new AssertionError("Unable to locate block state nbt");
        }
        ListTag<CompoundTag> tag;
        byte[] data;
        try {
            //noinspection UnstableApiUsage
            data = ByteStreams.toByteArray(stream);
            //noinspection unchecked
            tag = (ListTag<CompoundTag>) NBTIO.readTag(new ByteArrayInputStream(data), ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        for (CompoundTag state : tag.getAll()) {
            int runtimeId = runtimeIdAllocator.getAndIncrement();

            String name = state.getCompound("block").getString("name");
            stringToRuntimeId.putIfAbsent(name, runtimeId);
            runtimeIdToString.putIfAbsent(runtimeId, name);

            if (!state.contains("meta")) continue;

            int id = state.getShort("id");
            int[] meta = state.getIntArray("meta");

            // Resolve to first legacy id
            runtimeIdToLegacy.put(runtimeId, id << 6 | meta[0]);

            stringToLegacyId.put(name, id << 6 | meta[0]);
            legacyIdToString.put(id << 6 | meta[0], name);

            for (int val : meta) {
                int legacyId = id << 6 | val;
                legacyToRuntimeId.putIfAbsent(legacyId, runtimeId);
            }
            state.remove("meta"); // No point in sending this since the client doesn't use it.
        }
        return data;
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

    @Override
    public String getNameByRuntimeId(int runtimeId) {
        String name = runtimeIdToString.get(runtimeId);
        return name == null ? "minecraft:air" : name;
    }

    @Override
    public String getNameByBlockId(int blockId) {
        String name = legacyIdToString.get(blockId);
        return name == null ? "minecraft:air" : name;
    }

    @Override
    public int getBlockIdByName(String name) {
        return stringToLegacyId.getInt(name);
    }
}

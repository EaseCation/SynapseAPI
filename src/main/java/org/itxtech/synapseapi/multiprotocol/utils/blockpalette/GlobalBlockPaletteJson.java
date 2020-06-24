package org.itxtech.synapseapi.multiprotocol.utils.blockpalette;

import cn.nukkit.Server;
import cn.nukkit.utils.BinaryStream;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPaletteInterface;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockData;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockTable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalBlockPaletteJson implements AdvancedGlobalBlockPaletteInterface {

    final Int2IntArrayMap legacyToRuntimeId = new Int2IntArrayMap();
    final Int2IntArrayMap runtimeIdToLegacy = new Int2IntArrayMap();
    final Int2ObjectMap<String> runtimeIdToString = new Int2ObjectOpenHashMap<>();
    final Object2IntMap<String> stringToRuntimeId = new Object2IntOpenHashMap<>();
    final AtomicInteger runtimeIdAllocator = new AtomicInteger(0);
    final byte[] compiledTable;
    final byte[] itemDataPalette;

    public GlobalBlockPaletteJson(AbstractProtocol protocol, String blockPaletteFile) {
        this(protocol, blockPaletteFile, null);
    }

    public GlobalBlockPaletteJson(AbstractProtocol protocol, PaletteBlockTable blockTable, String itemDataPaletteJsonFile) {
        Server.getInstance().getLogger().info("Loading Advanced Global Block Palette from PaletteBlockTable(old json)");
        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);
        stringToRuntimeId.defaultReturnValue(-1);

        BinaryStream table = new BinaryStream();

        table.putUnsignedVarInt(blockTable.size());

        for (int i = 0; i < blockTable.size(); i++) {
            int meta = 0;
            PaletteBlockData data = blockTable.get(i);
            if (data.legacyStates != null && data.legacyStates.length > 0) {
                meta = data.legacyStates[0].val;
                runtimeIdToLegacy.put(i, data.legacyStates[0].id << 6 | (short) data.legacyStates[0].val);
                for (PaletteBlockData.LegacyStates legacyState : data.legacyStates) {
                    int legacyId = legacyState.id << 6 | (short) legacyState.val;
                    legacyToRuntimeId.put(legacyId, i);
                }
            }

            table.putString(data.block.name);
            table.putLShort(meta);
            if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_112.ordinal()) table.putLShort(data.id);
        }
        compiledTable = table.getBuffer();

        itemDataPalette = loadItemDataPalette(itemDataPaletteJsonFile);
    }

    public GlobalBlockPaletteJson(AbstractProtocol protocol, String blockPaletteFile, String itemDataPaletteJsonFile) {
        Server.getInstance().getLogger().info("Loading Advanced Global Block Palette from " + blockPaletteFile);
        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);
        stringToRuntimeId.defaultReturnValue(-1);

        compiledTable = loadBlockPaletteJson(protocol, blockPaletteFile);

        itemDataPalette = loadItemDataPalette(itemDataPaletteJsonFile);
    }

    private byte[] loadBlockPaletteJson(AbstractProtocol protocol, String file) {
        InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(file);
        if (stream == null) {
            throw new AssertionError("Unable to locate RuntimeID table (.json)");
        }
        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<TableEntry>>() {
        }.getType();
        Collection<TableEntry> entries = gson.fromJson(reader, collectionType);
        BinaryStream table = new BinaryStream();

        table.putUnsignedVarInt(entries.size());

        for (TableEntry entry : entries) {
            registerMapping((entry.id << 4) | entry.data, entry.name);
            table.putString(entry.name);
            table.putLShort(entry.data);
            if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_112.ordinal()) table.putLShort(entry.id);
        }
        return table.getBuffer();
    }

    public int getOrCreateRuntimeId(int id, int meta) {
        return getOrCreateRuntimeId((id << 4) | meta);
    }

    public int getOrCreateRuntimeId(int legacyId) {
        int runtimeId = legacyToRuntimeId.get(legacyId);
        if (runtimeId == -1) {
            //runtimeId = registerMapping(runtimeIdAllocator.incrementAndGet(), legacyId);
            throw new RuntimeException("Unmapped block registered");
        }
        return runtimeId;
    }

    private int registerMapping(int legacyId, String name) {
        int runtimeId = runtimeIdAllocator.getAndIncrement();
        runtimeIdToLegacy.put(runtimeId, legacyId);
        legacyToRuntimeId.put(legacyId, runtimeId);
        stringToRuntimeId.put(name, runtimeId);
        runtimeIdToString.put(runtimeId, name);
        return runtimeId;
    }

    @Override
    public int getLegacyId(int runtimeId) {
        return runtimeIdToLegacy.get(runtimeId);
    }

    @Override
    public String getName(int runtimeId) {
        String name = runtimeIdToString.get(runtimeId);
        return name == null ? "minecraft:air" : name;
    }

    @Override
    public byte[] getCompiledTable() {
        return compiledTable;
    }

    @Override
    public byte[] getItemDataPalette() {
        return itemDataPalette;
    }

    private static class TableEntry {
        private int id;
        private int data;
        private String name;
    }
}

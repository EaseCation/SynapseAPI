package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.Server;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.BinaryStream;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AdvancedGlobalBlockPalette {

    private static final Map<AbstractProtocol, AdvancedGlobalBlockPalette[]> palettes = new HashMap<AbstractProtocol, AdvancedGlobalBlockPalette[]>(){{
        put(AbstractProtocol.PROTOCOL_16, new AdvancedGlobalBlockPalette[]{
                new AdvancedGlobalBlockPalette(AbstractProtocol.PROTOCOL_16, "block_state_list_16.json"),
                new AdvancedGlobalBlockPalette(AbstractProtocol.PROTOCOL_16, "block_state_list_16_netease.json")
        });
        put(AbstractProtocol.PROTOCOL_17, new AdvancedGlobalBlockPalette[]{
                new AdvancedGlobalBlockPalette(AbstractProtocol.PROTOCOL_17, "block_state_list_17.json"),
                new AdvancedGlobalBlockPalette(AbstractProtocol.PROTOCOL_17, "block_state_list_17_netease.json")
        });
        put(AbstractProtocol.PROTOCOL_18, new AdvancedGlobalBlockPalette[]{
                new AdvancedGlobalBlockPalette(AbstractProtocol.PROTOCOL_18, "block_state_list_18.json"),
                new AdvancedGlobalBlockPalette(AbstractProtocol.PROTOCOL_18, "block_state_list_18_netease.json")
        });
        put(AbstractProtocol.PROTOCOL_19, new AdvancedGlobalBlockPalette[]{
                new AdvancedGlobalBlockPalette(AbstractProtocol.PROTOCOL_19, "block_state_list_19.json"),
                new AdvancedGlobalBlockPalette(AbstractProtocol.PROTOCOL_19, "block_state_list_19_netease.json")
        });
        put(AbstractProtocol.PROTOCOL_110, new AdvancedGlobalBlockPalette[]{
                new AdvancedGlobalBlockPalette(AbstractProtocol.PROTOCOL_110, "block_state_list_110.json")
        });
        put(AbstractProtocol.PROTOCOL_111, new AdvancedGlobalBlockPalette[]{
                new AdvancedGlobalBlockPalette(AbstractProtocol.PROTOCOL_111, "block_state_list_111.json"),
                new AdvancedGlobalBlockPalette(AbstractProtocol.PROTOCOL_111, "block_state_list_111_netease.json")
        });
        put(AbstractProtocol.PROTOCOL_112, new AdvancedGlobalBlockPalette[]{
                new AdvancedGlobalBlockPalette(AbstractProtocol.PROTOCOL_112, "block_state_list_112.json", "runtime_item_ids_112.json")
        });
        put(AbstractProtocol.PROTOCOL_113, new AdvancedGlobalBlockPalette[]{
                new AdvancedGlobalBlockPalette(AbstractProtocol.PROTOCOL_113, "block_state_list_113.dat", "runtime_item_ids_112.json")
        });
    }};

    private final Int2IntArrayMap legacyToRuntimeId = new Int2IntArrayMap();
    private final Int2IntArrayMap runtimeIdToLegacy = new Int2IntArrayMap();
    private final AtomicInteger runtimeIdAllocator = new AtomicInteger(0);
    private final byte[] compiledTable;
    private final byte[] itemDataPalette;

    public AdvancedGlobalBlockPalette(AbstractProtocol protocol, String blockPaletteFile) {
        this(protocol, blockPaletteFile, null);
    }

    public AdvancedGlobalBlockPalette(AbstractProtocol protocol, String blockPaletteFile, String itemDataPaletteJsonFile) {
        Server.getInstance().getLogger().info("Loading Advanced Global Block Palette from " + blockPaletteFile);
        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);

        if (blockPaletteFile.endsWith(".dat")) {
            compiledTable = loadBlockPaletteNBT(protocol, blockPaletteFile);
        } else {
            compiledTable = loadBlockPaletteJson(protocol, blockPaletteFile);
        }

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
            registerMapping((entry.id << 4) | entry.data);
            table.putString(entry.name);
            table.putLShort(entry.data);
            if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_112.ordinal()) table.putLShort(entry.id);
        }
        return table.getBuffer();
    }

    private byte[] loadBlockPaletteNBT(AbstractProtocol protocol, String file) {
        byte[] BLOCK_PALETTE;

        InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(file);
        if (stream == null) {
            throw new AssertionError("Unable to locate block state nbt");
        }
        ListTag<CompoundTag> tag;
        try {
            //noinspection UnstableApiUsage
            BLOCK_PALETTE = ByteStreams.toByteArray(stream);
            //noinspection unchecked
            tag = (ListTag<CompoundTag>) NBTIO.readNetwork(new ByteArrayInputStream(BLOCK_PALETTE));
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        for (CompoundTag state : tag.getAll()) {
            int runtimeId = runtimeIdAllocator.getAndIncrement();
            if (!state.contains("meta")) continue;

            int id = state.getShort("id");
            int[] meta = state.getIntArray("meta");

            // Resolve to first legacy id
            runtimeIdToLegacy.put(runtimeId, id << 6 | meta[0]);
            for (int val : meta) {
                int legacyId = id << 6 | val;
                legacyToRuntimeId.put(legacyId, runtimeId);
            }
            //state.remove("meta"); // No point in sending this since the client doesn't use it.
        }

        return BLOCK_PALETTE;
    }

    private byte[] loadItemDataPalette(String jsonFile) {
        if (jsonFile == null || jsonFile.isEmpty()) return new byte[0];
        InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(jsonFile);
        if (stream == null) {
            throw new AssertionError("Unable to locate RuntimeID table: " + jsonFile);
        }
        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<ItemData>>() {
        }.getType();
        Collection<ItemData> entries = gson.fromJson(reader, collectionType);
        BinaryStream paletteBuffer = new BinaryStream();

        paletteBuffer.putUnsignedVarInt(entries.size());

        for (ItemData data : entries) {
            paletteBuffer.putString(data.name);
            paletteBuffer.putLShort(data.id);
        }

        return paletteBuffer.getBuffer();
    }

    private static class ItemData {
        private String name;
        private int id;
    }

    private int getOrCreateRuntimeId0(int id, int meta) {
        return getOrCreateRuntimeId0((id << 4) | meta);
    }

    private int getOrCreateRuntimeId0(int legacyId) {
        int runtimeId = legacyToRuntimeId.get(legacyId);
        if (runtimeId == -1) {
            //runtimeId = registerMapping(runtimeIdAllocator.incrementAndGet(), legacyId);
            throw new RuntimeException("Unmapped block registered");
        }
        return runtimeId;
    }

    public int getOrCreateRuntimeIdNew(int id, int meta) {
        int legacyId = id << 6 | meta;
        int runtimeId = legacyToRuntimeId.get(legacyId);
        if (runtimeId == -1) {
            //runtimeId = registerMapping(runtimeIdAllocator.incrementAndGet(), legacyId);
            throw new RuntimeException("Unmapped block registered");
        }
        return runtimeId;
    }

    public int getOrCreateRuntimeIdNew(int legacyId)  {
        return getOrCreateRuntimeIdNew(legacyId >> 4, legacyId & 0xf);
    }

    private int registerMapping(int legacyId) {
        int runtimeId = runtimeIdAllocator.getAndIncrement();
        runtimeIdToLegacy.put(runtimeId, legacyId);
        legacyToRuntimeId.put(legacyId, runtimeId);
        return runtimeId;
    }

    public byte[] getCompiledTable0() {
        return compiledTable;
    }

    private static class TableEntry {
        private int id;
        private int data;
        private String name;
    }

    public static int getOrCreateRuntimeId(AbstractProtocol protocol, boolean netease, int legacyId) {
        if (palettes.containsKey(protocol)) {
            AdvancedGlobalBlockPalette[] versions = palettes.get(protocol);
            if (versions.length > 1) {
                if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_113.ordinal()) {
                    return netease ? versions[1].getOrCreateRuntimeIdNew(legacyId) : versions[0].getOrCreateRuntimeIdNew(legacyId);
                } else {
                    return netease ? versions[1].getOrCreateRuntimeId0(legacyId) : versions[0].getOrCreateRuntimeId0(legacyId);
                }
            }
            return versions[0].getOrCreateRuntimeId0(legacyId);
        } else {
            throw new RuntimeException("Advanced global block palette protocol " + protocol.name() + " not found");
        }
    }

    public static int getOrCreateRuntimeId(AbstractProtocol protocol, boolean netease, int id, int meta) {
        return getOrCreateRuntimeId(protocol, netease, (id << 4) | meta);
    }

    public static byte[] getCompiledTable(AbstractProtocol protocol, boolean netease) {
        if (palettes.containsKey(protocol)) {
            AdvancedGlobalBlockPalette[] versions = palettes.get(protocol);
            if (versions.length > 1) {
                return netease ? versions[1].getCompiledTable0() : versions[0].getCompiledTable0();
            }
            return versions[0].getCompiledTable0();
        } else {
            throw new RuntimeException("Advanced global block palette protocol " + protocol.name() + " not found");
        }
    }

    public static byte[] getCompiledItemDataPalette(AbstractProtocol protocol, boolean netease) {
        if (palettes.containsKey(protocol)) {
            AdvancedGlobalBlockPalette[] versions = palettes.get(protocol);
            if (versions.length > 1) {
                return netease ? versions[1].itemDataPalette : versions[0].itemDataPalette;
            }
            return versions[0].itemDataPalette;
        } else {
            throw new RuntimeException("Item data palette protocol " + protocol.name() + " not found");
        }
    }
}

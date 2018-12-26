package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.Server;
import cn.nukkit.utils.BinaryStream;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

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
                new AdvancedGlobalBlockPalette("block_state_list_16.json"),
                new AdvancedGlobalBlockPalette("block_state_list_16_netease.json")
        });
        put(AbstractProtocol.PROTOCOL_17, new AdvancedGlobalBlockPalette[]{
                new AdvancedGlobalBlockPalette("block_state_list_17.json"),
                new AdvancedGlobalBlockPalette("block_state_list_17_netease.json")
        });
        put(AbstractProtocol.PROTOCOL_18, new AdvancedGlobalBlockPalette[]{
                new AdvancedGlobalBlockPalette("block_state_list_18.json")
        });
    }};

    private final Int2IntArrayMap legacyToRuntimeId = new Int2IntArrayMap();
    private final Int2IntArrayMap runtimeIdToLegacy = new Int2IntArrayMap();
    private final AtomicInteger runtimeIdAllocator = new AtomicInteger(0);
    private final byte[] compiledTable;

    public AdvancedGlobalBlockPalette(String jsonFile) {
        Server.getInstance().getLogger().info("Loading Advanced Global Block Palette from " + jsonFile);
        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);

        InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(jsonFile);
        if (stream == null) {
            throw new AssertionError("Unable to locate RuntimeID table");
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
        }

        compiledTable = table.getBuffer();
    }

    public static int getOrCreateRuntimeId(AbstractProtocol protocol, boolean netease, int legacyId) {
        if (palettes.containsKey(protocol)) {
            AdvancedGlobalBlockPalette[] versions = palettes.get(protocol);
            if (versions.length > 1) {
                return netease ? versions[1].getOrCreateRuntimeId0(legacyId) : versions[0].getOrCreateRuntimeId0(legacyId);
            }
            return versions[0].getOrCreateRuntimeId0(legacyId);
        } else {
            throw new RuntimeException("Advanced global block palette protocol " + protocol.name() + " not found");
        }
    }

    public static int getOrCreateRuntimeId(AbstractProtocol protocol, boolean netease, int id, int meta) {
        return getOrCreateRuntimeId(protocol, netease, (id << 4) | meta);
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
}

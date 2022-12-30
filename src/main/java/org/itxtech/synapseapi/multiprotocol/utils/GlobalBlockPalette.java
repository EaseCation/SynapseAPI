package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.utils.MainLogger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.itxtech.synapseapi.SynapseAPI;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalBlockPalette {
    private static final Int2IntMap legacyToRuntimeId = new Int2IntOpenHashMap();
    private static final Int2IntMap runtimeIdToLegacy = new Int2IntOpenHashMap();
    private static final AtomicInteger runtimeIdAllocator = new AtomicInteger(0);
    private static final boolean isNetEase;

    static {
        isNetEase = SynapseAPI.getInstance().getConfig().getBoolean("netease", false);
        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);

        Gson gson = new Gson();
        //Reader reader = new InputStreamReader(SynapseAPI.class.getClassLoader().getResourceAsStream(isNetEase ? "runtime_id_table_15_netease.json" : "runtime_id_table_15.json"), StandardCharsets.UTF_8);
        Reader reader = new InputStreamReader(SynapseAPI.class.getClassLoader().getResourceAsStream("runtimeid_table_15_netease.json"), StandardCharsets.UTF_8);
        Type collectionType = new TypeToken<Collection<TableEntry>>(){}.getType();
        Collection<TableEntry> entries = gson.fromJson(reader, collectionType);

        for (TableEntry entry : entries) {
            registerMapping(entry.runtimeID, (entry.id << 4) | entry.data);
        }

    }

    public static int getOrCreateRuntimeId(int id, int meta) {
        return getOrCreateRuntimeId((id << 4) | meta);
    }

    public static int getOrCreateRuntimeId(int legacyId) {
        int runtimeId = legacyToRuntimeId.get(legacyId);
        if (runtimeId == -1) {
            runtimeId = registerMapping(runtimeIdAllocator.incrementAndGet(), legacyId);
            MainLogger.getLogger().warning("Unmapped block registered. May not be recognised client-side");
        }
        return runtimeId;
    }

    private static int registerMapping(int runtimeId, int legacyId) {
        runtimeIdToLegacy.put(runtimeId, legacyId);
        legacyToRuntimeId.put(legacyId, runtimeId);
        runtimeIdAllocator.set(Math.max(runtimeIdAllocator.get(), runtimeId));
        return runtimeId;
    }

    public static int getLegacyId(int runtimeId) {
        return runtimeIdToLegacy.get(runtimeId);
    }

    private static class TableEntry {
        private int id;
        private int data;
        private int runtimeID;
        private String name;
    }
}

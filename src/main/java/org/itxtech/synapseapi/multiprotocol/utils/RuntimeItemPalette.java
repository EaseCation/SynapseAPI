package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.item.Item;
import cn.nukkit.item.RuntimeItemPaletteInterface.Entry;
import cn.nukkit.utils.BinaryStream;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.itxtech.synapseapi.SynapseAPI;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RuntimeItemPalette implements AdvancedRuntimeItemPaletteInterface {

    private static final Gson GSON = new Gson();
    private static final Type ENTRY_TYPE = new TypeToken<ArrayList<Entry>>(){}.getType();

    private final Int2IntMap legacyNetworkMap = new Int2IntOpenHashMap();
    private final Int2ObjectMap<String> legacyStringMap = new Int2ObjectOpenHashMap<>();
    private final Int2IntMap networkLegacyMap = new Int2IntOpenHashMap();

    private final byte[] itemDataPalette;

    public RuntimeItemPalette(String runtimeItemIdJsonFile) {
        List<Entry> entries;
        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(runtimeItemIdJsonFile);
             InputStreamReader reader = new InputStreamReader(stream)) {
            entries = GSON.fromJson(reader, ENTRY_TYPE);
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load runtime item palette", e);
        }

        BinaryStream paletteBuffer = new BinaryStream();
        paletteBuffer.putUnsignedVarInt(entries.size());

        legacyNetworkMap.defaultReturnValue(-1);
        networkLegacyMap.defaultReturnValue(-1);

        for (Entry entry : entries) {
            paletteBuffer.putString(entry.name);
            paletteBuffer.putLShort(entry.id);
            paletteBuffer.putBoolean(false); // Component item

            if (entry.oldId != null) {
                boolean hasData = entry.oldData != null;
                int fullId = getFullId(entry.oldId, hasData ? entry.oldData : 0);
                legacyNetworkMap.put(fullId, (entry.id << 1) | (hasData ? 1 : 0));
                legacyStringMap.put(fullId, entry.name);
                networkLegacyMap.put(entry.id, fullId | (hasData ? 1 : 0));
            }
        }

        itemDataPalette = paletteBuffer.getBuffer();
    }

    @Override
    public int getNetworkFullId(Item item) {
        int fullId = getFullId(item.getId(), item.hasMeta() ? item.getDamage() : -1);
        int networkId = legacyNetworkMap.get(fullId);
        if (networkId == -1) {
            networkId = legacyNetworkMap.get(getFullId(item.getId(), 0));
        }
        if (networkId == -1) {
            throw new IllegalArgumentException("Unknown item mapping " + item.getId() + ":" + item.getDamage());
        }

        return networkId;
    }

    public String getString(Item item) {
        int fullId = getFullId(item.getId(), item.hasMeta() ? item.getDamage() : -1);
        if (!legacyStringMap.containsKey(fullId)) {
            throw new IllegalArgumentException("Unknown item mapping " + item.getId() + ":" + item.getDamage());
        }
        return legacyStringMap.get(fullId);
    }

    @Override
    public int getLegacyFullId(int networkId) {
        int fullId = networkLegacyMap.get(networkId);
        if (fullId == -1) {
            throw new IllegalArgumentException("Unknown network mapping: " + networkId);
        }
        return fullId;
    }

    @Override
    public int getId(int fullId) {
        return (short) (fullId >> 16);
    }

    @Override
    public int getData(int fullId) {
        return ((fullId >> 1) & 0x7fff);
    }

    private static int getFullId(int id, int data) {
        return (((short) id) << 16) | ((data & 0x7fff) << 1);
    }

    @Override
    public int getNetworkId(int networkFullId) {
        return networkFullId >> 1;
    }

    @Override
    public boolean hasData(int id) {
        return (id & 0x1) != 0;
    }

    @Override
    public byte[] getCompiledData() {
        return this.itemDataPalette;
    }
}

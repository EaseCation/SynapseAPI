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
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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

    private final List<Entry> entries = new ArrayList<>();
    private final Int2IntMap legacyNetworkMap = new Int2IntOpenHashMap();
    private final Int2ObjectMap<String> legacyStringMap = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<String> nameToLegacy = new Object2IntOpenHashMap<>();
    private final Int2IntMap networkLegacyMap = new Int2IntOpenHashMap();

    private byte[] itemDataPalette;

    public RuntimeItemPalette(String runtimeItemIdJsonFile) {
        List<Entry> entries;
        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(runtimeItemIdJsonFile);
             InputStreamReader reader = new InputStreamReader(stream)) {
            entries = GSON.fromJson(reader, ENTRY_TYPE);
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load runtime item palette", e);
        }

        legacyNetworkMap.defaultReturnValue(-1);
        nameToLegacy.defaultReturnValue(-1);
        networkLegacyMap.defaultReturnValue(-1);

        for (Entry entry : entries) {
            registerItem(entry);
        }

        this.buildPaletteBuffer();
    }

    public void registerItem(Entry entry) {
        entries.add(entry);
        if (entry.oldId != null) {
            boolean hasData = entry.oldData != null;
            int fullId = getFullId(entry.oldId, hasData ? entry.oldData : 0);
            legacyNetworkMap.put(fullId, (entry.id << 1) | (hasData ? 1 : 0));
            legacyStringMap.put(fullId, entry.name);
            nameToLegacy.put(entry.name, fullId | (hasData ? 1 : 0));
            networkLegacyMap.put(entry.id, fullId | (hasData ? 1 : 0));
        }
    }

    public void buildPaletteBuffer() {
        BinaryStream paletteBuffer = new BinaryStream();
        paletteBuffer.putUnsignedVarInt(entries.size());
        for (Entry entry : entries) {
            paletteBuffer.putString(entry.name);
            paletteBuffer.putLShort(entry.id);
            paletteBuffer.putBoolean(false); // Component item
        }
        itemDataPalette = paletteBuffer.getBuffer();
    }

    @Override
    public int getNetworkFullId(Item item) {
        int id = item.getId();
        if (id < 0) {
            return id;
        }
        int meta = item.getDamage();
        int fullId = getFullId(id, item.hasMeta() ? meta : -1);
        int networkId = legacyNetworkMap.get(fullId);
        if (networkId == -1) {
            networkId = legacyNetworkMap.get(getFullId(id, 0));
        }
        if (networkId == -1) {
            throw new IllegalArgumentException("Unknown item mapping " + id + ":" + meta);
        }

        return networkId;
    }

    @Override
    public String getString(Item item) {
        int fullId = getFullId(item.getId(), item.hasMeta() ? item.getDamage() : -1);
        String name = legacyStringMap.get(fullId);
        if (name == null) {
            throw new IllegalArgumentException("Unknown item mapping " + item.getId() + ":" + item.getDamage());
        }
        return name;
    }

    @Override
    public int getLegacyFullIdByName(String name) {
        return nameToLegacy.getInt(name);
    }

    @Override
    public int getLegacyFullId(int networkId) {
        if (networkId < 0) {
            return networkId;
        }
        int fullId = networkLegacyMap.get(networkId);
        if (fullId == -1) {
            throw new IllegalArgumentException("Unknown network mapping: " + networkId);
        }
        return fullId;
    }

    @Override
    public int getId(int fullId) {
        if (fullId < 0) {
            return fullId;
        }
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
        if (networkFullId < 0) {
            return networkFullId;
        }
        return networkFullId >> 1;
    }

    @Override
    public boolean hasData(int id) {
        if (id < 0) {
            return false;
        }
        return (id & 0x1) != 0;
    }

    @Override
    public byte[] getCompiledData() {
        return this.itemDataPalette;
    }
}

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
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.utils.item.LegacyItemSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class RuntimeItemPalette implements AdvancedRuntimeItemPaletteInterface {

    private static final Gson GSON = new Gson();
    private static final Type ENTRY_TYPE = new TypeToken<ArrayList<Entry>>(){}.getType();

    private final String fileName;

    private final List<Entry> entries = new ArrayList<>();
    private final Int2IntMap legacyNetworkMap = new Int2IntOpenHashMap();
    private final Int2ObjectMap<String> legacyStringMap = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<String> nameToLegacy = new Object2IntOpenHashMap<>();
    private final Object2IntMap<String> nameToNetworkMap = new Object2IntOpenHashMap<>();
    private final Int2IntMap networkLegacyMap = new Int2IntOpenHashMap();
    private final Int2IntMap blockLegacyToFlatten = new Int2IntOpenHashMap();
//    private final Int2IntMap blockFlattenToLegacy = new Int2IntOpenHashMap();

    private byte[] itemDataPalette;

    public RuntimeItemPalette(String runtimeItemIdJsonFile) {
        this.fileName = runtimeItemIdJsonFile;

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
        blockLegacyToFlatten.defaultReturnValue(-1);
        nameToNetworkMap.defaultReturnValue(-1);
//        blockFlattenToLegacy.defaultReturnValue(-1);

        for (Entry entry : entries) {
            registerItem(entry);
        }

        this.buildNetworkCache();
    }

    @Override
    public void registerItem(Entry entry) {
        int oldId;
        if (entry.oldId == null) {
            int fullId = LegacyItemSerializer.getInternalMapping().getInt(entry.name);
            if ((fullId & 0xffff) == 0xffff) {
                oldId = -1;
            } else {
                oldId = fullId >> 16;
            }

            if (oldId == -1) {
                entries.add(entry);
                log.trace("Unmapped runtime item: id {} name {} ({})", entry.id, entry.name, fileName);
                return;
            }

            entry = new Entry(entry.name, entry.id, oldId, entry.oldData, entry.component);
        } else {
            oldId = entry.oldId;
        }
        entries.add(entry);

        boolean hasData = entry.oldData != null;
        int fullId = getFullId(oldId, hasData ? entry.oldData : 0);
        legacyNetworkMap.put(fullId, (entry.id << 1) | (hasData ? 1 : 0));
        legacyStringMap.put(fullId, entry.name);
        nameToLegacy.put(entry.name, fullId | (hasData ? 1 : 0));
        networkLegacyMap.put(entry.id, fullId | (hasData ? 1 : 0));
        nameToNetworkMap.put(entry.name, entry.id);
        if (oldId < 0 && oldId != entry.id) {
            Integer meta = entry.oldData;
            int legacyId = (oldId << 16) | (meta != null ? meta : 0);
            int newId = entry.id;
            blockLegacyToFlatten.put(legacyId, newId);
//            blockFlattenToLegacy.put(newId, legacyId);
        }
    }

    @Override
    public void buildNetworkCache() {
        BinaryStream stream = new BinaryStream();
        stream.putUnsignedVarInt(entries.size());
        for (Entry entry : entries) {
            stream.putString(entry.name);
            stream.putLShort(entry.id);
            stream.putBoolean(entry.component);
        }
        itemDataPalette = stream.getBuffer();
    }

    @Override
    public int getNetworkFullId(Item item) {
        int id = item.getId();
        int meta = item.getDamage();

        if (id < 0) {
            return blockLegacyToFlatten.getOrDefault((id << 16) | meta, id);
        }

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
    public int getNetworkIdByName(String name) {
        return nameToNetworkMap.getInt(name);
    }

    @Override
    public int getLegacyFullId(int networkId) {
        if (networkId < 0) {
//            return blockFlattenToLegacy.getOrDefault(networkId, networkId); //TODO
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

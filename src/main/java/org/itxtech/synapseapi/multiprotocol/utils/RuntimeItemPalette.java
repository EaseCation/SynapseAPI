package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemRuntimeID;
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
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol116100.BinaryStreamHelper116100;
import org.itxtech.synapseapi.multiprotocol.utils.item.LegacyItemSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class RuntimeItemPalette implements AdvancedRuntimeItemPaletteInterface {

    private static final Gson GSON = new Gson();

    private static final Object2IntMap<String> LEGACY_ITEM_ID_MAP;

    static {
        try (InputStreamReader reader = new InputStreamReader(SynapseAPI.class.getClassLoader().getResourceAsStream("item_id_map_116.json"))) {
            LEGACY_ITEM_ID_MAP = GSON.fromJson(reader, new TypeToken<Object2IntOpenHashMap<String>>(){}.getType());
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load item_id_map_116.json", e);
        }
        LEGACY_ITEM_ID_MAP.defaultReturnValue(-1);

        LEGACY_ITEM_ID_MAP.put("minecraft:chest_boat", ItemRuntimeID.CHEST_BOAT); //HACK
    }

    private final AbstractProtocol protocol;

    private final List<Entry> entries = new ArrayList<>();
    private final Int2IntMap legacyNetworkMap = new Int2IntOpenHashMap();
    private final Int2ObjectMap<String> legacyStringMap = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<String> nameToLegacy = new Object2IntOpenHashMap<>();
    private final Object2IntMap<String> nameToNetworkMap = new Object2IntOpenHashMap<>();
    private final Int2IntMap networkLegacyMap = new Int2IntOpenHashMap();
    private final Int2IntMap blockLegacyToFlatten = new Int2IntOpenHashMap();
//    private final Int2IntMap blockFlattenToLegacy = new Int2IntOpenHashMap();
    private final Object2IntMap<String> nameToNetTodoMap = new Object2IntOpenHashMap<>();

    private byte[] itemDataPalette;

    public RuntimeItemPalette(AbstractProtocol protocol, String runtimeItemIdJsonFile) {
        this(protocol, runtimeItemIdJsonFile, false);
    }

    public RuntimeItemPalette(AbstractProtocol protocol, String runtimeItemIdJsonFile, boolean microsoftToNetease) {
        this.protocol = protocol;

        List<Entry> entries;
        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(runtimeItemIdJsonFile);
             InputStreamReader reader = new InputStreamReader(stream)) {
            entries = GSON.fromJson(reader, new TypeToken<ArrayList<Entry>>(){}.getType());
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load runtime item palette", e);
        }

        legacyNetworkMap.defaultReturnValue(-1);
        nameToLegacy.defaultReturnValue(-1);
        networkLegacyMap.defaultReturnValue(-1);
        blockLegacyToFlatten.defaultReturnValue(-1);
        nameToNetworkMap.defaultReturnValue(-1);
//        blockFlattenToLegacy.defaultReturnValue(-1);
        nameToNetTodoMap.defaultReturnValue(-1);

        for (Entry entry : entries) {
            registerItem(entry);
        }

        if (microsoftToNetease) {
            registerItem(new Entry("minecraft:mod_ore", 230, 230, null));
            registerItem(new Entry("minecraft:micro_block", -9735, -9735, null));
            registerItem(new Entry("minecraft:mod_armor", 1996, null, null));
            registerItem(new Entry("minecraft:mod", 1997, null, null));
            registerItem(new Entry("minecraft:mod_ex", 1998, null, null));
            registerItem(new Entry("minecraft:debug_stick", 1999, null, null));
        }

        this.buildNetworkCache();
    }

    public RuntimeItemPalette(AbstractProtocol protocol, String runtimeItemIdJsonFile, String legacyItemIdJsonFile, String itemFlattenJsonFile) {
        this.protocol = protocol;

        List<RuntimeEntry> runtimeIds;
        try (InputStreamReader reader = new InputStreamReader(SynapseAPI.class.getClassLoader().getResourceAsStream(runtimeItemIdJsonFile))) {
            runtimeIds = GSON.fromJson(reader, new TypeToken<ArrayList<RuntimeEntry>>(){}.getType());
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load runtime item map", e);
        }

        Object2IntMap<String> legacyIds;
        try (InputStreamReader reader = new InputStreamReader(SynapseAPI.class.getClassLoader().getResourceAsStream(legacyItemIdJsonFile))) {
            legacyIds = GSON.fromJson(reader, new TypeToken<Object2IntOpenHashMap<String>>(){}.getType());
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load legacy item id map", e);
        }
        legacyIds.defaultReturnValue(-1);

        Map<String, Map<String, String>> itemFlattenMap;
        try (InputStreamReader reader = new InputStreamReader(SynapseAPI.class.getClassLoader().getResourceAsStream(itemFlattenJsonFile))) {
            itemFlattenMap = GSON.fromJson(reader, new TypeToken<HashMap<String, HashMap<String, String>>>(){}.getType());
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load item flatten map", e);
        }
        Object2IntMap<String> itemFlattens = new Object2IntOpenHashMap<>();
        itemFlattens.defaultReturnValue(-1);
        itemFlattenMap.forEach((legacyName, metaMap) -> metaMap.forEach((meta, newName) -> {
            int legacyId = LEGACY_ITEM_ID_MAP.getInt(legacyName);
            if (legacyId == -1) {
                log.error("Unmapped runtime item: name '{}' legacy name '{}' ({})", newName, legacyName, itemFlattenJsonFile);
                return;
            }
            itemFlattens.put(newName, Item.getFullId(legacyId, Integer.parseInt(meta)));
        }));

        legacyNetworkMap.defaultReturnValue(-1);
        nameToLegacy.defaultReturnValue(-1);
        networkLegacyMap.defaultReturnValue(-1);
        blockLegacyToFlatten.defaultReturnValue(-1);
        nameToNetworkMap.defaultReturnValue(-1);
//        blockFlattenToLegacy.defaultReturnValue(-1);
        nameToNetTodoMap.defaultReturnValue(-1);

        for (RuntimeEntry entry : runtimeIds) {
            Integer oldId;
            Integer oldData;
            String name = entry.name;
            int runtimeId = entry.id;
            if (runtimeId < 256) {
                oldId = runtimeId;
                oldData = 0;
            } else {
                int legacyId = legacyIds.getInt(name);
                if (legacyId != -1) {
                    oldId = legacyId;
                    oldData = null;
                } else {
                    int itemFullId = itemFlattens.getInt(name);
                    if (itemFullId != -1) {
                        oldId = Item.getIdFromFullId(itemFullId);
                        oldData = Item.getMetaFromFullId(itemFullId);
                    } else {
                        oldId = null;
                        oldData = null;
                        log.trace("Unmapped runtime item: name '{}' runtimeId '{}' ({})", runtimeId, name, protocol);
                    }
                }
            }
            registerItem(new Entry(name, runtimeId, oldId, oldData));
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
                nameToNetTodoMap.put(entry.name, entry.id);
                log.trace("Unmapped runtime item: id {} name {} ({})", entry.id, entry.name, protocol);
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
        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_120_50.getProtocolStart()) {
            for (Entry entry : entries) {
                stream.putString(entry.name);
                stream.putLShort(entry.id);
                stream.putBoolean(entry.component);
            }
        } else {
            for (Entry entry : entries) {
                stream.putString(entry.name);
                stream.putLShort(BinaryStreamHelper116100.convertCustomBlockItemServerIdToClientId(entry.id));
                stream.putBoolean(entry.component);
            }
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
    public int getNetworkIdByNameTodo(String name) {
        return nameToNetTodoMap.getInt(name);
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

    private record RuntimeEntry(String name, int id) {
    }
}

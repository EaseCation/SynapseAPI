package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.utils.BinaryStream;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Log4j2
public final class CreativeItemUtil {
    static final Object2IntMap<String> ITEM_NAME_TO_ID;

    static final Map<String, ObjectIntPair<String>> FLATTENED_TO_LEGACY = new Object2ObjectOpenHashMap<>();

    static {
        log.debug("Loading creative item data...");
        Gson gson = new Gson();

        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(ByteStreams.toByteArray(SynapseAPI.class.getClassLoader().getResourceAsStream("item_id_map_12050.json"))))) {
            ITEM_NAME_TO_ID = gson.fromJson(reader, new TypeToken<Object2IntOpenHashMap<String>>(){});
            ITEM_NAME_TO_ID.defaultReturnValue(Item.AIR);
        } catch (Exception e) {
            throw new AssertionError("Unable to load item_id_map.json", e);
        }

        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(ByteStreams.toByteArray(SynapseAPI.class.getClassLoader().getResourceAsStream("item_flatten_map_12050.json"))))) {
            gson.fromJson(reader, JsonObject.class).entrySet().forEach(entry -> {
                String legacyName = entry.getKey();

                List<String> metaToNewName = new ObjectArrayList<>();
                entry.getValue().getAsJsonObject().entrySet().forEach(pair -> {
                    int legacyMeta = Integer.parseInt(pair.getKey());
                    String newName = pair.getValue().getAsString();

                    while (metaToNewName.size() <= legacyMeta) {
                        metaToNewName.add(null);
                    }
                    metaToNewName.set(legacyMeta, newName);

                    ObjectIntPair<String> existed = FLATTENED_TO_LEGACY.get(newName);
                    if (existed != null && existed.rightInt() > legacyMeta) {
                        return;
                    }

                    FLATTENED_TO_LEGACY.put(newName, ObjectIntPair.of(legacyName, legacyMeta));
                });
            });
        } catch (Exception e) {
            throw new AssertionError("Unable to load item_flatten_map.json", e);
        }

        ITEM_NAME_TO_ID.put("minecraft:air", ItemID.AIR);

        for (Entry<String> entry : LegacyItemSerializer.getInternalMapping().object2IntEntrySet()) {
            Integer fullId = entry.getIntValue();
            if ((fullId & 0xffff) == 0xffff) {
                continue;
            }
            ITEM_NAME_TO_ID.putIfAbsent(entry.getKey(), fullId >> 16);
        }
    }

    @Nullable
    public static Item deserializeItem(JsonObject itemEntry) {
        String name = itemEntry.get("name").getAsString();
        int meta = itemEntry.has("meta") ? itemEntry.get("meta").getAsInt() : 0;
        int count = itemEntry.has("count") ? itemEntry.get("count").getAsInt() : 1;
        int blockRuntimeId = itemEntry.has("blockNetId") ? itemEntry.get("blockNetId").getAsInt() : -1;
        String nbt = itemEntry.has("nbt") ? itemEntry.get("nbt").getAsString() : null;

        if (blockRuntimeId != -1) {
            BinaryStream stream = new BinaryStream();
            stream.setHelper(AbstractProtocol.PROTOCOL_121_60.getHelper());
            stream.putVarInt(ITEM_NAME_TO_ID.getInt(name));
            stream.putLShort(count);
            stream.putUnsignedVarInt(0);
            stream.putVarInt(blockRuntimeId);
            stream.putByteArray(new byte[]{
                    0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0,
            });
            Item networkItem = stream.getItemInstance();
            if (nbt != null) {
                networkItem.setCompoundTag(Base64.getDecoder().decode(nbt));
            }
            return networkItem;
        }

        int id;
        ObjectIntPair<String> legacy = FLATTENED_TO_LEGACY.get(name);
        if (legacy != null) {
            id = ITEM_NAME_TO_ID.getOrDefault(legacy.left(), Integer.MIN_VALUE);
            meta = legacy.rightInt();
        } else {
            id = ITEM_NAME_TO_ID.getOrDefault(name, Integer.MIN_VALUE);
        }

        if (id == Integer.MIN_VALUE) {
            int networkLegacyFullId = AdvancedRuntimeItemPalette.getLegacyFullIdByName(AbstractProtocol.PROTOCOL_121_60, false, name);
            if (networkLegacyFullId != -1) {
                id = AdvancedRuntimeItemPalette.getId(AbstractProtocol.PROTOCOL_121_60, false, networkLegacyFullId);
                if (AdvancedRuntimeItemPalette.hasData(AbstractProtocol.PROTOCOL_121_60, false, networkLegacyFullId)) {
                    meta = AdvancedRuntimeItemPalette.getData(AbstractProtocol.PROTOCOL_121_60, false, networkLegacyFullId);
                }
            } else {
                log.debug("Unknown item name: {}", name);
                return null;
            }
        }
/*
        if (blockRuntimeId != -1) {
            int legacyId = AdvancedGlobalBlockPalette.getLegacyId(AbstractProtocol.PROTOCOL_121_60, false, blockRuntimeId);

            if (legacyId == -1) {
                log.debug("Invalid block runtime ID: {}", blockRuntimeId);
                return null;
            }

            meta = legacyId & 0x3fff;

            assert id == Block.getItemId(legacyId >> 14);
        }
*/
        if (id == Item.BANNER) {
            if ("CgAAAwQAVHlwZQAAAAAA".equals(nbt)) {
                nbt = null;
            }
        }

        return Item.getCraftingItem(id, meta, count, nbt != null ? Base64.getDecoder().decode(nbt) : new byte[0]);
    }

    private CreativeItemUtil() {
        throw new IllegalStateException();
    }
}

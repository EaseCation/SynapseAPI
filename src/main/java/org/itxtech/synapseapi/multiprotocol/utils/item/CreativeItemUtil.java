package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemFullNames;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.Items;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;

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

        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(ByteStreams.toByteArray(SynapseAPI.class.getClassLoader().getResourceAsStream("item_id_map_12150.json"))))) {
            ITEM_NAME_TO_ID = gson.fromJson(reader, new TypeToken<Object2IntOpenHashMap<String>>(){});
            ITEM_NAME_TO_ID.defaultReturnValue(Item.AIR);
        } catch (Exception e) {
            throw new AssertionError("Unable to load item_id_map.json", e);
        }

        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(ByteStreams.toByteArray(SynapseAPI.class.getClassLoader().getResourceAsStream("item_flatten_map_12150.json"))))) {
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

        ITEM_NAME_TO_ID.put(ItemFullNames.AIR, ItemID.AIR);

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

        int id = Items.getIdByName(name);

        if (blockRuntimeId != -1) {
            int legacyId = AdvancedGlobalBlockPalette.getLegacyId(AbstractProtocol.PROTOCOL_121_60, false, blockRuntimeId);

            if (legacyId == -1) {
                log.debug("Invalid block runtime ID: {}", blockRuntimeId);
                return null;
            }

            meta = legacyId & 0x3fff;

            if (meta != 0) {
                log.trace("creative item '{}' has meta: {} - {}", id, meta, itemEntry);
            }

            int expectedId = Block.getItemId(legacyId >> 14);
            if (id != expectedId) {
                log.error("creative item id {} != {} : {}", id, expectedId, itemEntry);
            }
        }

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

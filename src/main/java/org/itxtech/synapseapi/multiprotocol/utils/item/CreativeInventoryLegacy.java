package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol12160.protocol.CreativeContentPacket12160.Entry;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.List;

import static org.itxtech.synapseapi.SynapseSharedConstants.*;

@Log4j2
public final class CreativeInventoryLegacy {
    private static final List<Entry> ITEMS = new ObjectArrayList<>();

    static {
        log.info("Loading Creative Items from creative_items.json (legacy) 1.18.0");

        ITEMS.clear();

        JsonArray items;
        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream("creativeitems_118.json");
             InputStreamReader reader = new InputStreamReader(stream)) {
            items = new Gson().fromJson(reader, JsonArray.class);
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load creative_items.json", e);
        }

        for (JsonElement itemEntry : items) {
            try {
                Item item = deserializeItem(itemEntry.getAsJsonObject());

                if (item == null) {
                    continue;
                }

                if (!ENABLE_CHEMISTRY_FEATURE && item.isChemistryFeature()) {
                    continue;
                }

                ITEMS.add(new Entry(item, CreativeInventoryGrouped.getGroupIndex(item)));
            } catch (Exception e) {
                log.error("Failed to parse creative item", e);
            }
        }
    }

    public static List<Entry> getItems() {
        return ITEMS;
    }

    @Nullable
    public static Item deserializeItem(JsonObject itemEntry) {
        String name = itemEntry.get("name").getAsString();
        int meta = itemEntry.has("meta") ? itemEntry.get("meta").getAsInt() : 0;
        int count = itemEntry.has("count") ? itemEntry.get("count").getAsInt() : 1;
        int blockRuntimeId = itemEntry.has("blockNetId") ? itemEntry.get("blockNetId").getAsInt() : -1;
        String nbt = itemEntry.has("nbt") ? itemEntry.get("nbt").getAsString() : null;

        int id;
        ObjectIntPair<String> legacy = ItemUtil.FLATTENED_TO_LEGACY.get(name);
        if (legacy != null) {
            id = ItemUtil.ITEM_NAME_TO_ID.getOrDefault(legacy.left(), Integer.MIN_VALUE);
            meta = legacy.rightInt();
        } else {
            id = ItemUtil.ITEM_NAME_TO_ID.getOrDefault(name, Integer.MIN_VALUE);
        }

        if (id == Integer.MIN_VALUE) {
            log.debug("Unknown item name: " + name);
            return null;
        }

        if (blockRuntimeId != -1) {
            int legacyId = AdvancedGlobalBlockPalette.getLegacyId(AbstractProtocol.PROTOCOL_118, false, blockRuntimeId);

            if (legacyId == -1) {
                log.debug("Invalid block runtime ID: " + blockRuntimeId);
                return null;
            }

            meta = legacyId & 0x3fff;

            assert id == Block.getItemId(legacyId >> 14);
        }

        return Item.getCraftingItem(id, meta, count, nbt != null ? Base64.getDecoder().decode(nbt) : new byte[0]);
    }

    public static void init() {
    }

    private CreativeInventoryLegacy() {
    }
}

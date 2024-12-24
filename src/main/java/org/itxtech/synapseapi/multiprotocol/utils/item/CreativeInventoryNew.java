package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.item.Item;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.itxtech.synapseapi.SynapseSharedConstants.*;

@Log4j2
public final class CreativeInventoryNew {
    private static final List<Item> ITEMS = new ObjectArrayList<>();

    static {
        log.info("Loading Creative Items from creative_items.json (new) 1.20.10");

        ITEMS.clear();

        JsonArray items;
        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream("creativeitems_12010.json");
             InputStreamReader reader = new InputStreamReader(stream)) {
            items = new Gson().fromJson(reader, JsonArray.class);
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load creative_items.json", e);
        }

        for (JsonElement itemEntry : items) {
            try {
                Item item = ItemUtil.deserializeItem(itemEntry.getAsJsonObject());

                if (item == null) {
                    continue;
                }

                if (!ENABLE_CHEMISTRY_FEATURE && item.isChemistryFeature()) {
                    continue;
                }

                ITEMS.add(item);
            } catch (Exception e) {
                log.error("Failed to parse creative item", e);
            }
        }
    }

    public static List<Item> getItems() {
        return ITEMS;
    }

    public static void init() {
    }

    private CreativeInventoryNew() {
    }
}

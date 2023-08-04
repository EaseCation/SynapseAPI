package org.itxtech.synapseapi.multiprotocol.utils.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Data;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

@Data
public class ItemUpgradeSchema {
    private final Map<String, String> renamedIds;
    private final Map<String, Int2ObjectMap<String>> remappedMetas;

    public ItemUpgradeSchema(JsonObject json) {
        JsonElement renamedIds = json.get("renamedIds");
        if (renamedIds != null) {
            this.renamedIds = new Object2ObjectOpenHashMap<>();

            for (Entry<String, JsonElement> entry : renamedIds.getAsJsonObject().entrySet()) {
                this.renamedIds.put(entry.getKey(), entry.getValue().getAsString());
            }
        } else {
            this.renamedIds = Collections.emptyMap();
        }

        JsonElement remappedMetas = json.get("remappedMetas");
        if (remappedMetas != null) {
            this.remappedMetas = new Object2ObjectOpenHashMap<>();

            for (Entry<String, JsonElement> entry : remappedMetas.getAsJsonObject().entrySet()) {
                Int2ObjectMap<String> metas = this.remappedMetas.get(entry.getKey());
                if (metas == null) {
                    metas = new Int2ObjectOpenHashMap<>();
                    this.remappedMetas.put(entry.getKey(), metas);
                }

                for (Entry<String, JsonElement> pair : entry.getValue().getAsJsonObject().entrySet()) {
                    metas.put(Integer.parseInt(pair.getKey()), pair.getValue().getAsString());
                }
            }
        } else {
            this.remappedMetas = Collections.emptyMap();
        }
    }
}

package org.itxtech.synapseapi.multiprotocol.utils.block;

import cn.nukkit.nbt.tag.ByteTag;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.IntTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.nbt.tag.Tag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Data
public class BlockUpgradeSchema {
    private final int version;

    private final int maxVersionMajor;
    private final int maxVersionMinor;
    private final int maxVersionPatch;
    private final int maxVersionRevision;

    private final Map<String, String> renamedIds;
    private final Map<String, Map<String, Tag>> addedProperties;
    private final Map<String, String[]> removedProperties;
    private final Map<String, Map<String, String>> renamedProperties;
    private final Map<String, Map<String, ValueRemap[]>> remappedPropertyValues;
    private final Map<String, BlockRemap[]> remappedStates;

    public BlockUpgradeSchema(JsonObject json) {
        maxVersionMajor = json.get("maxVersionMajor").getAsInt();
        maxVersionMinor = json.get("maxVersionMinor").getAsInt();
        maxVersionPatch = json.get("maxVersionPatch").getAsInt();
        maxVersionRevision = json.get("maxVersionRevision").getAsInt();
        version = (maxVersionMajor << 24) | (maxVersionMinor << 16) | (maxVersionPatch << 8) | maxVersionRevision;

        JsonElement renamedIds = json.get("renamedIds");
        if (renamedIds != null) {
            this.renamedIds = new Object2ObjectOpenHashMap<>();

            for (Entry<String, JsonElement> entry : renamedIds.getAsJsonObject().entrySet()) {
                this.renamedIds.put(entry.getKey(), entry.getValue().getAsString());
            }
        } else {
            this.renamedIds = Collections.emptyMap();
        }

        JsonElement addedProperties = json.get("addedProperties");
        if (addedProperties != null) {
            this.addedProperties = new Object2ObjectOpenHashMap<>();

            for (Entry<String, JsonElement> entry : addedProperties.getAsJsonObject().entrySet()) {
                Map<String, Tag> states = new Object2ObjectOpenHashMap<>();
                for (Entry<String, JsonElement> state : entry.getValue().getAsJsonObject().entrySet()) {
                    String stateName = state.getKey();
                    states.put(stateName, stateFromJson(state.getValue().getAsJsonObject()).setName(stateName));
                }
                this.addedProperties.put(entry.getKey(), states);
            }
        } else {
            this.addedProperties = Collections.emptyMap();
        }

        JsonElement removedProperties = json.get("removedProperties");
        if (removedProperties != null) {
            this.removedProperties = new Object2ObjectOpenHashMap<>();

            for (Entry<String, JsonElement> entry : removedProperties.getAsJsonObject().entrySet()) {
                JsonArray array = entry.getValue().getAsJsonArray();
                List<String> states = new ObjectArrayList<>(array.size());
                for (JsonElement element : entry.getValue().getAsJsonArray()) {
                    states.add(element.getAsString());
                }
                this.removedProperties.put(entry.getKey(), states.toArray(new String[0]));
            }
        } else {
            this.removedProperties = Collections.emptyMap();
        }

        JsonElement renamedProperties = json.get("renamedProperties");
        if (renamedProperties != null) {
            this.renamedProperties = new Object2ObjectOpenHashMap<>();

            for (Entry<String, JsonElement> entry : renamedProperties.getAsJsonObject().entrySet()) {
                Map<String, String> pairs = new Object2ObjectOpenHashMap<>();
                for (Entry<String, JsonElement> pair : entry.getValue().getAsJsonObject().entrySet()) {
                    pairs.put(pair.getKey(), pair.getValue().getAsString());
                }
                this.renamedProperties.put(entry.getKey(), pairs);
            }
        } else {
            this.renamedProperties = Collections.emptyMap();
        }

        JsonElement remappedPropertyValues = json.get("remappedPropertyValues");
        if (remappedPropertyValues != null) {
            this.remappedPropertyValues = new Object2ObjectOpenHashMap<>();

            JsonObject remappedPropertyValuesIndex = json.get("remappedPropertyValuesIndex").getAsJsonObject();
            for (Entry<String, JsonElement> entry : remappedPropertyValues.getAsJsonObject().entrySet()) {
                Map<String, ValueRemap[]> remap = new Object2ObjectOpenHashMap<>();
                for (Entry<String, JsonElement> palette : entry.getValue().getAsJsonObject().entrySet()) {
                    String stateName = palette.getKey();
                    List<ValueRemap> pairs = new ObjectArrayList<>();
                    for (JsonElement element : remappedPropertyValuesIndex.get(palette.getValue().getAsString()).getAsJsonArray()) {
                        JsonObject obj = element.getAsJsonObject();
                        pairs.add(new ValueRemap(stateFromJson(obj.getAsJsonObject("old")).setName(stateName),
                                stateFromJson(obj.getAsJsonObject("new")).setName(stateName)));
                    }
                    remap.put(stateName, pairs.toArray(new ValueRemap[0]));
                }
                this.remappedPropertyValues.put(entry.getKey(), remap);
            }
        } else {
            this.remappedPropertyValues = Collections.emptyMap();
        }

        JsonElement remappedStates = json.get("remappedStates");
        if (remappedStates != null) {
            this.remappedStates = new Object2ObjectOpenHashMap<>();

            for (Entry<String, JsonElement> entry : remappedStates.getAsJsonObject().entrySet()) {
                JsonArray array = entry.getValue().getAsJsonArray();
                List<BlockRemap> data = new ObjectArrayList<>(array.size());
                for (JsonElement element : entry.getValue().getAsJsonArray()) {
                    JsonObject obj = element.getAsJsonObject();
                    CompoundTag oldStates = new CompoundTag();
                    for (Entry<String, JsonElement> state : obj.get("oldState").getAsJsonObject().entrySet()) {
                        String stateName = state.getKey();
                        oldStates.put(stateName, stateFromJson(state.getValue().getAsJsonObject()).setName(stateName));
                    }

                    String newName = obj.get("newName").getAsString();
                    CompoundTag newStates = new CompoundTag();
                    for (Entry<String, JsonElement> state : obj.get("newState").getAsJsonObject().entrySet()) {
                        String stateName = state.getKey();
                        newStates.put(stateName, stateFromJson(state.getValue().getAsJsonObject()).setName(stateName));
                    }

                    data.add(new BlockRemap(oldStates, newName, newStates));
                }
                this.remappedStates.put(entry.getKey(), data.toArray(new BlockRemap[0]));
            }
        } else {
            this.remappedStates = Collections.emptyMap();
        }
    }

    private static Tag stateFromJson(JsonObject value) {
        for (Entry<String, JsonElement> entry : value.entrySet()) {
            switch (entry.getKey()) {
                case "byte":
                    return new ByteTag("", entry.getValue().getAsInt());
                case "int":
                    return new IntTag("", entry.getValue().getAsInt());
                case "string":
                    return new StringTag("", entry.getValue().getAsString());
            }
            break;
        }
        throw new IllegalArgumentException("Invalid block state value: " + value);
    }

    @AllArgsConstructor
    @Data
    public static class ValueRemap {
        private final Tag oldValue;
        private final Tag newValue;
    }

    @AllArgsConstructor
    @Data
    public static class BlockRemap {
        private final CompoundTag oldStates;

        private final String newName;
        private final CompoundTag newStates;
    }
}

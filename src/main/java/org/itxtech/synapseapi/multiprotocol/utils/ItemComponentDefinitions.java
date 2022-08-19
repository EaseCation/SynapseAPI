package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.Tag;
import com.google.common.io.ByteStreams;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class ItemComponentDefinitions {
    private static final Map<AbstractProtocol, Map<String, byte[]>> DEFINITIONS = new EnumMap<>(AbstractProtocol.class);

    static {
        // vanilla 在 1.18.10 前发的都是空的
        try {
            Map<String, byte[]> definition11810 = load("item_components11810.nbt");
            Map<String, byte[]> definition11830 = load("item_components11830.nbt");
            Map<String, byte[]> definition119 = load("item_components119.nbt");
            Map<String, byte[]> definition11910 = load("item_components11910.nbt");

            DEFINITIONS.put(AbstractProtocol.PROTOCOL_118_10, definition11810);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_118_30, definition11830);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119, definition119);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_10, definition11910);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_20, definition11910);
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load item_components.nbt");
        }
    }

    private static Map<String, byte[]> load(String file) throws IOException {
        Map<String, byte[]> map = new Object2ObjectOpenHashMap<>();
        CompoundTag nbt = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource(file)));
        for (Map.Entry<String, Tag> entry : nbt.getTags().entrySet()) {
            map.put(entry.getKey(), NBTIO.writeNetwork(entry.getValue()));
        }
        return map;
    }

    public static Map<String, byte[]> get(AbstractProtocol protocol) {
        Map<String, byte[]> data = DEFINITIONS.get(protocol);
        return data != null ? data : Collections.emptyMap();
    }

    public static void init() {
//        if (!SynapseSharedConstants.CHECK_RESOURCE_DATA) {
//            return;
//        }
    }

    private ItemComponentDefinitions() {
    }
}

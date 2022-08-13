package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.Tag;
import com.google.common.io.ByteStreams;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public final class ItemComponentDefinitions {
    private static final Int2ObjectMap<Map<String, byte[]>> DEFINITIONS = new Int2ObjectOpenHashMap<>();

    static {
        DEFINITIONS.defaultReturnValue(Collections.emptyMap()); // vanilla 在 1.18.10 前发的都是空的
        try {
            Map<String, byte[]> definition11810 = load("item_components11810.nbt");
            Map<String, byte[]> definition11830 = load("item_components11830.nbt");
            Map<String, byte[]> definition119 = load("item_components119.nbt");

            DEFINITIONS.put(AbstractProtocol.PROTOCOL_118_10.getProtocolStart(), definition11810);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_118_30.getProtocolStart(), definition11830);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119.getProtocolStart(), definition119);
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

    public static Map<String, byte[]> get(int protocol) {
        return DEFINITIONS.get(protocol);
    }

    public static void init() {
//        if (!SynapseSharedConstants.CHECK_RESOURCE_DATA) {
//            return;
//        }
    }

    private ItemComponentDefinitions() {
    }
}

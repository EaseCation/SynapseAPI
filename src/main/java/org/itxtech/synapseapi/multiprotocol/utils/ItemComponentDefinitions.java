package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.Tag;
import com.google.common.io.ByteStreams;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@Log4j2
public final class ItemComponentDefinitions {
    private static final Map<AbstractProtocol, Map<String, byte[]>> DEFINITIONS = new EnumMap<>(AbstractProtocol.class);

    static {
        log.debug("Loading item component definitions...");

        // vanilla 在 1.18.10 前发的都是空的
        try {
            Map<String, byte[]> definition11810 = load("item_components11810.nbt");
            Map<String, byte[]> definition11830 = load("item_components11830.nbt");
            Map<String, byte[]> definition119 = load("item_components119.nbt");
            Map<String, byte[]> definition11910 = load("item_components11910.nbt");
            Map<String, byte[]> definition11950 = load("item_components11950.nbt");
            Map<String, byte[]> definition11960 = load("item_components11960.nbt");
            Map<String, byte[]> definition11970 = load("item_components11970.nbt");
            Map<String, byte[]> definition11980 = load("item_components11980.nbt");

            DEFINITIONS.put(AbstractProtocol.PROTOCOL_118_10, definition11810);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_118_30, definition11830);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_118_30_NE, definition11830);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119, definition119);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_10, definition11910);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_20, definition11910);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_21, definition11910);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_30, definition11910);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_40, definition11910);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_50, definition11950);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_60, definition11960);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_63, definition11960);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_70, definition11970);
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_80, definition11980);
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load item_components.nbt");
        }

        for (AbstractProtocol protocol : AbstractProtocol.values0()) {
            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                continue;
            }
            if (DEFINITIONS.get(protocol) == null) {
                throw new AssertionError("Missing item_components.nbt: " + protocol);
            }
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

    public static Map<String, byte[]> get(AbstractProtocol protocol, boolean netease) {
        if (netease) {
            //TODO: dump netease data
            return Collections.emptyMap();
        }

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

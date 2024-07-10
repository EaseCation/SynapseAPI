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
    private static final Map<AbstractProtocol, Map<String, byte[]>[]> DEFINITIONS = new EnumMap<>(AbstractProtocol.class);

    static {
        log.debug("Loading item component definitions...");

        // vanilla 在 1.18.10 前发的都是空的
        try {
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_118_10, new Map[]{
                    load("item_components11810.nbt", AbstractProtocol.PROTOCOL_118_10, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_118_30, new Map[]{
                    load("item_components11830.nbt", AbstractProtocol.PROTOCOL_118_30, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_118_30_NE, new Map[]{
                    null,
                    load("item_components11830.nbt", AbstractProtocol.PROTOCOL_118_30_NE, true),
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119, new Map[]{
                    load("item_components119.nbt", AbstractProtocol.PROTOCOL_119, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_10, new Map[]{
                    load("item_components11910.nbt", AbstractProtocol.PROTOCOL_119_10, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_20, new Map[]{
                    load("item_components11910.nbt", AbstractProtocol.PROTOCOL_119_20, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_21, new Map[]{
                    load("item_components11910.nbt", AbstractProtocol.PROTOCOL_119_21, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_30, new Map[]{
                    load("item_components11910.nbt", AbstractProtocol.PROTOCOL_119_30, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_40, new Map[]{
                    load("item_components11910.nbt", AbstractProtocol.PROTOCOL_119_40, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_50, new Map[]{
                    load("item_components11950.nbt", AbstractProtocol.PROTOCOL_119_50, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_60, new Map[]{
                    load("item_components11960.nbt", AbstractProtocol.PROTOCOL_119_60, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_63, new Map[]{
                    load("item_components11960.nbt", AbstractProtocol.PROTOCOL_119_63, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_70, new Map[]{
                    load("item_components11970.nbt", AbstractProtocol.PROTOCOL_119_70, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_119_80, new Map[]{
                    load("item_components11980.nbt", AbstractProtocol.PROTOCOL_119_80, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_120, new Map[]{
                    load("item_components120.nbt", AbstractProtocol.PROTOCOL_120, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_120_10, new Map[]{
                    load("item_components12010.nbt", AbstractProtocol.PROTOCOL_120_10, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_120_30, new Map[]{
                    load("item_components12030.nbt", AbstractProtocol.PROTOCOL_120_30, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_120_40, new Map[]{
                    load("item_components12030.nbt", AbstractProtocol.PROTOCOL_120_40, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_120_50, new Map[]{
                    load("item_components12050.nbt", AbstractProtocol.PROTOCOL_120_50, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_120_60, new Map[]{
                    load("item_components12060.nbt", AbstractProtocol.PROTOCOL_120_60, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_120_70, new Map[]{
                    load("item_components12070.nbt", AbstractProtocol.PROTOCOL_120_70, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_120_80, new Map[]{
                    load("item_components12080.nbt", AbstractProtocol.PROTOCOL_120_80, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_121, new Map[]{
                    load("item_components121.nbt", AbstractProtocol.PROTOCOL_121, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_121_2, new Map[]{
                    load("item_components121.nbt", AbstractProtocol.PROTOCOL_121_2, false),
                    null,
            });
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load item_components.nbt", e);
        }

        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                continue;
            }
            if (DEFINITIONS.get(protocol) == null) {
                throw new AssertionError("Missing item_components.nbt: " + protocol);
            }
        }
    }

    private static Map<String, byte[]> load(String file, AbstractProtocol protocol, boolean netease) throws IOException {
        Map<String, byte[]> map = new Object2ObjectOpenHashMap<>();
        CompoundTag nbt = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource(file)));
        for (Map.Entry<String, Tag> entry : nbt.getTags().entrySet()) {
            String identifier = entry.getKey();
            CompoundTag tag = (CompoundTag) entry.getValue();
            int defaultNetId = tag.getInt("id");
            int networkId = AdvancedRuntimeItemPalette.getNetworkIdByName(protocol, netease, identifier);
            if (networkId == -1) {
                networkId = AdvancedRuntimeItemPalette.getNetworkIdByNameTodo(protocol, netease, identifier);
                if (networkId != -1) {
                    log.trace("Unavailable item: {} | {} => {} ({})", identifier, defaultNetId, networkId, protocol);
                }
            }
            if (networkId != -1) {
                tag.putInt("id", networkId);
                if (defaultNetId != networkId) {
                    log.trace("item network id changed: {} | {} => {} ({})", identifier, defaultNetId, networkId, protocol);
                }
            } else {
                log.warn("Unmapped network item: {} | {} ({})", identifier, defaultNetId, protocol);
            }
            map.put(entry.getKey(), NBTIO.writeNetwork(tag));
        }
        return map;
    }

    public static Map<String, byte[]> get(AbstractProtocol protocol, boolean netease) {
        Map<String, byte[]> data = DEFINITIONS.get(protocol)[netease ? 1 : 0];
        return data != null ? data : Collections.emptyMap();
    }

    public static void init() {
//        if (!SynapseSharedConstants.CHECK_RESOURCE_DATA) {
//            return;
//        }
    }

    public static void registerCustomItemComponent(String name, int id, CompoundTag compoundTag) {
        CompoundTag fullTag = new CompoundTag()
            .putInt("id", id)
            .putString("name", name)
            .putCompound("components", compoundTag);
        DEFINITIONS.forEach((protocol, map) -> {
            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                return;
            }

            for (int i = 0; i <= 1; i++) {
                Map<String, byte[]> data = map[i];
                if (data == null) {
                    continue;
                }
                try {
                    data.put(name, NBTIO.writeNetwork(fullTag));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private ItemComponentDefinitions() {
    }
}

package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.BatchPacket.Track;
import cn.nukkit.network.protocol.DataPacket;
import com.google.common.io.ByteStreams;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.ItemComponentPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.ItemComponentPacket116100.Entry;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.function.IntFunction;
import java.util.zip.Deflater;

@Log4j2
public final class ItemComponentDefinitions {
    private static final Map<AbstractProtocol, Map<String, CompoundTag>[]> DEFINITIONS = new EnumMap<>(AbstractProtocol.class);
    private static final Map<AbstractProtocol, BatchPacket[]> PACKETS = new EnumMap<>(AbstractProtocol.class);

    static {
        log.debug("Loading item component definitions...");

        try {
/*
            // vanilla 在 1.18.10 前发的都是空的
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
*/
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_120_10, new Map[]{
                    load("item_components12010.nbt", AbstractProtocol.PROTOCOL_120_10, false),
                    load("item_components12010.nbt", AbstractProtocol.PROTOCOL_120_10, true),
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
                    load("item_components12050.nbt", AbstractProtocol.PROTOCOL_120_50, true),
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
                    load("item_components121.nbt", AbstractProtocol.PROTOCOL_121_2, true),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_121_20, new Map[]{
                    load("item_components12120.nbt", AbstractProtocol.PROTOCOL_121_20, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_121_30, new Map[]{
                    load("item_components12130.nbt", AbstractProtocol.PROTOCOL_121_30, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_121_40, new Map[]{
                    load("item_components12140.nbt", AbstractProtocol.PROTOCOL_121_40, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_121_50, new Map[]{
                    load("item_components12150.nbt", AbstractProtocol.PROTOCOL_121_50, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_121_60, new Map[]{
                    load("item_components12160.nbt", AbstractProtocol.PROTOCOL_121_60, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_121_70, new Map[]{
                    load("item_components12160.nbt", AbstractProtocol.PROTOCOL_121_70, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_121_80, new Map[]{
                    load("item_components12180.nbt", AbstractProtocol.PROTOCOL_121_80, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_121_90, new Map[]{
                    load("item_components12180.nbt", AbstractProtocol.PROTOCOL_121_90, false),
                    null,
            });
            DEFINITIONS.put(AbstractProtocol.PROTOCOL_121_93, new Map[]{
                    load("item_components12180.nbt", AbstractProtocol.PROTOCOL_121_93, false),
                    null,
            });
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load item_components.nbt", e);
        }

        cachePackets();
    }

    public static void cachePackets() {
        log.debug("cache item component definitions...");

        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                continue;
            }
            if (protocol.ordinal() < AbstractProtocol.FIRST_AVAILABLE_PROTOCOL.ordinal()) {
                // drop support for unavailable versions
                continue;
            }

            Map<String, CompoundTag>[] definition = DEFINITIONS.get(protocol);
            if (definition == null) {
                throw new AssertionError("Missing item_components.nbt: " + protocol);
            }

            DataPacket packet;
            DataPacket packetNe;
            if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_60.getProtocolStart()) {
                packet = AdvancedRuntimeItemPalette.createItemRegistryPacket(protocol, false, definition[0]);

                packetNe = AdvancedRuntimeItemPalette.createItemRegistryPacket(protocol, true, definition[0]);
            } else {
                ItemComponentPacket116100 itemComponentPacket = new ItemComponentPacket116100();
                itemComponentPacket.entries = mapToEntries(definition[0], protocol, false);
                packet = itemComponentPacket;

                ItemComponentPacket116100 itemComponentPacketNe = new ItemComponentPacket116100();
                itemComponentPacketNe.entries = mapToEntries(definition[1], protocol, true);
                packetNe = itemComponentPacketNe;
            }

            packet.setHelper(protocol.getHelper());
            packet.tryEncode();

            BatchPacket batch = packet.compress(Deflater.BEST_COMPRESSION, true);
            batch.tracks = new Track[]{new Track(packet.pid(), packet.getCount())};

            packetNe.setHelper(protocol.getHelper());
            packetNe.neteaseMode = true;
            packetNe.tryEncode();

            BatchPacket batchNe = packetNe.compress(Deflater.BEST_COMPRESSION, true);
            batchNe.tracks = new Track[]{new Track(packetNe.pid(), packetNe.getCount())};

            PACKETS.put(protocol, new BatchPacket[]{batch, batchNe});
        }
    }

    private static Entry[] mapToEntries(@Nullable Map<String, CompoundTag> definition, AbstractProtocol protocol, boolean netease) {
        if (definition == null) {
            return new Entry[0];
        }

        List<Entry> entries = new ArrayList<>(definition.size());
        for (Map.Entry<String, CompoundTag> entry : definition.entrySet()) {
            String identifier = entry.getKey();
            CompoundTag tag = entry.getValue();
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

            byte[] data;
            try {
                data = NBTIO.writeNetwork(tag);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            entries.add(new Entry(entry.getKey(), data));
        }

        return entries.toArray(new Entry[0]);
    }

    private static Map<String, CompoundTag> load(String file, AbstractProtocol protocol, boolean netease) throws IOException {
        Map<String, CompoundTag> map = new HashMap<>();
        CompoundTag nbt = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource(file)));
        nbt.getTagsUnsafe().forEach((name, data) -> map.put(name, (CompoundTag) data));
        return map;
    }

    @Nullable
    public static Map<String, CompoundTag> get(AbstractProtocol protocol, boolean netease) {
        return DEFINITIONS.get(protocol)[netease ? 1 : 0];
    }

    @Nullable
    public static DataPacket getPacket(AbstractProtocol protocol, boolean netease) {
        return PACKETS.get(protocol)[netease ? 1 : 0];
    }

    public static void init() {
    }

    public static void registerCustomItemComponent(String name, int id, IntFunction<CompoundTag> componentsSupplier) {
        DEFINITIONS.forEach((protocol, map) -> {
            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                return;
            }

            CompoundTag fullTag = new CompoundTag()
                    .putCompound("components", componentsSupplier.apply(protocol.getProtocolStart()));

            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_121_60.getProtocolStart()) {
                fullTag.putInt("id", id);
                fullTag.putString("name", name);
            }

            for (int i = 0; i <= 1; i++) {
                Map<String, CompoundTag> data = map[i];
                if (data == null) {
                    continue;
//                    data = new HashMap<>();
//                    map[i] = data;
                }
                data.put(name, fullTag);
            }
        });
    }

    private ItemComponentDefinitions() {
    }
}

package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.BatchPacket.Track;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.MainLogger;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.CreativeContentPacket116;
import org.itxtech.synapseapi.multiprotocol.utils.item.CreativeInventoryLegacy;
import org.itxtech.synapseapi.multiprotocol.utils.item.CreativeInventoryNew;

import javax.annotation.Nullable;
import java.util.*;
import java.util.zip.Deflater;

import static cn.nukkit.GameVersion.*;
import static org.itxtech.synapseapi.SynapseSharedConstants.*;

@Log4j2
public class CreativeItemsPalette {

    private static final Map<AbstractProtocol, List<Item>[]> palettes = new EnumMap<>(AbstractProtocol.class);

    private static final Map<AbstractProtocol, BatchPacket[]> PACKETS = new EnumMap<>(AbstractProtocol.class);
    private static final BatchPacket[] NULL_PACKET = new BatchPacket[2];

    public static void init() {
        log.debug("Loading creative items...");

        register(AbstractProtocol.PROTOCOL_19, load("creativeitems_19.json"), null);
        register(AbstractProtocol.PROTOCOL_110, load("creativeitems_19.json"), null);
        register(AbstractProtocol.PROTOCOL_111, load("creativeitems_111.json"), null);
        register(AbstractProtocol.PROTOCOL_112, load("creativeitems_111.json"), null);
        register(AbstractProtocol.PROTOCOL_113, load("creativeitems_113.json"), null);
        register(AbstractProtocol.PROTOCOL_114, load("creativeitems_114.json"), null);
        register(AbstractProtocol.PROTOCOL_114_60, load("creativeitems_114.json"), null);
        register(AbstractProtocol.PROTOCOL_116, load("creativeitems_116.json"), null);
        register(AbstractProtocol.PROTOCOL_116_20, load("creativeitems_11620.json"), null);
        register(AbstractProtocol.PROTOCOL_116_100_NE, load("creativeitems_116.json"), null);
        register(AbstractProtocol.PROTOCOL_116_100, load("creativeitems_116100.json"), null);
        register(AbstractProtocol.PROTOCOL_116_200, load("creativeitems_116100.json"), null);
        register(AbstractProtocol.PROTOCOL_116_210, load("creativeitems_116100.json"), null);
        register(AbstractProtocol.PROTOCOL_116_220, load("creativeitems_116100.json", true), null);
        register(AbstractProtocol.PROTOCOL_117, load("creativeitems_116100.json", true), null);
        register(AbstractProtocol.PROTOCOL_117_10, load("creativeitems_116100.json", true), null);
        register(AbstractProtocol.PROTOCOL_117_30, load("creativeitems_116100.json", true), null);
        register(AbstractProtocol.PROTOCOL_117_40, load("creativeitems_116100.json", true), null);

        if (!V1_18_0.isAvailable()) {
            register(AbstractProtocol.PROTOCOL_118, load("creativeitems_116100.json", true), null);
            register(AbstractProtocol.PROTOCOL_118_10, load("creativeitems_116100.json", true), null);
            register(AbstractProtocol.PROTOCOL_118_30, load("creativeitems_116100.json", true), null);
            register(AbstractProtocol.PROTOCOL_118_30_NE, load("creativeitems_116100.json", true), null);
        } else {
            register(AbstractProtocol.PROTOCOL_118, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_118_10, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_118_30, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_118_30_NE, CreativeInventoryLegacy.getItems(), null);
        }

        if (!V1_19_0.isAvailable()) {
            register(AbstractProtocol.PROTOCOL_119, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_10, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_20, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_21, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_30, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_40, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_50, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_60, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_63, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_70, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_80, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120_10, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120_30, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120_40, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120_50, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120_60, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120_70, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120_80, CreativeInventoryLegacy.getItems(), null);
            register(AbstractProtocol.PROTOCOL_121, CreativeInventoryLegacy.getItems(), null);
        } else {
            register(AbstractProtocol.PROTOCOL_119, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_10, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_20, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_21, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_30, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_40, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_50, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_60, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_63, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_70, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_119_80, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120_10, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120_30, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120_40, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120_50, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120_60, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120_70, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_120_80, CreativeInventoryNew.getItems(), null);
            register(AbstractProtocol.PROTOCOL_121, CreativeInventoryNew.getItems(), null);
        }

        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_19.getProtocolStart()) {
                continue;
            }
            if (palettes.get(protocol) == null) {
                throw new AssertionError("Missing creative_items.json: " + protocol);
            }
        }

        cachePackets();
    }

    private static void register(AbstractProtocol protocol, List<Item> list, List<Item> listNetEase) {
        Objects.requireNonNull(list);
        List<Item>[] data =
                listNetEase != null
                        ? new List[]{list, listNetEase}
                        : new List[]{list};
        palettes.put(protocol, data);
    }

    private static List<Item> load(String file) {
        return load(file, false);
    }

    @SuppressWarnings("unchecked")
    private static List<Item> load(String file, boolean ignoreUnsupported) {
        Server.getInstance().getLogger().debug("Loading Creative Items Palette from " + file);
        List<Item> result = new ObjectArrayList<>();
        Config config = new Config(Config.JSON);
        config.load(SynapseAPI.class.getClassLoader().getResourceAsStream(file));
        List<Map<?, ?>> list = config.getMapList("items");

        for (Map map : list) {
            try {
                Item item = Item.fromJson(map, ignoreUnsupported);
                if (item != null && (!item.isChemistryFeature() || ENABLE_CHEMISTRY_FEATURE)) {
                    result.add(item.clone());
                }
            } catch (Exception e) {
                MainLogger.getLogger().logException(e);
            }
        }
        return result;
    }

    public static List<Item> getCreativeItems(AbstractProtocol protocol, boolean netease) {
        List<Item>[] lists = palettes.get(protocol);
        if (lists == null) return new ObjectArrayList<>(Item.getCreativeItems());
        if (netease && lists.length > 1) {
            return lists[1];
        } else {
            return lists[0];
        }
    }

    public static void cachePackets() {
        log.debug("cache creative content...");

        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.ordinal() < AbstractProtocol.PROTOCOL_116.ordinal()) {
                continue;
            }
            if (protocol.ordinal() < AbstractProtocol.FIRST_AVAILABLE_PROTOCOL.ordinal()) {
                // drop support for unavailable versions
                continue;
            }

            CreativeContentPacket116 packet = new CreativeContentPacket116();
            packet.entries = getCreativeItems(protocol, false).toArray(new Item[0]);
            packet.setHelper(protocol.getHelper());
            packet.tryEncode();

            BatchPacket batch = packet.compress(Deflater.BEST_COMPRESSION, true);
            batch.tracks = new Track[]{new Track(packet.pid(), packet.getCount())};

            CreativeContentPacket116 packetNe = new CreativeContentPacket116();
            packetNe.entries = getCreativeItems(protocol, true).toArray(new Item[0]);
            packetNe.setHelper(protocol.getHelper());
            packetNe.neteaseMode = true;
            packetNe.tryEncode();

            BatchPacket batchNe = packetNe.compress(Deflater.BEST_COMPRESSION, true);
            batchNe.tracks = new Track[]{new Track(packetNe.pid(), packetNe.getCount())};

            PACKETS.put(protocol, new BatchPacket[]{batch, batchNe});
        }
    }

    @Nullable
    public static BatchPacket getCachedCreativeContentPacket(AbstractProtocol protocol, boolean netease) {
        return PACKETS.getOrDefault(protocol, NULL_PACKET)[netease ? 1 : 0];
    }
}

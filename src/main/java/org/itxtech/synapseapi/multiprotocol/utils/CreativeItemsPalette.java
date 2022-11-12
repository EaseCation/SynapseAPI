package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.MainLogger;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.item.CreativeInventory;

import java.util.*;

import static cn.nukkit.GameVersion.*;

@Log4j2
public class CreativeItemsPalette {

    private static final Map<AbstractProtocol, List<Item>[]> palettes = new EnumMap<>(AbstractProtocol.class);

    public static void init() {
        log.debug("Loading creative items...");

        register(AbstractProtocol.PROTOCOL_19, load("creativeitems_19.json"), null);
        register(AbstractProtocol.PROTOCOL_110, load("creativeitems_19.json"), null);
        register(AbstractProtocol.PROTOCOL_111, load("creativeitems_111.json"), null);
        register(AbstractProtocol.PROTOCOL_112, load("creativeitems_111.json"), null);
        register(AbstractProtocol.PROTOCOL_113, load("creativeitems_113.json"), null);
        register(AbstractProtocol.PROTOCOL_114, load("creativeitems_114.json"), null);
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
        register(AbstractProtocol.PROTOCOL_118, load("creativeitems_116100.json", true), null);
        register(AbstractProtocol.PROTOCOL_118_10, load("creativeitems_116100.json", true), null);
        register(AbstractProtocol.PROTOCOL_118_30, load("creativeitems_116100.json", true), null);
        register(AbstractProtocol.PROTOCOL_119, load("creativeitems_116100.json", true), null);
        register(AbstractProtocol.PROTOCOL_119_10, load("creativeitems_116100.json", true), null);
        register(AbstractProtocol.PROTOCOL_119_20, load("creativeitems_116100.json", true), null);
        register(AbstractProtocol.PROTOCOL_119_21, load("creativeitems_116100.json", true), null);
        register(AbstractProtocol.PROTOCOL_119_30, load("creativeitems_116100.json", true), null);
        register(AbstractProtocol.PROTOCOL_119_40, load("creativeitems_116100.json", true), null);

        if (!V1_19_0.isAvailable()) {
            return;
        }
        register(AbstractProtocol.PROTOCOL_119, CreativeInventory.getItems(), null);
        register(AbstractProtocol.PROTOCOL_119_10, CreativeInventory.getItems(), null);
        register(AbstractProtocol.PROTOCOL_119_20, CreativeInventory.getItems(), null);
        register(AbstractProtocol.PROTOCOL_119_21, CreativeInventory.getItems(), null);
        register(AbstractProtocol.PROTOCOL_119_30, CreativeInventory.getItems(), null);
        register(AbstractProtocol.PROTOCOL_119_40, CreativeInventory.getItems(), null);
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
        Server.getInstance().getLogger().info("Loading Creative Items Palette from " + file);
        List<Item> result = new ObjectArrayList<>();
        Config config = new Config(Config.YAML);
        config.load(SynapseAPI.class.getClassLoader().getResourceAsStream(file));
        List<Map> list = config.getMapList("items");

        for (Map map : list) {
            try {
                Item item = Item.fromJson(map, ignoreUnsupported);
                if (item != null) {
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

}

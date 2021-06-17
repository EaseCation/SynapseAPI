package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.MainLogger;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.util.*;

public class CreativeItemsPalette {

    public static class CreativeItemsList extends ArrayList<Item> {}

    private static final Map<AbstractProtocol, CreativeItemsList[]> palettes = new HashMap<>();

    public static void init() {
        register(AbstractProtocol.PROTOCOL_19, load("creativeitems_19.json"), null);
        register(AbstractProtocol.PROTOCOL_110, load("creativeitems_19.json"), null);
        register(AbstractProtocol.PROTOCOL_111, load("creativeitems_111.json"), null);
        register(AbstractProtocol.PROTOCOL_112, load("creativeitems_111.json"), null);
        register(AbstractProtocol.PROTOCOL_113, load("creativeitems_113.json"), null);
        register(AbstractProtocol.PROTOCOL_114, load("creativeitems_114.json"), null);
        register(AbstractProtocol.PROTOCOL_116, load("creativeitems_116.json"), null);
        register(AbstractProtocol.PROTOCOL_116_20, load("creativeitems_11620.json"), null);
        register(AbstractProtocol.PROTOCOL_116_100_NE, load("creativeitems_116.json"), null);
        register(AbstractProtocol.PROTOCOL_116_100, load("creativeitems_11620.json"), null);
        register(AbstractProtocol.PROTOCOL_116_200, load("creativeitems_11620.json"), null);
        register(AbstractProtocol.PROTOCOL_116_210, load("creativeitems_11620.json"), null);
        register(AbstractProtocol.PROTOCOL_116_220, load("creativeitems_11620.json", true), null);
        register(AbstractProtocol.PROTOCOL_117, load("creativeitems_11620.json", true), null);
    }

    private static void register(AbstractProtocol protocol, CreativeItemsList list, CreativeItemsList listNetEase) {
        Objects.requireNonNull(list);
        CreativeItemsList[] data =
                listNetEase != null
                        ? new CreativeItemsList[]{list, listNetEase}
                        : new CreativeItemsList[]{list};
        palettes.put(protocol, data);
    }

    private static CreativeItemsList load(String file) {
        return load(file, false);
    }

    @SuppressWarnings("unchecked")
    private static CreativeItemsList load(String file, boolean ignoreUnsupported) {
        Server.getInstance().getLogger().info("Loading Creative Items Palette from " + file);
        CreativeItemsList result = new CreativeItemsList();
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

    public static CreativeItemsList getCreativeItems(AbstractProtocol protocol, boolean netease) {
        CreativeItemsList[] lists = palettes.get(protocol);
        if (netease && lists.length > 1) {
            return lists[1];
        } else {
            return lists[0];
        }
    }

}

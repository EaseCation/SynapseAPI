package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.MainLogger;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreativeItemsPalette {

    private static Map<AbstractProtocol, ArrayList<Item>> palettes = new HashMap<AbstractProtocol, ArrayList<Item>>() {{
        put(AbstractProtocol.PROTOCOL_19, load("creativeitems_19.json"));
        put(AbstractProtocol.PROTOCOL_110, load("creativeitems_19.json"));
        put(AbstractProtocol.PROTOCOL_111, load("creativeitems_111.json"));
        put(AbstractProtocol.PROTOCOL_112, load("creativeitems_111.json"));
        put(AbstractProtocol.PROTOCOL_113, load("creativeitems_113.json"));
        put(AbstractProtocol.PROTOCOL_114, load("creativeitems_114.json"));
        put(AbstractProtocol.PROTOCOL_116, load("creativeitems_116.json"));
        put(AbstractProtocol.PROTOCOL_116_20, load("creativeitems_11620.json"));
    }};

    @SuppressWarnings("unchecked")
    private static ArrayList<Item> load(String file) {
        Server.getInstance().getLogger().info("Loading Creative Items Palette from " + file);
        ArrayList<Item> result = new ArrayList<>();
        Config config = new Config(Config.YAML);
        config.load(SynapseAPI.class.getClassLoader().getResourceAsStream(file));
        List<Map> list = config.getMapList("items");

        for (Map map : list) {
            try {
                result.add(Item.fromJson(map).clone());
            } catch (Exception e) {
                MainLogger.getLogger().logException(e);
            }
        }
        return result;
    }

    public static ArrayList<Item> getCreativeItems(AbstractProtocol protocol) {
        return new ArrayList<>(palettes.getOrDefault(protocol, Item.getCreativeItems()));
    }

    public static void init() {
        //NOOP
    }
}

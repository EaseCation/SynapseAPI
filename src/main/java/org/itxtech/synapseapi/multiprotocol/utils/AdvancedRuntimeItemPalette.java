package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.item.Item;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

//TODO: 网易的硬编码数据可能不一样 -- 2020/11/14
public final class AdvancedRuntimeItemPalette {

    public static final Object2ObjectMap<AbstractProtocol, AdvancedRuntimeItemPaletteInterface> palettes = new Object2ObjectOpenHashMap<>();

    static {
        palettes.defaultReturnValue(RuntimeItemPaletteLegacy.INSTANCE);

        palettes.put(AbstractProtocol.PROTOCOL_116_100, new RuntimeItemPalette("runtime_item_ids_116100.json"));
    }

    public static int getNetworkFullId(AbstractProtocol protocol, Item item) {
        return palettes.get(protocol).getNetworkFullId(item);
    }

    public static int getLegacyFullId(AbstractProtocol protocol, int networkId) {
        return palettes.get(protocol).getLegacyFullId(networkId);
    }

    public static int getId(AbstractProtocol protocol, int fullId) {
        return palettes.get(protocol).getId(fullId);
    }

    public static int getData(AbstractProtocol protocol, int fullId) {
        return palettes.get(protocol).getData(fullId);
    }

    public static int getNetworkId(AbstractProtocol protocol, int networkFullId) {
        return palettes.get(protocol).getNetworkId(networkFullId);
    }

    public static boolean hasData(AbstractProtocol protocol, int id) {
        return palettes.get(protocol).hasData(id);
    }

    public static byte[] getCompiledData(AbstractProtocol protocol) {
        return palettes.get(protocol).getCompiledData();
    }

    public static void init() {
        // NOOP
    }
}

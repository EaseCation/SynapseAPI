package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.item.Item;
import cn.nukkit.item.RuntimeItemPaletteInterface;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.util.Objects;

/**
 * 由于hardcode了物品调色板（目前应该只有中国版写死了2021.06.17）
 * 以及slot相关编码使用NetworkId
 * 所以需要分版本对数据包进行处理
 * 注意：最好所有涉及到的包都有多协议适配版（XXXPacket116200这类），这样才可以在BinaryStream中自动设置进去neteaseMode，不然都将按照国际版进行编码
 */

public final class AdvancedRuntimeItemPalette {

    public static final Object2ObjectMap<AbstractProtocol, AdvancedRuntimeItemPaletteInterface[]> palettes = new Object2ObjectOpenHashMap<>();

    static {
        palettes.defaultReturnValue(new AdvancedRuntimeItemPaletteInterface[]{RuntimeItemPaletteLegacy.INSTANCE});

        RuntimeItemPalette palette116100 = new RuntimeItemPalette("runtime_item_ids_116100.json");
        RuntimeItemPalette palette116200NE = new RuntimeItemPalette("runtime_item_ids_116200NE.json");
        RuntimeItemPalette palette117 = new RuntimeItemPalette("runtime_item_ids_117.json");
        RuntimeItemPalette palette11710 = new RuntimeItemPalette("runtime_item_ids_11710.json");

        register(AbstractProtocol.PROTOCOL_116_100, palette116100, null);
        register(AbstractProtocol.PROTOCOL_116_200, palette116100, palette116200NE);
        register(AbstractProtocol.PROTOCOL_116_210, palette116100, null);
        register(AbstractProtocol.PROTOCOL_116_220, palette116100, null);
        register(AbstractProtocol.PROTOCOL_117, palette117, null);
        register(AbstractProtocol.PROTOCOL_117_10, palette11710, null);
    }

    private static void register(AbstractProtocol protocol, RuntimeItemPalette palette, RuntimeItemPalette paletteNetEase) {
        Objects.requireNonNull(palette);
        AdvancedRuntimeItemPaletteInterface[] data =
                paletteNetEase != null
                        ? new AdvancedRuntimeItemPaletteInterface[]{palette, paletteNetEase}
                        : new AdvancedRuntimeItemPaletteInterface[]{palette};
        palettes.put(protocol, data);
    }

    private static AdvancedRuntimeItemPaletteInterface getPalette(AbstractProtocol protocol, boolean netease) {
        AdvancedRuntimeItemPaletteInterface[] interfaces = palettes.get(protocol);
        if (netease && interfaces.length > 1) {
            return interfaces[1];
        } else {
            return interfaces[0];
        }
    }

    public static int getNetworkFullId(AbstractProtocol protocol, boolean netease, Item item) {
        return getPalette(protocol, netease).getNetworkFullId(item);
    }

    public static int getLegacyFullId(AbstractProtocol protocol, boolean netease, int networkId) {
        return getPalette(protocol, netease).getLegacyFullId(networkId);
    }

    public static int getId(AbstractProtocol protocol, boolean netease, int fullId) {
        return getPalette(protocol, netease).getId(fullId);
    }

    public static int getData(AbstractProtocol protocol, boolean netease, int fullId) {
        return getPalette(protocol, netease).getData(fullId);
    }

    public static int getNetworkId(AbstractProtocol protocol, boolean netease, int networkFullId) {
        return getPalette(protocol, netease).getNetworkId(networkFullId);
    }

    public static boolean hasData(AbstractProtocol protocol, boolean netease, int id) {
        return getPalette(protocol, netease).hasData(id);
    }

    public static byte[] getCompiledData(AbstractProtocol protocol, boolean netease) {
        return getPalette(protocol, netease).getCompiledData();
    }

    public static void init() {
        // NOOP
    }
}

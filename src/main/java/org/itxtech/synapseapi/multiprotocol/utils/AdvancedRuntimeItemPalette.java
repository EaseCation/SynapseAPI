package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.item.Item;
import cn.nukkit.item.RuntimeItemPaletteInterface;
import cn.nukkit.nbt.tag.CompoundTag;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.item.LegacyItemSerializer;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;

/*
 * 由于hardcode了物品调色板（目前应该只有中国版写死了2021.06.17）
 * 以及slot相关编码使用NetworkId
 * 所以需要分版本对数据包进行处理
 * 注意：最好所有涉及到的包都有多协议适配版（XXXPacket116200这类），这样才可以在BinaryStream中自动设置进去neteaseMode，不然都将按照国际版进行编码
 */
@Log4j2
public final class AdvancedRuntimeItemPalette {

    public static final Map<AbstractProtocol, AdvancedRuntimeItemPaletteInterface[]> palettes = new EnumMap<>(AbstractProtocol.class);

    static {
        log.debug("Loading advanced runtime item palette...");

        LegacyItemSerializer.initialize();

        RuntimeItemPalette palette116100 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_116_100, "runtime_item_ids_116100.json");
        RuntimeItemPalette palette116200NE = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_116_200, "runtime_item_ids_116200NE.json");
        RuntimeItemPalette palette117 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_117, "runtime_item_ids_117.json");
        RuntimeItemPalette palette117NE = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_117, "runtime_item_ids_117NE.json");
        RuntimeItemPalette palette11710 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_117_10, "runtime_item_ids_11710.json");
        RuntimeItemPalette palette11730 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_117_30, "runtime_item_ids_11730.json");
        RuntimeItemPalette palette11740 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_117_40, "runtime_item_ids_11740.json");
        RuntimeItemPalette palette118 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_118, "runtime_item_ids_118.json");
        RuntimeItemPalette palette118NE = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_118, "runtime_item_ids_118NE.json");
        RuntimeItemPalette palette11810 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_118_10, "runtime_item_ids_11810.json");
        RuntimeItemPalette palette11830 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_118_30, "runtime_item_ids_11830.json");
        RuntimeItemPalette palette11830NE = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_118_30_NE, "runtime_item_ids_11830NE.json");
        RuntimeItemPalette palette119 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_119, "runtime_item_ids_119.json");
        RuntimeItemPalette palette11910 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_119_10, "runtime_item_ids_11910.json");
        RuntimeItemPalette palette11950 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_119_50, "runtime_item_ids_11950.json");
        RuntimeItemPalette palette11960 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_119_60, "runtime_item_ids_11960.json");
        RuntimeItemPalette palette11970 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_119_70, "runtime_item_ids_11970.json");
        RuntimeItemPalette palette11980 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_119_80, "runtime_item_ids_11980.json");
        RuntimeItemPalette palette120 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_120, "runtime_item_ids_120.json");
        RuntimeItemPalette palette12010 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_120_10, "runtime_item_ids_12010.json");
        RuntimeItemPalette palette12010N = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_120_10, "runtime_item_ids_12010NE.json"); //NOTICE: 中国版部分物品的运行时id硬编码在客户端 (目前已知所有药水容器都是静态id, 使用动态id会导致无法放进酿造台)
        RuntimeItemPalette palette12030 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_120_30, "runtime_item_ids_12030.json");
        RuntimeItemPalette palette12050 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_120_50, "runtime_item_ids_12050.json");
//        RuntimeItemPalette palette12050 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_120_50, "runtime_item_ids_12050_raw.json", "item_id_map_12050.json", "item_flatten_map_12050.json");
        RuntimeItemPalette palette12050N = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_120_50, "runtime_item_ids_12050NE.json");
        RuntimeItemPalette palette12060 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_120_60, "runtime_item_ids_12060.json");
        RuntimeItemPalette palette12070 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_120_70, "runtime_item_ids_12070.json");
        RuntimeItemPalette palette12080 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_120_80, "runtime_item_ids_12080.json");
        RuntimeItemPalette palette121 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_121, "runtime_item_ids_121.json");
        RuntimeItemPalette palette12120 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_121_20, "runtime_item_ids_12120.json");
        RuntimeItemPalette palette12130 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_121_30, "runtime_item_ids_12130.json");
        RuntimeItemPalette palette12140 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_121_40, "runtime_item_ids_12140.json");
        RuntimeItemPalette palette12150 = new RuntimeItemPalette(AbstractProtocol.PROTOCOL_121_50, "runtime_item_ids_12150.json");

        register(AbstractProtocol.PROTOCOL_116_100, palette116100, null);
        register(AbstractProtocol.PROTOCOL_116_200, palette116100, palette116200NE);
        register(AbstractProtocol.PROTOCOL_116_210, palette116100, null);
        register(AbstractProtocol.PROTOCOL_116_220, palette116100, null);
        register(AbstractProtocol.PROTOCOL_117, palette117, palette117NE);
        register(AbstractProtocol.PROTOCOL_117_10, palette11710, null);
        register(AbstractProtocol.PROTOCOL_117_30, palette11730, null);
        register(AbstractProtocol.PROTOCOL_117_40, palette11740, null);
        register(AbstractProtocol.PROTOCOL_118, palette118, palette118NE);
        register(AbstractProtocol.PROTOCOL_118_10, palette11810, null);
        register(AbstractProtocol.PROTOCOL_118_30, palette11830, palette11830NE);
        register(AbstractProtocol.PROTOCOL_118_30_NE, palette11830, palette11830NE);
        register(AbstractProtocol.PROTOCOL_119, palette119, null);
        register(AbstractProtocol.PROTOCOL_119_10, palette11910, null);
        register(AbstractProtocol.PROTOCOL_119_20, palette11910, null);
        register(AbstractProtocol.PROTOCOL_119_21, palette11910, null);
        register(AbstractProtocol.PROTOCOL_119_30, palette11910, null);
        register(AbstractProtocol.PROTOCOL_119_40, palette11910, null);
        register(AbstractProtocol.PROTOCOL_119_50, palette11950, null);
        register(AbstractProtocol.PROTOCOL_119_60, palette11960, null);
        register(AbstractProtocol.PROTOCOL_119_63, palette11960, null);
        register(AbstractProtocol.PROTOCOL_119_70, palette11970, null);
        register(AbstractProtocol.PROTOCOL_119_80, palette11980, null);
        register(AbstractProtocol.PROTOCOL_120, palette120, null);
        register(AbstractProtocol.PROTOCOL_120_10, palette12010, palette12010N);
        register(AbstractProtocol.PROTOCOL_120_30, palette12030, null);
        register(AbstractProtocol.PROTOCOL_120_40, palette12030, null);
        register(AbstractProtocol.PROTOCOL_120_50, palette12050, palette12050N);
        register(AbstractProtocol.PROTOCOL_120_60, palette12060, null);
        register(AbstractProtocol.PROTOCOL_120_70, palette12070, null);
        register(AbstractProtocol.PROTOCOL_120_80, palette12080, null);
        register(AbstractProtocol.PROTOCOL_121, palette121, null);
        register(AbstractProtocol.PROTOCOL_121_2, palette121, null);
        register(AbstractProtocol.PROTOCOL_121_20, palette12120, null);
        register(AbstractProtocol.PROTOCOL_121_30, palette12130, null);
        register(AbstractProtocol.PROTOCOL_121_40, palette12140, null);
        register(AbstractProtocol.PROTOCOL_121_50, palette12150, null);
    }

    private static void register(AbstractProtocol protocol, RuntimeItemPalette palette, RuntimeItemPalette paletteNetEase) {
        Objects.requireNonNull(palette);
        AdvancedRuntimeItemPaletteInterface[] data =
                paletteNetEase != null
                        ? new AdvancedRuntimeItemPaletteInterface[]{palette, paletteNetEase}
                        : new AdvancedRuntimeItemPaletteInterface[]{palette};
        palettes.put(protocol, data);
    }

    public static void registerCustomItem(String fullName, int id) {
        registerCustomItem(fullName, id, null, null);
    }

    public static void registerCustomItem(String fullName, int id, @Nullable Integer oldId) {
        registerCustomItem(fullName, id, oldId, null);
    }

    public static void registerCustomItem(String fullName, int id, @Nullable IntFunction<CompoundTag> componentsSupplier) {
        registerCustomItem(fullName, id, null, componentsSupplier);
    }

    public static void registerCustomItem(String fullName, int id, @Nullable Integer oldId, @Nullable IntFunction<CompoundTag> componentsSupplier) {
        Set<AdvancedRuntimeItemPaletteInterface> finished = new ObjectOpenHashSet<>();
        for (Entry<AbstractProtocol, AdvancedRuntimeItemPaletteInterface[]> entry : palettes.entrySet()) {
            AbstractProtocol protocol = entry.getKey();
            AdvancedRuntimeItemPaletteInterface[] interfaces = entry.getValue();
            for (AdvancedRuntimeItemPaletteInterface palette : interfaces) {
                if (!finished.add(palette)) {
                    continue;
                }

                palette.registerItem(new RuntimeItemPaletteInterface.Entry(fullName, id, oldId, null, componentsSupplier != null && componentsSupplier.apply(protocol.getProtocolStart()).contains("item_properties")));
            }
        }

        if (componentsSupplier != null) {
            ItemComponentDefinitions.registerCustomItemComponent(fullName, id, componentsSupplier);
        }
    }

    public static void rebuildNetworkCache() {
        Set<AdvancedRuntimeItemPaletteInterface> finished = new ObjectOpenHashSet<>();
        for (AdvancedRuntimeItemPaletteInterface[] interfaces : AdvancedRuntimeItemPalette.palettes.values()) {
            for (AdvancedRuntimeItemPaletteInterface palette : interfaces) {
                if (!finished.add(palette)) {
                    continue;
                }

                palette.buildNetworkCache();
            }
        }
    }

    private static AdvancedRuntimeItemPaletteInterface getPalette(AbstractProtocol protocol, boolean netease) {
        AdvancedRuntimeItemPaletteInterface[] interfaces = palettes.get(protocol);
        if (interfaces == null) {
            return RuntimeItemPaletteLegacy.INSTANCE;
        }
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

    public static String getString(AbstractProtocol protocol, boolean netease, Item item) {
        return getPalette(protocol, netease).getString(item);
    }

    public static int getLegacyFullIdByName(AbstractProtocol protocol, boolean netease, String name) {
        return getPalette(protocol, netease).getLegacyFullIdByName(name);
    }

    public static int getNetworkIdByName(AbstractProtocol protocol, boolean netease, String name) {
        return getPalette(protocol, netease).getNetworkIdByName(name);
    }

    public static int getNetworkIdByNameTodo(AbstractProtocol protocol, boolean netease, String name) {
        return getPalette(protocol, netease).getNetworkIdByNameTodo(name);
    }

    public static void init() {
        // NOOP
    }
}

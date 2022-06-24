package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.Server;
import cn.nukkit.level.GlobalBlockPaletteInterface;
import cn.nukkit.level.GlobalBlockPaletteInterface.StaticVersion;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.itxtech.synapseapi.SynapseSharedConstants;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.*;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockTable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class AdvancedGlobalBlockPalette {

    public static final Map<AbstractProtocol, AdvancedGlobalBlockPaletteInterface[]> palettes = new HashMap<>();
    public static final Map<StaticVersion, GlobalBlockPaletteInterface> staticPalettes = new HashMap<>();

    static {
        palettes.put(AbstractProtocol.PROTOCOL_16, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_16, "block_state_list_16.json"),
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_16, "block_state_list_16_netease.json")
        });
        palettes.put(AbstractProtocol.PROTOCOL_17, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_17, "block_state_list_17.json"),
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_17, "block_state_list_17_netease.json")
        });
        palettes.put(AbstractProtocol.PROTOCOL_18, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_18, "block_state_list_18.json"),
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_18, "block_state_list_18_netease.json")
        });
        palettes.put(AbstractProtocol.PROTOCOL_19, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_19, "block_state_list_19.json"),
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_19, "block_state_list_19_netease.json")
        });
        palettes.put(AbstractProtocol.PROTOCOL_110, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_110, "block_state_list_110.json")
        });
        palettes.put(AbstractProtocol.PROTOCOL_111, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_111, "block_state_list_111.json"),
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_111, "block_state_list_111_netease.json")
        });

        PaletteBlockTable table119 = PaletteBlockTable.fromNBTV3("block_state_list_119.nbt", true);
        PaletteBlockTable table11830 = PaletteBlockTable.fromNBTV3("block_state_list_11830.nbt", true);
        PaletteBlockTable table11810 = PaletteBlockTable.fromNBTV3("block_state_list_11810.dat");
        PaletteBlockTable table118NE = PaletteBlockTable.fromDumpJson("block_state_list_118_netease.json");
        PaletteBlockTable table11740 = PaletteBlockTable.fromNBTV3("block_state_list_11740.dat");
        PaletteBlockTable table11730 = PaletteBlockTable.fromNBTV3("block_state_list_11730.dat");
        PaletteBlockTable table11710 = PaletteBlockTable.fromNBTV3("block_state_list_11710.dat");
        PaletteBlockTable table117NE = PaletteBlockTable.fromDumpJson("block_state_list_117_netease.json");
        PaletteBlockTable table117 = PaletteBlockTable.fromNBTV3("block_state_list_117.dat");
        PaletteBlockTable table116210 = PaletteBlockTable.fromNBTV3("block_state_list_116210.dat");
        PaletteBlockTable table116200NE = PaletteBlockTable.fromNetEaseJson("block_state_list_116200_netease.json");
        PaletteBlockTable table116100 = PaletteBlockTable.fromNBTV3("block_state_list_116100.dat"); //static now :( sort by block name )
        PaletteBlockTable table11620 = PaletteBlockTable.fromNBT("block_state_list_11620.dat");
        PaletteBlockTable table116 = PaletteBlockTable.fromNBT("block_state_list_116.dat");
        PaletteBlockTable table114 = PaletteBlockTable.fromNBT("block_state_list_114.dat");
        PaletteBlockTable table113 = PaletteBlockTable.fromNBTOld("block_state_list_113.dat");
        PaletteBlockTable table112 = PaletteBlockTable.fromJson("block_state_list_112.json");

        palettes.put(AbstractProtocol.PROTOCOL_112, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_112, table112.trim(table11620), "runtime_item_ids_112.json")
        });
        palettes.put(AbstractProtocol.PROTOCOL_113, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBTOld(AbstractProtocol.PROTOCOL_113, table113.trim(table11620), "runtime_item_ids_112.json")
        });
        palettes.put(AbstractProtocol.PROTOCOL_114, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_114, table114.trim(table11620), "runtime_item_ids_114.json", true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_114_60, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_114_60, table114.trim(table11620), "runtime_item_ids_114.json", true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_116, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116, table116.trim(table11620), "runtime_item_ids_116.json", true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_116_20, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116_20, table11620, "runtime_item_ids_116.json", true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_116_100_NE, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116_100_NE, table116.trim(table11620), "runtime_item_ids_116.json", true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_116_100, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116_100, table116100, null, true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_116_200, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116_200, table116100, null, true),
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116_200, table116200NE, null, true),
        });
        palettes.put(AbstractProtocol.PROTOCOL_116_210, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116_210, table116210, null, true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_116_220, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116_220, table116210, null, true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_117, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_117, table117, null, true),
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_117, table117NE, null, true),
        });
        palettes.put(AbstractProtocol.PROTOCOL_117_10, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_117_10, table11710, null, true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_117_30, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_117_30, table11730, null, true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_117_40, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_117_40, table11740, null, true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_118, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_118, table11740, null, true),
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_118, table118NE, null, true),
        });
        palettes.put(AbstractProtocol.PROTOCOL_118_10, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_118_10, table11810, null, true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_118_30, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_118_30, table11830, null, true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_119, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_119, table119, null, true)
        });

        staticPalettes.put(StaticVersion.V1_16_100, new GlobalBlockPaletteStatic(palettes.get(AbstractProtocol.PROTOCOL_116_100)[0]));
        staticPalettes.put(StaticVersion.V1_16_200_NETEASE, new GlobalBlockPaletteStatic(palettes.get(AbstractProtocol.PROTOCOL_116_200)[1]));
        staticPalettes.put(StaticVersion.V1_16_210, new GlobalBlockPaletteStatic(palettes.get(AbstractProtocol.PROTOCOL_116_210)[0]));
        staticPalettes.put(StaticVersion.V1_17, new GlobalBlockPaletteStatic(palettes.get(AbstractProtocol.PROTOCOL_117)[0]));
        staticPalettes.put(StaticVersion.V1_17_NETEASE, new GlobalBlockPaletteStatic(palettes.get(AbstractProtocol.PROTOCOL_117)[1]));
        staticPalettes.put(StaticVersion.V1_17_10, new GlobalBlockPaletteStatic(palettes.get(AbstractProtocol.PROTOCOL_117_10)[0]));
        staticPalettes.put(StaticVersion.V1_17_30, new GlobalBlockPaletteStatic(palettes.get(AbstractProtocol.PROTOCOL_117_30)[0]));
        staticPalettes.put(StaticVersion.V1_17_40, new GlobalBlockPaletteStatic(palettes.get(AbstractProtocol.PROTOCOL_117_40)[0]));
        staticPalettes.put(StaticVersion.V1_18, new GlobalBlockPaletteStatic(palettes.get(AbstractProtocol.PROTOCOL_117_40)[0]));
        staticPalettes.put(StaticVersion.V1_18_NETEASE, new GlobalBlockPaletteStatic(palettes.get(AbstractProtocol.PROTOCOL_118)[1]));
        staticPalettes.put(StaticVersion.V1_18_10, new GlobalBlockPaletteStatic(palettes.get(AbstractProtocol.PROTOCOL_118_10)[0]));
        staticPalettes.put(StaticVersion.V1_18_30, new GlobalBlockPaletteStatic(palettes.get(AbstractProtocol.PROTOCOL_118_30)[0]));
        staticPalettes.put(StaticVersion.V1_19, new GlobalBlockPaletteStatic(palettes.get(AbstractProtocol.PROTOCOL_119)[0]));
    }

    public static int getOrCreateRuntimeId(AbstractProtocol protocol, boolean netease, int legacyId) {
        if (palettes.containsKey(protocol)) {
            AdvancedGlobalBlockPaletteInterface[] versions = palettes.get(protocol);
            if (versions.length > 1) {
                return netease ? versions[1].getOrCreateRuntimeId(legacyId) : versions[0].getOrCreateRuntimeId(legacyId);
            }
            return versions[0].getOrCreateRuntimeId(legacyId);
        } else {
            throw new RuntimeException("Advanced global block palette protocol " + protocol.name() + " not found");
        }
    }

    public static int getOrCreateRuntimeId(AbstractProtocol protocol, boolean netease, int id, int meta) {
        if (palettes.containsKey(protocol)) {
            AdvancedGlobalBlockPaletteInterface[] versions = palettes.get(protocol);
            if (versions.length > 1) {
                return netease ? versions[1].getOrCreateRuntimeId(id, meta) : versions[0].getOrCreateRuntimeId(id, meta);
            }
            return versions[0].getOrCreateRuntimeId(id, meta);
        } else {
            throw new RuntimeException("Advanced global block palette protocol " + protocol.name() + " not found");
        }
    }

    public static int getLegacyId(AbstractProtocol protocol, boolean netease, int runtimeId) {
        if (palettes.containsKey(protocol)) {
            AdvancedGlobalBlockPaletteInterface[] versions = palettes.get(protocol);
            if (versions.length > 1) {
                return netease ? versions[1].getLegacyId(runtimeId) : versions[0].getLegacyId(runtimeId);
            }
            return versions[0].getLegacyId(runtimeId);
        } else {
            throw new RuntimeException("Advanced global block palette protocol " + protocol.name() + " not found");
        }
    }

    public static byte[] getCompiledTable(AbstractProtocol protocol, boolean netease) {
        if (palettes.containsKey(protocol)) {
            AdvancedGlobalBlockPaletteInterface[] versions = palettes.get(protocol);
            if (versions.length > 1) {
                return netease ? versions[1].getCompiledTable() : versions[0].getCompiledTable();
            }
            return versions[0].getCompiledTable();
        } else {
            throw new RuntimeException("Advanced global block palette protocol " + protocol.name() + " not found");
        }
    }

    public static byte[] getCompiledItemDataPalette(AbstractProtocol protocol, boolean netease) {
        if (palettes.containsKey(protocol)) {
            AdvancedGlobalBlockPaletteInterface[] versions = palettes.get(protocol);
            if (versions.length > 1) {
                return netease ? versions[1].getItemDataPalette() : versions[0].getItemDataPalette();
            }
            return versions[0].getItemDataPalette();
        } else {
            throw new RuntimeException("Item data palette protocol " + protocol.name() + " not found");
        }
    }

    public static GlobalBlockPaletteInterface getStaticBlockPalette0(StaticVersion version) {
        return staticPalettes.get(version);
    }

    public static void init() { //检查数据
        if (!SynapseSharedConstants.CHECK_RESOURCE_DATA) {
            return;
        }
        PaletteBlockTable table112 = PaletteBlockTable.fromJson("block_state_list_112.json");
        PaletteBlockTable table113 = PaletteBlockTable.fromNBTOld("block_state_list_113.dat");
        PaletteBlockTable table114 = PaletteBlockTable.fromNBT("block_state_list_114.dat");
        PaletteBlockTable table116 = PaletteBlockTable.fromNBT("block_state_list_116.dat");
        PaletteBlockTable table11620 = PaletteBlockTable.fromNBT("block_state_list_11620.dat");
        PaletteBlockTable table116100 = PaletteBlockTable.fromNBTV3("block_state_list_116100.dat");

        PaletteBlockTable table112trimmed = table112.trim(table116100);
        PaletteBlockTable table113trimmed = table113.trim(table116100);
        PaletteBlockTable table114trimmed = table114.trim(table116100);
        PaletteBlockTable table116trimmed = table116.trim(table116100);
        PaletteBlockTable table11620trimmed = table11620.trim(table116100);
        //PaletteBlockTable table116100trimmed = table116100.trim(target);

        IntList ignoreIds = IntArrayList.wrap(new int[]{
                166, //荧光棒物品
                210, //允许方块
                211, //拒绝方块
                212, //边界方块
                217, //结构空位
                230, //黑板方块
                242, //相机方块
        });
        AtomicInteger index = new AtomicInteger(AbstractProtocol.PROTOCOL_112.ordinal());
        Stream.of(
                table112trimmed, //10
                table113trimmed, //11
                table114trimmed, //12
                table116trimmed, //14
                table11620trimmed //15
        ).forEach(trimmedTable -> {
            boolean[] existedIds = new boolean[256];

            trimmedTable.forEach(entry -> {
                if (entry.id < 256) {
                    existedIds[entry.id] = true;
                }
            });

            for (int i = 0; i < 256; i++) {
                if (!existedIds[i] && !ignoreIds.contains(i)) {
                    Server.getInstance().getLogger().warning("Block id does not exist: " + i + " (protocol " + AbstractProtocol.values0()[index.get()].name() + ")");
                }
            }

            if (index.incrementAndGet() == AbstractProtocol.PROTOCOL_114_60.ordinal() || index.get() == AbstractProtocol.PROTOCOL_116_200.ordinal()) {
                index.incrementAndGet();
            }
        });
    }
}

package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.Server;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.GlobalBlockPaletteJson;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.GlobalBlockPaletteNBT;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.GlobalBlockPaletteNBTOld;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockTable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class AdvancedGlobalBlockPalette {

    public static final Map<AbstractProtocol, AdvancedGlobalBlockPaletteInterface[]> palettes = new HashMap<>();

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

        PaletteBlockTable table116100 = PaletteBlockTable.fromNBTV3("block_state_list_116210.dat"); //TODO: Microjang hardcoded :( sort by block name )
        PaletteBlockTable table11620 = PaletteBlockTable.fromNBT("block_state_list_11620.dat");
        PaletteBlockTable table116 = PaletteBlockTable.fromNBT("block_state_list_116.dat");
        PaletteBlockTable table114 = PaletteBlockTable.fromNBT("block_state_list_114.dat");
        PaletteBlockTable table113 = PaletteBlockTable.fromNBTOld("block_state_list_113.dat");
        PaletteBlockTable table112 = PaletteBlockTable.fromJson("block_state_list_112.json");
        PaletteBlockTable target = table116100;

        palettes.put(AbstractProtocol.PROTOCOL_112, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_112, table112.trim(target), "runtime_item_ids_112.json")
        });
        palettes.put(AbstractProtocol.PROTOCOL_113, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBTOld(AbstractProtocol.PROTOCOL_113, table113.trim(target), "runtime_item_ids_112.json")
        });
        palettes.put(AbstractProtocol.PROTOCOL_114, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_114, table114.trim(target), "runtime_item_ids_114.json", true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_114_60, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_114_60, table114.trim(target), "runtime_item_ids_114.json", true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_116, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116, table116.trim(target), "runtime_item_ids_116.json", true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_116_20, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116_20, table11620.trim(target), "runtime_item_ids_116.json", true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_116_100_NE, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116_100_NE, table116.trim(target), "runtime_item_ids_116.json", true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_116_100, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116_100, table116100.trim(target), null, true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_116_200, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116_200, table116100.trim(target), null, true)
        });
        palettes.put(AbstractProtocol.PROTOCOL_116_210, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116_210, table116100.trim(target), null, true)
        });
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

    public static void init() { //检查数据
        PaletteBlockTable table112 = PaletteBlockTable.fromJson("block_state_list_112.json");
        PaletteBlockTable table113 = PaletteBlockTable.fromNBTOld("block_state_list_113.dat");
        PaletteBlockTable table114 = PaletteBlockTable.fromNBT("block_state_list_114.dat");
        PaletteBlockTable table116 = PaletteBlockTable.fromNBT("block_state_list_116.dat");
        PaletteBlockTable table11620 = PaletteBlockTable.fromNBT("block_state_list_11620.dat");
        PaletteBlockTable table116100 = PaletteBlockTable.fromNBTV3("block_state_list_116100.dat");
        PaletteBlockTable target = table116100;

        PaletteBlockTable table112trimmed = table112.trim(target);
        PaletteBlockTable table113trimmed = table113.trim(target);
        PaletteBlockTable table114trimmed = table114.trim(target);
        PaletteBlockTable table116trimmed = table116.trim(target);
        PaletteBlockTable table11620trimmed = table11620.trim(target);
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

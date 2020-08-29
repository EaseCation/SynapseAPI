package org.itxtech.synapseapi.multiprotocol.utils;

import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.GlobalBlockPaletteJson;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.GlobalBlockPaletteNBT;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.GlobalBlockPaletteNBTOld;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockTable;

import java.util.HashMap;
import java.util.Map;

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
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_114, table114.trim(table11620), "runtime_item_ids_114.json")
        });
        palettes.put(AbstractProtocol.PROTOCOL_114_60, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_114_60, table114.trim(table11620), "runtime_item_ids_114.json")
        });
        palettes.put(AbstractProtocol.PROTOCOL_116, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116, table116.trim(table11620), "runtime_item_ids_116.json")
        });
        palettes.put(AbstractProtocol.PROTOCOL_116_20, new AdvancedGlobalBlockPaletteInterface[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_116_20, table11620, "runtime_item_ids_116.json")
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

    public static String getName(AbstractProtocol protocol, boolean netease, int runtimeId) {
        if (palettes.containsKey(protocol)) {
            AdvancedGlobalBlockPaletteInterface[] versions = palettes.get(protocol);
            if (versions.length > 1) {
                return netease ? versions[1].getName(runtimeId) : versions[0].getName(runtimeId);
            }
            return versions[0].getName(runtimeId);
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

    public static void init() {
        //NOOP
    }
}

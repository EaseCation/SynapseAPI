package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.utils.BinaryStream;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.GlobalBlockPaletteJson;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.GlobalBlockPaletteNBT;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.GlobalBlockPaletteNBTOld;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface AdvancedGlobalBlockPalette {

    Map<AbstractProtocol, AdvancedGlobalBlockPalette[]> palettes = new HashMap<AbstractProtocol, AdvancedGlobalBlockPalette[]>() {{
        put(AbstractProtocol.PROTOCOL_16, new AdvancedGlobalBlockPalette[]{
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_16, "block_state_list_16.json"),
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_16, "block_state_list_16_netease.json")
        });
        put(AbstractProtocol.PROTOCOL_17, new AdvancedGlobalBlockPalette[]{
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_17, "block_state_list_17.json"),
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_17, "block_state_list_17_netease.json")
        });
        put(AbstractProtocol.PROTOCOL_18, new AdvancedGlobalBlockPalette[]{
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_18, "block_state_list_18.json"),
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_18, "block_state_list_18_netease.json")
        });
        put(AbstractProtocol.PROTOCOL_19, new AdvancedGlobalBlockPalette[]{
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_19, "block_state_list_19.json"),
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_19, "block_state_list_19_netease.json")
        });
        put(AbstractProtocol.PROTOCOL_110, new AdvancedGlobalBlockPalette[]{
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_110, "block_state_list_110.json")
        });
        put(AbstractProtocol.PROTOCOL_111, new AdvancedGlobalBlockPalette[]{
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_111, "block_state_list_111.json"),
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_111, "block_state_list_111_netease.json")
        });
        put(AbstractProtocol.PROTOCOL_112, new AdvancedGlobalBlockPalette[]{
                new GlobalBlockPaletteJson(AbstractProtocol.PROTOCOL_112, "block_state_list_112.json", "runtime_item_ids_112.json")
        });
        put(AbstractProtocol.PROTOCOL_113, new AdvancedGlobalBlockPalette[]{
                new GlobalBlockPaletteNBTOld(AbstractProtocol.PROTOCOL_113, "block_state_list_113.dat", "runtime_item_ids_112.json")
        });
        put(AbstractProtocol.PROTOCOL_114, new AdvancedGlobalBlockPalette[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_114, "block_state_list_114.dat", "runtime_item_ids_114.json")
        });
        put(AbstractProtocol.PROTOCOL_114_60, new AdvancedGlobalBlockPalette[]{
                new GlobalBlockPaletteNBT(AbstractProtocol.PROTOCOL_114_60, "block_state_list_114.dat", "runtime_item_ids_114.json")
        });
    }};

    int getOrCreateRuntimeId(int id, int meta);

    int getOrCreateRuntimeId(int legacyId);

    byte[] getCompiledTable();

    byte[] getItemDataPalette();

    static int getOrCreateRuntimeId(AbstractProtocol protocol, boolean netease, int legacyId) {
        if (palettes.containsKey(protocol)) {
            AdvancedGlobalBlockPalette[] versions = palettes.get(protocol);
            if (versions.length > 1) {
                return netease ? versions[1].getOrCreateRuntimeId(legacyId) : versions[0].getOrCreateRuntimeId(legacyId);
            }
            return versions[0].getOrCreateRuntimeId(legacyId);
        } else {
            throw new RuntimeException("Advanced global block palette protocol " + protocol.name() + " not found");
        }
    }

    static int getOrCreateRuntimeId(AbstractProtocol protocol, boolean netease, int id, int meta) {
        if (palettes.containsKey(protocol)) {
            AdvancedGlobalBlockPalette[] versions = palettes.get(protocol);
            if (versions.length > 1) {
                return netease ? versions[1].getOrCreateRuntimeId(id, meta) : versions[0].getOrCreateRuntimeId(id, meta);
            }
            return versions[0].getOrCreateRuntimeId(id, meta);
        } else {
            throw new RuntimeException("Advanced global block palette protocol " + protocol.name() + " not found");
        }
    }

    static byte[] getCompiledTable(AbstractProtocol protocol, boolean netease) {
        if (palettes.containsKey(protocol)) {
            AdvancedGlobalBlockPalette[] versions = palettes.get(protocol);
            if (versions.length > 1) {
                return netease ? versions[1].getCompiledTable() : versions[0].getCompiledTable();
            }
            return versions[0].getCompiledTable();
        } else {
            throw new RuntimeException("Advanced global block palette protocol " + protocol.name() + " not found");
        }
    }

    static byte[] getCompiledItemDataPalette(AbstractProtocol protocol, boolean netease) {
        if (palettes.containsKey(protocol)) {
            AdvancedGlobalBlockPalette[] versions = palettes.get(protocol);
            if (versions.length > 1) {
                return netease ? versions[1].getItemDataPalette() : versions[0].getItemDataPalette();
            }
            return versions[0].getItemDataPalette();
        } else {
            throw new RuntimeException("Item data palette protocol " + protocol.name() + " not found");
        }
    }

    default byte[] loadItemDataPalette(String jsonFile) {
        if (jsonFile == null || jsonFile.isEmpty()) return new byte[0];
        InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(jsonFile);
        if (stream == null) {
            throw new AssertionError("Unable to locate RuntimeID table: " + jsonFile);
        }
        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<ItemData>>() {
        }.getType();
        Collection<ItemData> entries = gson.fromJson(reader, collectionType);
        BinaryStream paletteBuffer = new BinaryStream();

        paletteBuffer.putUnsignedVarInt(entries.size());

        for (ItemData data : entries) {
            paletteBuffer.putString(data.name);
            paletteBuffer.putLShort(data.id);
        }

        return paletteBuffer.getBuffer();
    }

    class ItemData {
        private String name;
        private int id;
    }
}

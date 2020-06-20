package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.utils.BinaryStream;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.itxtech.synapseapi.SynapseAPI;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public interface AdvancedGlobalBlockPaletteInterface {

    int getOrCreateRuntimeId(int id, int meta);

    int getOrCreateRuntimeId(int legacyId);

    byte[] getCompiledTable();

    byte[] getItemDataPalette();

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

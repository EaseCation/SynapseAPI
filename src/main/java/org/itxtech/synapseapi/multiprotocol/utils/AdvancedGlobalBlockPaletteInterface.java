package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.block.Block;
import cn.nukkit.math.Mth;
import cn.nukkit.nbt.tag.CompoundTag;
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
//    int BLOCK_ID_COUNT = Mth.smallestEncompassingPowerOfTwo(9990); // damn netease micro_block
//    int BLOCK_ID_MASK = BLOCK_ID_COUNT - 1;
    int BLOCK_META_COUNT = Mth.smallestEncompassingPowerOfTwo(5469); // wtf cobblestone_wall
    int BLOCK_META_MASK = BLOCK_META_COUNT - 1;
    int BLOCK_META_BITS = Mth.log2PowerOfTwo(BLOCK_META_COUNT);
    int FULL_BLOCK_COUNT = Block.BLOCK_ID_COUNT << BLOCK_META_BITS;
    int FULL_BLOCK_MASK = FULL_BLOCK_COUNT - 1;
    int FULL_BLOCK_ID_MASK = Block.BLOCK_ID_MASK << BLOCK_META_BITS;

    CompoundTag UNKNOWN_BLOCK_STATE = new CompoundTag()
            .putString("name", "minecraft:info_update")
            .putShort("val", 0);

    int getOrCreateRuntimeId(int id, int meta);

    int getOrCreateRuntimeId(int legacyId);

    int getLegacyId(int runtimeId);

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
        public String name;
        public int id;
        public boolean componentBased = false;
    }

}

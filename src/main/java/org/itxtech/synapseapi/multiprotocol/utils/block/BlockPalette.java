package org.itxtech.synapseapi.multiprotocol.utils.block;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockTable.DumpJsonTableEntry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

@Log4j2
@ToString
public class BlockPalette {
    public final List<BlockData> palette = new ObjectArrayList<>();

    public static BlockPalette fromNBT(String file) {
        BlockPalette instance = new BlockPalette();
        ListTag<CompoundTag> root;
        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(file)) {
            if (stream == null) {
                throw new AssertionError("Unable to locate block_palette.nbt: " + file);
            }
            //noinspection unchecked
            root = (ListTag<CompoundTag>) NBTIO.readTag(new ByteArrayInputStream(ByteStreams.toByteArray(stream)), ByteOrder.LITTLE_ENDIAN, false);
        } catch (IOException e) {
            throw new AssertionError("Unable to load block_palette.nbt: " + file, e);
        }
        int runtimeIdAllocator = 0;
        for (CompoundTag entry : root.getAllUnsafe()) {
            CompoundTag blockData = entry.getCompound("block");
            instance.palette.add(new BlockData(blockData.getString("name"), blockData.getCompound("states"), runtimeIdAllocator++));
        }
        return instance;
    }

    public static BlockPalette fromJson(String file) {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<DumpJsonTableEntry>>() {
        }.getType();
        Collection<DumpJsonTableEntry> entries;
        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(file)) {
            if (stream == null) {
                throw new AssertionError("Unable to locate block_palette.json: " + file);
            }
            try (Reader reader = new InputStreamReader(new ByteArrayInputStream(ByteStreams.toByteArray(stream)), StandardCharsets.UTF_8)) {
                entries = gson.fromJson(reader, collectionType);
            }
            BlockPalette instance = new BlockPalette();
            int runtimeIdAllocator = 0;
            for (DumpJsonTableEntry entry : entries) {
                CompoundTag states = new CompoundTag("states", new Object2ObjectOpenHashMap<>(entry.states.size()));
                entry.states.forEach(state -> {
                    switch (state.type) {
                        case "string":
                            states.putString(state.name, (String) state.value);
                            break;
                        case "byte":
                            states.putByte(state.name, ((Number) state.value).intValue());
                            break;
                        case "int":
                            states.putInt(state.name, ((Number) state.value).intValue());
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown block state type: " + state.type);
                    }
                });
                instance.palette.add(new BlockData(entry.name, states, runtimeIdAllocator++, entry.id, entry.val));
            }
            return instance;
        } catch (Exception e) {
            throw new AssertionError("Unable to load block_palette.json: " + file, e);
        }
    }

    @ToString
    @AllArgsConstructor
    public static class BlockData {
        public final String name;
        public final CompoundTag states;
        public final int runtimeId;

        public int id;
        public int val;

        public BlockData(String name, CompoundTag states, int runtimeId) {
            this(name, states, runtimeId, -1, -1);
        }
    }
}

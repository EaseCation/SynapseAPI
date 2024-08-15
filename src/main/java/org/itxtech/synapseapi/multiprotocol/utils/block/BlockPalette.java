package org.itxtech.synapseapi.multiprotocol.utils.block;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.utils.Hash;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockTable.DumpJsonTableEntry;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

@Log4j2
@ToString
public class BlockPalette {
    public final List<BlockData> palette = new ObjectArrayList<>();

    /**
     * Custom block definitions.
     */
    public final List<BlockProperty> properties = new ObjectArrayList<>();

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

    public static BlockPalette fromVanillaNBT(String file) {
        return fromVanillaNBT(file, false);
    }

    public static BlockPalette fromVanillaNBT(String file, boolean gzip) {
        BlockPalette instance = new BlockPalette();
        CompoundTag root;
        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(file)) {
            if (stream == null) {
                throw new AssertionError("Unable to locate block_palette.nbt: " + file);
            }
            root = NBTIO.read(gzip ? new BufferedInputStream(new GZIPInputStream(stream)) : new ByteArrayInputStream(ByteStreams.toByteArray(stream)));
        } catch (IOException e) {
            throw new AssertionError("Unable to load block_palette.nbt: " + file, e);
        }
        int runtimeIdAllocator = 0;
        for (CompoundTag block : root.getList("blocks", CompoundTag.class).getAllUnsafe()) {
            instance.palette.add(new BlockData(block.getString("name"), block.getCompound("states"), runtimeIdAllocator++));
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

    public ListTag<CompoundTag> toNBT() {
        return toNBT(0);
    }

    public ListTag<CompoundTag> toNBT(int version) {
        ListTag<CompoundTag> container = new ListTag<>();
        for (BlockData block : palette) {
            CompoundTag tag = new CompoundTag()
                    .putString("name", block.name)
                    .putCompound("states", block.states);
            if (version != 0) {
                tag.putInt("version", version);
            }
            container.add(new CompoundTag().putCompound("block", tag));
        }
        return container;
    }

    public CompoundTag toVanillaNBT() {
        return toVanillaNBT(0);
    }

    public CompoundTag toVanillaNBT(int version) {
        ListTag<CompoundTag> container = new ListTag<>();
        for (BlockData block : palette) {
            CompoundTag tag = new CompoundTag()
                    .putString("name", block.name)
                    .putCompound("states", block.states);
            if (version != 0) {
                tag.putInt("version", version);
            }
            container.add(tag);
        }
        return new CompoundTag().putList("blocks", container);
    }

    public List<Map<String, Object>> toJson() {
        List<Map<String, Object>> container = new ArrayList<>();
        for (BlockData block : palette) {
            List<Map<String, Object>> states = new ArrayList<>();
            for (Entry<String, Tag> entry : new CompoundTag(new TreeMap<>(block.states.getTagsUnsafe())).entrySet()) {
                Tag tag = entry.getValue();
                Map<String, Object> state = new LinkedHashMap<>();
                state.put("name", entry.getKey());
                state.put("type", switch (tag.getId()) {
                    case Tag.TAG_String -> "string";
                    case Tag.TAG_Byte -> "byte";
                    case Tag.TAG_Int -> "int";
                    default -> throw new IllegalArgumentException("Unexpected tag type: " + Tag.getTagName(tag.getId()));
                });
                state.put("value", tag.parseValue());
                states.add(state);
            }

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", block.name);
            entry.put("states", states);
            container.add(entry);
        }
        return container;
    }

    public Map<String, List<Map<String, Object>>> toVanillaJson() {
        Map<String, List<Map<String, Object>>> container = new HashMap<>();
        container.put("blocks", toJson());
        return container;
    }

    public void sortLegacy() {
        //TODO: 太旧的版本不需要做了
    }

    /**
     * @since 1.18.30
     */
    public void sortHash() {
        sort(new Object2ObjectRBTreeMap<>((o1, o2) -> Long.compareUnsigned(Hash.hashIdentifier(o1), Hash.hashIdentifier(o2))));
    }

    private void sort(Map<String, List<BlockData>> runtimePalette) {
        String lastName = null;
        List<BlockData> group = new ObjectArrayList<>();
        for (BlockData entry : palette) {
            String name = entry.name;

            if (lastName != null && !name.equals(lastName)) {
                runtimePalette.put(lastName, group);
                group = new ObjectArrayList<>();
            }

            group.add(entry);

            lastName = name;
        }
        if (lastName != null) {
            runtimePalette.put(lastName, group);
        }

        palette.clear();
        int runtimeId = 0;
        for (Entry<String, List<BlockData>> entry : runtimePalette.entrySet()) {
            for (BlockData data : entry.getValue()) {
                palette.add(new BlockData(data, runtimeId++));
            }
        }
    }

    public BlockPalette toNetease() {
        BlockPalette netease = new BlockPalette();
        netease.palette.addAll(this.palette);
        netease.properties.addAll(this.properties);

        netease.palette.add(new BlockData("minecraft:mod_ore", 230));
        netease.palette.add(new BlockData("minecraft:micro_block", 9990));

        netease.sortHash();
        return netease;
    }

    @ToString
    @AllArgsConstructor
    public static class BlockData {
        public final String name;
        public final CompoundTag states;
        public final int runtimeId;

        public int id;
        public int val;

        public BlockData(String name, CompoundTag states) {
            this(name, states, -1);
        }

        public BlockData(String name, CompoundTag states, int runtimeId) {
            this(name, states, runtimeId, -1, -1);
        }

        BlockData(String name, int id) {
            this(name, new CompoundTag("states"), -1, id, 0);
        }

        private BlockData(BlockData other, int newRuntimeId) {
            this(other.name, other.states, newRuntimeId, other.id, other.val);
        }
    }
}

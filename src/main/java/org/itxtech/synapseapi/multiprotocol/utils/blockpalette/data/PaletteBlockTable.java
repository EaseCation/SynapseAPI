package org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data;

import cn.nukkit.block.Block;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.Tag;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

@Log4j2
public class PaletteBlockTable extends ArrayList<PaletteBlockData> {

    private PaletteBlockTable() { }

    public ListTag<CompoundTag> toTag() {
        ListTag<CompoundTag> list = new ListTag<>();
        for (PaletteBlockData data : this) {
            CompoundTag tag = new CompoundTag();
            tag.putShort("id", data.id);
            tag.putCompound("block", data.block.toTag());
            list.add(tag);
        }
        return list;
    }

    public static PaletteBlockTable fromNBTV3(String file) {
        return fromNBTV3(file, false);
    }

    public static PaletteBlockTable fromNBTV3(String file, boolean newName) {
        PaletteBlockTable table = new PaletteBlockTable();

        PaletteBlockData air = PaletteBlockData.createUnknownData();
        PaletteBlockData unknown = new PaletteBlockData(Short.MAX_VALUE, new PaletteBlockData.LegacyStates[0], new PaletteBlockData.Block("minecraft:info_update", 1, new ArrayList<>()));

        ListTag<CompoundTag> tag;
        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(file)) {
            if (stream == null) {
                throw new AssertionError("Unable to locate block state nbt");
            }
            //noinspection unchecked
            tag = (ListTag<CompoundTag>) NBTIO.readTag(new ByteArrayInputStream(ByteStreams.toByteArray(stream)), ByteOrder.LITTLE_ENDIAN, false);
        } catch (IOException e) {
            throw new AssertionError("Unable to load block palette", e);
        }

        for (CompoundTag state : tag.getAll()) {
            CompoundTag blockTag = state.getCompound("block");

            if (!state.contains("LegacyStates")) {
                String name = blockTag.getString("name");
                int id;
                try {
                    id = newName ? GlobalBlockPalette.getBlockIdByNewName(name) : GlobalBlockPalette.getBlockIdByName(name);
                } catch (NoSuchElementException e) {
                    //table.add(air);
                    table.add(unknown);
                    continue;
                }
                List<Tag> statesData = new ArrayList<>();
                blockTag.getCompound("states").getTags().forEach((stateName, stateValue) -> statesData.add(stateValue));
                table.add(new PaletteBlockData(id, null, new PaletteBlockData.Block(name, blockTag.getInt("version"), statesData)));
                continue;
            }

            PaletteBlockData.LegacyStates[] legacyStates;
            List<PaletteBlockData.LegacyStates> legacyStatesList = new ArrayList<>();
            state.getList("LegacyStates", CompoundTag.class).getAll()
                    .forEach(legacy -> legacyStatesList.add(new PaletteBlockData.LegacyStates(legacy.getInt("id"), legacy.getShort("val"))));
            legacyStates = legacyStatesList.toArray(new PaletteBlockData.LegacyStates[0]);

            // Resolve to first legacy id
            PaletteBlockData.LegacyStates firstState = legacyStates[0];

            CompoundTag blockStatesTag = blockTag.getCompound("states");
            List<Tag> statesData = new ArrayList<>();
            blockStatesTag.getTags().forEach((stateName, stateValue) -> statesData.add(stateValue));
            PaletteBlockData.Block block = new PaletteBlockData.Block(blockTag.getString("name"), blockTag.getInt("version"), statesData);

            PaletteBlockData data = new PaletteBlockData(
                    firstState.id,
                    legacyStates,
                    block
            );
            table.add(data);

            if (firstState.id == Block.AIR) {
                air.legacyStates = data.legacyStates;
                air.id = data.id;
                air.block = data.block;

                unknown.block.version = data.block.version;
            }
        }
        return table;
    }

    public static PaletteBlockTable fromNBT(String file) {
        PaletteBlockTable table = new PaletteBlockTable();
        ListTag<CompoundTag> tag;
        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(file)) {
            if (stream == null) {
                throw new AssertionError("Unable to locate block state nbt");
            }
            //noinspection unchecked
            tag = (ListTag<CompoundTag>) NBTIO.readTag(new ByteArrayInputStream(ByteStreams.toByteArray(stream)), ByteOrder.LITTLE_ENDIAN, false);
        } catch (IOException e) {
            throw new AssertionError("Unable to load block palette", e);
        }

        for (CompoundTag state : tag.getAll()) {
            PaletteBlockData.LegacyStates[] legacyStates = null;
            if (state.contains("LegacyStates")) {
                List<PaletteBlockData.LegacyStates> legacyStatesList = new ArrayList<>();
                state.getList("LegacyStates", CompoundTag.class).getAll()
                        .forEach(legacy -> legacyStatesList.add(new PaletteBlockData.LegacyStates(legacy.getInt("id"), legacy.getShort("val"))));
                legacyStates = legacyStatesList.toArray(new PaletteBlockData.LegacyStates[0]);
                // Resolve to first legacy id
            }
            CompoundTag blockTag = state.getCompound("block");
            CompoundTag blockStatesTag = blockTag.getCompound("states");
            List<Tag> statesData = new ArrayList<>();
            blockStatesTag.getTags().forEach((stateName, stateValue) -> statesData.add(stateValue));
            PaletteBlockData.Block block = new PaletteBlockData.Block(blockTag.getString("name"), blockTag.getInt("version"), statesData);

            PaletteBlockData data = new PaletteBlockData(
                    state.getShort("id"),
                    legacyStates,
                    block
            );
            table.add(data);

            if (legacyStates != null && legacyStates.length != 0) {
                PaletteBlockData.LegacyStates firstState = legacyStates[0];
                if (firstState.id == Block.LOG || firstState.id == Block.LOG2) { // 六面树皮的中国版临时修复
                    if (firstState.val < 4) {
                        table.add(new PaletteBlockData(
                                firstState.id,
                                new PaletteBlockData.LegacyStates[]{new PaletteBlockData.LegacyStates(firstState.id, firstState.val | 0b1100)},
                                block
                        ));
                        log.debug("Manual mapping: id {} meta {} to id {} meta {}", firstState.id, firstState.val | 0b1100, firstState.id, firstState.val);
                    }
                }
            }
        }
        return table;
    }

    public static PaletteBlockTable fromNBTOld(String file) {
        InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(file);
        if (stream == null) {
            throw new AssertionError("Unable to locate block state nbt");
        }
        PaletteBlockTable table = new PaletteBlockTable();
        ListTag<CompoundTag> tag;
        byte[] data0;
        try {
            //noinspection UnstableApiUsage
            data0 = ByteStreams.toByteArray(stream);
            //noinspection unchecked
            tag = (ListTag<CompoundTag>) NBTIO.readTag(new ByteArrayInputStream(data0), ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        for (CompoundTag state : tag.getAll()) {
            int id = state.getShort("id");
            PaletteBlockData.LegacyStates[] legacyStatesData = null;
            if (state.contains("meta")) {
                List<PaletteBlockData.LegacyStates> legacyStatesList = new ArrayList<>();
                int[] meta = state.getIntArray("meta");
                for (int m : meta) {
                    legacyStatesList.add(new PaletteBlockData.LegacyStates(id, m));
                }
                legacyStatesData = legacyStatesList.toArray(new PaletteBlockData.LegacyStates[0]);
            }
            CompoundTag blockTag = state.getCompound("block");
            CompoundTag blockStatesTag = blockTag.getCompound("states");
            List<Tag> statesData = new ArrayList<>();
            blockStatesTag.getTags().forEach((stateName, stateValue) -> statesData.add(stateValue));
            PaletteBlockData.Block block = new PaletteBlockData.Block(blockTag.getString("name"), blockTag.getInt("version"), statesData);

            PaletteBlockData data = new PaletteBlockData(
                    state.getShort("id"),
                    legacyStatesData,
                    block
            );
            table.add(data);
        }
        return table;
    }

    public static PaletteBlockTable fromJson(String file) {
        InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(file);
        if (stream == null) {
            throw new AssertionError("Unable to locate RuntimeID table (.json)");
        }

        PaletteBlockTable table = new PaletteBlockTable();

        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<JsonTableEntry>>(){}.getType();
        Collection<JsonTableEntry> entries = gson.fromJson(reader, collectionType);

        for (JsonTableEntry entry : entries) {
            table.add(
                    new PaletteBlockData(
                            entry.id,
                            new PaletteBlockData.LegacyStates[]{new PaletteBlockData.LegacyStates(entry.id, entry.data)},
                            new PaletteBlockData.Block(entry.name, 0, new ArrayList<>())
                    )
            );
        }

        return table;
    }

    public static PaletteBlockTable fromNetEaseJson(String file) {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<NetEaseJsonTableEntry>>() {
        }.getType();

        Collection<NetEaseJsonTableEntry> entries;

        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(file)) {
            if (stream == null) {
                throw new AssertionError("Unable to locate runtime_block_ids.json (netease json)");
            }
            try (Reader reader = new InputStreamReader(new ByteArrayInputStream(ByteStreams.toByteArray(stream)), StandardCharsets.UTF_8)) {
                entries = gson.fromJson(reader, collectionType);
            }
            PaletteBlockTable table = new PaletteBlockTable();
            for (NetEaseJsonTableEntry entry : entries) {
                table.add(
                        new PaletteBlockData(
                                entry.blockId,
                                new PaletteBlockData.LegacyStates[]{new PaletteBlockData.LegacyStates(entry.blockId, entry.meta)},
                                new PaletteBlockData.Block(entry.blockName, 0, new ArrayList<>())
                        )
                );
            }
            return table;
        } catch (IOException e) {
            throw new AssertionError("Unable to load block palette", e);
        }
    }

    public static PaletteBlockTable fromDumpJson(String file) {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<DumpJsonTableEntry>>() {
        }.getType();

        Collection<DumpJsonTableEntry> entries;

        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream(file)) {
            if (stream == null) {
                throw new AssertionError("Unable to locate block_palette.json (dump json)");
            }
            try (Reader reader = new InputStreamReader(new ByteArrayInputStream(ByteStreams.toByteArray(stream)), StandardCharsets.UTF_8)) {
                entries = gson.fromJson(reader, collectionType);
            }
            PaletteBlockTable table = new PaletteBlockTable();
            for (DumpJsonTableEntry entry : entries) {
                table.add(
                        new PaletteBlockData(
                                entry.id,
                                new PaletteBlockData.LegacyStates[]{new PaletteBlockData.LegacyStates(entry.id, entry.val)},
                                new PaletteBlockData.Block(entry.name, 0, new ArrayList<>())
                        )
                );
            }
            return table;
        } catch (IOException e) {
            throw new AssertionError("Unable to load block palette", e);
        }
    }

    @ToString
    static class JsonTableEntry {
        private int id;
        private int data;
        private String name;
    }

    @ToString
    static class NetEaseJsonTableEntry {
        private int runTimeId;
        private int blockId;
        private int meta;
        private String blockName;
    }

    @ToString
    static class DumpJsonTableEntry {
        private String name;
        private int val;
        private List<StateEntry> states;
        private int id;
    }

    @ToString
    static class StateEntry {
        private String name;
        private String type;
        private Object value;
    }

    public PaletteBlockTable trim(PaletteBlockTable according) {
        if (this == according) return this;

        PaletteBlockTable table = new PaletteBlockTable();
        for (PaletteBlockData accordingData : according) {
            PaletteBlockData find = this.stream()
                    .filter(data -> data.id == accordingData.id)
                    //.filter(data -> data.block.equals(accordingData.block))
                    .filter(
                            data -> data.legacyStates != null && accordingData.legacyStates != null
                                    &&
                                    Arrays.stream(data.legacyStates)
                                            .anyMatch(l ->
                                                    Arrays.stream(accordingData.legacyStates)
                                                            .anyMatch(l0 -> l.id == l0.id && l.val == l0.val)
                                            )
                    )
                    .findFirst().orElse(PaletteBlockData.AIR);
            table.add(find);
        }

        this.forEach(data -> {
            if (!table.contains(data)) table.add(data);
        });

        return table;
    }

    public PaletteBlockTable cutAdvanced() {
        PaletteBlockTable table = new PaletteBlockTable();
        for (PaletteBlockData data : this) {
            if (data.id < 256) table.add(data);
        }
        return table;
    }

}

package org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.utils.BinaryStream;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.GlobalBlockPaletteJson;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    private static class JsonTableEntry {
        private int id;
        private int data;
        private String name;
    }

    public PaletteBlockTable trim(PaletteBlockTable according) {
        PaletteBlockTable table = new PaletteBlockTable();
        for (PaletteBlockData accordingData : according) {
            PaletteBlockData find = this.stream()
                    .filter(data -> data.id == accordingData.id)
                    .filter(data -> data.block.equals(accordingData.block))
                    .findFirst().orElse(PaletteBlockData.AIR);
            table.add(find);
        }
        return table;
    }
}

package org.itxtech.synapseapi.multiprotocol.utils.blockpalette;

import cn.nukkit.Server;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.BinaryStream;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPaletteInterface;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockData;
import org.itxtech.synapseapi.multiprotocol.utils.blockpalette.data.PaletteBlockTable;

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
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalBlockPaletteHardcode implements AdvancedGlobalBlockPaletteInterface { //TODO: 1.16.100

    final Int2IntMap legacyToRuntimeId = new Int2IntOpenHashMap();
    final Int2IntMap runtimeIdToLegacy = new Int2IntOpenHashMap();
    final Int2ObjectMap<String> runtimeIdToString = new Int2ObjectOpenHashMap<>();
    final Object2IntMap<String> stringToRuntimeId = new Object2IntOpenHashMap<>();
    final Int2ObjectMap<CompoundTag> runtimeIdToState = new Int2ObjectOpenHashMap<>();
    final AtomicInteger runtimeIdAllocator = new AtomicInteger(0);
    final byte[] compiledTable;
    final byte[] itemDataPalette;

    public GlobalBlockPaletteHardcode(AbstractProtocol protocol, String blockPaletteFile) {
        this(protocol, blockPaletteFile, null);
    }

    public GlobalBlockPaletteHardcode(AbstractProtocol protocol, PaletteBlockTable blockTable, String itemDataPaletteJsonFile) {
        Server.getInstance().getLogger().info("Loading Advanced Global Block Palette from PaletteBlockTable(nbt)");
        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);
        stringToRuntimeId.defaultReturnValue(-1);

        try {
            compiledTable = NBTIO.write(blockTable.toTag(), ByteOrder.LITTLE_ENDIAN, true);
            for (int i = 0; i < blockTable.size(); i++) {
                PaletteBlockData data = blockTable.get(i);

                String name = data.block.name;
                stringToRuntimeId.put(name, i);
                runtimeIdToString.put(i, name);

                if (data.legacyStates != null && data.legacyStates.length > 0) {
                    runtimeIdToLegacy.put(i, data.legacyStates[0].id << 6 | (short) data.legacyStates[0].val);
                    for (PaletteBlockData.LegacyStates legacyState : data.legacyStates) {
                        int legacyId = legacyState.id << 6 | (short) legacyState.val;
                        legacyToRuntimeId.put(legacyId, i);
                    }
                }
            }
        } catch (IOException e) {
            throw new AssertionError("Unable to write block palette", e);
        }
        itemDataPalette = loadItemDataPalette(itemDataPaletteJsonFile);
    }

    public GlobalBlockPaletteHardcode(AbstractProtocol protocol, String blockPaletteFile, String itemDataPaletteJsonFile) {
        Server.getInstance().getLogger().info("Loading Advanced Global Block Palette from " + blockPaletteFile);
        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);
        stringToRuntimeId.defaultReturnValue(-1);

        compiledTable = loadBlockPaletteNBT(protocol, blockPaletteFile);
        itemDataPalette = loadItemDataPalette(itemDataPaletteJsonFile);
    }

    private byte[] loadBlockPaletteNBT(AbstractProtocol protocol, String file) {
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
            int runtimeId = runtimeIdAllocator.getAndIncrement();
            runtimeIdToState.put(runtimeId, state);

            String name = state.getCompound("block").getString("name");
            stringToRuntimeId.put(name, runtimeId);
            runtimeIdToString.put(runtimeId, name);

            if (!state.contains("LegacyStates")) continue;

            List<CompoundTag> legacyStates = state.getList("LegacyStates", CompoundTag.class).getAll();

            // Resolve to first legacy id
            CompoundTag firstState = legacyStates.get(0);
            runtimeIdToLegacy.put(runtimeId, firstState.getInt("id") << 6 | firstState.getShort("val"));

            for (CompoundTag legacyState : legacyStates) {
                int legacyId = legacyState.getInt("id") << 6 | legacyState.getShort("val");
                legacyToRuntimeId.put(legacyId, runtimeId);
            }
            //state.remove("LegacyStates"); // No point in sending this since the client doesn't use it.
        }

        try {
            return NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException e) {
            throw new AssertionError("Unable to write block palette", e);
        }
    }

    @Override
    public int getOrCreateRuntimeId(int id, int meta) {
        int legacyId = id << 6 | meta;
        int runtimeId = legacyToRuntimeId.get(legacyId);
        if (runtimeId == -1) {
            runtimeId = legacyToRuntimeId.get(id << 6);
            if (runtimeId == -1) {
                throw new NoSuchElementException("Unmapped block registered id:" + id + " meta:" + meta);
            }
        }
        return runtimeId;
    }

    @Override
    public int getOrCreateRuntimeId(int legacyId) throws NoSuchElementException {
        return getOrCreateRuntimeId(legacyId >> 4, legacyId & 0xf);
    }

    @Override
    public int getLegacyId(int runtimeId) {
        return runtimeIdToLegacy.get(runtimeId);
    }

    @Override
    public String getName(int runtimeId) {
        String name = runtimeIdToString.get(runtimeId);
        return name == null ? "minecraft:air" : name;
    }

    @Override
    public byte[] getCompiledTable() {
        return compiledTable;
    }

    @Override
    public byte[] getItemDataPalette() {
        return itemDataPalette;
    }

    @Override
    public byte[] loadItemDataPalette(String jsonFile) {
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
            paletteBuffer.putBoolean(data.componentBased);
        }

        return paletteBuffer.getBuffer();
    }
}

package org.itxtech.synapseapi.multiprotocol.utils.block;

import cn.nukkit.GameVersion;
import cn.nukkit.block.BlockUpgrader;
import cn.nukkit.block.BlockUpgrader.BedrockBlockUpgrader;
import cn.nukkit.nbt.stream.NBTInputStream;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.IntTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.utils.Binary;
import cn.nukkit.utils.VarInt;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockUpgradeSchema.BlockFlatten;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockUpgradeSchema.BlockRemap;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockUpgradeSchema.ValueRemap;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import static cn.nukkit.GameVersion.*;

@Log4j2
public final class VanillaBlockUpgrader {
    private static final int ALLOWED_VERSION = (1 << 24) | (18 << 16) | (0 << 8) | (0 << 0); //TODO: Chunker.app temporary HACK

    private static final Map<GameVersion, List<BlockUpgradeSchema>> SCHEMAS = new EnumMap<>(GameVersion.class);
    private static final List<BlockUpgradeSchema> UPGRADE_SCHEMAS = new ObjectArrayList<>();
    private static int CURRENT_VERSION;

    private static final Map<String, BlockData[]> LEGACY_TO_CURRENT = new Object2ObjectOpenHashMap<>();

    private static final Gson GSON = new Gson();

    public static int getCurrentVersion() {
        return CURRENT_VERSION;
    }

    public static List<BlockUpgradeSchema> getSchemas(Predicate<GameVersion> filter) {
        List<BlockUpgradeSchema> schemas = new ObjectArrayList<>();
        for (Entry<GameVersion, List<BlockUpgradeSchema>> entry : SCHEMAS.entrySet()) {
            if (!filter.test(entry.getKey())) {
                continue;
            }
            schemas.addAll(entry.getValue());
        }
        return schemas;
    }

    public static void upgrade(CompoundTag tag) {
        upgrade(tag, UPGRADE_SCHEMAS);
    }

    public static void upgrade(CompoundTag tag, Predicate<GameVersion> filter) {
        upgrade(tag, getSchemas(filter));
    }

    public static void upgrade(CompoundTag tag, List<BlockUpgradeSchema> schemas) {
        int version = tag.getInt("version");
//        if (version > CURRENT_VERSION) {
        if (version > ALLOWED_VERSION) {
            log.warn("Unsupported blockstate version: {}", version);
            return;
        }

        if (version == 0) {
            String name = tag.getString("name");
            if (name.isEmpty()) {
                log.debug("Block 'name' tag is missing");
                unknownBlock(tag);
                return;
            }

            BlockData[] metaMapping = LEGACY_TO_CURRENT.get(name);
            if (metaMapping == null) {
                log.trace("Unmapped (1.12) block name: {}", name);
                unknownBlock(tag);
                return;
            }

            int meta = tag.getShort("val");
            if (meta < 0 || meta >= metaMapping.length) {
                log.debug("Unmapped block aux value: name {}, val {}", name, meta);
                meta = 0;
            }

            BlockData block = metaMapping[meta];
            tag.putString("name", block.name);
            tag.putCompound("states", block.states.clone());
            tag.putInt("version", block.version);
            tag.remove("val");
            return;
        }

        if (!tag.getString("name").startsWith("minecraft:")) {
            // custom block
            tag.putInt("version", CURRENT_VERSION);
            return;
        }

        SCHEMAS:
        for (BlockUpgradeSchema schema : schemas) {
            if (version > schema.getVersion()) {
                // even if this is actually the same version, we have to apply it anyway because mojang are dumb and
                // didn't always bump the blockstate version when changing it :(
                continue;
            }

            String name = tag.getString("name");
            CompoundTag states = tag.getCompound("states");

            BlockRemap[] remappedStates = schema.getRemappedStates().get(name);
            if (remappedStates != null) {
                REMAP:
                for (BlockRemap remap : remappedStates) {
                    CompoundTag oldStates = remap.getOldStates();

                    if (oldStates.size() > states.size()) {
                        // match criteria has more requirements than we have state properties
                        continue;
                    }

                    for (Entry<String, Tag> entry : oldStates.getTagsUnsafe().entrySet()) {
                        if (!entry.getValue().equals(states.get(entry.getKey()))) {
                            continue REMAP;
                        }
                    }

                    String newName = remap.getNewName();

                    if (newName == null) {
                        BlockFlatten flatten = remap.getNewFlattenedName();
                        String stateName = flatten.getFlattenedProperty();

                        Tag flattened = states.get(stateName);
                        String value;
                        if (flattened instanceof StringTag stringTag) {
                            value = stringTag.data;
                        } else if (flattened instanceof IntTag intTag) {
                            value = String.valueOf(intTag.data);
                        } else {
                            // flattened property is not a stringable tag, so this transformation is not applicable
                            continue;
                        }
                        newName = flatten.getPrefix() + flatten.getFlattenedValueRemaps().getOrDefault(value, value) + flatten.getSuffix();

                        states.remove(stateName);
                    }

                    tag.putString("name", newName);

                    CompoundTag newStates = remap.getNewStates();
                    if (newStates != null) {
                        newStates = newStates.clone();
                    } else {
                        newStates = new CompoundTag();
                    }

                    String[] copiedStates = remap.getCopiedStates();
                    if (copiedStates != null) {
                        for (String stateName : copiedStates) {
                            Tag state = states.get(stateName);
                            if (state != null) {
                                newStates.put(stateName, state.copy());
                            }
                        }
                    }

                    tag.putCompound("states", newStates);
                    continue SCHEMAS;
                }
            }

            boolean dirty = false;

            String[] removedProperties = schema.getRemovedProperties().get(name);
            if (removedProperties != null) {
                for (String stateName : removedProperties) {
                    if (states.removeAndGet(stateName) != null) {
                        dirty = true;
                    }
                }
            }

            Map<String, Tag> addedProperties = schema.getAddedProperties().get(name);
            if (addedProperties != null) {
                for (Entry<String, Tag> added : addedProperties.entrySet()) {
                    if (states.putIfAbsent(added.getKey(), added.getValue().copy()) == null) {
                        dirty = true;
                    }
                }
            }

            Map<String, ValueRemap[]> remappedPropertyValues = schema.getRemappedPropertyValues().get(name);
            if (remappedPropertyValues != null) {
                for (Entry<String, ValueRemap[]> remap : remappedPropertyValues.entrySet()) {
                    String stateName = remap.getKey();

                    Tag oldValue = states.get(stateName);
                    if (oldValue == null) {
                        continue;
                    }

                    ValueRemap[] pairs = remap.getValue();
                    for (ValueRemap pair : pairs) {
                        if (!oldValue.equals(pair.getOldValue())) {
                            continue;
                        }

                        states.put(stateName, pair.getNewValue().copy());
                        dirty = true;
                        break;
                    }
                }
            }
            // value remaps are always indexed by old property name

            Map<String, String> renamedProperties = schema.getRenamedProperties().get(name);
            if (renamedProperties != null) {
                for (Entry<String, String> rename : renamedProperties.entrySet()) {
                    String oldName = rename.getKey();
                    Tag oldValue = states.removeAndGet(oldName);
                    if (oldValue == null) {
                        continue;
                    }

                    states.put(rename.getValue(), oldValue);
                    dirty = true;
                }
            }

            if (dirty) {
                tag.putCompound("states", states);
            }

            String newName = schema.getRenamedIds().get(name);
            if (newName != null) {
                tag.putString("name", newName);
            }
        }

        tag.putInt("version", CURRENT_VERSION);
    }

    private static void unknownBlock(CompoundTag tag) {
        tag.remove("val");

        BlockUtil.unknownBlock(tag);
    }

    private static BlockUpgradeSchema addSchema(String file, GameVersion baseGameVersion) {
        JsonObject json;
        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(ByteStreams.toByteArray(SynapseAPI.class.getClassLoader().getResourceAsStream("block_upgrade_schemas/" + file))))) {
            json = GSON.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            throw new AssertionError("Unable to load block upgrade schema: " + file, e);
        }

        BlockUpgradeSchema schema = new BlockUpgradeSchema(json);
        if (baseGameVersion.isAvailable()) {
            UPGRADE_SCHEMAS.add(schema);

            int version = schema.getVersion();
            if (version > CURRENT_VERSION) {
                CURRENT_VERSION = version;
            }
        }

        List<BlockUpgradeSchema> schemas = SCHEMAS.computeIfAbsent(baseGameVersion, k -> new ObjectArrayList<>());
        schemas.add(schema);

        return schema;
    }

    static {
        log.debug("Loading vanilla block upgrader...");

        Map<String, List<CompoundTag>> legacyToCurrent = new Object2ObjectOpenHashMap<>();
        try (InputStream stream = new ByteArrayInputStream(ByteStreams.toByteArray(SynapseAPI.class.getClassLoader().getResourceAsStream("legacy_block_to_states.bin")))) {
            //legacy_block_to_states_112_to_118.bin
            byte[] buffer = new byte[2];
            while (stream.available() > 0) {
                int len = (int) VarInt.readUnsignedVarInt(stream);
                byte[] arr = new byte[len];
                if (stream.read(arr) != len) {
                    throw new EOFException("name");
                }
                String name = new String(arr, StandardCharsets.UTF_8);

                if (stream.read(buffer) != 2) {
                    throw new EOFException("val");
                }
                int meta = Binary.readLShort(buffer);

                CompoundTag states = (CompoundTag) Tag.readNamedTag(new NBTInputStream(stream, ByteOrder.LITTLE_ENDIAN, true));

                List<CompoundTag> list = legacyToCurrent.computeIfAbsent(name, k -> new ObjectArrayList<>());
                while (list.size() <= meta) {
                    list.add(meta, null);
                }
                list.set(meta, states);
            }
        } catch (Exception e) {
            throw new AssertionError("Unable to load legacy_block_to_states.bin", e);
        }
/*
        Map<String, List<CompoundTag>> legacyToCurrent = new Object2ObjectOpenHashMap<>();
        try (InputStream stream = new ByteArrayInputStream(ByteStreams.toByteArray(SynapseAPI.class.getClassLoader().getResourceAsStream("legacy_block_to_states_112_to_11810.bin")))) {
            while (stream.available() > 0) {
                int len = (int) VarInt.readUnsignedVarInt(stream);
                byte[] arr = new byte[len];
                if (stream.read(arr) != len) {
                    throw new EOFException("name");
                }
                String name = new String(arr, StandardCharsets.UTF_8);

                int count = (int) VarInt.readUnsignedVarInt(stream);

                List<CompoundTag> list = legacyToCurrent.get(name);
                if (list == null) {
                    list = new ObjectArrayList<>(count);
                    legacyToCurrent.put(name, list);
                }

                for (int i = 0; i < count; i++) {
                    int meta = (int) VarInt.readUnsignedVarInt(stream);
                    CompoundTag states = (CompoundTag) NBTIO.readOnly(stream, ByteOrder.LITTLE_ENDIAN);

                    while (list.size() <= meta) {
                        list.add(meta, null);
                    }
                    list.set(meta, states);
                }
            }
        } catch (Exception e) {
            throw new AssertionError("Unable to load legacy_block_to_states_112_to_11810.bin", e);
        }
*/
        legacyToCurrent.forEach((name, metaMapping) -> LEGACY_TO_CURRENT.put(name, metaMapping.stream()
                .map(block -> new BlockData(block.getString("name"), block.getCompound("states"), block.getInt("version")))
                .toArray(BlockData[]::new)));

        addSchema("0001_1.9.0_to_1.10.0.json", V1_10_0);
        addSchema("0011_1.10.0_to_1.12.0.json", V1_12_0);
        addSchema("0021_1.12.0_to_1.13.0.json", V1_13_0);
        addSchema("0031_1.13.0_to_1.14.0.json", V1_14_0);
        addSchema("0041_1.14.0_to_1.16.0.57_beta.json", V1_16_0);
        addSchema("0051_1.16.0.57_beta_to_1.16.0.59_beta.json", V1_16_0);
        addSchema("0061_1.16.0.59_beta_to_1.16.0.68_beta.json", V1_16_0);
        addSchema("0071_1.16.0_to_1.16.100.json", V1_16_100);
        addSchema("0081_1.16.200_to_1.16.210.json", V1_16_210);
        addSchema("0091_1.17.10_to_1.17.30.json", V1_17_30);
        addSchema("0101_1.17.30_to_1.17.40.json", V1_17_40);
        addSchema("0111_1.18.0_to_1.18.10.json", V1_18_10);
        addSchema("0121_1.18.10_to_1.18.20.27_beta.json", V1_18_30);
        addSchema("0131_1.18.20.27_beta_to_1.18.30.json", V1_18_30);
        addSchema("0141_1.18.30_to_1.19.0.34_beta.json", V1_19_0);
        addSchema("0151_1.19.0.34_beta_to_1.19.20.json", V1_19_20);
        addSchema("0161_1.19.50_to_1.19.60.26_beta.json", V1_19_60);
        addSchema("0171_1.19.60_to_1.19.70.26_beta.json", V1_19_70);
        addSchema("0181_1.19.70_to_1.19.80.24_beta.json", V1_19_80);
        addSchema("0191_1.19.80.24_beta_to_1.20.0.23_beta.json", V1_20_0);
        addSchema("0201_1.20.0.23_beta_to_1.20.10.24_beta.json", V1_20_10);
        addSchema("0211_1.20.10.24_beta_to_1.20.20.23_beta.json", V1_20_30);
        addSchema("0221_1.20.20.23_beta_to_1.20.30.22_beta.json", V1_20_30);
        addSchema("0231_1.20.30.22_beta_to_1.20.40.24_beta.json", V1_20_40);
        addSchema("0241_1.20.40.24_beta_to_1.20.50.23_beta.json", V1_20_50);
        addSchema("0251_1.20.50.23_beta_to_1.20.60.26_beta.json", V1_20_60);
        addSchema("0261_1.20.60.26_beta_to_1.20.70.24_beta.json", V1_20_70);
        addSchema("0271_1.20.70.24_beta_to_1.20.80.24_beta.json", V1_20_80);
        addSchema("0281_1.20.80.24_beta_to_1.21.0.25_beta.json", V1_21_0);
        addSchema("0291_1.21.0.25_beta_to_1.21.20.24_beta.json", V1_21_20);
        addSchema("0301_1.21.20.24_beta_to_1.21.30.24_beta.json", V1_21_30);
        if (V1_21_40.isAvailable()) {
            addSchema("0311_1.21.30.24_beta_to_1.21.40.20_beta_vanilla.json", V1_21_40);
        } else {
            addSchema("0311_1.21.30.24_beta_to_1.21.40.20_beta.json", V1_21_40);
        }

        BlockUpgrader.setUpgrader(new BedrockBlockUpgrader() {
            @Override
            public void upgrade(CompoundTag tag) {
                VanillaBlockUpgrader.upgrade(tag);
            }

            @Override
            public int getCurrentVersion() {
                return VanillaBlockUpgrader.getCurrentVersion();
            }
        });
    }

    public static void initialize() {
    }

    private VanillaBlockUpgrader() {
        throw new IllegalStateException();
    }

    @ToString
    @AllArgsConstructor
    private static class BlockData {
        private final String name;
        private final CompoundTag states;
        private final int version;
    }
}

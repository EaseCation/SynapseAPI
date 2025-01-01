package org.itxtech.synapseapi.utils;

import cn.nukkit.block.Block;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.JsonUtil;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;
import org.itxtech.synapseapi.multiprotocol.utils.ItemComponentDefinitions;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockPalette.BlockData;
import org.itxtech.synapseapi.multiprotocol.utils.block.RuntimeBlockMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class DataExporter {
    public static void exportAll(Path saveDir, boolean minify) throws IOException {
        Files.createDirectories(saveDir);
        exportBlockPalettes(saveDir.resolve("block_palette"), minify);
        exportBlockComponents(saveDir.resolve("custom_block"), minify);
        exportItemLists(saveDir.resolve("item_list"), minify);
        exportItemComponents(saveDir.resolve("item_component"), minify);
    }

    public static void exportBlockPalettes(Path saveDir, boolean minify) throws IOException {
        Files.createDirectories(saveDir);
        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.getProtocolStart() < AbstractProtocol.FIRST_AVAILABLE_PROTOCOL.getProtocolStart()) {
                continue;
            }
            BlockPalette microsoft = null;
            for (int v = 0; v <= 1; v++) {
                boolean netease = v == 1;
                BlockPalette palette = RuntimeBlockMapper.PALETTES.get(protocol)[v];
                if (!netease) {
                    microsoft = palette;
                } else if (microsoft == palette) {
                    palette = palette.toNetease();
                }
                List<Map<String, Object>> container = new ArrayList<>();
                ListTag<CompoundTag> containerNbt = new ListTag<>();
                List<Map<String, Object>> containerCommunity = netease ? new ArrayList<>() : null;
                for (BlockData block : palette.palette) {
                    CompoundTag statesSorted = new CompoundTag(new TreeMap<>(block.states.getTagsUnsafe()));
                    List<Map<String, Object>> states = new ArrayList<>();
                    for (Entry<String, Tag> entry : statesSorted.entrySet()) {
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
                    entry.put("runtime_id", block.runtimeId);
//                    entry.put("network_id", block.networkId);
//                    entry.put("name_hash", block.nameHash);
                    entry.put("name", block.name);
                    entry.put("states", states);
                    entry.put("id", block.id);
                    entry.put("val", block.val);
                    container.add(entry);

                    if (netease) {
                        Map<String, Object> entryCommunity = new LinkedHashMap<>(entry);
                        switch (block.id) {
                            case Block.LOG2, Block.LEAVES2 -> {
                                int meta = block.val;
                                int newMeta = ((meta & ~0b1) << 1) | (meta & 0b1);
                                if (meta != newMeta) {
                                    entryCommunity.put("val", newMeta);
                                }
                            }
                            case Block.BLOCK_FRAME, Block.BLOCK_GLOW_FRAME -> {
                                int meta = block.val;
                                int newMeta = (meta & ~0b111) | (5 - (meta & 0b111));
                                if (meta != newMeta) {
                                    entryCommunity.put("val", newMeta);
                                }
                            }
                        }
                        containerCommunity.add(entryCommunity);
                    }

                    containerNbt.addCompound(new CompoundTag(new LinkedHashMap<>())
                            .putInt("runtime_id", block.runtimeId)
//                            .putInt("network_id", block.networkId)
//                            .putLong("name_hash", block.nameHash)
                            .putString("name", block.name)
                            .putCompound("states", statesSorted)
                            .putInt("id", block.id)
                            .putInt("val", block.val)
                    );
                }
                String fileName = getFileName(protocol, netease);
                Files.writeString(saveDir.resolve(fileName + ".json"), JsonUtil.PRETTY_JSON_MAPPER.writeValueAsString(container));
                Files.writeString(saveDir.resolve(fileName + ".mojangson"), containerNbt.toMojangson(true));
                if (netease) {
                    Files.writeString(saveDir.resolve(fileName + "_community.json"), JsonUtil.PRETTY_JSON_MAPPER.writeValueAsString(containerCommunity)); //用于阿尔尼亚RPG服的社区旧版nk
                }
                if (minify) {
                    Files.writeString(saveDir.resolve(fileName + ".min.json"), JsonUtil.TRUSTED_JSON_MAPPER.writeValueAsString(container));
                    Files.writeString(saveDir.resolve(fileName + ".min.mojangson"), containerNbt.toMojangson(false));
                }
            }
        }
    }

    public static void exportBlockComponents(Path saveDir, boolean minify) throws IOException {
        Files.createDirectories(saveDir);
        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.getProtocolStart() < AbstractProtocol.FIRST_AVAILABLE_PROTOCOL.getProtocolStart()) {
                continue;
            }
            for (int v = 0; v <= 1; v++) {
                boolean netease = v == 1;
                byte[] properties = AdvancedGlobalBlockPalette.getCompiledBlockProperties(protocol, netease);
                if (properties.length <= 1) {
                    continue;
                }
                BinaryStream stream = new BinaryStream(properties);
                int count = (int) stream.getUnsignedVarInt();
                CompoundTag container = new CompoundTag(new LinkedHashMap<>());
                for (int i = 0; i < count; i++) {
                    String name = stream.getString();

                    byte[] buffer = stream.getBufferUnsafe();
                    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                    bais.skip(stream.getOffset());
                    int available = bais.available();
                    CompoundTag property = NBTIO.read(bais, ByteOrder.LITTLE_ENDIAN, true);
                    stream.skip(available - bais.available());

                    container.putCompound(name, property);
                }
                String fileName = getFileName(protocol, netease);
                Files.writeString(saveDir.resolve(fileName + ".mojangson"), container.toMojangson(true));
                if (minify) {
                    Files.writeString(saveDir.resolve(fileName + ".min.mojangson"), container.toMojangson(false));
                }
            }
        }
    }

    public static void exportItemLists(Path saveDir, boolean minify) throws IOException {
        Files.createDirectories(saveDir);
        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.getProtocolStart() < AbstractProtocol.FIRST_AVAILABLE_PROTOCOL.getProtocolStart()) {
                continue;
            }
            for (int v = 0; v <= 1; v++) {
                boolean netease = v == 1;
                byte[] bytes = AdvancedRuntimeItemPalette.getCompiledData(protocol, netease);
                BinaryStream stream = new BinaryStream(bytes);
                int count = (int) stream.getUnsignedVarInt();
                Object2IntMap<String> container = new Object2IntLinkedOpenHashMap<>();
                List<Map<String, Object>> containerFull = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    String name = stream.getString();
                    short id = (short) stream.getLShort();
                    boolean component = stream.getBoolean();

                    container.put(name, id);

                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("name", name);
                    entry.put("runtime_id", id);
                    entry.put("component", component);
                    containerFull.add(entry);
                }
                String fileName = getFileName(protocol, netease);
                Files.writeString(saveDir.resolve(fileName + ".json"), JsonUtil.PRETTY_JSON_MAPPER.writeValueAsString(container));
                Files.writeString(saveDir.resolve(fileName + "_full.json"), JsonUtil.PRETTY_JSON_MAPPER.writeValueAsString(containerFull));
                if (minify) {
                    Files.writeString(saveDir.resolve(fileName + ".min.json"), JsonUtil.TRUSTED_JSON_MAPPER.writeValueAsString(container));
                    Files.writeString(saveDir.resolve(fileName + "_full.min.json"), JsonUtil.TRUSTED_JSON_MAPPER.writeValueAsString(containerFull));
                }
            }
        }
    }

    public static void exportItemComponents(Path saveDir, boolean minify) throws IOException {
        Files.createDirectories(saveDir);
        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.getProtocolStart() < AbstractProtocol.FIRST_AVAILABLE_PROTOCOL.getProtocolStart()) {
                continue;
            }
            for (int v = 0; v <= 1; v++) {
                boolean netease = v == 1;
                Map<String, CompoundTag> definitions = ItemComponentDefinitions.get(protocol, netease);
                if (definitions == null) {
                    continue;
                }
                CompoundTag container = new CompoundTag(new TreeMap<>(definitions));
                String fileName = getFileName(protocol, netease);
                Files.writeString(saveDir.resolve(fileName + ".mojangson"), container.toMojangson(true));
                if (minify) {
                    Files.writeString(saveDir.resolve(fileName + ".min.mojangson"), container.toMojangson(false));
                }
            }
        }
    }

    private static String getFileName(AbstractProtocol protocol, boolean netease) {
        String name = String.valueOf(protocol.getProtocolStart());
        if (netease) {
            name += "n";
        }
        return name;
    }
}

/* NETEASE EDITION
Block(name=minecraft:mod_ore, states={}, id=230, val=0) // chalkboard
Block(name=minecraft:micro_block, states={}, id=9990, val=0)

Item(name=minecraft:micro_block, id=-9735)
Item(name=minecraft:mod_ore, id=230)

Item(name=minecraft:mod_armor, id=454) // board
Item(name=minecraft:mod, id=456) // portfolio
Item(name=minecraft:mod_ex, id=498) // camera

Item(name=minecraft:debug_stick, id=735)
*/

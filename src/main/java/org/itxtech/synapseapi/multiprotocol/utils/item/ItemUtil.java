package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockSerializer;
import cn.nukkit.block.BlockUpgrader;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockUtil;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteOrder;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Log4j2
public final class ItemUtil {
    private static final AbstractProtocol DATA_VERSION = AbstractProtocol.PROTOCOL_119;

    static final Object2IntMap<String> ITEM_NAME_TO_ID;
    static final String[] ITEM_ID_TO_NAME = new String[Short.MAX_VALUE];
    static final String[] BLOCK_ID_TO_NAME = new String[Block.BLOCK_ID_COUNT];

    static final Map<String, String> ITEM_TO_BLOCK;

    static final Map<String, String[]> LEGACY_TO_FLATTENED = new Object2ObjectOpenHashMap<>();
    static final Map<String, ObjectIntPair<String>> FLATTENED_TO_LEGACY = new Object2ObjectOpenHashMap<>();

    static {
        log.debug("Loading item data...");
        Gson gson = new Gson();

        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(ByteStreams.toByteArray(SynapseAPI.class.getClassLoader().getResourceAsStream("item_id_map_118.json"))))) {
            ITEM_NAME_TO_ID = gson.fromJson(reader, new TypeToken<Object2IntOpenHashMap<String>>(){});
            ITEM_NAME_TO_ID.defaultReturnValue(Item.AIR);

            ITEM_NAME_TO_ID.forEach((name, id) -> {
                if (id >= 0) {
                    ITEM_ID_TO_NAME[id] = name;
                } else {
                    BLOCK_ID_TO_NAME[0xff - id] = name;
                }
            });

            for (int id = 1; id <= 0xff; id++) {
                if (ITEM_ID_TO_NAME[id] != null) {
                    continue;
                }
                ITEM_ID_TO_NAME[id] = BLOCK_ID_TO_NAME[id];
            }
        } catch (Exception e) {
            throw new AssertionError("Unable to load item_id_map.json", e);
        }

        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(ByteStreams.toByteArray(SynapseAPI.class.getClassLoader().getResourceAsStream("item_block_map_118.json"))))) {
            ITEM_TO_BLOCK = gson.fromJson(reader, new TypeToken<Object2ObjectOpenHashMap<String, String>>(){});
        } catch (Exception e) {
            throw new AssertionError("Unable to load item_block_map.json", e);
        }

        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(ByteStreams.toByteArray(SynapseAPI.class.getClassLoader().getResourceAsStream("item_flatten_map_117.json"))))) {
            gson.fromJson(reader, JsonObject.class).entrySet().forEach(entry -> {
                String legacyName = entry.getKey();

                List<String> metaToNewName = new ObjectArrayList<>();
                entry.getValue().getAsJsonObject().entrySet().forEach(pair -> {
                    int legacyMeta = Integer.parseInt(pair.getKey());
                    String newName = pair.getValue().getAsString();

                    while (metaToNewName.size() <= legacyMeta) {
                        metaToNewName.add(null);
                    }
                    metaToNewName.set(legacyMeta, newName);

                    FLATTENED_TO_LEGACY.put(newName, ObjectIntPair.of(legacyName, legacyMeta));
                });

                LEGACY_TO_FLATTENED.put(legacyName, metaToNewName.toArray(new String[0]));
            });
        } catch (Exception e) {
            throw new AssertionError("Unable to load item_flatten_map.json", e);
        }

        ITEM_ID_TO_NAME[ItemID.AIR] = "minecraft:air";
        BLOCK_ID_TO_NAME[BlockID.AIR] = "minecraft:air";
        ITEM_NAME_TO_ID.put("minecraft:air", ItemID.AIR);
        ITEM_TO_BLOCK.put("minecraft:air", "minecraft:air");
    }

    public static void emptyItem(CompoundTag tag) {
        tag.remove("Block");
        tag.remove("tag");
        tag.remove("CanPlaceOn");
        tag.remove("CanDestroy");

        tag.putString("Name", "");
        tag.putShort("Damage", 0);

        tag.putByte("Count", 0);

        tag.putBoolean("WasPickedUp", false);
    }

    public static void unknownBlockItem(CompoundTag tag) {
        tag.putString("Name", "minecraft:info_update");
        tag.putShort("Damage", 0);

        CompoundTag block = new CompoundTag();
        BlockUtil.unknownBlock(block);
        tag.putCompound("Block", block);

        tag.putByte("Count", 0);
    }

    @Nullable
    public static Item deserializeItem(JsonObject itemEntry) {
        String name = itemEntry.get("name").getAsString();
        int meta = itemEntry.has("meta") ? itemEntry.get("meta").getAsInt() : 0;
        int count = itemEntry.has("count") ? itemEntry.get("count").getAsInt() : 1;
        String blockStates = itemEntry.has("block_states") ? itemEntry.get("block_states").getAsString() : null;
        String nbt = itemEntry.has("nbt") ? itemEntry.get("nbt").getAsString() : null;

        if (meta == 0x7fff) {
            meta = -1;
        }

        int fullId = AdvancedRuntimeItemPalette.getLegacyFullIdByName(DATA_VERSION, false, name);
        if (fullId == -1) {
            return null;
        }

        int id = AdvancedRuntimeItemPalette.getId(DATA_VERSION, false, fullId);
        if (AdvancedRuntimeItemPalette.hasData(DATA_VERSION, false, fullId)) {
            meta = AdvancedRuntimeItemPalette.getData(DATA_VERSION, false, fullId);
        }

        if (blockStates != null) {
            CompoundTag blockTag = new CompoundTag()
                    .putString("name", ITEM_TO_BLOCK.getOrDefault(name, name))
                    .putInt("version", BlockUpgrader.getCurrentVersion());
            try {
                blockTag.putCompound("states", NBTIO.read(Base64.getDecoder().decode(blockStates), ByteOrder.LITTLE_ENDIAN));
            } catch (IOException e) {
                throw new RuntimeException("Invalid block states", e);
            }

            int blockFullId = BlockSerializer.deserializeRuntime(blockTag);
            int blockId = blockFullId >> Block.BLOCK_META_BITS;
            if (blockId == BlockID.INFO_UPDATE) {
                log.debug("Unknown item: " + name);
                return null;
            }

            meta = blockFullId & Block.BLOCK_META_MASK;
        }

        return Item.getCraftingItem(id, meta, count, nbt != null ? Base64.getDecoder().decode(nbt) : new byte[0]);
    }

    private ItemUtil() {
        throw new IllegalStateException();
    }
}

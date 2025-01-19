package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.GameVersion;
import cn.nukkit.block.Block;
import cn.nukkit.item.*;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol12160.protocol.CreativeContentPacket12160.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12160.protocol.CreativeContentPacket12160.Group;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockPalette.BlockData;
import org.itxtech.synapseapi.multiprotocol.utils.block.RuntimeBlockMapper;
import org.itxtech.synapseapi.multiprotocol.utils.block.VanillaBlockUpgrader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import static cn.nukkit.GameVersion.*;

@Log4j2
public final class CreativeInventoryGrouped {
    private static final List<Group> GROUPS = new ObjectArrayList<>();
    private static final List<Entry> ITEMS = new ObjectArrayList<>();

    public static final IntObjectPair<Group> ANONYMOUS_BLOCK_GROUP;
    public static final IntObjectPair<Group> ANONYMOUS_ITEM_GROUP;
    public static final IntObjectPair<Group> ANONYMOUS_EQUIPMENT_GROUP;
    public static final IntObjectPair<Group> ANONYMOUS_NATURE_GROUP;
    public static final IntObjectPair<Group> CUSTOM_BLOCK_GROUP;
    public static final IntObjectPair<Group> CUSTOM_ITEM_GROUP;
    public static final IntObjectPair<Group> CUSTOM_EQUIPMENT_GROUP;
    public static final IntObjectPair<Group> CUSTOM_NATURE_GROUP;

    private static final Int2IntMap GROUP_INDEX_BY_BLOCK_RUNTIME_ID = new Int2IntOpenHashMap();

    private static int SPAWN_EGG_GROUP_INDEX = -1;
    private static int SKULL_GROUP_INDEX = -1;
    private static int MUSHROOM_GROUP_INDEX = -1;

    static {
        log.info("Loading Creative Items from creative_items.json (grouped) 1.21.60");

        GROUPS.clear();
        ITEMS.clear();

        JsonObject root;
        try (InputStream stream = SynapseAPI.class.getClassLoader().getResourceAsStream("creativegroups_12160.json");
             InputStreamReader reader = new InputStreamReader(stream)) {
            root = new Gson().fromJson(reader, JsonObject.class);
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load creative_items.json", e);
        }

        JsonArray groups = root.getAsJsonArray("groups");
        for (JsonElement element : groups) {
            JsonObject entry = element.getAsJsonObject();
            String category = entry.get("category").getAsString();
            String text = entry.get("text").getAsString();
            int categoryId = switch (category) {
                case "construction" -> 1;
                case "nature" -> 2;
                case "equipment" -> 3;
                case "items" -> 4;
                case "command_only" -> 5;
                default -> throw new IllegalStateException("Unknown category: " + category);
            };
            if ("itemGroup.name.mobEgg".equals(text)) {
                SPAWN_EGG_GROUP_INDEX = GROUPS.size();
            } else if ("itemGroup.name.skull".equals(text)) {
                SKULL_GROUP_INDEX = GROUPS.size();
            } else if ("itemGroup.name.mushroom".equals(text)) {
                MUSHROOM_GROUP_INDEX = GROUPS.size();
            }
            Item icon;
            try {
                icon = CreativeItemUtil.deserializeItem(entry);

                if (icon == null && !"minecraft:air".equals(entry.get("name").getAsString())) {
                    log.debug("Unknown creative group icon: {}", entry);
                }
            } catch (Exception e) {
                icon = null;
            }
            if (icon == null) {
                if ("itemGroup.name.skull".equals(text)) {
                    icon = Item.get(Item.SKULL, ItemSkull.HEAD_CREEPER);
                } else {
                    icon = Items.air();
                }
            }
            GROUPS.add(new Group(categoryId, text, icon));
        }
        ANONYMOUS_BLOCK_GROUP = registerGroup(new Group(Group.CATEGORY_CONSTRUCTION, "", Items.air()));
        ANONYMOUS_ITEM_GROUP = registerGroup(new Group(Group.CATEGORY_ITEMS, "", Items.air()));
        ANONYMOUS_EQUIPMENT_GROUP = registerGroup(new Group(Group.CATEGORY_EQUIPMENT, "", Items.air()));
        ANONYMOUS_NATURE_GROUP = registerGroup(new Group(Group.CATEGORY_NATURE, "", Items.air()));
        CUSTOM_BLOCK_GROUP = registerGroup(new Group(Group.CATEGORY_CONSTRUCTION, "itemGroup.name.custom", Items.air()));
        CUSTOM_ITEM_GROUP = registerGroup(new Group(Group.CATEGORY_ITEMS, "itemGroup.name.custom", Items.air()));
        CUSTOM_EQUIPMENT_GROUP = registerGroup(new Group(Group.CATEGORY_EQUIPMENT, "itemGroup.name.custom", Items.air()));
        CUSTOM_NATURE_GROUP = registerGroup(new Group(Group.CATEGORY_NATURE, "itemGroup.name.custom", Items.air()));

        GROUP_INDEX_BY_BLOCK_RUNTIME_ID.defaultReturnValue(ANONYMOUS_BLOCK_GROUP.leftInt());

        if (SPAWN_EGG_GROUP_INDEX == -1 || SKULL_GROUP_INDEX == -1 || MUSHROOM_GROUP_INDEX == -1) {
            throw new AssertionError("vanilla item group not found");
        }

        JsonArray items = root.getAsJsonArray("items");
        for (JsonElement element : items) {
            JsonObject entry = element.getAsJsonObject();
            int group = entry.get("group").getAsInt();
            JsonElement blockNetId = entry.get("blockNetId");
            if (blockNetId != null) {
                GROUP_INDEX_BY_BLOCK_RUNTIME_ID.put(blockNetId.getAsInt(), group);
            }
            try {
                Item item = CreativeItemUtil.deserializeItem(entry);

                if (item == null) {
                    log.debug("Unknown creative entry: {}", entry);
                    continue;
                }

                ITEMS.add(new Entry(item, group));
            } catch (Exception e) {
                log.error("Failed to parse grouped creative item", e);
            }
        }
    }

    private static IntObjectPair<Group> registerGroup(Group group) {
        int index = GROUPS.size();
        GROUPS.add(group);
        return IntObjectPair.of(index, group);
    }

    public static List<Group> getGroups() {
        return GROUPS;
    }

    public static List<Entry> getItems() {
        return ITEMS;
    }

    public static int getGroupIndex(Item item) {
        return ITEMS.stream()
                .filter(entry -> item.equals(entry.item))
                .findFirst()
                .map(entry -> entry.groupIndex)
                .orElseGet(() -> {
                    int itemId = item.getId();
                    if (itemId == ItemID.LODESTONE_COMPASS) {
                        return ANONYMOUS_EQUIPMENT_GROUP.leftInt();
                    }
                    if (itemId == ItemID.SPAWN_EGG) {
                        return SPAWN_EGG_GROUP_INDEX;
                    }
                    if (itemId == ItemID.SKULL) {
                        return SKULL_GROUP_INDEX;
                    }
                    if (itemId == ItemBlockID.BROWN_MUSHROOM) {
                        return MUSHROOM_GROUP_INDEX;
                    }
                    if (item.isBlockItem()) {
                        Block block = item.getBlock();
                        CompoundTag blockTag = NBTIO.putBlockHelper(block);
                        VanillaBlockUpgrader.upgrade(blockTag, ver -> ver.ordinal() >= GameVersion.getFeatureVersion().ordinal() && ver.ordinal() <= V1_21_60.ordinal());
                        Optional<BlockData> match = RuntimeBlockMapper.PALETTES.get(AbstractProtocol.PROTOCOL_121_60)[0].palette.stream()
                                .filter(data -> blockTag.getString("name").equals(data.name) && blockTag.getCompound("states").equalsTags(data.states))
                                .findFirst();
                        if (match.isPresent()) {
                            return GROUP_INDEX_BY_BLOCK_RUNTIME_ID.get(match.get().runtimeId);
                        }
                    }

                    log.debug("no matching creative group found for {}", item);
                    if (item.isBlockItem()) {
                        return ANONYMOUS_BLOCK_GROUP.leftInt();
                    }
                    return ANONYMOUS_ITEM_GROUP.leftInt();
                });
    }

    public static void init() {
    }

    private CreativeInventoryGrouped() {
    }
}

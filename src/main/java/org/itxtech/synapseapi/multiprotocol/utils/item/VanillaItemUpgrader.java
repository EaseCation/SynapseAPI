package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.GameVersion;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockUpgrader;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.ItemUpgrader;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ShortTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.nbt.tag.Tag;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static cn.nukkit.GameVersion.*;

@Log4j2
public final class VanillaItemUpgrader {
    private static final Map<GameVersion, List<ItemUpgradeSchema>> SCHEMAS = new EnumMap<>(GameVersion.class);
    private static final List<ItemUpgradeSchema> UPGRADE_SCHEMAS = new ObjectArrayList<>();

    public static void upgrade(CompoundTag tag) {
        String rawNameId;
        boolean wasNumId = false;

        Tag nameTag = tag.get("Name");
        if (nameTag instanceof StringTag) {
            // BE 1.6+
            rawNameId = ((StringTag) nameTag).data;

            if (rawNameId.isEmpty()) {
                // empty item stack
                return;
            }

            if (!rawNameId.startsWith("minecraft:")) {
                // custom item
                return;
            }
        } else {
            int id;

            Tag idTag = tag.get("id");
            if (idTag instanceof ShortTag) {
                // BE <= 1.5
                id = ((ShortTag) idTag).data;
                if (id == ItemID.AIR) {
                    // 0 is a special case for air, which is not a valid item ID
                    // this isn't supposed to be saved, but this appears in some places due to bugs in older versions
                    emptyItem(tag);
                    return;
                }

                if (id >= 0) {
                    rawNameId = ItemUtil.ITEM_ID_TO_NAME[id];

                    if (rawNameId == null) {
                        log.debug("Unknown item id: " + id);
                        emptyItem(tag);
                        return;
                    }
                } else {
                    int blockId = 0xff - id;
                    if (blockId >= Block.BLOCK_ID_COUNT) {
                        log.debug("Unknown item block id: " + blockId);
                        ItemUtil.unknownBlockItem(tag);
                        return;
                    }

                    rawNameId = ItemUtil.BLOCK_ID_TO_NAME[blockId];
                    if (rawNameId == null) {
                        log.debug("Unknown block item id: " + id);
                        ItemUtil.unknownBlockItem(tag);
                        return;
                    }
                }

                ObjectIntPair<String> legacy = ItemUtil.FLATTENED_TO_LEGACY.get(rawNameId);
                if (legacy != null) {
                    rawNameId = legacy.left();
                }

                wasNumId = true;
            } else if (idTag instanceof StringTag) {
                // JE - best we can do here is hope the string IDs match
                rawNameId = ((StringTag) idTag).data;
            } else {
                log.debug("Item 'Name' tag is missing");
                emptyItem(tag);
                return;
            }
        }

        int meta = tag.getShort("Damage");

        String newName = rawNameId;
        int newMeta = meta;
        for (ItemUpgradeSchema schema : UPGRADE_SCHEMAS) {
            Int2ObjectMap<String> metas = schema.getRemappedMetas().get(newName);
            if (metas != null) {
                String remappedMetaId = metas.get(newMeta);
                if (remappedMetaId != null) {
                    newName = remappedMetaId;
                    newMeta = 0;
                    continue;
                }
            }

            String renamedId = schema.getRenamedIds().get(newName);
            if (renamedId != null) {
                newName = renamedId;
            }
        }

        CompoundTag blockTag = tag.getCompound("Block", null);
        String blockName;
        if (blockTag != null) {
            BlockUpgrader.upgrade(blockTag);
        } else if ((blockName = ItemUtil.ITEM_TO_BLOCK.get(newName)) != null) {
            ObjectIntPair<String> legacy = ItemUtil.FLATTENED_TO_LEGACY.get(blockName);
            if (legacy != null) {
                blockName = legacy.left();
            }

            // this is a legacy block item represented by ID + meta
            blockTag = new CompoundTag()
                    .putString("name", blockName)
                    .putShort("val", meta);

            BlockUpgrader.upgrade(blockTag);

            if (wasNumId && "minecraft:info_update".equals(blockTag.getString("name")) && !"minecraft:info_update".equals(rawNameId)) {
                int id = ItemUtil.ITEM_NAME_TO_ID.getInt(newName);
                if (id != ItemID.AIR) {
                    // NK 1.13-1.18
                    blockTag = BlockUtil.LEGACY_ITEM_BLOCK_LOOKUP.serialize(Block.getFullId(Block.itemIdToBlockId(id), meta)).clone();

                    BlockUpgrader.upgrade(blockTag);
                } else {
                    log.debug("unknown item block: " + rawNameId);
                }
            }

            tag.putCompound("Block", blockTag);
        }

        tag.putString("Name", newName);
        tag.putShort("Damage", newMeta);
    }

    private static void emptyItem(CompoundTag tag) {
        tag.remove("id");

        ItemUtil.emptyItem(tag);
    }

    private static ItemUpgradeSchema addSchema(String file, GameVersion baseGameVersion) {
        JsonObject json;
        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(ByteStreams.toByteArray(SynapseAPI.class.getClassLoader().getResourceAsStream("item_upgrade_schemas/" + file))))) {
            json = new Gson().fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            throw new AssertionError("Unable to load item upgrade schema: " + file, e);
        }

        ItemUpgradeSchema schema = new ItemUpgradeSchema(json);
        if (baseGameVersion.isAvailable()) {
            UPGRADE_SCHEMAS.add(schema);
        }

        List<ItemUpgradeSchema> schemas = SCHEMAS.get(baseGameVersion);
        if (schemas == null) {
            schemas = new ObjectArrayList<>();
            SCHEMAS.put(baseGameVersion, schemas);
        }
        schemas.add(schema);

        return schema;
    }

    static {
        log.debug("Loading vanilla item upgrader...");

        addSchema("0001_1.6_beta_to_1.6.0.json", V1_6_0);
        addSchema("0011_1.11.4_to_1.12.0.json", V1_12_0);
        addSchema("0015_1.16.0.57_beta_to_1.16.0.59_beta.json", V1_16_100); // beta only
        addSchema("0016_1.16.0.59_beta_to_1.16.0.68_beta.json", V1_16_100); // beta only
        addSchema("0021_1.16.0_to_1.16.100.json", V1_16_100);
        addSchema("0031_1.16.100_to_1.16.200.json", V1_16_200);
        addSchema("0041_1.16.200_to_1.17.30.json", V1_17_30);
        addSchema("0051_1.17.40_to_1.18.0.json", V1_18_0);
        addSchema("0061_1.18.0_to_1.18.10.json", V1_18_10);
        addSchema("0065_1.18.10_to_1.18.20.27_beta.json", V1_18_30); // experiment only
        addSchema("0071_1.18.20_to_1.18.30.json", V1_18_30);
        addSchema("0081_1.18.30_to_1.19.0.json", V1_19_0);
        addSchema("0091_1.19.60_to_1.19.70.26_beta.json", V1_19_70);
        addSchema("0101_1.19.70_to_1.19.80.24_beta.json", V1_19_80);
        addSchema("0111_1.19.80_to_1.20.0.23_beta.json", V1_20_0);
        addSchema("0121_1.20.0.23_beta_to_1.20.10.24_beta.json", V1_20_10);
        addSchema("0131_1.20.10.24_beta_to_1.20.20.23_beta.json", V1_20_30);
        addSchema("0141_1.20.20.23_beta_to_1.20.30.22_beta.json", V1_20_30);

        ItemUpgrader.setUpgrader(VanillaItemUpgrader::upgrade);
    }

    public static void initialize() {
    }

    private VanillaItemUpgrader() {
        throw new IllegalStateException();
    }
}

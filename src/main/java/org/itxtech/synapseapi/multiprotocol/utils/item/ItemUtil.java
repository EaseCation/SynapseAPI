package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockSerializer;
import cn.nukkit.block.BlockUpgrader;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Base64;

@Log4j2
public final class ItemUtil {
    private static final AbstractProtocol DATA_VERSION = AbstractProtocol.PROTOCOL_119;

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
                    .putString("name", name)
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

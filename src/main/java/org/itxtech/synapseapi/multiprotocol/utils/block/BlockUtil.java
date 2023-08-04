package org.itxtech.synapseapi.multiprotocol.utils.block;

import cn.nukkit.block.BlockUpgrader;
import cn.nukkit.nbt.tag.CompoundTag;

public final class BlockUtil {
    public static void emptyBlock(CompoundTag tag) {
        tag.putString("name", "minecraft:air");
        tag.putCompound("states", new CompoundTag());
        tag.putInt("version", BlockUpgrader.getCurrentVersion());
    }

    public static void unknownBlock(CompoundTag tag) {
        tag.putString("name", "minecraft:info_update");
        tag.putCompound("states", new CompoundTag());
        tag.putInt("version", BlockUpgrader.getCurrentVersion());
    }

    private BlockUtil() {
        throw new IllegalStateException();
    }
}

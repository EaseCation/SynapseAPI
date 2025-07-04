package org.itxtech.synapseapi.multiprotocol.utils.block;

import cn.nukkit.block.BlockUpgrader;
import cn.nukkit.nbt.tag.CompoundTag;

public final class BlockUtil {
    public static final RuntimeBlockSerializer LEGACY_ITEM_BLOCK_LOOKUP = new RuntimeBlockSerializer(BlockPalette.fromJson("block_state_list_118_netease.json"));

    public static void emptyBlock(CompoundTag tag) {
        tag.putString("name", "minecraft:air");
        tag.putCompound("states", new CompoundTag());
        tag.putInt("version", BlockUpgrader.getCurrentVersion());
    }

    public static void unknownBlock(CompoundTag tag) {
        tag.putString("name", "minecraft:unknown");
        tag.putCompound("states", new CompoundTag());
        tag.putInt("version", BlockUpgrader.getCurrentVersion());
    }

    public static void initialize() {
    }

    private BlockUtil() {
        throw new IllegalStateException();
    }
}

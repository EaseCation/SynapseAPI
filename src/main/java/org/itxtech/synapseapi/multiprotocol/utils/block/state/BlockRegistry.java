package org.itxtech.synapseapi.multiprotocol.utils.block.state;

import cn.nukkit.nbt.tag.CompoundTag;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockPalette.BlockData;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class BlockRegistry {
    private final int version;
    private final Map<String, BlockLegacy> blocks = new HashMap<>();

    public BlockRegistry(int version) {
        this.version = version;
    }

    public BlockRegistry(int version, BlockRegistry copy) {
        this(version);
        blocks.putAll(copy.blocks);
    }

    public int getVersion() {
        return version;
    }

    public void createBlockPermutations() {
        blocks.values().forEach(BlockLegacy::createPermutations);
    }

    public BlockLegacy registerBlock(String name, int id) {
        BlockLegacy block = new BlockLegacy(id, name);
        blocks.put(name, block);
        return block;
    }

    public BlockLegacy replace(BlockLegacy oldBlock, BlockState oldState, BlockState newState) {
        return replace(oldBlock, oldState, newState, newState.variationCount);
    }

    public BlockLegacy replace(BlockLegacy oldBlock, BlockState oldState, BlockState newState, int variationCount) {
        BlockLegacy newBlock = oldBlock.replace(oldState, newState, variationCount);
        blocks.put(newBlock.name, newBlock);
        return newBlock;
    }

    public BlockLegacy rename(BlockLegacy oldBlock, String newName) {
        blocks.remove(oldBlock.name);

        BlockLegacy newBlock = oldBlock.rename(newName);
        blocks.put(newBlock.name, newBlock);
        return newBlock;
    }

    public BlockLegacy flatten(BlockLegacy oldBlock, String newName, int newId, BlockState... flattenedStates) {
        blocks.remove(oldBlock.name);

        BlockLegacy newBlock = oldBlock.flatten(newName, newId, flattenedStates);
        blocks.put(newBlock.name, newBlock);
        return newBlock;
    }

    public BlockPalette createBlockPalette() {
        BlockPalette instance = new BlockPalette();
        for (Entry<String, BlockLegacy> entry : blocks.entrySet()) {
            String name = entry.getKey();
            BlockLegacy block = entry.getValue();
            for (BlockNew state : block.permutations) {
                if (state == null) {
                    continue;
                }
                CompoundTag states = new CompoundTag("states");
                int[] stateValues = state.stateValues;
                for (int i = 0; i < stateValues.length; i++) {
                    int value = stateValues[i];
                    if (value == -1) {
                        continue;
                    }
                    block.states[i].state.toNBT(states, value);
                }
                instance.palette.add(new BlockData(name, states));
            }
        }
        instance.sortHash();
        return instance;
    }
}

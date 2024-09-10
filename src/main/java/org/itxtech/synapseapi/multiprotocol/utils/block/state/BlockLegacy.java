package org.itxtech.synapseapi.multiprotocol.utils.block.state;

import cn.nukkit.math.Mth;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import lombok.ToString;

import java.util.Arrays;

@ToString(doNotUseGetters = true)
public class BlockLegacy {
    public final int id;
    public final String name;

    public int bitsUsed;
    public BlockStateInstance[] states = new BlockStateInstance[BlockStates.STATE_COUNT];

    public BlockNew[] permutations;
    public BlockNew defaultState;

    public BlockLegacy(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public BlockLegacy addState(BlockState state) {
        return addState(state, state.variationCount);
    }

    public BlockLegacy addState(BlockState state, int variationCount) {
        if (variationCount < 2) {
            throw new IllegalArgumentException("variationCount must be at least 2");
        }
        int numBits = Mth.ceillog2(variationCount);
        BlockStateInstance instance = new BlockStateInstance(bitsUsed, numBits, variationCount, state);
        states[state.id] = instance;
        bitsUsed += numBits;
        return this;
    }

    public BlockLegacy createPermutations() {
        int numStates = Math.max(1, (int) Math.pow(2, Math.min(bitsUsed, 16)));
        permutations = new BlockNew[numStates];
        PERMUTATION:
        for (int i = 0; i < numStates; i++) {
            int[] stateValues = new int[BlockStates.STATE_COUNT];
            Arrays.fill(stateValues, -1);
            for (BlockStateInstance instance : states) {
                if (instance == null) {
                    continue;
                }
                if (!instance.isValidData(i)) {
                    continue PERMUTATION;
                }
                stateValues[instance.state.id] = instance.get(i);
            }
            permutations[i] = new BlockNew(this, i, stateValues);
        }
        return this;
    }

    public void setDefaultState(BlockNew state) {
        defaultState = state;
    }

    public BlockNew getDefaultState() {
        if (defaultState == null) {
            return permutations[0];
        }
        return defaultState;
    }

    public BlockNew setState(BlockState state, int value, int meta) {
        BlockStateInstance instance = states[state.id];
        BlockNew block = instance.set(meta, value, permutations);
        if (block == null) {
            return getDefaultState();
        }
        return block;
    }

    public BlockNew setState(BlockState state, boolean value, int meta) {
        return setState(state, value ? 1 : 0, meta);
    }

    public BlockNew setState(BlockState state, Enum<?> value, int meta) {
        return setState(state, value.ordinal(), meta);
    }

    public int getState(BlockState state, int meta) {
        return states[state.id].get(meta);
    }

    /**
     * @return new block
     */
    public BlockLegacy flatten(String name, int id, BlockState... flattenedStates) {
        BlockLegacy newBlock = new BlockLegacy(id, name);
        Int2ObjectMap<BlockStateInstance> permutations = new Int2ObjectRBTreeMap<>();
        int found = 0;
        INSTANCE:
        for (BlockStateInstance instance : states) {
            if (instance == null) {
                continue;
            }
            for (BlockState flattenedState : flattenedStates) {
                if (flattenedState == instance.state) {
                    found++;
                    continue INSTANCE;
                }
            }
            permutations.put(instance.startBit, instance);
        }
        if (found != flattenedStates.length) {
            throw new IllegalArgumentException("states mismatch");
        }
        for (BlockStateInstance instance : permutations.values()) {
            newBlock.addState(instance.state, instance.variationCount);
        }
        return newBlock;
    }

    /**
     * @return new block
     */
    public BlockLegacy rename(String name) {
        BlockLegacy newBlock = new BlockLegacy(id, name);
        Int2ObjectMap<BlockStateInstance> permutations = new Int2ObjectRBTreeMap<>();
        for (BlockStateInstance instance : states) {
            if (instance == null) {
                continue;
            }
            permutations.put(instance.startBit, instance);
        }
        for (BlockStateInstance instance : permutations.values()) {
            newBlock.addState(instance.state, instance.variationCount);
        }
        return newBlock;
    }
}

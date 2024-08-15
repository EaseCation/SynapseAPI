package org.itxtech.synapseapi.multiprotocol.utils.block.state;

import lombok.ToString;

@ToString(exclude = "legacyBlock")
public class BlockNew {
    public final BlockLegacy legacyBlock;
    public final int meta;
    public final int[] stateValues;

    public BlockNew(BlockLegacy oldBlock, int auxDataVal, int[] stateValues) {
        legacyBlock = oldBlock;
        this.meta = auxDataVal;
        this.stateValues = stateValues;
    }

    public BlockNew setState(BlockState state, int value) {
        return legacyBlock.setState(state, value, meta);
    }

    public BlockNew setState(BlockState state, boolean value) {
        return setState(state, value ? 1 : 0);
    }

    public BlockNew setState(BlockState state, Enum<?> value) {
        return setState(state, value.ordinal());
    }

    public int getState(BlockState state) {
        return legacyBlock.getState(state, meta);
    }
}

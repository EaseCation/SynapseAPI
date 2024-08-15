package org.itxtech.synapseapi.multiprotocol.utils.block.state;

import cn.nukkit.nbt.tag.CompoundTag;
import lombok.ToString;

import java.util.List;

@ToString
public abstract class BlockState {
    public final int id;
    public final String name;
    public final int variationCount;

    protected BlockState(String name, int variationCount) {
        this.id = BlockStates.assignId();
        this.name = name;
        this.variationCount = variationCount;
    }

    public abstract List<?> getValues();

    public abstract void toNBT(CompoundTag tag, int val);

    public abstract int fromNBT(CompoundTag tag);
}

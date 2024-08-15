package org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data;

import cn.nukkit.nbt.tag.CompoundTag;

public class EntityPropertyDataInt implements EntityPropertyData {

    private final String name;
    private final int max;
    private final int min;
    private final int defaultValue;

    public EntityPropertyDataInt(String name, int max, int min) {
        this(name, max, min, min);
    }

    public EntityPropertyDataInt(String name, int max, int min, int defaultValue) {
        this.name = name;
        this.max = max;
        this.min = min;
        this.defaultValue = defaultValue;
    }

    @Override
    public EntityPropertyType getType() {
        return EntityPropertyType.INT;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    @Override
    public CompoundTag toTag() {
        return new CompoundTag()
            .putString("name", name)
            .putInt("max", max)
            .putInt("min", min)
            .putInt("type", getType().getType());
    }

    @Override
    public int getDefaultIntValue() {
        return defaultValue;
    }
}

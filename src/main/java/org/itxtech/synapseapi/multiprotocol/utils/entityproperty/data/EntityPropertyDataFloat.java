package org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data;

import cn.nukkit.nbt.tag.CompoundTag;

public class EntityPropertyDataFloat implements EntityPropertyData {

    private final String name;
    private final float max;
    private final float min;

    public EntityPropertyDataFloat(String name, float max, float min) {
        this.name = name;
        this.max = max;
        this.min = min;
    }

    @Override
    public EntityPropertyType getType() {
        return EntityPropertyType.FLOAT;
    }

    @Override
    public String getName() {
        return name;
    }

    public float getMax() {
        return max;
    }

    public float getMin() {
        return min;
    }

    @Override
    public CompoundTag toTag() {
        return new CompoundTag()
            .putString("name", name)
            .putFloat("max", max)
            .putFloat("min", min)
            .putInt("type", getType().getType());
    }
}

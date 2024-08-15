package org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data;

import cn.nukkit.nbt.tag.CompoundTag;

public class EntityPropertyDataBool implements EntityPropertyData {

    private final String name;
    private final boolean defaultValue;

    public EntityPropertyDataBool(String name) {
        this(name, false);
    }

    public EntityPropertyDataBool(String name, boolean defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public EntityPropertyType getType() {
        return EntityPropertyType.BOOL;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CompoundTag toTag() {
        return new CompoundTag()
            .putString("name", name)
            .putInt("type", getType().getType());
    }

    @Override
    public int getDefaultIntValue() {
        return defaultValue ? 1 : 0;
    }
}

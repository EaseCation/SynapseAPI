package org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data;

import cn.nukkit.nbt.tag.CompoundTag;

public class EntityPropertyDataBool implements EntityPropertyData {

    private final String name;

    public EntityPropertyDataBool(String name) {
        this.name = name;
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
}

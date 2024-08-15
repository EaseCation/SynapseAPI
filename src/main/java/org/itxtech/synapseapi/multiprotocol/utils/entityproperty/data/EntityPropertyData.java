package org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data;

import cn.nukkit.nbt.tag.CompoundTag;

public interface EntityPropertyData {

    EntityPropertyType getType();

    String getName();

    CompoundTag toTag();

    default int getDefaultIntValue() {
        throw new UnsupportedOperationException();
    }

    default float getDefaultFloatValue() {
        throw new UnsupportedOperationException();
    }
}

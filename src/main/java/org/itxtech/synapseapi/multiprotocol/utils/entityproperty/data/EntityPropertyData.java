package org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data;

import cn.nukkit.nbt.tag.CompoundTag;

public interface EntityPropertyData {

    EntityPropertyType getType();

    String getName();

    CompoundTag toTag();
}

package org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data;

import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EntityPropertiesTable extends ObjectArrayList<EntityPropertyData> {

    private final String entityIdentifier;

    public EntityPropertiesTable(String entityIdentifier) {
        this(entityIdentifier, List.of());
    }

    public EntityPropertiesTable(String entityIdentifier, EntityPropertyData... properties) {
        this(entityIdentifier, Arrays.asList(properties));
    }

    public EntityPropertiesTable(String entityIdentifier, Collection<EntityPropertyData> properties) {
        this.entityIdentifier = entityIdentifier;
        this.addAll(properties);
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    public int getPropertyIndex(String name) {
        for (int i = 0; i < size(); i++) {
            if (name.equals(get(i).getName())) {
                return i;
            }
        }
        return -1;
    }

    public CompoundTag toTag() {
        ListTag<CompoundTag> propertiesTag = new ListTag<>("properties");
        for (EntityPropertyData property : this) {
            propertiesTag.add(property.toTag());
        }
        return new CompoundTag()
                .putString("type", entityIdentifier)
                .putList("properties", propertiesTag);
    }
}

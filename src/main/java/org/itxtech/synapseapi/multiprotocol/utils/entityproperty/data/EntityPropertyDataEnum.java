package org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data;

import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EntityPropertyDataEnum implements EntityPropertyData {

    private final String name;
    private final List<String> values;
    private final int defaultValueIndex;

    public EntityPropertyDataEnum(String name, String... values) {
        this(name, Arrays.asList(values));
    }

    public EntityPropertyDataEnum(String name, int defaultValueIndex, String... values) {
        this(name, defaultValueIndex, Arrays.asList(values));
    }

    public EntityPropertyDataEnum(String name, Collection<String> values) {
        this(name, 0, values);
    }

    public EntityPropertyDataEnum(String name, int defaultValueIndex, Collection<String> values) {
        this.name = name;
        this.values = List.copyOf(values);
        this.defaultValueIndex = defaultValueIndex;
    }

    @Override
    public EntityPropertyType getType() {
        return EntityPropertyType.ENUM;
    }

    @Override
    public String getName() {
        return name;
    }

    public List<String> getValues() {
        return values;
    }

    @Override
    public CompoundTag toTag() {
        ListTag<StringTag> enumTag = new ListTag<>("enum");
        for (String value : values) {
            enumTag.addString(value);
        }
        return new CompoundTag()
            .putString("name", name)
            .putList("enum", enumTag)
            .putInt("type", getType().getType());
    }

    @Override
    public int getDefaultIntValue() {
        return defaultValueIndex;
    }
}

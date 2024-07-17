package org.itxtech.synapseapi.event.player;

import cn.nukkit.Server;
import cn.nukkit.event.HandlerList;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Map;

public class SynapsePlayerRequestAvailableEntityPropertiesPaletteEvent extends SynapsePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Map<String, byte[]> compiled;
    private Map<String, CompoundTag> namedTag = null;

    public SynapsePlayerRequestAvailableEntityPropertiesPaletteEvent(SynapsePlayer player, Map<String, byte[]> compiled) {
        super(player);
        this.compiled = compiled;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public Map<String, CompoundTag> getNamedTag() {
        if (namedTag == null) {
            try {
                namedTag = new Object2ObjectArrayMap<>();
                for (Map.Entry<String, byte[]> entry : compiled.entrySet()) {
                    CompoundTag tag = NBTIO.read(new ByteArrayInputStream(entry.getValue()), ByteOrder.LITTLE_ENDIAN, true);
                    namedTag.put(entry.getKey(), tag);
                }
            } catch (IOException e) {
                Server.getInstance().getLogger().logException(e);
                namedTag = new Object2ObjectArrayMap<>();
            }
        }
        return namedTag;
    }

    public SynapsePlayerRequestAvailableEntityPropertiesPaletteEvent addEntityPropertyInt(String entityIdentifier, String name, int max, int min) {
        CompoundTag propertyTag = new EntityPropertyDataInt(name, max, min).toTag();
        return addEntityProperty(entityIdentifier, propertyTag);
    }

    public SynapsePlayerRequestAvailableEntityPropertiesPaletteEvent addEntityPropertyInt(String entityIdentifier, String name, float max, float min) {
        CompoundTag propertyTag = new EntityPropertyDataFloat(name, max, min).toTag();
        return addEntityProperty(entityIdentifier, propertyTag);
    }

    public SynapsePlayerRequestAvailableEntityPropertiesPaletteEvent addEntityPropertyBool(String entityIdentifier, String name) {
        CompoundTag propertyTag = new EntityPropertyDataBool(name).toTag();
        return addEntityProperty(entityIdentifier, propertyTag);
    }

    public SynapsePlayerRequestAvailableEntityPropertiesPaletteEvent addEntityPropertyEnum(String entityIdentifier, String name, String... values) {
        CompoundTag propertyTag = new EntityPropertyDataEnum(name, values).toTag();
        return addEntityProperty(entityIdentifier, propertyTag);
    }

    public SynapsePlayerRequestAvailableEntityPropertiesPaletteEvent addEntityProperty(String entityIdentifier, CompoundTag propertyTag) {
        CompoundTag tableTag = getNamedTag().computeIfAbsent(entityIdentifier, k -> new EntityPropertiesTable(k).toTag());
        tableTag.putList("properties", tableTag.getList("properties", CompoundTag.class).addCompound(propertyTag));
        return this;
    }

    public Map<String, byte[]> getCompiled() {
        try {
            if (namedTag == null) return compiled;
            Map<String, byte[]> compiled = new Object2ObjectArrayMap<>(namedTag.size());
            for (Map.Entry<String, CompoundTag> entry : namedTag.entrySet()) {
                compiled.put(entry.getKey(), NBTIO.writeNetwork(entry.getValue()));
            }
            return compiled;
        } catch (IOException e) {
            Server.getInstance().getLogger().logException(e);
            return compiled;
        }
    }
}

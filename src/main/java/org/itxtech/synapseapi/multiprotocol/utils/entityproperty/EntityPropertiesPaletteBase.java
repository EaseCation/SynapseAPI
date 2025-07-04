package org.itxtech.synapseapi.multiprotocol.utils.entityproperty;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import org.itxtech.synapseapi.multiprotocol.utils.EntityPropertiesPaletteInterface;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data.EntityPropertiesTable;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data.EntityPropertyData;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityPropertiesPaletteBase implements EntityPropertiesPaletteInterface {

    private final Map<String, EntityPropertiesTable> palette = new HashMap<>();
    private final Map<String, byte[]> compiled = new HashMap<>();

    public EntityPropertiesPaletteBase() {
        this(List.of());
    }

    public EntityPropertiesPaletteBase(EntityPropertiesTable... palette) {
        this(Arrays.asList(palette));
    }

    public EntityPropertiesPaletteBase(Collection<EntityPropertiesTable> palette) {
        for (EntityPropertiesTable table : palette) {
            registerProperties(table);
        }

        this.rebuildNetworkCache();
    }

    public EntityPropertiesPaletteBase(EntityPropertiesPaletteInterface copy, EntityPropertiesTable... palette) {
        this(Stream.of(copy.getEntityPropertiesTables(), Arrays.asList(palette))
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
    }

    @Override
    public void registerProperties(EntityPropertiesTable properties) {
        palette.put(properties.getEntityIdentifier(), properties);
    }

    @Override
    public void rebuildNetworkCache() {
        compiled.clear();
        for (Map.Entry<String, EntityPropertiesTable> entry : palette.entrySet()) {
            CompoundTag tag = entry.getValue().toTag();
            try {
                compiled.put(entry.getKey(), NBTIO.writeNetwork(tag));
            } catch (IOException e) {
                throw new AssertionError("Unable to write entity properties palette", e);
            }
        }
    }

    @Override
    public Set<String> getAllEntityIdentifiers() {
        return Collections.unmodifiableSet(palette.keySet());
    }

    @Override
    public Collection<EntityPropertiesTable> getEntityPropertiesTables() {
        return palette.values();
    }

    @Override
    public List<String> getAllPropertyNames(String entityIdentifier) {
        EntityPropertiesTable table = palette.get(entityIdentifier);
        if (table == null) return List.of();
        List<String> names = new ArrayList<>(table.size());
        for (EntityPropertyData property : table) {
            names.add(property.getName());
        }
        return Collections.unmodifiableList(names);
    }

    @Override
    public Optional<EntityPropertyData> getProperty(String entityIdentifier, String propertyName) {
        EntityPropertiesTable table = palette.get(entityIdentifier);
        if (table == null) return Optional.empty();
        for (EntityPropertyData property : table) {
            if (property.getName().equals(propertyName)) return Optional.of(property);
        }
        return Optional.empty();
    }

    @Override
    public EntityPropertiesTable getProperties(String entityIdentifier) {
        return palette.get(entityIdentifier);
    }

    @Override
    public Map<String, byte[]> getCompiledPalette() {
        return Collections.unmodifiableMap(compiled);
    }
}

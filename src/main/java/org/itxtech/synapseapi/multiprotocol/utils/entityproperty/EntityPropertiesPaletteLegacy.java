package org.itxtech.synapseapi.multiprotocol.utils.entityproperty;

import org.itxtech.synapseapi.multiprotocol.utils.EntityPropertiesPaletteInterface;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data.EntityPropertiesTable;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data.EntityPropertyData;

import java.util.*;

public class EntityPropertiesPaletteLegacy implements EntityPropertiesPaletteInterface {

    public static final EntityPropertiesPaletteLegacy INSTANCE = new EntityPropertiesPaletteLegacy();

    private EntityPropertiesPaletteLegacy() {
        // NOOP
    }

    @Override
    public void registerProperties(EntityPropertiesTable properties) {
    }

    @Override
    public void rebuildNetworkCache() {
    }

    @Override
    public Set<String> getAllEntityIdentifiers() {
        return Set.of();
    }

    @Override
    public List<String> getAllPropertyNames(String entityIdentifier) {
        return List.of();
    }

    @Override
    public Optional<EntityPropertyData> getProperty(String entityIdentifier, String propertyName) {
        return Optional.empty();
    }

    @Override
    public EntityPropertiesTable getProperties(String entityIdentifier) {
        return null;
    }

    @Override
    public Map<String, byte[]> getCompiledPalette() {
        return Map.of();
    }
}

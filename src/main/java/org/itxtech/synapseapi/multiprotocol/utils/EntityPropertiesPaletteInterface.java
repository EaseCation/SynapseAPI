package org.itxtech.synapseapi.multiprotocol.utils;

import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data.EntityPropertiesTable;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data.EntityPropertyData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface EntityPropertiesPaletteInterface {
    void registerProperties(EntityPropertiesTable properties);

    void rebuildNetworkCache();

    Set<String> getAllEntityIdentifiers();

    List<String> getAllPropertyNames(String entityIdentifier);

    Optional<EntityPropertyData> getProperty(String entityIdentifier, String propertyName);

    @Nullable
    EntityPropertiesTable getProperties(String entityIdentifier);

    Map<String, byte[]> getCompiledPalette();
}

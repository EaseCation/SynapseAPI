package org.itxtech.synapseapi.multiprotocol.utils;

import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data.EntityPropertiesTable;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data.EntityPropertyData;

import javax.annotation.Nullable;
import java.util.*;

public interface EntityPropertiesPaletteInterface {
    void registerProperties(EntityPropertiesTable properties);

    void rebuildNetworkCache();

    Set<String> getAllEntityIdentifiers();

    Collection<EntityPropertiesTable> getEntityPropertiesTables();

    List<String> getAllPropertyNames(String entityIdentifier);

    Optional<EntityPropertyData> getProperty(String entityIdentifier, String propertyName);

    @Nullable
    EntityPropertiesTable getProperties(String entityIdentifier);

    Map<String, byte[]> getCompiledPalette();
}

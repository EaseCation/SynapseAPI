package org.itxtech.synapseapi.multiprotocol.utils;

import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data.EntityPropertyData;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface EntityPropertiesPaletteInterface {

    Set<String> getAllEntityIdentifiers();

    List<String> getAllPropertyNames(String entityIdentifier);

    Optional<EntityPropertyData> getProperty(String entityIdentifier, String propertyName);

    Map<String, byte[]> getCompiledPalette();
}

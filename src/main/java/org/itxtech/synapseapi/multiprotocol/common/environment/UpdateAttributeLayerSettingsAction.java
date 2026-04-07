package org.itxtech.synapseapi.multiprotocol.common.environment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpdateAttributeLayerSettingsAction implements SyncAttributeLayerAction {
    public String name;
    public int dimension;
    public AttributeLayerSettings settings;

    @Override
    public SyncAttributeLayerActionType getType() {
        return SyncAttributeLayerActionType.UPDATE_ATTRIBUTE_LAYER_SETTINGS;
    }
}

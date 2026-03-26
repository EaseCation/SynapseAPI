package org.itxtech.synapseapi.multiprotocol.common.environment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpdateAttributeLayersAction implements SyncAttributeLayerAction {
    public AttributeLayer[] layers;

    @Override
    public SyncAttributeLayerActionType getType() {
        return SyncAttributeLayerActionType.UPDATE_ATTRIBUTE_LAYERS;
    }
}

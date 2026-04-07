package org.itxtech.synapseapi.multiprotocol.common.environment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpdateEnvironmentAttributesAction implements SyncAttributeLayerAction {
    public String name;
    public int dimension;
    public EnvironmentAttribute[] attributes;

    @Override
    public SyncAttributeLayerActionType getType() {
        return SyncAttributeLayerActionType.UPDATE_ENVIRONMENT_ATTRIBUTES;
    }
}

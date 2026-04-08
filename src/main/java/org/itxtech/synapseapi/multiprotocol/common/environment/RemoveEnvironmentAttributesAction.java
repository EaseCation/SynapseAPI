package org.itxtech.synapseapi.multiprotocol.common.environment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RemoveEnvironmentAttributesAction implements SyncAttributeLayerAction {
    public String name;
    public int dimension;
    public String[] attributes;

    @Override
    public SyncAttributeLayerActionType getType() {
        return SyncAttributeLayerActionType.REMOVE_ENVIRONMENT_ATTRIBUTES;
    }
}

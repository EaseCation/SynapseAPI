package org.itxtech.synapseapi.multiprotocol.common.environment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AttributeLayerSettings {
    public int priority;
    public WeightData weight;
    public boolean enabled;
    public boolean transitionsPaused;
}

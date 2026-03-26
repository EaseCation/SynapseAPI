package org.itxtech.synapseapi.multiprotocol.common.environment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AttributeLayer {
    public String name;
    public int dimension;
    public AttributeLayerSettings settings;
    @Default
    public EnvironmentAttribute[] attributes = EnvironmentAttribute.EMPTY;
}

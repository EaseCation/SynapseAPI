package org.itxtech.synapseapi.multiprotocol.common.biome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Collection;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BiomeConditionalTransformationData {
    public Collection<BiomeWeightedData> transformsInto;
    public String conditionJson;
    public int minPassingNeighbors;
}

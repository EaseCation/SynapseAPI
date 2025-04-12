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
public class BiomeOverworldGenRulesData {
    public Collection<BiomeWeightedData> hillsTransformations;
    public Collection<BiomeWeightedData> mutateTransformations;
    public Collection<BiomeWeightedData> riverTransformations;
    public Collection<BiomeWeightedData> shoreTransformations;
    public Collection<BiomeConditionalTransformationData> preHillsEdge;
    public Collection<BiomeConditionalTransformationData> postShoreEdge;
    public Collection<BiomeWeightedTemperatureData> climate;
}

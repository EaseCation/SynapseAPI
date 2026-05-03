package org.itxtech.synapseapi.multiprotocol.common.biome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BiomeWeightedTemperatureData {
    @Default
    public BiomeTemperatureCategory temperatureCategory = BiomeTemperatureCategory.MEDIUM;
    public int weight;
}

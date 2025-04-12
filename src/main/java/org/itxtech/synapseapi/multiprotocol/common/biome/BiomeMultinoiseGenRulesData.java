package org.itxtech.synapseapi.multiprotocol.common.biome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BiomeMultinoiseGenRulesData {
    public float temperature;
    public float humidity;
    public float altitude;
    public float weirdness;
    public float weight;
}

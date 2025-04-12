package org.itxtech.synapseapi.multiprotocol.common.biome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BiomeReplacementData {
    public String replacementBiome;
    public String dimension;
    public String targetBiomes;
    public float amount;
    public float noiseFrequencyScale;
    public int replacementIndex;
}

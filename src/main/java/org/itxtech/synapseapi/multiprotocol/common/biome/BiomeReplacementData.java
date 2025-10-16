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
public class BiomeReplacementData {
    public String replacementBiome;
    public String dimension;
    public Collection<String> targetBiomes;
    public float amount;
    public float noiseFrequencyScale;
    public int replacementIndex;
}

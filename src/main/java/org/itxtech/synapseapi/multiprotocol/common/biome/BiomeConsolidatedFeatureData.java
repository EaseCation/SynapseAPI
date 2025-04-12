package org.itxtech.synapseapi.multiprotocol.common.biome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BiomeConsolidatedFeatureData {
    public BiomeScatterParamData scatter;
    public String feature;
    public String identifier;
    public String pass;
    public boolean canUseInternal;
}

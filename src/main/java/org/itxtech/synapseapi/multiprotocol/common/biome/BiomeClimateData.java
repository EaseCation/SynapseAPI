package org.itxtech.synapseapi.multiprotocol.common.biome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BiomeClimateData {
    public float temperature;
    public float downfall;
    public float redSporeDesnity;
    public float blueSporeDesnity;
    public float ashDesnity;
    public float whiteAshDesnity;
    public float minSnowAccumulation;
    public float maxSnowAccumulation;
}

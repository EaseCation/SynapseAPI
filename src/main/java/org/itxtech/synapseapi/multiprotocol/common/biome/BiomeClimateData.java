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
    /**
     * @deprecated 1.21.110
     */
    public float redSporeDesnity;
    /**
     * @deprecated 1.21.110
     */
    public float blueSporeDesnity;
    /**
     * @deprecated 1.21.110
     */
    public float ashDesnity;
    /**
     * @deprecated 1.21.110
     */
    public float whiteAshDesnity;
    public float minSnowAccumulation;
    public float maxSnowAccumulation;
}

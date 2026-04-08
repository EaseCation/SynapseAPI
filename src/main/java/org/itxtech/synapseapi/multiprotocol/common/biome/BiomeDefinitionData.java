package org.itxtech.synapseapi.multiprotocol.common.biome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BiomeDefinitionData {
    /**
     * Custom biome ID. (uint16)
     */
    @Nullable
    public Integer id;
    public float temperature;
    public float downfall;
    /**
     * 0-1 how frozen the leaves look.
     * @since 1.21.110
     */
    public float foliageSnow;
    /**
     * @deprecated 1.21.110
     */
    public float redSporeDensity;
    /**
     * @deprecated 1.21.110
     */
    public float blueSporeDensity;
    /**
     * @deprecated 1.21.110
     */
    public float ashDensity;
    /**
     * @deprecated 1.21.110
     */
    public float whiteAshDensity;
    public float depth;
    public float scale;
    public int mapWaterColorARGB;
    public boolean rain;
    /**
     * @since 1.21.90-netease
     */
    public int dimension;
    /**
     * @since 1.21.90-netease
     */
    @Default
    public String vanilla = "";
    @Nullable
    public Set<String> tags;
    @Nullable
    public BiomeDefinitionChunkGenData chunkGenData;
}

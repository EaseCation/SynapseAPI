package org.itxtech.synapseapi.multiprotocol.common.biome;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
    public float redSporeDesnity;
    public float blueSporeDesnity;
    public float ashDesnity;
    public float whiteAshDesnity;
    public float depth;
    public float scale;
    public int mapWaterColorARGB;
    public boolean rain;
    @Nullable
    public Set<String> tags;
    @Nullable
    public BiomeDefinitionChunkGenData chunkGenData;
}

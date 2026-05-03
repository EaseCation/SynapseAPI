package org.itxtech.synapseapi.multiprotocol.common.biome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BiomeSurfaceBuilderData {
    public static final BiomeSurfaceBuilderData DEFAULT = builder().build();

    @Nullable
    public BiomeSurfaceMaterialData surfaceMaterials;
    /**
     * @since 1.21.111
     */
    public boolean hasDefaultOverworldSurface;
    public boolean hasSwampSurface;
    public boolean hasFrozenOceanSurface;
    public boolean hasTheEndSurface;
    @Nullable
    public BiomeMesaSurfaceData mesaSurface;
    @Nullable
    public BiomeCappedSurfaceData cappedSurface;
    @Nullable
    public BiomeNoiseGradientSurfaceData noiseGradientSurface;
}

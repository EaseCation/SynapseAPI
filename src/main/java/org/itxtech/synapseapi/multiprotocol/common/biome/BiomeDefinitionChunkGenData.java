package org.itxtech.synapseapi.multiprotocol.common.biome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.Collection;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BiomeDefinitionChunkGenData {
    @Nullable
    public BiomeClimateData climate;
    @Nullable
    public Collection<BiomeConsolidatedFeatureData> consolidatedFeatures;
    @Nullable
    public BiomeMountainParamsData mountainParams;
    @Nullable
    public Collection<BiomeSurfaceMaterialAdjustmentData> surfaceMaterialAdjustments;
    @Nullable
    public BiomeSurfaceMaterialData surfaceMaterials;
    public boolean hasSwampSurface;
    public boolean hasFrozenOceanSurface;
    public boolean hasTheEndSurface;
    @Nullable
    public BiomeMesaSurfaceData mesaSurface;
    @Nullable
    public BiomeCappedSurfaceData cappedSurface;
    @Nullable
    public BiomeOverworldGenRulesData overworldGenRules;
    @Nullable
    public BiomeMultinoiseGenRulesData multinoiseGenRules;
    /**
     * BiomeLegacyWorldGenRulesData
     */
    @Nullable
    public Collection<BiomeConditionalTransformationData> legacyPreHillsEdge;
    @Nullable
    public Collection<BiomeReplacementData> replaceBiomes;
}

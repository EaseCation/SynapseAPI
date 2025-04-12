package org.itxtech.synapseapi.multiprotocol.common.biome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BiomeSurfaceMaterialAdjustmentData {
    public float noiseFreqScale;
    public float noiseLowerBound;
    public float noiseUpperBound;
    public ExpressionOp minHeightType;
    public String minHeight;
    public ExpressionOp maxHeightType;
    public String maxHeight;
    public BiomeSurfaceMaterialData adjustedMaterials;
}

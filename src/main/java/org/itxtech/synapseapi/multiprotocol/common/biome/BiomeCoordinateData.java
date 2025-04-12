package org.itxtech.synapseapi.multiprotocol.common.biome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BiomeCoordinateData {
    public ExpressionOp minValueType;
    public String minValue;
    public ExpressionOp maxValueType;
    public String maxValue;
    public int gridOffset;
    public int gridStepSize;
    public RandomDistributionType distribution;
}

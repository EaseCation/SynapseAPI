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
public class BiomeScatterParamData {
    public Collection<BiomeCoordinateData> coordinates;
    public CoordinateEvaluationOrder evalOrder;
    public ExpressionOp chancePercentType;
    public String chancePercent;
    public int chanceNumerator;
    public int chanceDenominator;
    public ExpressionOp iterationsType;
    public String iterations;
}

package org.itxtech.synapseapi.multiprotocol.common.environment;

public record FloatWeightData(float value) implements WeightData {
    @Override
    public WeightDataType getType() {
        return WeightDataType.FLOAT;
    }
}

package org.itxtech.synapseapi.multiprotocol.common.environment;

public record StringWeightData(String value) implements WeightData {
    @Override
    public WeightDataType getType() {
        return WeightDataType.STRING;
    }
}

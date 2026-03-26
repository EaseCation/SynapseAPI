package org.itxtech.synapseapi.multiprotocol.common.environment;

public record StringColorData(String value) implements ColorData {
    @Override
    public ColorDataType getType() {
        return ColorDataType.STRING;
    }
}

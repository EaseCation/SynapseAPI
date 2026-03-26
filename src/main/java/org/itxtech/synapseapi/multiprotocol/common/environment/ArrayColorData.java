package org.itxtech.synapseapi.multiprotocol.common.environment;

public record ArrayColorData(int r, int g, int b, int a) implements ColorData {
    @Override
    public ColorDataType getType() {
        return ColorDataType.ARRAY;
    }
}

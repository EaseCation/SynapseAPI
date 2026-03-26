package org.itxtech.synapseapi.multiprotocol.common.camera;

public enum SplineType {
    LINE("linear"),
    HERMITE("catmullrom"),
    ;

    public final String name;

    SplineType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }
}

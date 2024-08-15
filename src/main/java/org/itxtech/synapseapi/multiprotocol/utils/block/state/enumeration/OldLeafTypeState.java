package org.itxtech.synapseapi.multiprotocol.utils.block.state.enumeration;

public enum OldLeafTypeState {
    OAK("oak"),
    SPRUCE("spruce"),
    BIRCH("birch"),
    JUNGLE("jungle"),
    ;

    private final String name;

    OldLeafTypeState(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

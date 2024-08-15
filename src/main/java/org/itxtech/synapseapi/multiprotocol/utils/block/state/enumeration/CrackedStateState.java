package org.itxtech.synapseapi.multiprotocol.utils.block.state.enumeration;

public enum CrackedStateState {
    NO_CRACKS("no_cracks"),
    CRACKED("cracked"),
    MAX_CRACKED("max_cracked"),
    ;

    private final String name;

    CrackedStateState(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

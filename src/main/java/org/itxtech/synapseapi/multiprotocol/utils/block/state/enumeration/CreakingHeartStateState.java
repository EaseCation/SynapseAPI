package org.itxtech.synapseapi.multiprotocol.utils.block.state.enumeration;

public enum CreakingHeartStateState {
    UPROOTED("uprooted"),
    DORMANT("dormant"),
    AWAKE("awake"),
    ;

    private final String name;

    CreakingHeartStateState(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

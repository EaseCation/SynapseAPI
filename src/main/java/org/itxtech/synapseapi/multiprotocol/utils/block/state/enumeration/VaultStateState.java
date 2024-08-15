package org.itxtech.synapseapi.multiprotocol.utils.block.state.enumeration;

public enum VaultStateState {
    INACTIVE("inactive"),
    ACTIVE("active"),
    UNLOCKING("unlocking"),
    EJECTING("ejecting"),
    ;

    private final String name;

    VaultStateState(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

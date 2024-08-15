package org.itxtech.synapseapi.multiprotocol.utils.block.state.enumeration;

public enum BambooStalknessThickState {
    THIN("thin"),
    THICK("thick"),
    ;

    private final String name;

    BambooStalknessThickState(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

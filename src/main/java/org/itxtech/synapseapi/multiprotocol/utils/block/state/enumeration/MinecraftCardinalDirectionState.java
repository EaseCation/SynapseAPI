package org.itxtech.synapseapi.multiprotocol.utils.block.state.enumeration;

public enum MinecraftCardinalDirectionState {
    SOUTH("south"),
    WEST("west"),
    NORTH("north"),
    EAST("east"),
    ;

    private final String name;

    MinecraftCardinalDirectionState(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

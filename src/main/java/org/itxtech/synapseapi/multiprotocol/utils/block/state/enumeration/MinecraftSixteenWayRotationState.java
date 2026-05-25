package org.itxtech.synapseapi.multiprotocol.utils.block.state.enumeration;

public enum MinecraftSixteenWayRotationState {
    DOWN("down"),
    UP("up"),
    NORTH("north"),
    SOUTH("south"),
    WEST("west"),
    EAST("east"),
    ;

    private final String name;

    MinecraftSixteenWayRotationState(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

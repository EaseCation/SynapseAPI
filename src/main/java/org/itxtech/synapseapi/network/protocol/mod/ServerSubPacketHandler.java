package org.itxtech.synapseapi.network.protocol.mod;

public interface ServerSubPacketHandler extends SubPacketHandler {
    default void handle(StoreBuySuccessPacket packet) {
    }

    default void handle(AnimationEmotePacket packet) {
    }
}

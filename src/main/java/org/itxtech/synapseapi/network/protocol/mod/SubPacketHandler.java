package org.itxtech.synapseapi.network.protocol.mod;

public interface SubPacketHandler {
    default void handle(SubPacket packet) {
        packet.handle(this);
    }
}

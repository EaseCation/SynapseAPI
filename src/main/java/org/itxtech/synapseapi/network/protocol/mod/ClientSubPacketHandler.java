package org.itxtech.synapseapi.network.protocol.mod;

public interface ClientSubPacketHandler extends SubPacketHandler {
    default void handle(EncryptedPacket packet) {
    }
}

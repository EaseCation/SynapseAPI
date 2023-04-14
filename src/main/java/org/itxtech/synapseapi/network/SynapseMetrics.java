package org.itxtech.synapseapi.network;

public interface SynapseMetrics {
    default void bytesOut(int size) {
    }

    default void packetOut(int packetId, int size) {
    }
}

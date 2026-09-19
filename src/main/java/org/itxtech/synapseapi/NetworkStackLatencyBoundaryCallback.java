package org.itxtech.synapseapi;

public interface NetworkStackLatencyBoundaryCallback {
    void onAppended(long timestamp);

    void onDropped(NetworkStackLatencyBoundaryFailure reason);
}

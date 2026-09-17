package org.itxtech.synapseapi;

import java.util.concurrent.atomic.AtomicBoolean;

final class OnceNetworkStackLatencyBoundaryCallback
        implements NetworkStackLatencyBoundaryCallback {
    private final NetworkStackLatencyBoundaryCallback delegate;
    private final AtomicBoolean completed = new AtomicBoolean();

    OnceNetworkStackLatencyBoundaryCallback(NetworkStackLatencyBoundaryCallback delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onAppended(long timestamp) {
        if (completed.compareAndSet(false, true)) {
            delegate.onAppended(timestamp);
        }
    }

    @Override
    public void onDropped(NetworkStackLatencyBoundaryFailure reason) {
        if (completed.compareAndSet(false, true)) {
            delegate.onDropped(reason);
        }
    }
}

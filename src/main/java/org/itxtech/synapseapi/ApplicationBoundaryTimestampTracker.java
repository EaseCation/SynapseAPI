package org.itxtech.synapseapi;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

final class ApplicationBoundaryTimestampTracker {
    static final int DEFAULT_CAPACITY = 8192;

    private final int capacity;
    private final LongSet timestamps = new LongLinkedOpenHashSet();

    ApplicationBoundaryTimestampTracker() {
        this(DEFAULT_CAPACITY);
    }

    ApplicationBoundaryTimestampTracker(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    synchronized void record(long timestamp) {
        if (!timestamps.add(timestamp)) {
            return;
        }
        while (timestamps.size() > capacity) {
            ((LongLinkedOpenHashSet) timestamps).removeFirstLong();
        }
    }

    synchronized boolean consume(long receivedTimestamp) {
        if (timestamps.remove(receivedTimestamp)) {
            return true;
        }
        if (receivedTimestamp % 1_000L == 0
                && timestamps.remove(receivedTimestamp / 1_000L)) {
            return true;
        }
        return receivedTimestamp % 1_000_000L == 0
                && timestamps.remove(receivedTimestamp / 1_000_000L);
    }

    synchronized boolean remove(long timestamp) {
        return timestamps.remove(timestamp);
    }

    synchronized int size() {
        return timestamps.size();
    }
}

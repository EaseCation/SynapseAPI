package org.itxtech.synapseapi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationBoundaryTimestampTrackerTest {
    @Test
    void consumesExactAndScaledPongsOnlyOnce() {
        ApplicationBoundaryTimestampTracker tracker = new ApplicationBoundaryTimestampTracker();
        tracker.record(101);
        tracker.record(202);
        tracker.record(303);

        assertTrue(tracker.consume(101));
        assertTrue(tracker.consume(202_000L));
        assertTrue(tracker.consume(303_000_000L));
        assertFalse(tracker.consume(101));
        assertFalse(tracker.consume(202_000L));
        assertFalse(tracker.consume(303_000_000L));
        assertEquals(0, tracker.size());
    }

    @Test
    void ordinaryPongDoesNotConsumeBoundary() {
        ApplicationBoundaryTimestampTracker tracker = new ApplicationBoundaryTimestampTracker();
        tracker.record(77);

        assertFalse(tracker.consume(76));
        assertFalse(tracker.consume(78_000L));
        assertEquals(1, tracker.size());
        assertTrue(tracker.consume(77_000_000L));
    }

    @Test
    void capacityEvictsOldestBoundary() {
        ApplicationBoundaryTimestampTracker tracker = new ApplicationBoundaryTimestampTracker(2);
        tracker.record(1);
        tracker.record(2);
        tracker.record(3);

        assertFalse(tracker.consume(1));
        assertTrue(tracker.consume(2));
        assertTrue(tracker.consume(3));
    }

    @Test
    void explicitRemovalPreventsLaterConsumption() {
        ApplicationBoundaryTimestampTracker tracker = new ApplicationBoundaryTimestampTracker();
        tracker.record(42);

        assertTrue(tracker.remove(42));
        assertFalse(tracker.consume(42_000_000L));
    }

    @Test
    void boundaryCompletionCanOnlySucceedOrDropOnce() {
        CountingCallback appended = new CountingCallback();
        OnceNetworkStackLatencyBoundaryCallback appendedOnce =
                new OnceNetworkStackLatencyBoundaryCallback(appended);
        appendedOnce.onAppended(10);
        appendedOnce.onDropped(NetworkStackLatencyBoundaryFailure.ENCODE_FAILED);
        appendedOnce.onAppended(11);

        assertEquals(1, appended.appendedCount);
        assertEquals(0, appended.droppedCount);
        assertEquals(10, appended.timestamp);

        CountingCallback dropped = new CountingCallback();
        OnceNetworkStackLatencyBoundaryCallback droppedOnce =
                new OnceNetworkStackLatencyBoundaryCallback(dropped);
        droppedOnce.onDropped(NetworkStackLatencyBoundaryFailure.PLAYER_CLOSED);
        droppedOnce.onAppended(20);
        droppedOnce.onDropped(NetworkStackLatencyBoundaryFailure.QUEUE_REJECTED);

        assertEquals(0, dropped.appendedCount);
        assertEquals(1, dropped.droppedCount);
        assertEquals(NetworkStackLatencyBoundaryFailure.PLAYER_CLOSED, dropped.reason);
    }

    private static final class CountingCallback
            implements NetworkStackLatencyBoundaryCallback {
        private int appendedCount;
        private int droppedCount;
        private long timestamp;
        private NetworkStackLatencyBoundaryFailure reason;

        @Override
        public void onAppended(long timestamp) {
            appendedCount++;
            this.timestamp = timestamp;
        }

        @Override
        public void onDropped(NetworkStackLatencyBoundaryFailure reason) {
            droppedCount++;
            this.reason = reason;
        }
    }
}

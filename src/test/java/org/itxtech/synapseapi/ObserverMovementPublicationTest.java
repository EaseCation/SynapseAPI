package org.itxtech.synapseapi;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class ObserverMovementPublicationTest {
    private static void raw(ObserverMovementPublication state, long tick, long epoch, float x, Runnable send) {
        state.publish(tick, epoch, x, 64, 0, 0, 0, () -> true, send);
    }
    private static void commit(ObserverMovementPublication state, long tick, long epoch, float x, Runnable send) {
        state.reconcile(tick, epoch, x, 64, 0, 0, 0, send);
    }
    @Test void newerRawIsNotOverwrittenByEarlierFifoCommitOrDuplicatedWhenAccepted() {
        ObserverMovementPublication state = new ObserverMovementPublication();
        List<String> wire = new ArrayList<>();
        raw(state, 105, 0, 5, () -> wire.add("raw105"));
        commit(state, 101, 0, 1, () -> wire.add("commit101"));
        commit(state, 105, 0, 5, () -> wire.add("commit105"));
        assertEquals(List.of("raw105"), wire);
    }
    @Test void rejectedPositionConvergesEvenWhenFollowingInputIsStationary() {
        ObserverMovementPublication state = new ObserverMovementPublication();
        List<String> wire = new ArrayList<>();
        raw(state, 105, 0, 5, () -> wire.add("raw"));
        commit(state, 105, 1, 0, () -> wire.add("correct"));
        commit(state, 106, 1, 0, () -> wire.add("duplicate"));
        assertEquals(List.of("raw", "correct"), wire);
    }
    @Test void stationaryInputsDoNotMultiplyObserverTraffic() {
        ObserverMovementPublication state = new ObserverMovementPublication();
        List<String> wire = new ArrayList<>();
        raw(state, 1, 0, 5, () -> wire.add("position"));
        for (int tick = 2; tick <= 100; tick++) raw(state, tick, 0, 5, () -> wire.add("duplicate"));
        commit(state, 50, 0, 4, () -> wire.add("old"));
        commit(state, 100, 0, 5, () -> wire.add("accepted"));
        assertEquals(List.of("position"), wire);
    }
    @Test void equivalentAnglesDoNotOscillateBetweenRawAndCommittedPublication() {
        ObserverMovementPublication state = new ObserverMovementPublication();
        List<String> wire = new ArrayList<>();
        for (int tick = 1; tick <= 100; tick++) {
            state.publish(tick, 0, 1, 64, 0, -0.000015258789f, 0, () -> true, () -> wire.add("raw"));
            state.reconcile(tick, 0, 1, 64, 0, 360, 0, () -> wire.add("duplicate"));
        }
        assertEquals(List.of("raw"), wire);
    }
    @Test void lifecycleInvalidationWinsOverNewerRawTick() {
        ObserverMovementPublication state = new ObserverMovementPublication();
        raw(state, 105, 0, 5, () -> {});
        List<String> wire = new ArrayList<>();
        commit(state, 101, 1, 0, () -> wire.add("teleport"));
        state.publish(106, 0, 5, 64, 0, 0, 0, () -> false, () -> fail("Retired source"));
        assertEquals(List.of("teleport"), wire);
    }
    @Test void invalidNumbersDuplicateTicksAndReversedTicksAreNotShown() {
        ObserverMovementPublication state = new ObserverMovementPublication();
        raw(state, 105, 0, 5, () -> {});
        raw(state, 105, 0, 6, () -> fail("Duplicate"));
        raw(state, 104, 0, 6, () -> fail("Reversed"));
        raw(state, 106, 0, Float.NaN, () -> fail("NaN"));
        raw(state, 106, 0, Float.POSITIVE_INFINITY, () -> fail("Infinity"));
    }
    @Test void hangingNetworkCallbackCannotBlockMainAndEventuallyConverges() throws Exception {
        ObserverMovementPublication state = new ObserverMovementPublication();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread network = new Thread(() -> raw(state, 105, 0, 5, () -> {
            entered.countDown();
            try { assertTrue(release.await(5, TimeUnit.SECONDS)); }
            catch (InterruptedException exception) { throw new AssertionError(exception); }
        }));
        network.start();
        try {
            assertTrue(entered.await(2, TimeUnit.SECONDS));
            assertTimeoutPreemptively(java.time.Duration.ofMillis(100),
                    () -> commit(state, 105, 1, 0, () -> fail("Must defer display only")));
        } finally { release.countDown(); network.join(); }
        List<String> wire = new ArrayList<>();
        commit(state, 105, 1, 0, () -> wire.add("correct"));
        assertEquals(List.of("correct"), wire);
    }
    @Test void mainMayFinishBeforeNetworkGetsToDisplayWithoutLateRawRollback() {
        ObserverMovementPublication state = new ObserverMovementPublication();
        commit(state, 105, 0, 5, () -> {});
        raw(state, 105, 0, 4, () -> fail("Already consumed input must not be shown late"));
    }
    @Test void displayCallbackFailureDoesNotEscapeIntoInputOrLoginFlow() {
        ObserverMovementPublication state = new ObserverMovementPublication();
        assertDoesNotThrow(() -> raw(state, 1, 0, 1, () -> { throw new IllegalStateException("Fault injection"); }));
        assertDoesNotThrow(() -> commit(state, 1, 0, 0, () -> { throw new IllegalStateException("Fault injection"); }));
        raw(state, 2, 0, 2, () -> fail("Failed display path must stop publishing"));
    }
}

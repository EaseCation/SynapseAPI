package org.itxtech.synapseapi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PendingPingTest {
    @Test
    void repeatedRequestDoesNotReplaceTheUnfinishedOrigin() {
        PendingPing ping = new PendingPing();
        assertTrue(ping.begin(100));
        assertFalse(ping.begin(120));
        assertEquals(-1, ping.acknowledge(120, 140));
        assertTrue(ping.isPending());
        assertEquals(40, ping.acknowledge(100, 140));
    }

    @Test
    void unrelatedAndDuplicateRepliesCannotUpdateTheNextSample() {
        PendingPing ping = new PendingPing();
        assertTrue(ping.begin(100));
        assertEquals(-1, ping.acknowledge(101, 105));
        assertEquals(5, ping.acknowledge(100, 105));
        assertFalse(ping.isPending());
        assertEquals(-1, ping.acknowledge(100, 110));
        assertTrue(ping.begin(200));
        assertEquals(-1, ping.acknowledge(100, 205));
        assertEquals(5, ping.acknowledge(200, 205));
    }

    @Test
    void implementedWireTimestampScalesCorrelateIncludingSignedOverflow() {
        long started = Long.MAX_VALUE - 17;
        for (long factor : new long[]{1, 1000, 1000000}) {
            PendingPing ping = new PendingPing();
            assertTrue(ping.begin(started));
            assertEquals(5, ping.acknowledge(started * factor, started + 5));
        }
    }

    @Test
    void expiredRequestCannotCompleteItsReplacement() {
        PendingPing ping = new PendingPing();
        assertTrue(ping.begin(100));
        ping.expire();
        assertTrue(ping.begin(200));
        assertEquals(-1, ping.acknowledge(100, 250));
        assertTrue(ping.isPending());
        assertEquals(50, ping.acknowledge(200, 250));
    }

    @Test
    void validFastReplyIsNotDiscardedByAnAssumedTickFloor() {
        PendingPing ping = new PendingPing();
        assertTrue(ping.begin(1000000));
        assertEquals(2000000, ping.acknowledge(1000000, 3000000));
    }

    @Test
    void clockRegressionDoesNotConsumeThePendingRequest() {
        PendingPing ping = new PendingPing();
        assertTrue(ping.begin(100));
        assertEquals(-1, ping.acknowledge(100, 99));
        assertTrue(ping.isPending());
        assertEquals(1, ping.acknowledge(100, 101));
    }
}

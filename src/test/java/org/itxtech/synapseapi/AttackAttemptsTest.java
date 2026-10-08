package org.itxtech.synapseapi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttackAttemptsTest {
    @Test
    void separateDrainsInTheSameWorldTickShareTheOriginalOneAttemptLimit() {
        AttackAttempts attempts = new AttackAttempts(2);
        assertTrue(attempts.claim(37));
        assertFalse(attempts.claim(37));
        assertFalse(attempts.claim(37));
        assertEquals(37, attempts.tick());
        assertEquals(2, attempts.count(37));
    }

    @Test
    void aNewRealTickReceivesOneNewAllowance() {
        AttackAttempts attempts = new AttackAttempts(2);
        assertTrue(attempts.claim(37));
        assertFalse(attempts.claim(37));
        assertEquals(0, attempts.count(38));
        assertTrue(attempts.claim(38));
        assertFalse(attempts.claim(38));
    }

    @Test
    void elapsedOrSkippedTicksDoNotAccumulateAttackCredits() {
        AttackAttempts attempts = new AttackAttempts(2);
        assertTrue(attempts.claim(1));
        assertTrue(attempts.claim(100));
        assertFalse(attempts.claim(100));
    }

    @Test
    void anUnusedWindowCannotBeConsumedByDiagnostics() {
        AttackAttempts attempts = new AttackAttempts(2);
        assertEquals(0, attempts.count(37));
        assertEquals(0, attempts.count(38));
        assertEquals(Integer.MIN_VALUE, attempts.tick());
        assertTrue(attempts.claim(38));
        assertEquals(1, attempts.count(38));
    }

    @Test
    void sustainedRejectedTrafficCannotWrapAndRegainPermission() {
        AttackAttempts attempts = new AttackAttempts(2);
        assertTrue(attempts.claim(37));
        for (int index = 0; index < 10000; index++) {
            assertFalse(attempts.claim(37));
        }
        assertEquals(2, attempts.count(37));
        assertTrue(attempts.claim(38));
    }
}

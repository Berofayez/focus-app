package com.focusapp.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LockManagerTest {

    @Test
    fun `lockedUntil is startedAt plus plannedMinutes`() {
        val manager = LockManager()
        val startedAt = 1_000_000L
        assertEquals(1_000_000L + 5 * 60_000L, manager.lockedUntil(startedAt, plannedMinutes = 5))
    }

    @Test
    fun `isLocked is true before the end timestamp`() {
        val startedAt = 0L
        val lockedUntil = LockManager().lockedUntil(startedAt, plannedMinutes = 10)
        val manager = LockManager(nowMillis = { lockedUntil - 1 })
        assertTrue(manager.isLocked(lockedUntil))
    }

    @Test
    fun `isLocked is false exactly at the end timestamp`() {
        val lockedUntil = 10_000L
        val manager = LockManager(nowMillis = { lockedUntil })
        assertFalse(manager.isLocked(lockedUntil))
    }

    @Test
    fun `isLocked is false after the end timestamp`() {
        val lockedUntil = 10_000L
        val manager = LockManager(nowMillis = { lockedUntil + 1 })
        assertFalse(manager.isLocked(lockedUntil))
    }

    @Test
    fun `remainingMillis counts down to zero and never goes negative`() {
        val lockedUntil = 10_000L
        assertEquals(4_000L, LockManager(nowMillis = { 6_000L }).remainingMillis(lockedUntil))
        assertEquals(0L, LockManager(nowMillis = { 10_000L }).remainingMillis(lockedUntil))
        assertEquals(0L, LockManager(nowMillis = { 15_000L }).remainingMillis(lockedUntil))
    }

    @Test
    fun `lock survives being recomputed from scratch, as after an app restart`() {
        // Simulates killing and reopening the app: a fresh LockManager/clock,
        // but the same persisted startedAt and plannedMinutes.
        val startedAt = 500_000L
        val plannedMinutes = 1
        val lockedUntil = LockManager().lockedUntil(startedAt, plannedMinutes)

        val afterRestart = LockManager(nowMillis = { startedAt + 30_000L })
        assertTrue(afterRestart.isLocked(lockedUntil))
        assertEquals(30_000L, afterRestart.remainingMillis(lockedUntil))
    }
}

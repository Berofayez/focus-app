package com.focusapp.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class EmergencyPassPolicyTest {
    // Fixed zone so these tests don't depend on the machine's local timezone.
    private val zone = ZoneOffset.UTC

    // 2024-01-08 is a Monday.
    private val mondayStart = Instant.parse("2024-01-08T00:00:00Z").toEpochMilli()

    @Test
    fun `weekStart for a mid-week timestamp is that week's Monday`() {
        val wednesday = Instant.parse("2024-01-10T15:30:00Z").toEpochMilli()
        assertEquals(mondayStart, EmergencyPassPolicy(zone = zone).weekStart(wednesday))
    }

    @Test
    fun `weekStart on Monday itself is the same day's start`() {
        val mondayNoon = Instant.parse("2024-01-08T12:00:00Z").toEpochMilli()
        assertEquals(mondayStart, EmergencyPassPolicy(zone = zone).weekStart(mondayNoon))
    }

    @Test
    fun `weekStart on Sunday rolls back to the previous Monday`() {
        val sunday = Instant.parse("2024-01-14T23:59:00Z").toEpochMilli()
        assertEquals(mondayStart, EmergencyPassPolicy(zone = zone).weekStart(sunday))
    }

    @Test
    fun `weekStart for the following Monday is a week later`() {
        val nextMonday = Instant.parse("2024-01-15T00:00:00Z").toEpochMilli()
        assertEquals(nextMonday, EmergencyPassPolicy(zone = zone).weekStart(nextMonday))
    }

    @Test
    fun `canUsePass respects the weekly maximum`() {
        val policy = EmergencyPassPolicy(maxPassesPerWeek = 2)
        assertTrue(policy.canUsePass(usedThisWeek = 0))
        assertTrue(policy.canUsePass(usedThisWeek = 1))
        assertFalse(policy.canUsePass(usedThisWeek = 2))
    }

    @Test
    fun `waitUntil is requestedAt plus waitMinutes`() {
        val policy = EmergencyPassPolicy(waitMinutes = 15)
        assertEquals(1_000_000L + 15 * 60_000L, policy.waitUntil(1_000_000L))
    }

    @Test
    fun `isWaiting and remainingMillis count down like the session lock`() {
        val waitUntil = 10_000L
        val policy = EmergencyPassPolicy(nowMillis = { 4_000L })
        assertTrue(policy.isWaiting(waitUntil))
        assertEquals(6_000L, policy.remainingMillis(waitUntil))
    }

    @Test
    fun `isWaiting is false once the wait completes, even after an app restart`() {
        val requestedAt = 500_000L
        val policy = EmergencyPassPolicy(waitMinutes = 15)
        val waitUntil = policy.waitUntil(requestedAt)

        val afterRestart = EmergencyPassPolicy(nowMillis = { waitUntil + 1 })
        assertFalse(afterRestart.isWaiting(waitUntil))
    }
}

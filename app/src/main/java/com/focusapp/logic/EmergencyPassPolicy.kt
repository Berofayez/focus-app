package com.focusapp.logic

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

// Pure logic, no Android dependencies - mirrors LockManager's "derive from a
// stored timestamp, never a running countdown" approach, so the wait and the
// weekly limit both survive an app restart.
class EmergencyPassPolicy(
    private val nowMillis: () -> Long = System::currentTimeMillis,
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val maxPassesPerWeek: Int = 2,
    private val waitMinutes: Int = 15,
) {
    fun currentWeekStart(): Long = weekStart(nowMillis())

    // The most recent Monday 00:00 in local time, for the given instant.
    fun weekStart(epochMillis: Long): Long {
        val date = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return monday.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun canUsePass(usedThisWeek: Int): Boolean = usedThisWeek < maxPassesPerWeek

    fun waitUntil(requestedAt: Long): Long = requestedAt + waitMinutes * MILLIS_PER_MINUTE

    fun isWaiting(waitUntil: Long): Boolean = nowMillis() < waitUntil

    fun remainingMillis(waitUntil: Long): Long = (waitUntil - nowMillis()).coerceAtLeast(0)

    companion object {
        private const val MILLIS_PER_MINUTE = 60_000L
    }
}

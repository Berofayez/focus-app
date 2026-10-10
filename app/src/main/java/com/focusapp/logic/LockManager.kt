package com.focusapp.logic

// Pure logic, no Android dependencies, so it can be unit tested on the plain JVM.
// The lock is always derived from a fixed end timestamp, never a running countdown,
// so it reads correctly after the app or phone restarts.
class LockManager(private val nowMillis: () -> Long = System::currentTimeMillis) {

    fun lockedUntil(startedAt: Long, plannedMinutes: Int): Long =
        startedAt + plannedMinutes * MILLIS_PER_MINUTE

    fun isLocked(lockedUntil: Long): Boolean = nowMillis() < lockedUntil

    fun remainingMillis(lockedUntil: Long): Long = (lockedUntil - nowMillis()).coerceAtLeast(0)

    companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}

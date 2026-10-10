package com.focusapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val title: String,
    val firstStep: String,
    val plannedMinutes: Int,
    val allowedApps: List<String>,
    val allowedSites: List<String>,
    val status: SessionStatus,
    val startedAt: Long?,
    val endedAt: Long?,
    val outcome: SessionOutcome? = null,
    // Non-null while an emergency exit's 15-minute wait is running.
    val emergencyExitRequestedAt: Long? = null,
    val emergencyExitReason: String? = null,
)

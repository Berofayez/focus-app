package com.focusapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// One row per consumed emergency pass; feeds the weekly limit and the
// weekly-review list of reasons (ticket 09).
@Entity(tableName = "emergency_pass_logs")
data class EmergencyPassLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val reason: String,
    val usedAt: Long,
)

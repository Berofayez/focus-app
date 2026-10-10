package com.focusapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyPassLogDao {
    @Insert
    suspend fun insert(log: EmergencyPassLog)

    @Query("SELECT * FROM emergency_pass_logs WHERE usedAt >= :weekStart ORDER BY usedAt DESC")
    fun getSince(weekStart: Long): Flow<List<EmergencyPassLog>>
}

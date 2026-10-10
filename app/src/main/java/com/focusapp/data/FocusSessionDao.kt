package com.focusapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Insert
    suspend fun insert(session: FocusSession): Long

    @Update
    suspend fun update(session: FocusSession)

    // Sessions that are not yet finished, i.e. today's remaining work.
    @Query("SELECT * FROM focus_sessions WHERE status IN ('PLANNED', 'LOCKED') ORDER BY id ASC")
    fun getActive(): Flow<List<FocusSession>>

    // At most one session is locked at a time (one thing at a time).
    @Query("SELECT * FROM focus_sessions WHERE status = 'LOCKED' ORDER BY id DESC LIMIT 1")
    fun getCurrentLocked(): Flow<FocusSession?>
}

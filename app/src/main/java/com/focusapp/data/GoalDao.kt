package com.focusapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert
    suspend fun insert(goal: Goal)

    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Goal>>
}

package com.focusapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Goal::class, FocusSession::class, EmergencyPassLog::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FocusAppDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun emergencyPassLogDao(): EmergencyPassLogDao

    companion object {
        @Volatile
        private var instance: FocusAppDatabase? = null

        fun getInstance(context: Context): FocusAppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FocusAppDatabase::class.java,
                    "focus-app.db",
                )
                    // Schema is still evolving ticket by ticket pre-release; wiping
                    // local data on a schema bump is acceptable until we ship.
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}

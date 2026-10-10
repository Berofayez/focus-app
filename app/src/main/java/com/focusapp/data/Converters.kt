package com.focusapp.data

import androidx.room.TypeConverter

// Allowed apps/sites are stored as a single comma-separated column;
// none of these values are expected to contain commas themselves.
class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(",")

    @TypeConverter
    fun fromSessionStatus(status: SessionStatus): String = status.name

    @TypeConverter
    fun toSessionStatus(value: String): SessionStatus = SessionStatus.valueOf(value)

    @TypeConverter
    fun fromSessionOutcome(outcome: SessionOutcome): String = outcome.name

    @TypeConverter
    fun toSessionOutcome(value: String): SessionOutcome = SessionOutcome.valueOf(value)
}

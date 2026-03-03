package com.application.requiemproject.data.converters

import androidx.room.TypeConverter

/**
 * First of all, this converter is needed to convert a list into a string for the room database.
 * In this application, it is used to write all the values from a specific account's "Achievements"
 * to its database.
 */
class RoomTypeConverters {

    @TypeConverter
    fun fromListToString(privilege: List<String>?): String? {
        return privilege?.joinToString(",")
    }

    @TypeConverter
    fun fromStringToList(privilege: String?): List<String>? {
        return privilege?.split(",")?.map { it.trim() }
    }

}
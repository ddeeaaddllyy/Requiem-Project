package com.application.requiemproject.data.converters

import androidx.room.TypeConverter

/**
 * Type converters for Room database.
 *
 * Room does not natively support storing collections like List<String>.
 * This class provides methods to convert between List<String> and a
 * comma-separated String representation, which can be stored in a
 * single database column.
 *
 * The converters are automatically applied by Room to any entity field
 * of type List<String> when annotated with @TypeConverter.
 *
 * @see TypeConverter
 * @since 1.0.0
 */
class RoomTypeConverters {

    /**
     * Converts a list of strings into a single comma-separated string.
     *
     * This method is used by Room when persisting a List<String> property.
     * If the input list is null, the result is also null, which allows
     * storing NULL in the database for absent values.
     *
     * @param list The list of strings to convert. Can be null.
     * @return A comma-separated string containing all elements of the input list,
     *         or null if the input list is null.
     *
     * Example:
     * ```
     * fromListToString(listOf("apple", "banana")) // returns "apple,banana"
     * fromListToString(null)                      // returns null
     * ```
     */
    @TypeConverter
    fun fromListToString(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    /**
     * Reconstructs a list of strings from a comma-separated string.
     *
     * This method reverses the conversion performed by [fromListToString].
     * It splits the input string by commas and trims each resulting element
     * to remove any accidental leading or trailing whitespace.
     *
     * If the input string is null, the method returns null, preserving
     * the NULL database value as a null list.
     *
     * @param data The comma-separated string to convert. Can be null.
     * @return A list of strings obtained by splitting the input, or null
     *         if the input is null. Each element is trimmed of whitespace.
     *
     * Example:
     *```
     * fromStringToList("apple, banana") // returns ["apple", "banana"]
     * fromStringToList(null)             // returns null
     *```
     */
    @TypeConverter
    fun fromStringToList(data: String?): List<String>? {
        return data?.split(",")?.map { it.trim() }
    }
}
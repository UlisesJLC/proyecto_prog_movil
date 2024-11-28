package com.example.inventory.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",") // O usar Gson para JSON
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return value.split(",").filter { it.isNotEmpty() }
    }
}

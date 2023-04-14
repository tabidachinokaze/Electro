package cn.tabidachi.electro.data.database.entity

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ListTypeConverter {
    @TypeConverter
    fun encode(list: List<Long>): String {
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun decode(json: String): List<Long> {
        return Json.decodeFromString(json)
    }
}
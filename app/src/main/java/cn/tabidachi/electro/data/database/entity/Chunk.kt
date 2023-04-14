package cn.tabidachi.electro.data.database.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Chunk(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val md5: String = "",
    val path: String,
    val offset: Int,
    val uploaded: Boolean
) {
    @Ignore
    val filename = "$md5-$id"
}
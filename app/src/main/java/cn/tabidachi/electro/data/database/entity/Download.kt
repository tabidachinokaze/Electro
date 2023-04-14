package cn.tabidachi.electro.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Download(
    @PrimaryKey
    val id: String,
    val path: String,
    val completed: Boolean
)
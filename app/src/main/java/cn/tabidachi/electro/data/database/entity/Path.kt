package cn.tabidachi.electro.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Path(
    @PrimaryKey
    val id: String,
    val path: String?
)
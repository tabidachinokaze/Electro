package cn.tabidachi.electro.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Search(
    @PrimaryKey
    val uid: Long,
    val query: String
)
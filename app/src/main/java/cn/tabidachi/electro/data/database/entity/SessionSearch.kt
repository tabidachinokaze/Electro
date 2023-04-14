package cn.tabidachi.electro.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class SessionSearch(
    @PrimaryKey
    val sid: Long,
    val type: SessionType,
    val title: String?,
    val description: String?,
    val image: String?,
    val createTime: Long,
    val count: Int,
)
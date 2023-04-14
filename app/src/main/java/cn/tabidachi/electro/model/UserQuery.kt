package cn.tabidachi.electro.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class UserQuery(
    @PrimaryKey
    val uid: Long,
    val username: String,
    val avatar: String
)
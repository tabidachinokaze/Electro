package cn.tabidachi.electro.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.tabidachi.electro.model.Identifiable
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class User(
    @PrimaryKey
    val uid: Long,
    val username: String,
    val email: String,
    val avatar: String
) : Identifiable {
    override fun identification(): String = "user.$uid.avatar"
}

package cn.tabidachi.electro.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.tabidachi.electro.model.Identifiable
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Session(
    @PrimaryKey
    val sid: Long,
    val type: SessionType,
    val title: String?,
    val description: String?,
    val image: String?,
    val createTime: Long,
    val updateTime: Long,
    val isPublic: Boolean,
    val needRequest: Boolean
) : Identifiable {
    override fun identification(): String = "session.$sid.image"
}
package cn.tabidachi.electro.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.tabidachi.electro.model.Identifiable
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class MessageSendRequest(
    @PrimaryKey
    val id: String,
    val sid: Long,
    val uid: Long,
    val forward: Long?,
    val reply: Long?,
    val type: MessageType,
    val text: String?,
    val attachment: String?,
    val createTime: Long,
) : Identifiable {
    override fun identification() = "MessageSendRequest.$id"
}
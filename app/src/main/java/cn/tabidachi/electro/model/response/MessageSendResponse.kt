package cn.tabidachi.electro.model.response

import cn.tabidachi.electro.data.database.entity.MessageType
import kotlinx.serialization.Serializable

@Serializable
data class MessageSendResponse(
    val id: String,
    val mid: Long,
    val sid: Long,
    val uid: Long,
    val forward: Long?,
    val reply: Long?,
    val type: MessageType,
    val text: String?,
    val attachment: String?,
    val createTime: Long,
    val updateTime: Long
)
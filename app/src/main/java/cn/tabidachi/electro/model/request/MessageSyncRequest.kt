package cn.tabidachi.electro.model.request

import cn.tabidachi.electro.data.database.entity.Message
import kotlinx.serialization.Serializable

@Serializable
data class MessageSyncRequest(
    val mid: Long,
    val updateTime: Long,
)

@Serializable
data class MessageSyncResponse(
    val updates: List<Message>,
    val deletes: List<Long>
)


package cn.tabidachi.electro.model.request

import kotlinx.serialization.Serializable

@Serializable
data class MessageRequest(
    val sid: Long,
    val between: Pair<Long?, Long?>,
    val type: Type,
    val limit: Int
) {
    enum class Type {
        NONE, UNREAD, BETWEEN
    }
}
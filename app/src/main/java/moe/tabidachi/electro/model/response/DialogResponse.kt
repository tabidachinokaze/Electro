package moe.tabidachi.electro.model.response

import kotlinx.serialization.Serializable
import moe.tabidachi.electro.data.database.entity.SessionType

@Serializable
data class DialogResponse(
    val sid: Long,
    val uid: Long,
    val type: SessionType,
    val image: String?,
    val title: String?,
    val subtitle: String?,
    val latest: Long?,
    val unread: Int?,
    val extras: String?
)

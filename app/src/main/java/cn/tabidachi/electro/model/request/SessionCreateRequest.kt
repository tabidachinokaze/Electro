package cn.tabidachi.electro.model.request

import cn.tabidachi.electro.data.database.entity.SessionType
import kotlinx.serialization.Serializable

@Serializable
data class SessionCreateRequest(
    val type: SessionType,
    val title: String?,
    val description: String?,
    val image: String?,
)

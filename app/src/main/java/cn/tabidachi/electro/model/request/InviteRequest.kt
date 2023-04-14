package cn.tabidachi.electro.model.request

import kotlinx.serialization.Serializable

@Serializable
data class InviteRequest(
    val sid: Long,
    val target: Long,
)
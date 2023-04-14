package cn.tabidachi.electro.model

import kotlinx.serialization.Serializable

@Serializable
data class OnlineStatus(
    val target: Long,
    val isOnline: Boolean
)
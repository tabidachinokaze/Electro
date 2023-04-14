package cn.tabidachi.electro.model.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val uid: Long,
    val token: String,
)
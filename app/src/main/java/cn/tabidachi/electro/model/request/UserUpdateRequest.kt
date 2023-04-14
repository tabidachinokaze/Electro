package cn.tabidachi.electro.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateRequest(
    val username: String?,
    val password: String?,
    val email: String?,
    val avatar: String?
)

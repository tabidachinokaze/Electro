package cn.tabidachi.electro.model.request

import kotlinx.serialization.Serializable

@Serializable
data class GroupUpdateRequest(
    val image: String?,
    val title: String?,
    val description: String?
)

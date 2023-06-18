package cn.tabidachi.electro.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ChannelRole(
    val sid: Long,
    val uid: Long,
    val type: ChannelRoleType,
    val canPostMessage: Boolean,
    val canBanUser: Boolean,
    val canEditMessageOfOthers: Boolean,
    val canDeleteMessageOfOthers: Boolean,
    val canAddNewAdmin: Boolean,
)

enum class ChannelRoleType {
    OWNER, ADMIN
}
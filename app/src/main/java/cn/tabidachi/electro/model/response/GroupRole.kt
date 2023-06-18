package cn.tabidachi.electro.model.response

import kotlinx.serialization.Serializable

@Serializable
data class GroupRole(
    val sid: Long,
    val uid: Long,
    val type: GroupRoleType,
    val canChangeGroupInfo: Boolean,
    val canDeleteMessage: Boolean,
    val canBanUser: Boolean,
    val canPinMessage: Boolean,
    val canAddNewAdmin: Boolean,
)

enum class GroupRoleType {
    OWNER, ADMIN
}
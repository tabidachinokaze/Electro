package cn.tabidachi.electro.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class SessionUser(
    @PrimaryKey
    val sid: Long,
    val type: SessionType,
    val users: List<Long>
)

enum class SessionUserState {
    NONE,
    // 创建者
    CREATOR,
    // 请求加入
    REQUEST,
    // 会话成员
    MEMBER,
    // 被封禁
    BANNED,
    // 被邀请
    INVITED
}
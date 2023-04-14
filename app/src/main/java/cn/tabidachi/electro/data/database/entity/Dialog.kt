package cn.tabidachi.electro.data.database.entity

import androidx.room.Entity
import cn.tabidachi.electro.data.network.DialogResponse
import cn.tabidachi.electro.model.Identifiable
import kotlinx.serialization.Serializable

@Entity(primaryKeys = ["sid", "uid"])
@Serializable
data class Dialog(
    val sid: Long,
    val uid: Long,
    val type: SessionType,
    val image: String?,
    val title: String?,
    val subtitle: String?,
    val latest: Long?,
    val unread: Int?,
    val extras: String?
) : Identifiable {
    constructor(response: DialogResponse) : this(
        response.sid,
        response.uid,
        response.type,
        response.image,
        response.title,
        response.subtitle,
        response.latest,
        response.unread,
        response.extras,
    )
    override fun identification() = "dialog.$sid.$uid.$image"
}

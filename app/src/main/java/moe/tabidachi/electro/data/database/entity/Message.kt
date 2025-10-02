package moe.tabidachi.electro.data.database.entity

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import moe.tabidachi.electro.R
import moe.tabidachi.electro.model.Describable
import moe.tabidachi.electro.model.Identifiable
import moe.tabidachi.electro.model.attachment.Attachment
import moe.tabidachi.electro.model.attachment.AudioAttachment
import moe.tabidachi.electro.model.attachment.FileAttachment
import moe.tabidachi.electro.model.attachment.ImageAttachment
import moe.tabidachi.electro.model.attachment.LocationAttachment
import moe.tabidachi.electro.model.attachment.VideoAttachment
import moe.tabidachi.electro.model.attachment.VoiceAttachment
import moe.tabidachi.electro.model.attachment.WebRTCAttachment
import moe.tabidachi.electro.model.attachment.deserialize
import moe.tabidachi.electro.model.response.MessageSendResponse
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Message(
    @PrimaryKey
    val mid: Long,
    val sid: Long,
    val uid: Long,
    val forward: Long?,
    val reply: Long?,
    val type: MessageType,
    val text: String?,
    val attachment: String?,
    val createTime: Long,
    val updateTime: Long
) : Identifiable, Describable {
    constructor(response: MessageSendResponse) : this(
        response.mid,
        response.sid,
        response.uid,
        response.forward,
        response.reply,
        response.type,
        response.text,
        response.attachment,
        response.createTime,
        response.updateTime,
    )

    override fun identification() = "message.$mid"

    @Ignore
    val deserialize: Attachment? = Attachment.deserialize(type, attachment)

    @Composable
    override fun description(): String = when (deserialize) {
        is AudioAttachment -> "[${stringResource(id = R.string.music)}]${deserialize.title ?: deserialize.filename} ${text ?: ""}"
        is FileAttachment -> "[${stringResource(id = R.string.file)}]${deserialize.filename} ${text ?: ""}"
        is ImageAttachment -> "[${stringResource(id = R.string.image)}]${deserialize.filename} ${text ?: ""}"
        is VideoAttachment -> "[${stringResource(id = R.string.video)}]${deserialize.filename} ${text ?: ""}"
        is VoiceAttachment -> "[${stringResource(id = R.string.voice)}] ${text ?: ""}"
        is LocationAttachment -> "[${stringResource(id = R.string.location)}] ${text ?: ""}"
        is WebRTCAttachment -> "[${stringResource(id = R.string.call)}] ${text ?: ""}"
        null -> text ?: ""
    }
}

enum class MessageType {
    TEXT, IMAGE, AUDIO, VIDEO, LOCATION, VOICE, FILE, WEBRTC
}

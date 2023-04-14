package cn.tabidachi.electro.model.attachment

import cn.tabidachi.electro.data.database.entity.MessageType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed interface Attachment {
    companion object
}

fun Attachment.serialize(): String {
    return when (val attachment = this) {
        is AudioAttachment -> Json.encodeToString(attachment)
        is FileAttachment -> Json.encodeToString(attachment)
        is WebRTCAttachment -> Json.encodeToString(attachment)
        is ImageAttachment -> Json.encodeToString(attachment)
        is VideoAttachment -> Json.encodeToString(attachment)
        is VoiceAttachment -> Json.encodeToString(attachment)
        is LocationAttachment -> Json.encodeToString(attachment)
    }
}

fun Attachment.convert(): MessageType {
    return when (this) {
        is AudioAttachment -> MessageType.AUDIO
        is FileAttachment -> MessageType.FILE
        is ImageAttachment -> MessageType.IMAGE
        is VideoAttachment -> MessageType.VIDEO
        is VoiceAttachment -> MessageType.VOICE
        is LocationAttachment -> MessageType.LOCATION
        is WebRTCAttachment -> MessageType.WEBRTC
    }
}

fun Attachment.Companion.deserialize(type: MessageType, string: String?): Attachment? {
    if (string == null) return null
    return kotlin.runCatching {
        when (type) {
            MessageType.TEXT -> null
            MessageType.IMAGE -> Json.decodeFromString<ImageAttachment>(string)
            MessageType.AUDIO -> Json.decodeFromString<AudioAttachment>(string)
            MessageType.VIDEO -> Json.decodeFromString<VideoAttachment>(string)
            MessageType.LOCATION -> Json.decodeFromString<LocationAttachment>(string)
            MessageType.VOICE -> Json.decodeFromString<VoiceAttachment>(string)
            MessageType.FILE -> Json.decodeFromString<FileAttachment>(string)
            MessageType.WEBRTC -> Json.decodeFromString<WebRTCAttachment>(string)
        }
    }.getOrNull()
}

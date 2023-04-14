package cn.tabidachi.electro.model.attachment

import kotlinx.serialization.Serializable

@Serializable
data class VoiceAttachment(
    override val contentType: String,
    override val uri: String?,
    override val path: String?,
    override val filename: String,
    override val size: Long,
    override val url: String?,
    override val bucket: String? = null,
    override val `object`: String? = null,
    override val displayName: String?,
    val duration: Long
) : DocumentAttachment

class VoiceAttachmentDSL {
    var contentType: String = ""
    var uri: String? = null
    var path: String? = null
    var filename: String = ""
    var size: Long = 0
    var url: String? = null
    var bucket: String? = null
    var `object`: String? = null
    var displayName: String? = null
    var duration: Long = 0

    fun build(): VoiceAttachment {
        return VoiceAttachment(
            contentType,
            uri,
            path,
            filename,
            size,
            url,
            bucket,
            `object`,
            displayName,
            duration
        )
    }
}

fun VoiceAttachment(block: VoiceAttachmentDSL.() -> Unit): VoiceAttachment {
    return VoiceAttachmentDSL().apply(block).build()
}
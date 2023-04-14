package cn.tabidachi.electro.model.attachment

import kotlinx.serialization.Serializable

@Serializable
data class AudioAttachment(
    override val contentType: String,
    override val uri: String?,
    override val path: String?,
    override val filename: String,
    override val size: Long,
    override val url: String?,
    override val displayName: String?,
    override val bucket: String? = null,
    override val `object`: String? = null,
    val duration: Long,
    val title: String? = null,
    val artist: String?,
    val coverPath: String?,
    val coverUrl: String?,
    val artwork: ByteArray? = null
) : DocumentAttachment

class AudioAttachmentDSL {
    var contentType: String = "*/*"
    var uri: String? = null
    var path: String? = null
    var filename: String = ""
    var size: Long = 0
    var url: String? = null
    var bucket: String? = null
    var `object`: String? = null
    var displayName: String? = null
    var duration: Long = 0
    var title: String? = null
    var artist: String? = null
    var coverPath: String? = null
    var coverUrl: String? = null
    var artwork: ByteArray? = null

    fun build(): AudioAttachment {
        return AudioAttachment(
            contentType,
            uri,
            path,
            filename,
            size,
            url,
            bucket,
            `object`,
            displayName,
            duration,
            title,
            artist,
            coverPath,
            coverUrl,
            artwork
        )
    }
}

fun AudioAttachment(block: AudioAttachmentDSL.() -> Unit): AudioAttachment {
    return AudioAttachmentDSL().apply(block).build()
}
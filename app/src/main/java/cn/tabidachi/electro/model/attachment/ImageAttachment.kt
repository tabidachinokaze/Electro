package cn.tabidachi.electro.model.attachment

import kotlinx.serialization.Serializable

@Serializable
data class ImageAttachment(
    override val contentType: String,
    override val uri: String?,
    override val path: String?,
    override val filename: String,
    override val size: Long,
    override val url: String?,
    override val bucket: String? = null,
    override val `object`: String? = null,
    override val displayName: String?,
    val width: Int,
    val height: Int,
    val thumbPath: String?,
    val thumbUrl: String?,
    val thumb: ByteArray? = null
) : DocumentAttachment

class ImageAttachmentDSL {
    var contentType: String = "*/*"
    var uri: String? = null
    var path: String? = null
    var filename: String = ""
    var size: Long = 0
    var url: String? = null
    var bucket: String? = null
    var `object`: String? = null
    var displayName: String? = null
    var width: Int = 0
    var height: Int = 0
    var thumbPath: String? = null
    var thumbUrl: String? = null
    var thumb: ByteArray? = null

    fun build(): ImageAttachment {
        return ImageAttachment(
            contentType,
            uri,
            path,
            filename,
            size,
            url,
            bucket,
            `object`,
            displayName,
            width,
            height,
            thumbPath,
            thumbUrl,
            thumb
        )
    }
}

fun ImageAttachment(block: ImageAttachmentDSL.() -> Unit): ImageAttachment {
    return ImageAttachmentDSL().apply(block).build()
}
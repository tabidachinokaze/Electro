package cn.tabidachi.electro.model.attachment

import kotlinx.serialization.Serializable

@Serializable
data class FileAttachment(
    override val contentType: String,
    override val uri: String?,
    override val path: String?,
    override val filename: String,
    override val size: Long,
    override val url: String?,
    override val displayName: String?,
    override val bucket: String? = null,
    override val `object`: String? = null,
) : DocumentAttachment

class FileAttachmentDSL {
    var contentType: String = ""
    var uri: String? = null
    var path: String? = null
    var filename: String = ""
    var size: Long = 0
    var url: String? = null
    var bucket: String? = null
    var `object`: String? = null
    var displayName: String? = null

    fun build(): FileAttachment {
        return FileAttachment(
            contentType,
            uri,
            path,
            filename,
            size,
            url,
            bucket,
            `object`,
            displayName
        )
    }
}

fun FileAttachment(block: FileAttachmentDSL.() -> Unit): FileAttachment {
    return FileAttachmentDSL().apply(block).build()
}
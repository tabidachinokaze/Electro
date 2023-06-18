package cn.tabidachi.electro.model.attachment

import kotlinx.serialization.Serializable

/**
 * @param latitude 纬度
 * @param longitude 经度
 * @param address 地址描述
 */
@Serializable
data class LocationAttachment(
    val latitude: Double,
    val longitude: Double,
    val title: String?,
    val city: String?,
    val address: String?,
    val snippet: String?,
) : Attachment, java.io.Serializable

class LocationAttachmentDSL {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var title: String? = null
    var city: String? = null
    var address: String? = null
    var snippet: String? = null

    fun build(): LocationAttachment {
        return LocationAttachment(latitude, longitude, title, city, address, snippet)
    }
}

fun LocationAttachment(block: LocationAttachmentDSL.() -> Unit): LocationAttachment {
    return LocationAttachmentDSL().apply(block).build()
}
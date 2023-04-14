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
    val address: String
) : Attachment

class LocationAttachmentDSL {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var address: String = ""

    fun build(): LocationAttachment {
        return LocationAttachment(latitude, longitude, address)
    }
}

fun LocationAttachment(block: LocationAttachmentDSL.() -> Unit): LocationAttachment {
    return LocationAttachmentDSL().apply(block).build()
}
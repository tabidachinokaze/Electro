package cn.tabidachi.electro.model

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketMessage(
    val header: Header,
    val body: ByteArray
) {
    @Serializable
    data class Header(
        val type: String,
        val timestamp: Long,
        val extras: String?
    )
}

fun WebSocketMessage(block: WebSocketMessageBuilder.() -> Unit): WebSocketMessage {
    return WebSocketMessageBuilder().apply(block).build()
}

class WebSocketMessageBuilder {
    var header = header {  }
    var body: ByteArray = byteArrayOf()

    fun build(): WebSocketMessage {
        return WebSocketMessage(header, body)
    }
}

fun header(block: HeaderBuilder.() -> Unit): WebSocketMessage.Header {
    return HeaderBuilder().apply(block).build()
}

class HeaderBuilder {
    var type: String = ""
    var timestamp: Long = System.currentTimeMillis()
    var extras: String? = null

    fun build(): WebSocketMessage.Header {
        return WebSocketMessage.Header(type, timestamp, extras)
    }
}

package cn.tabidachi.electro.model.attachment

import kotlinx.serialization.Serializable

@Serializable
data class WebRTCAttachment(
    val duration: Long,
    val state: State
) : Attachment {
    enum class State {
        // 成功建立连接
        SUCCESS,

        // 对方未接听
        MISSED,

        // 对方拒绝
        REJECTED,

        // 对方未接听前取消
        CANCELED
    }
}

class WebRTCAttachmentDSL {
    val duration: Long = 0
    lateinit var state: WebRTCAttachment.State

    fun build(): WebRTCAttachment {
        require(::state.isInitialized) {
            "state field must be set!"
        }
        return WebRTCAttachment(duration, state)
    }
}

fun WebRTCAttachment(block: WebRTCAttachmentDSL.() -> Unit): WebRTCAttachment {
    return WebRTCAttachmentDSL().apply(block).build()
}
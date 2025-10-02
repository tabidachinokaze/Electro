package moe.tabidachi.electro.ui.call

import moe.tabidachi.electro.data.database.entity.User
import moe.tabidachi.compose.mvi.UnidirectionalViewModel

interface CallContract {
    interface ViewModel : UnidirectionalViewModel<State, Event, Effect>

    data class State(
        val mic: Boolean = true,
        val camera: Boolean = true,
        val isSpeakerphone: Boolean = false,
        val isFrontCamera: Boolean = true,
        val target: Long? = null,
        val user: User? = null,
        val barsVisible: Boolean = true
    )

    sealed interface Event {
        data class InitCall(val offer: Long, val answer: Long, val action: String) : Event
        data object ToggleImmersive : Event
        data object OnCallEnd : Event
        data object FlipCamera : Event
        data class OnMicEnabled(val value: Boolean) : Event
        data class OnCameraEnabled(val value: Boolean) : Event
        data class IsSpeakerphone(val value: Boolean) : Event
    }

    sealed interface Effect {
        data object OnCallEnd : Effect
    }
}

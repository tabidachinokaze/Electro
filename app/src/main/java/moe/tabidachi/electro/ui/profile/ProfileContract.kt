package moe.tabidachi.electro.ui.profile

import moe.tabidachi.electro.data.database.entity.User
import moe.tabidachi.compose.mvi.BaseViewModel

interface ProfileContract {
    abstract class ViewModel(initialState: State) :
        BaseViewModel<State, Event, Effect>(initialState)

    data class State(
        val user: User = User(-1, "", "", ""),
        val username: String = "",
        val email: String = "",
        val password: String = "",
        val passwordVisible: Boolean = false
    )

    sealed interface Event {
        data object GetUser : Event
        data object NavigateUp : Event
        data object Done : Event
        data class OnUsernameChange(val value: String) : Event
        data class OnEmailChange(val value: String) : Event
        data class OnPasswordChange(val value: String) : Event
        data object OnVisibleChange : Event
    }

    sealed interface Effect {
        data object NavigateUp : Effect
    }
}

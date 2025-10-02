package moe.tabidachi.electro.ui.sessions

import moe.tabidachi.electro.data.database.entity.Dialog
import moe.tabidachi.electro.data.database.entity.User
import moe.tabidachi.compose.mvi.BaseViewModel

interface SessionsContract {
    abstract class ViewModel(initialState: State) :
        BaseViewModel<State, Event, Effect>(initialState)

    data class State(
        val isRefresh: Boolean = false,
        val user: User = User(0, "", "", ""),
        val dialogs: List<Dialog> = emptyList(),
        val sessions: List<User> = emptyList()
    )

    sealed interface Event {
        data object Pull : Event
        data object FindUser : Event
        data object NavigateToSettings : Event
        data object NavigateToContact : Event
        data object NavigateToGroupCreate : Event
        data object NavigateToChannelCreate : Event
        data object NavigateToAuth : Event
        data object NavigateUp : Event
        data object NavigateToDialogs : Event
        data object NavigateToSearch : Event
        data object OnRefresh : Event
        data class NavigateToPair(val uid: Long) : Event
        data class NavigateToGroup(val sid: Long) : Event
        data class NavigateToChannel(val sid: Long) : Event
        data class SwitchAccount(val uid: Long) : Event
    }

    sealed interface Effect
}

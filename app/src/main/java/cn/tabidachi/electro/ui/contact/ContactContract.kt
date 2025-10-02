package cn.tabidachi.electro.ui.contact

import cn.tabidachi.electro.data.database.entity.User
import moe.tabidachi.compose.mvi.BaseViewModel

interface ContactContract {
    abstract class ViewModel(initialState: State) :
        BaseViewModel<State, Event, Effect>(initialState)

    data class State(
        val filter: String = "",
        val isSearch: Boolean = false,
        val users: List<User> = emptyList(),
        val filterUser: List<User> = emptyList()
    )

    sealed interface Event {
        data object GetContact : Event
        data class ChangeSearchState(val value: Boolean) : Event
        data class OnQueryValueChange(val value: String) : Event
        data class NavigateToPair(val uid: Long) : Event
        data object NavigateUp : Event
        data object OnSearch : Event
    }

    sealed interface Effect
}

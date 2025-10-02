package cn.tabidachi.electro.ui.search

import cn.tabidachi.electro.data.database.entity.Dialog
import cn.tabidachi.electro.data.database.entity.SessionSearch
import cn.tabidachi.electro.model.UserQuery
import moe.tabidachi.compose.mvi.BaseViewModel

interface SearchContract {
    abstract class ViewModel(initialState: State) :
        BaseViewModel<State, Event, Effect>(initialState)

    data class State(
        val currentTab: SearchTab = SearchTab.DIALOG,
        val query: String = "",
        val dialogs: DialogSearchState = DialogSearchState.None,
        val groups: SessionSearchState = SessionSearchState.None,
        val channels: SessionSearchState = SessionSearchState.None,
        val users: UserSearchState = UserSearchState.None
    )

    sealed interface Event {
        data class QueryValueChange(val value: String) : Event
        data object OnSearch : Event
        data object NavigateUp : Event
        data class NavigateToPair(val target: Long) : Event
        data class NavigateToGroup(val sid: Long) : Event
        data class OnGroupJoinRequest(val sid: Long) : Event
    }

    sealed interface Effect
}

sealed class UserSearchState {
    object None : UserSearchState()
    data class Success(val value: List<UserQuery>) : UserSearchState()
    object Failure : UserSearchState()
}

sealed class DialogSearchState {
    object None : DialogSearchState()
    data class Success(val value: List<Dialog>) : DialogSearchState()
    object Failure : DialogSearchState()
}

sealed class SessionSearchState {
    object None : SessionSearchState()
    data class Success(val value: List<SessionSearch>) : SessionSearchState()
    object Failure : SessionSearchState()
}

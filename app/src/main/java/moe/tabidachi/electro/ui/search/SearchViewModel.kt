package moe.tabidachi.electro.ui.search

import android.app.Application
import androidx.lifecycle.viewModelScope
import moe.tabidachi.electro.R
import moe.tabidachi.electro.data.Repository
import moe.tabidachi.electro.data.database.entity.SessionType
import moe.tabidachi.electro.data.database.entity.SessionUserState
import moe.tabidachi.electro.ext.toast
import moe.tabidachi.electro.ui.search.SearchContract.Event
import moe.tabidachi.electro.ui.search.SearchContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val application: Application,
    private val repository: Repository,
) : SearchContract.ViewModel(State()) {
    override fun event(event: Event) = when (event) {
        is Event.NavigateToGroup -> Unit
        is Event.NavigateToPair -> Unit
        Event.NavigateUp -> Unit
        is Event.OnGroupJoinRequest -> onGroupJoinRequest(event.sid)
        Event.OnSearch -> onSearch()
        is Event.QueryValueChange -> queryValueChange(event.value)
    }

    init {
        viewModelScope.launch {
            repository.loadSearch()?.let {
                onSearch(it)
            }
        }
    }

    private fun onTabChange(searchTab: SearchTab) {
        updateState { it.copy(currentTab = searchTab) }
    }

    private fun queryValueChange(value: String) {
        updateState { it.copy(query = value) }
    }

    private fun onSearch(query: String = state.value.query) {
        viewModelScope.launch {
            launch {
                repository.queryUserFlow(query).let { userQueries ->
                    updateState { it.copy(users = UserSearchState.Success(userQueries)) }
                }
            }
            launch {
                repository.sessionSearch(query).collect { list ->
                    list.groupBy { it.type }.let { typeListMap ->
                        updateState {
                            it.copy(
                                groups = SessionSearchState.Success(
                                    typeListMap[SessionType.ROOM] ?: emptyList()
                                ),
                                channels = SessionSearchState.Success(
                                    typeListMap[SessionType.CHANNEL] ?: emptyList()
                                )
                            )
                        }
                    }
                }
            }
            launch {
                repository.dialogsFlow(query).collect { dialogs ->
                    updateState {
                        it.copy(dialogs = DialogSearchState.Success(dialogs))
                    }
                }
            }
            launch {
                repository.saveSearch(query)
            }
        }
    }

    private fun onGroupJoinRequest(sid: Long) {
        viewModelScope.launch {
            repository.onSessionJoinRequest(sid).onSuccess {
                when (it.data) {
                    SessionUserState.REQUEST -> {
                        application.toast(application.resources.getString(R.string.request_success))
                    }

                    SessionUserState.MEMBER -> {
                        application.toast(application.resources.getString(R.string.join_success))
                    }

                    else -> {}
                }
            }
        }
    }
}

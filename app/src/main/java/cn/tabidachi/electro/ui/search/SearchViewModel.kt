package cn.tabidachi.electro.ui.search

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.database.entity.Dialog
import cn.tabidachi.electro.data.database.entity.SessionSearch
import cn.tabidachi.electro.data.database.entity.SessionType
import cn.tabidachi.electro.data.database.entity.SessionUserState
import cn.tabidachi.electro.ext.toast
import cn.tabidachi.electro.model.UserQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val application: Application,
    private val repository: Repository,
) : ViewModel() {
    private val _viewState = MutableStateFlow(SearchViewState())
    val viewState = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.loadSearch()?.let {
                onSearch(it)
            }
        }
    }

    fun onTabChange(searchTab: SearchTab) {
        _viewState.update { it.copy(currentTab = searchTab) }
    }

    fun queryValueChange(value: String) {
        _viewState.update { it.copy(query = value) }
    }

    fun onSearch(query: String = _viewState.value.query) {
        viewModelScope.launch {
            launch {
                repository.queryUserFlow(query).let { userQueries ->
                    _viewState.update { it.copy(users = UserSearchState.Success(userQueries)) }
                }
            }
            launch {
                repository.sessionSearch(query).collect { list ->
                    list.groupBy { it.type }.let { typeListMap ->
                        _viewState.update {
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
                    _viewState.update {
                        it.copy(dialogs = DialogSearchState.Success(dialogs))
                    }
                }
            }
            launch {
                repository.saveSearch(query)
            }
        }
    }

    fun onGroupJoinRequest(sid: Long) {
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

data class SearchViewState(
    val currentTab: SearchTab = SearchTab.DIALOG,
    val query: String = "",
    val dialogs: DialogSearchState = DialogSearchState.None,
    val groups: SessionSearchState = SessionSearchState.None,
    val channels: SessionSearchState = SessionSearchState.None,
    val users: UserSearchState = UserSearchState.None
)

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
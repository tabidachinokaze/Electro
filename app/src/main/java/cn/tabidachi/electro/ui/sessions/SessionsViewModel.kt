package cn.tabidachi.electro.ui.sessions

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.PreferenceConstant
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.database.entity.Path
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.data.network.MessageType
import cn.tabidachi.electro.ext.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val ktor: Ktor,
    val repository: Repository,
    private val application: Application
) : ViewModel() {
    companion object {
        val TAG = SessionsViewModel::class.simpleName
    }

    private val _viewState = MutableStateFlow(SessionsViewState())
    val viewState = _viewState.asStateFlow()
    val dialogs = repository.dialogsFlow()
    val sessions = repository.sessionsFlow()

    init {
        viewModelScope.launch {
            repository.pullDialogs()
        }
        viewModelScope.launch {
            ktor.ws.onWebSocketMessage.collectLatest { message ->
                when (message.header.type) {
                    MessageType.Dialog.New.toString() -> {
                        String(message.body).toLong().let(::pullDialog)
                    }

                    MessageType.Message.New.toString() -> {
                        val pair =
                            String(message.body).let<String, Pair<Long, Long>>(Json::decodeFromString)
                        pullDialog(pair.first)
                    }
                }
            }
        }
    }

    fun findUser() {
        viewModelScope.launch {
            repository.getUser(ktor.uid).getOrNull()?.data?.let { user ->
                _viewState.update { it.copy(user = user) }
            }
        }
    }

    private fun pullDialog(sid: Long) {
        viewModelScope.launch {
            repository.pullDialog(sid)
        }
    }

    fun pull() {
        viewModelScope.launch {
            changeRefreshState(true)
            repository.pullDialogs()
            changeRefreshState(false)
        }
    }

    fun onRefresh() {
        if (_viewState.value.isRefresh) return
        pull()
    }

    private fun changeRefreshState(value: Boolean) {
        _viewState.update { it.copy(isRefresh = value) }
    }

    fun findResource(id: String, image: String?, result: (Path?) -> Unit) {
        if (image == null) return
        viewModelScope.launch {
            val path = repository.findResource(id)
            result(path)
            if (path?.path == null) {
                repository.download(
                    id,
                    image,
                    onSuccess = {
                        launch {
                            result(repository.findResource(id))
                        }
                    }
                )
            }
        }
    }

    fun switchAccount(uid: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.findAccount(uid)?.let { account ->
                val token = account.token ?: return@launch
                application.dataStore.edit {
                    it[PreferenceConstant.Key.TOKEN] = token
                    it[PreferenceConstant.Key.UID] = account.uid
                }
            }
        }
    }
}

data class SessionsViewState(
    val isRefresh: Boolean = false,
    val user: User = User(0, "", "", "")
)
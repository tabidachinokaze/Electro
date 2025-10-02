package cn.tabidachi.electro.ui.sessions

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.PreferenceConstant
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.database.entity.Path
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.data.network.MessageType
import cn.tabidachi.electro.ext.dataStore
import cn.tabidachi.electro.ui.sessions.SessionsContract.Event
import cn.tabidachi.electro.ui.sessions.SessionsContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val ktor: Ktor,
    val repository: Repository,
    private val application: Application
) : SessionsContract.ViewModel(State()) {
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
        repository.dialogsFlow().onEach { dialogs ->
            updateState { it.copy(dialogs = dialogs) }
        }.launchIn(viewModelScope)
        repository.sessionsFlow().onEach { sessions ->
            updateState { it.copy(sessions = sessions) }
        }.launchIn(viewModelScope)
    }

    override fun event(event: Event) = when (event) {
        Event.FindUser -> findUser()
        Event.Pull -> pull()
        Event.OnRefresh -> onRefresh()
        is Event.SwitchAccount -> switchAccount(event.uid)
        else -> Unit
    }

    private fun findUser() {
        viewModelScope.launch {
            repository.getUser(ktor.uid).getOrNull()?.data?.let { user ->
                updateState { it.copy(user = user) }
            }
        }
    }

    private fun pullDialog(sid: Long) {
        viewModelScope.launch {
            repository.pullDialog(sid)
        }
    }

    private fun pull() {
        viewModelScope.launch {
            changeRefreshState(true)
            repository.pullDialogs()
            changeRefreshState(false)
        }
    }

    private fun onRefresh() {
        if (state.value.isRefresh) return
        pull()
    }

    private fun changeRefreshState(value: Boolean) {
        updateState { it.copy(isRefresh = value) }
    }

    private fun findResource(id: String, image: String?, result: (Path?) -> Unit) {
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

    private fun switchAccount(uid: Long) {
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

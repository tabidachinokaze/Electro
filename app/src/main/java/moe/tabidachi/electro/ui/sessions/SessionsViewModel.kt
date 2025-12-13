package moe.tabidachi.electro.ui.sessions

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import moe.tabidachi.electro.Prefs
import moe.tabidachi.electro.data.ElectroRepository
import moe.tabidachi.electro.data.database.entity.Path
import moe.tabidachi.electro.data.network.ElectroWebSocket
import moe.tabidachi.electro.data.network.MessageType
import moe.tabidachi.electro.data.provider.UidProvider
import moe.tabidachi.electro.ext.dataStore
import moe.tabidachi.electro.ui.sessions.SessionsContract.Event
import moe.tabidachi.electro.ui.sessions.SessionsContract.State
import javax.inject.Inject

@HiltViewModel
class SessionsViewModel @Inject constructor(
    val electroRepository: ElectroRepository,
    private val application: Application,
    private val webSocket: ElectroWebSocket,
    private val uidProvider: UidProvider
) : SessionsContract.ViewModel(State()) {
    init {
        viewModelScope.launch {
            electroRepository.pullDialogs()
        }
        viewModelScope.launch {
            webSocket.onWebSocketMessage.collectLatest { message ->
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
        electroRepository.dialogsFlow().onEach { dialogs ->
            updateState { it.copy(dialogs = dialogs) }
        }.launchIn(viewModelScope)
        electroRepository.sessionsFlow().onEach { sessions ->
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
            electroRepository.getUser(uidProvider.getUid()).getOrNull()?.data?.let { user ->
                updateState { it.copy(user = user) }
            }
        }
    }

    private fun pullDialog(sid: Long) {
        viewModelScope.launch {
            electroRepository.pullDialog(sid)
        }
    }

    private fun pull() {
        viewModelScope.launch {
            changeRefreshState(true)
            electroRepository.pullDialogs()
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
            val path = electroRepository.findResource(id)
            result(path)
            if (path?.path == null) {
                electroRepository.download(
                    id,
                    image,
                    onSuccess = {
                        launch {
                            result(electroRepository.findResource(id))
                        }
                    }
                )
            }
        }
    }

    private fun switchAccount(uid: Long) {
        viewModelScope.launch {
            electroRepository.findAccount(uid)?.let { account ->
                val token = account.token ?: return@launch
                application.dataStore.edit {
                    it[Prefs.TOKEN] = token
                    it[Prefs.UID] = account.uid
                }
            }
        }
    }
}

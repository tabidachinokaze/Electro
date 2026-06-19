package moe.tabidachi.electro

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import moe.tabidachi.compose.mvi.BaseViewModel
import moe.tabidachi.electro.ElectroContract.State
import moe.tabidachi.electro.data.ElectroRepository
import moe.tabidachi.electro.data.network.ElectroWebSocket
import moe.tabidachi.electro.data.repository.SharedRepository
import moe.tabidachi.electro.data.service.FirebaseApi
import moe.tabidachi.electro.ext.dataStore
import moe.tabidachi.electro.ktx.TAG
import moe.tabidachi.electro.ui.auth.AuthRoute
import moe.tabidachi.electro.ui.sessions.SessionsRoute
import moe.tabidachi.electro.ui.splash.SplashRoute
import moe.tabidachi.electro.ui.theme.DarkLight
import moe.tabidachi.electro.ui.theme.Theme
import javax.inject.Inject

@HiltViewModel
class ElectroViewModel @Inject constructor(
    private val application: Application,
    private val electroRepository: ElectroRepository,
    private val sharedRepository: SharedRepository,
    private val webSocket: ElectroWebSocket,
    private val firebaseApi: FirebaseApi
) : ElectroContract.ViewModel(State()) {
    init {
        val account = application.dataStore.data.map {
            it[Prefs.TOKEN] to (it[Prefs.UID] ?: 0)
        }
        viewModelScope.launch {
            account.collectLatest { (token: String?, uid: Long) ->
                Log.d(TAG, "token = ${token}, uid = $uid")
                if (token.isNullOrBlank() || uid == 0L) {
                    updateState { it.copy(startDestination = AuthRoute) }
                    webSocket.pause()
                    webSocket.close()
                } else {
                    with(sharedRepository.state.value) {
                        Log.d(
                            TAG,
                            "currentUserId = ${currentUserId}, tokens[currentUserId] = ${tokens[currentUserId]}"
                        )
                        if (currentUserId != uid && tokens[currentUserId] != token) {
                            webSocket.close()
                            updateState { it.copy(startDestination = SplashRoute) }
                        }
                    }
                    sharedRepository.updateState {
                        it.copy(
                            currentUserId = uid,
                            tokens = it.tokens.toMutableMap().apply { this[uid] = token })
                    }

                    FirebaseMessaging.getInstance().token.addOnCompleteListener {
                        viewModelScope.launch {
                            runCatching {
                                firebaseApi.firebase(it.result)
                            }.onFailure {
                                Log.d(TAG, "getDeviceToken: onFailure")
                                it.printStackTrace()
                            }.onSuccess {
                                Log.d(TAG, "getDeviceToken: onSuccess")
                            }
                        }
                    }
                    webSocket.resume()
                    delay(200)
                    updateState { it.copy(startDestination = SessionsRoute) }
                }
            }
        }
        viewModelScope.launch {
            application.dataStore.data.map {
                it[Prefs.DARK_LIGHT]
            }.filterNotNull().collect { darkLight ->
                updateState { it.copy(darkLight = DarkLight.valueOf(darkLight)) }
            }
        }
        viewModelScope.launch {
            application.dataStore.data.map {
                it[Prefs.THEME]
            }.filterNotNull().filter { it.isNotBlank() }.collect { theme ->
                updateState { it.copy(theme = Theme.valueOf(theme)) }
            }
        }
    }

    val map = HashMap<String, DownloadState>()

    override fun event(event: ElectroContract.Event) {}

    fun download(id: String, url: String): DownloadState {
        val state = DownloadState()
        map[id] = state
        viewModelScope.launch {
            electroRepository.download(
                id,
                url,
                state.onSuccess,
                progressListener = state.progressListener
            )
        }
        return state
    }

    class DownloadState {
        val progress = MutableStateFlow(0f)
        val success = MutableStateFlow(false)
        val progressListener: suspend (bytesSentTotal: Long, contentLength: Long?) -> Unit =
            { bytesSentTotal: Long, contentLength: Long? ->
                contentLength?.let { progress.value = bytesSentTotal.toFloat() / it.toFloat() }
            }
        val onSuccess = {
            success.value = true
        }
    }
}

interface ElectroContract {
    abstract class ViewModel(initialState: State) :
        BaseViewModel<State, Event, Effect>(initialState)

    data class State(
        val token: String? = null,
        val startDestination: NavKey = SplashRoute,
        val darkLight: DarkLight = DarkLight.SYSTEM,
        val theme: Theme = Theme.Dynamic
    )

    sealed interface Event

    sealed interface Effect
}

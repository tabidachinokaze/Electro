package cn.tabidachi.electro

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.ext.dataStore
import cn.tabidachi.electro.ui.ElectroDestinations
import cn.tabidachi.electro.ui.auth.AuthViewModel
import cn.tabidachi.electro.ui.settings.Themes
import cn.tabidachi.electro.ui.theme.DayNight
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ElectroViewModel @Inject constructor(
    private val application: Application,
    val ktor: Ktor,
    private val repository: Repository,
) : ViewModel() {
    private val _viewState = MutableStateFlow(ElectroViewState())
    val viewState = _viewState.asStateFlow()
    var dayNight by mutableStateOf(DayNight.SYSTEM)
    var theme: Themes by mutableStateOf(Themes.Dynamic)

    init {
        val account = application.dataStore.data.map {
            it[PreferenceConstant.Key.TOKEN] to (it[PreferenceConstant.Key.UID] ?: 0)
        }
        viewModelScope.launch {
            account.collectLatest { (token: String?, uid: Long) ->
                if (token.isNullOrBlank() || uid == 0L) {
                    _viewState.update { it.copy(startDestination = ElectroDestinations.AUTH_ROUTE) }
                    ktor.ws.pause()
                    ktor.ws.close()
                } else {
                    if (ktor.token != token && ktor.uid != uid) {
                        ktor.ws.close()
                        _viewState.update { it.copy(startDestination = ElectroDestinations.SPLASH_ROUTE) }
                    }
                    ktor.token = token
                    ktor.uid = uid

                    FirebaseMessaging.getInstance().token.addOnCompleteListener {
                        viewModelScope.launch {
                            kotlin.runCatching {
                                ktor.client.post("/firebase") {
                                    setBody(it.result)
                                }
                            }.onFailure {
                                Log.d(AuthViewModel.TAG, "getDeviceToken: onFailure")
                                it.printStackTrace()
                            }.onSuccess {
                                Log.d(AuthViewModel.TAG, "getDeviceToken: onSuccess")
                            }
                        }
                    }
                    ktor.ws.resume()
                    delay(200)
                    _viewState.update { it.copy(startDestination = ElectroDestinations.DIALOGS_ROUTE) }
                }
            }
        }
        viewModelScope.launch {
            application.dataStore.data.map {
                it[PreferenceConstant.Key.DAY_NIGHT]
            }.filterNotNull().collect {
                dayNight = DayNight.valueOf(it)
            }
        }
        viewModelScope.launch {
            application.dataStore.data.map {
                it[PreferenceConstant.Key.THEME]
            }.filterNotNull().collect {
                theme = Themes.valueOf(it)
            }
        }
    }

    val map = HashMap<String, DownloadState>()

    fun download(id: String, url: String): DownloadState {
        val state = DownloadState()
        map[id] = state
        viewModelScope.launch {
            repository.download(id, url, state.onSuccess, progressListener = state.progressListener)
        }
        return state
    }

    class DownloadState {
        val progress = MutableStateFlow(0f)
        val success = MutableStateFlow(false)
        val progressListener: suspend (bytesSentTotal: Long, contentLength: Long) -> Unit =
            { bytesSentTotal: Long, contentLength: Long ->
                progress.value = bytesSentTotal.toFloat() / contentLength.toFloat()
            }
        val onSuccess = {
            success.value = true
        }
    }
}

data class ElectroViewState(
    val token: String? = null,
    val isReady: Boolean = false,
    val startDestination: String = ElectroDestinations.SPLASH_ROUTE
)
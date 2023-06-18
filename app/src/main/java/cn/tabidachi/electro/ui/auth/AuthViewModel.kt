package cn.tabidachi.electro.ui.auth

import android.app.Application
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.PreferenceConstant
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.database.ElectroDatabase
import cn.tabidachi.electro.data.database.entity.Account
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.ext.dataStore
import cn.tabidachi.electro.ext.isEmail
import cn.tabidachi.electro.ext.isValidPassword
import cn.tabidachi.electro.model.request.CaptchaRequest
import cn.tabidachi.electro.model.request.LoginRequest
import cn.tabidachi.electro.model.request.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val application: Application,
    private val ktor: Ktor,
    private val database: ElectroDatabase
) : ViewModel() {
    private val _viewState = MutableStateFlow(AuthViewState())
    val viewState = _viewState.asStateFlow()
    val hostState: SnackbarHostState = SnackbarHostState()
    val currentState
        get() = _viewState.value

    fun onCodeRequest() {
        val email = _viewState.value.request.first
        when {
            !email.isEmail() -> {
                _viewState.update { it.copy(errorState = it.errorState.copy(email = true)) }
                return
            }
        }
        viewModelScope.launch {
            ktor.checkUserExist(email).onSuccess {
                if (it.status == HttpStatusCode.OK.value) {
                    if (it.data != null) {
                        hostState.showSnackbar(application.resources.getString(R.string.user_registered), withDismissAction = true)
                        return@launch
                    }
                }
                val captchaRequest = CaptchaRequest(
                    email,
                    CaptchaRequest.Method.EMAIL,
                    CaptchaRequest.Type.REGISTER
                )
                ktor.captcha(captchaRequest).onSuccess {
                    _viewState.update { it.copy(buttonEnabled = false) }
                    launch {
                        val countDown = 60
                        repeat(countDown) { index ->
                            _viewState.update { it.copy(buttonText = "${countDown - index}s") }
                            delay(1000)
                        }
                        _viewState.update { it.copy(buttonEnabled = true, buttonText = null) }
                    }
                    hostState.showSnackbar(it.message, withDismissAction = true)
                    _viewState.update { it.copy(isProcessing = false) }
                }.onFailure { t ->
                    hostState.showSnackbar(t.message.toString(), withDismissAction = true)
                    _viewState.update { it.copy(isProcessing = false) }
                    t.printStackTrace()
                }
            }.onFailure {
                hostState.showSnackbar(it.message.toString(), withDismissAction = true)
                return@launch
            }

        }
    }

    fun onPasswordVisibleChange(value: Boolean) {
        _viewState.update { it.copy(passwordVisible = value) }
    }

    fun errorStateChange(state: ErrorState) {
        _viewState.update { it.copy(errorState = state) }
    }

    fun onRequestChange(triple: Triple<String, String, String>) {
        _viewState.update { it.copy(request = triple) }
    }

    fun changeAuthMethod() {
        _viewState.update { it.copy(method = it.method.toggle()) }
    }

    fun auth() {
        if (currentState.isProcessing) return
        viewModelScope.launch {
            with(_viewState.value.request) {
                when {
                    !first.isEmail() -> {
                        _viewState.update { it.copy(errorState = it.errorState.copy(email = true)) }
                        return@launch
                    }

                    !second.isValidPassword() -> {
                        _viewState.update { it.copy(errorState = it.errorState.copy(password = true)) }
                        return@launch
                    }

                    _viewState.value.method == AuthMethod.REGISTER -> {
                        changeProcessing(true)
                        ktor.checkUserExist(_viewState.value.request.first).also {
                            changeProcessing(false)
                        }.onSuccess {
                            if (it.status == HttpStatusCode.OK.value) {
                                if (it.data != null) {
                                    hostState.showSnackbar(application.resources.getString(R.string.user_registered), withDismissAction = true)
                                    return@launch
                                }
                            }
                        }.onFailure {
                            hostState.showSnackbar(it.message.toString(), withDismissAction = true)
                            return@launch
                        }
                        if (third.isBlank()) {
                            _viewState.update { it.copy(errorState = it.errorState.copy(code = true)) }
                            return@launch
                        } else {

                        }
                    }

                    _viewState.value.method == AuthMethod.LOGIN -> {
                        changeProcessing(true)
                        ktor.checkUserExist(_viewState.value.request.first).also {
                            changeProcessing(false)
                        }.onSuccess {
                            if (it.status == HttpStatusCode.NotFound.value) {
                                hostState.showSnackbar(application.resources.getString(R.string.user_unregistered), withDismissAction = true)
                                return@launch
                            }
                        }.onFailure {
                            hostState.showSnackbar(it.message.toString(), withDismissAction = true)
                            return@launch
                        }
                    }

                    else -> Unit
                }
            }
            changeProcessing(true)
            when (_viewState.value.method) {
                AuthMethod.LOGIN -> {
                    _viewState.value.request.let {
                        LoginRequest(it.first, it.second)
                    }.let {
                        ktor.login(it).onSuccess {
                            Log.d(TAG, "auth: $it")
                        }.onFailure {
                            Log.e(TAG, "auth: login", it)
                        }
                    }
                }

                AuthMethod.REGISTER -> {
                    _viewState.value.request.let {
                        RegisterRequest(it.first, it.second, it.third)
                    }.let {
                        ktor.register(it).onSuccess {
                            Log.d(TAG, "auth: $it")
                        }.onFailure {
                            Log.e(TAG, "auth: login", it)
                        }
                    }
                }
            }.also {
                changeProcessing(false)
            }.onSuccess { (status, message, data) ->
                hostState.showSnackbar(message, withDismissAction = true)
                when (data) {
                    null -> {
                        _viewState.update { it.copy(isAuthSuccess = false) }
                    }

                    else -> {
                        application.dataStore.edit {
                            it[PreferenceConstant.Key.TOKEN] = data.token
                            it[PreferenceConstant.Key.UID] = data.uid
                        }
                        withContext(Dispatchers.IO) {
                            database.accountDao().upsert(Account(data.uid, data.token))
                        }
                        _viewState.update { it.copy(isAuthSuccess = true) }
                    }
                }
            }.onFailure {
                hostState.showSnackbar(it.message.toString(), withDismissAction = true)
                it.printStackTrace()
            }
        }
    }

    fun changeProcessing(isProcessing: Boolean) {
        _viewState.update { it.copy(isProcessing = isProcessing) }
    }

    private fun update(block: (AuthViewState) -> AuthViewState) {
        _viewState.update(block)
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun languageMenuExpandedChange(value: Boolean) {
        _viewState.update { it.copy(isLanguageMenuExpanded = value) }
    }

    companion object {
        val TAG = AuthViewModel::class.simpleName
    }
}

data class AuthViewState(
    val token: String? = null,
    val isProcessing: Boolean = false,
    val method: AuthMethod = AuthMethod.LOGIN,
    val request: Triple<String, String, String> = Triple("", "", ""),
    val errorState: ErrorState = ErrorState(),
    val passwordVisible: Boolean = false,
    val buttonText: String? = null,
    val buttonEnabled: Boolean = true,
    val isAuthSuccess: Boolean = false,
    val isLanguageMenuExpanded: Boolean = false
)

enum class AuthMethod(@StringRes val id: Int) {
    LOGIN(R.string.login), REGISTER(R.string.register);

    fun toggle() = when (this) {
        LOGIN -> REGISTER
        REGISTER -> LOGIN
    }
}
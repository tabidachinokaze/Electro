package moe.tabidachi.electro.ui.auth

import android.content.Context
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tabidachi.compose.mvi.BaseViewModel
import moe.tabidachi.electro.Prefs
import moe.tabidachi.electro.R
import moe.tabidachi.electro.data.database.ElectroDatabase
import moe.tabidachi.electro.data.database.entity.Account
import moe.tabidachi.electro.data.service.AuthApi
import moe.tabidachi.electro.data.service.UserApi
import moe.tabidachi.electro.ext.dataStore
import moe.tabidachi.electro.ext.isEmail
import moe.tabidachi.electro.ext.isValidPassword
import moe.tabidachi.electro.ktx.TAG
import moe.tabidachi.electro.model.request.CaptchaRequest
import moe.tabidachi.electro.model.request.LoginRequest
import moe.tabidachi.electro.model.request.RegisterRequest
import moe.tabidachi.electro.ui.auth.AuthContract.Effect
import moe.tabidachi.electro.ui.auth.AuthContract.Event
import moe.tabidachi.electro.ui.auth.AuthContract.State
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val database: ElectroDatabase,
    private val authApi: AuthApi,
    private val userApi: UserApi
) : BaseViewModel<State, Event, Effect>(State()), AuthContract.ViewModel {
    override fun event(event: Event) = when (event) {
        Event.Auth -> auth()
        Event.ChangeAuthMethod -> changeAuthMethod()
        is Event.ChangeProcessing -> changeProcessing(event.value)
        is Event.ErrorStateChange -> errorStateChange(event.value)
        is Event.LanguageMenuExpandedChange -> languageMenuExpandedChange(event.value)
        Event.OnCodeRequest -> onCodeRequest()
        is Event.OnPasswordVisibleChange -> onPasswordVisibleChange(event.value)
        is Event.OnRequestChange -> onRequestChange(event.value)
        Event.NavigateToServer -> Unit
        Event.NavigateToLocaleSettings -> Unit
    }

    private fun onCodeRequest() {
        val email = state.value.request.first
        when {
            !email.text.isEmail() -> {
                updateState { it.copy(errorState = it.errorState.copy(email = true)) }
                return
            }
        }
        viewModelScope.launch {
            runCatching { userApi.checkUserExist(email.text) }.onSuccess {
                if (it.status == HttpStatusCode.OK.value) {
                    if (it.data != null) {
                        emitEffect(Effect.Toast(context.resources.getString(R.string.user_registered)))
                        return@launch
                    }
                }
                val captchaRequest = CaptchaRequest(
                    email.text,
                    CaptchaRequest.Method.EMAIL,
                    CaptchaRequest.Type.REGISTER
                )
                runCatching {
                    authApi.captcha(captchaRequest)
                }.onSuccess {
                    updateState { it.copy(buttonEnabled = false) }
                    launch {
                        val countDown = 60
                        repeat(countDown) { index ->
                            updateState { it.copy(buttonText = "${countDown - index}s") }
                            delay(1000)
                        }
                        updateState { it.copy(buttonEnabled = true, buttonText = null) }
                    }
                    emitEffect(Effect.Toast(it.message))
                    updateState { it.copy(isProcessing = false) }
                }.onFailure { t ->
                    emitEffect(Effect.Toast(t.message.toString()))
                    updateState { it.copy(isProcessing = false) }
                    t.printStackTrace()
                }
            }.onFailure {
                emitEffect(Effect.Toast(it.message.toString()))
                return@launch
            }
        }
    }

    private fun onPasswordVisibleChange(value: Boolean) {
        updateState { it.copy(passwordVisible = value) }
    }

    private fun errorStateChange(state: ErrorState) {
        updateState { it.copy(errorState = state) }
    }

    private fun onRequestChange(triple: Triple<TextFieldValue, TextFieldValue, TextFieldValue>) {
        updateState { it.copy(request = triple) }
    }

    private fun changeAuthMethod() {
        updateState { it.copy(method = it.method.toggle()) }
    }

    private fun auth() {
        if (state.value.isProcessing) return
        viewModelScope.launch {
            with(state.value.request) {
                when {
                    !first.text.isEmail() -> {
                        updateState { it.copy(errorState = it.errorState.copy(email = true)) }
                        return@launch
                    }

                    !second.text.isValidPassword() -> {
                        updateState { it.copy(errorState = it.errorState.copy(password = true)) }
                        return@launch
                    }

                    state.value.method == AuthMethod.REGISTER -> {
                        changeProcessing(true)
                        runCatching { userApi.checkUserExist(state.value.request.first.text) }.also {
                            changeProcessing(false)
                        }.onSuccess {
                            if (it.status == HttpStatusCode.OK.value) {
                                if (it.data != null) {
                                    emitEffect(Effect.Toast(context.resources.getString(R.string.user_registered)))
                                    return@launch
                                }
                            }
                        }.onFailure {
                            emitEffect(Effect.Toast(it.message.toString()))
                            return@launch
                        }
                        if (third.text.isBlank()) {
                            updateState { it.copy(errorState = it.errorState.copy(code = true)) }
                            return@launch
                        } else {
                        }
                    }

                    state.value.method == AuthMethod.LOGIN -> {
                        changeProcessing(true)
                        runCatching { userApi.checkUserExist(state.value.request.first.text) }.also {
                            changeProcessing(false)
                        }.onSuccess {
                            if (it.status == HttpStatusCode.NotFound.value) {
                                emitEffect(Effect.Toast(context.resources.getString(R.string.user_unregistered)))
                                return@launch
                            }
                        }.onFailure {
                            emitEffect(Effect.Toast(it.message.toString()))
                            return@launch
                        }
                    }

                    else -> Unit
                }
            }
            changeProcessing(true)
            when (state.value.method) {
                AuthMethod.LOGIN -> {
                    state.value.request.let {
                        LoginRequest(it.first.text, it.second.text)
                    }.let {
                        runCatching { authApi.login(it) }.onSuccess {
                            Log.d(TAG, "auth: $it")
                        }.onFailure {
                            Log.e(TAG, "auth: login", it)
                        }
                    }
                }

                AuthMethod.REGISTER -> {
                    state.value.request.let {
                        RegisterRequest(it.first.text, it.second.text, it.third.text)
                    }.let {
                        runCatching { authApi.register(it) }.onSuccess {
                            Log.d(TAG, "auth: $it")
                        }.onFailure {
                            Log.e(TAG, "auth: login", it)
                        }
                    }
                }
            }.also {
                changeProcessing(false)
            }.onSuccess { (status, message, data) ->
                emitEffect(Effect.Toast(message))
                when (data) {
                    null -> {
                        updateState { it.copy(isAuthSuccess = false) }
                    }

                    else -> {
                        context.dataStore.edit {
                            it[Prefs.TOKEN] = data.token
                            it[Prefs.UID] = data.uid
                        }
                        withContext(Dispatchers.IO) {
                            database.accountDao().upsert(Account(data.uid, data.token))
                        }
                        updateState { it.copy(isAuthSuccess = true) }
                    }
                }
            }.onFailure {
                emitEffect(Effect.Toast(it.message.toString()))
                it.printStackTrace()
            }
        }
    }

    private fun changeProcessing(isProcessing: Boolean) {
        updateState { it.copy(isProcessing = isProcessing) }
    }

    private fun languageMenuExpandedChange(value: Boolean) {
        updateState { it.copy(isLanguageMenuExpanded = value) }
    }
}

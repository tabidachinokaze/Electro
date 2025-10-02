package moe.tabidachi.electro.ui.auth

import androidx.annotation.StringRes
import androidx.compose.ui.text.input.TextFieldValue
import moe.tabidachi.electro.R
import moe.tabidachi.compose.mvi.UnidirectionalViewModel

interface AuthContract {
    interface ViewModel : UnidirectionalViewModel<State, Event, Effect>

    data class State(
        val token: String? = null,
        val isProcessing: Boolean = false,
        val method: AuthMethod = AuthMethod.LOGIN,
        val request: Triple<TextFieldValue, TextFieldValue, TextFieldValue> = Triple(
            TextFieldValue(),
            TextFieldValue(),
            TextFieldValue()
        ),
        val errorState: ErrorState = ErrorState(),
        val passwordVisible: Boolean = false,
        val buttonText: String? = null,
        val buttonEnabled: Boolean = true,
        val isAuthSuccess: Boolean = false,
        val isLanguageMenuExpanded: Boolean = false
    )

    sealed interface Event {
        data object OnCodeRequest : Event
        data class OnPasswordVisibleChange(val value: Boolean) : Event
        data class ErrorStateChange(val value: ErrorState) : Event
        data class OnRequestChange(
            val value: Triple<TextFieldValue, TextFieldValue, TextFieldValue>
        ) : Event

        data object ChangeAuthMethod : Event
        data object Auth : Event
        data class ChangeProcessing(val value: Boolean) : Event
        data class LanguageMenuExpandedChange(val value: Boolean) : Event
        data object NavigateToServer : Event
        data object NavigateToLocaleSettings : Event
    }

    sealed interface Effect {
        data class Toast(val value: String) : Effect
    }
}

enum class AuthMethod(@StringRes val id: Int) {
    LOGIN(R.string.login), REGISTER(R.string.register);

    fun toggle() = when (this) {
        LOGIN -> REGISTER
        REGISTER -> LOGIN
    }
}

data class ErrorState(
    val email: Boolean = false,
    val password: Boolean = false,
    val code: Boolean = false
)

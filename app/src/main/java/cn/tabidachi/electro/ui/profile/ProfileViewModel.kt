package cn.tabidachi.electro.ui.profile

import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.model.request.UserUpdateRequest
import cn.tabidachi.electro.ui.profile.ProfileContract.Effect
import cn.tabidachi.electro.ui.profile.ProfileContract.Event
import cn.tabidachi.electro.ui.profile.ProfileContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: Repository,
    private val ktor: Ktor
) : ProfileContract.ViewModel(State()) {
    override fun event(event: Event) {
        when (event) {
            Event.Done -> done()
            Event.GetUser -> getUser()
            Event.NavigateUp -> Unit
            is Event.OnEmailChange -> onEmailChange(event.value)
            is Event.OnPasswordChange -> onPasswordChange(event.value)
            is Event.OnUsernameChange -> onUsernameChange(event.value)
            Event.OnVisibleChange -> onVisibleChange()
        }
    }

    fun getUser() {
        viewModelScope.launch {
            repository.getUser(ktor.uid).onSuccess {
                it.data?.let { user ->
                    updateState {
                        it.copy(
                            user = user,
                            username = user.username,
                            email = user.email
                        )
                    }
                }
            }
        }
    }

    fun onUsernameChange(value: String) {
        updateState { it.copy(username = value) }
    }

    fun onEmailChange(value: String) {
        updateState { it.copy(email = value) }
    }

    fun onPasswordChange(value: String) {
        updateState { it.copy(password = value) }
    }

    fun onVisibleChange() {
        updateState { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun done() {
        viewModelScope.launch {
            state.value.apply {
                if (username == user.username && email == user.email && password.isBlank()) {
                    emitEffect(Effect.NavigateUp)
                    return@apply
                }
                val username = if (username == user.username) null else username
                val password = password.ifBlank { null }
                val email = if (email == user.email) null else email
                val result =
                    repository.updateUserInfo(UserUpdateRequest(username, password, email, null))
                if (result) {
                    emitEffect(Effect.NavigateUp)
                }
            }
        }
    }
}

package moe.tabidachi.electro.ui.profile

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import moe.tabidachi.electro.data.ElectroRepository
import moe.tabidachi.electro.data.provider.UidProvider
import moe.tabidachi.electro.data.service.UserApi
import moe.tabidachi.electro.model.request.UserUpdateRequest
import moe.tabidachi.electro.ui.profile.ProfileContract.Effect
import moe.tabidachi.electro.ui.profile.ProfileContract.Event
import moe.tabidachi.electro.ui.profile.ProfileContract.State
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val electroRepository: ElectroRepository,
    private val userApi: UserApi,
    private val uidProvider: UidProvider
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
            electroRepository.getUser(uidProvider.getUid()).onSuccess {
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
                val result = runCatching {
                    userApi.userUpdate(UserUpdateRequest(username, password, email, null))
                }.getOrNull()?.data != null
                if (result) {
                    emitEffect(Effect.NavigateUp)
                }
            }
        }
    }
}

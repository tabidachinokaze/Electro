package cn.tabidachi.electro.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.model.request.UserUpdateRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: Repository,
    private val ktor: Ktor
) : ViewModel() {
    private val _viewState = MutableStateFlow(ProfileViewState())
    val viewState = _viewState.asStateFlow()

    fun getUser() {
        viewModelScope.launch {
            repository.getUser(ktor.uid).onSuccess {
                it.data?.let { user ->
                    _viewState.update {
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
        _viewState.update { it.copy(username = value) }
    }

    fun onEmailChange(value: String) {
        _viewState.update { it.copy(email = value) }
    }

    fun onPasswordChange(value: String) {
        _viewState.update { it.copy(password = value) }
    }

    fun onVisibleChange() {
        _viewState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun done(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _viewState.value.apply {
                if (username == user.username && email == user.email && password.isBlank()) {
                    onSuccess()
                    return@apply
                }
                val username = if (username == user.username) null else username
                val password = password.ifBlank { null }
                val email = if (email == user.email) null else email
                val result =
                    repository.updateUserInfo(UserUpdateRequest(username, password, email, null))
                if (result) {
                    onSuccess()
                }
            }
        }
    }
}

data class ProfileViewState(
    val user: User = User(-1, "", "", ""),
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false
)
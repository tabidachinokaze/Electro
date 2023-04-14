package cn.tabidachi.electro.ui.contact

import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.model.Messenger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    override val repository: Repository,
    override val ktor: Ktor
) : Messenger(repository, ktor) {
    private val _viewState = MutableStateFlow(ContactViewState())
    val viewState = _viewState.asStateFlow()

    fun getContact() {
        viewModelScope.launch {
            repository.contact().onSuccess {
                it.data?.mapNotNull {
                    repository.getUser(it).getOrNull()?.data
                }?.let { users ->
                    _viewState.update { it.copy(users = users) }
                }
            }
        }
    }

    fun onSearch() {

    }

    fun changeSearchState(isSearch: Boolean) {
        _viewState.update { it.copy(isSearch = isSearch, filter = "") }
    }

    fun onQueryValueChange(value: String) {
        _viewState.update { it.copy(filter = value) }
    }
}

data class ContactViewState(
    val filter: String = "",
    val isSearch: Boolean = false,
    val users: List<User> = emptyList(),
    val filterUser: List<User> = emptyList()
)
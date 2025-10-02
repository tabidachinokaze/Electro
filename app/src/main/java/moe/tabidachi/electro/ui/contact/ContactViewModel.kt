package moe.tabidachi.electro.ui.contact

import androidx.lifecycle.viewModelScope
import moe.tabidachi.electro.data.Repository
import moe.tabidachi.electro.data.network.Ktor
import moe.tabidachi.electro.model.BaseMessenger
import moe.tabidachi.electro.model.Messenger
import moe.tabidachi.electro.ui.contact.ContactContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val repository: Repository,
    private val ktor: Ktor,
) : ContactContract.ViewModel(State()) {
    val messenger: Messenger = BaseMessenger(
        repository = repository,
        ktor = ktor,
        scope = viewModelScope,
        sid = 0,
    )

    override fun event(event: ContactContract.Event) {
        when (event) {
            is ContactContract.Event.ChangeSearchState -> changeSearchState(event.value)
            ContactContract.Event.GetContact -> getContact()
            is ContactContract.Event.NavigateToPair -> Unit
            ContactContract.Event.NavigateUp -> Unit
            is ContactContract.Event.OnQueryValueChange -> onQueryValueChange(event.value)
            ContactContract.Event.OnSearch -> onSearch()
        }
    }

    private fun getContact() {
        viewModelScope.launch {
            repository.contact().onSuccess {
                it.data?.mapNotNull {
                    repository.getUser(it).getOrNull()?.data
                }?.let { users ->
                    updateState { it.copy(users = users) }
                }
            }
        }
    }

    private fun onSearch() {
    }

    private fun changeSearchState(isSearch: Boolean) {
        updateState { it.copy(isSearch = isSearch, filter = "") }
    }

    private fun onQueryValueChange(value: String) {
        updateState { it.copy(filter = value) }
    }
}

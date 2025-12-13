package moe.tabidachi.electro.ui.contact

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import moe.tabidachi.electro.data.ElectroRepository
import moe.tabidachi.electro.data.network.ElectroWebSocket
import moe.tabidachi.electro.data.provider.UidProvider
import moe.tabidachi.electro.data.service.ContactApi
import moe.tabidachi.electro.model.BaseMessenger
import moe.tabidachi.electro.model.Messenger
import moe.tabidachi.electro.ui.contact.ContactContract.State
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val electroRepository: ElectroRepository,
    private val contactApi: ContactApi,
    webSocket: ElectroWebSocket,
    private val uidProvider: UidProvider,
) : ContactContract.ViewModel(State()) {
    val messenger: Messenger = BaseMessenger(
        electroRepository = electroRepository,
        scope = viewModelScope,
        sid = 0,
        ws = webSocket,
        uidProvider = uidProvider,
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
            runCatching { contactApi.contact() }.onSuccess {
                it.data?.mapNotNull {
                    electroRepository.getUser(it).getOrNull()?.data
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

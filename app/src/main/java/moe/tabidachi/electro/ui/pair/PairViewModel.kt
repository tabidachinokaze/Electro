package moe.tabidachi.electro.ui.pair

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import moe.tabidachi.electro.CallActivity
import moe.tabidachi.electro.OFFER_ACTION
import moe.tabidachi.electro.R
import moe.tabidachi.electro.data.ElectroRepository
import moe.tabidachi.electro.data.database.entity.RelationState
import moe.tabidachi.electro.data.network.ElectroWebSocket
import moe.tabidachi.electro.data.provider.UidProvider
import moe.tabidachi.electro.data.service.ContactApi
import moe.tabidachi.electro.data.service.RelationApi
import moe.tabidachi.electro.data.service.UserApi
import moe.tabidachi.electro.ktx.TAG
import moe.tabidachi.electro.model.BaseMessenger
import moe.tabidachi.electro.model.Messenger
import moe.tabidachi.electro.ui.common.MessageManager
import moe.tabidachi.electro.ui.common.MessageManagerImpl
import moe.tabidachi.electro.ui.pair.PairContract.Event
import moe.tabidachi.electro.ui.pair.PairContract.State
import javax.inject.Inject

@HiltViewModel
class PairViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val electroRepository: ElectroRepository,
    private val relationApi: RelationApi,
    private val contactApi: ContactApi,
    private val userApi: UserApi,
    private val uidProvider: UidProvider,
    private val webSocket: ElectroWebSocket,
    savedStateHandle: SavedStateHandle
) : PairContract.ViewModel(
    initialState = State(
        uid = uidProvider.getUid(),
        target = savedStateHandle.toRoute<PairRoute>().target
    )
) {
    val messenger: Messenger = object : BaseMessenger(
        electroRepository = electroRepository,
        scope = viewModelScope,
        uidProvider = uidProvider,
        ws = webSocket,
        sid = null
    ) {
        override suspend fun getSessionId(): Long? {
            Log.d(TAG, "getSessionId: target = ${state.value.target}")
            Log.d(TAG, "super.getSessionId() = ${super.getSessionId()}")
            return super.getSessionId()
                ?: electroRepository.createSessionByPairUser(state.value.target)
                    .getOrNull()?.data.also { sid ->
                        updateState { it.copy(sid = sid) }
                        sid?.let(::setSessionId)
                    }
        }
    }

    val messageManager: MessageManager = MessageManagerImpl(
        context = context,
        electroRepository = electroRepository,
        scope = viewModelScope,
        uidProvider = uidProvider
    )

    init {
        event(Event.Initialize)
    }

    override fun event(event: Event) = when (event) {
        Event.Initialize -> handleOneTimeEvent(event) { initialize(state.value.target) }
        Event.Call -> call()
        Event.NavigateUp -> Unit
        is Event.OnMenuClick -> onMenuClick(event.value)
    }

    private fun initialize(target: Long) {
        findSessionId(target)
        getUser(target)
        getRelationState(target)
    }

    private fun getRelationState(target: Long) {
        viewModelScope.launch {
            runCatching { relationApi.getRelationState(target) }.onSuccess {
                val filter = state.value.menu.filter {
                    it !in listOf(
                        PairMenuItem.CONTACT_ADD,
                        PairMenuItem.CONTACT_DELETE,
                        PairMenuItem.USER_BLOCK,
                        PairMenuItem.USER_UNBLOCK,
                    )
                }
                when (it.data) {
                    RelationState.NONE, null -> {
                        mutableListOf(
                            PairMenuItem.CONTACT_ADD,
                            PairMenuItem.USER_BLOCK,
                        )
                    }

                    RelationState.CONTACT -> {
                        mutableListOf(
                            PairMenuItem.CONTACT_DELETE,
                            PairMenuItem.USER_BLOCK,
                        )
                    }

                    RelationState.BLOCK -> {
                        mutableListOf(
                            PairMenuItem.USER_UNBLOCK,
                        )
                    }
                }.let { menu ->
                    updateState {
                        it.copy(
                            menu = menu.apply {
                                addAll(filter)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun findSessionId(target: Long) {
        viewModelScope.launch {
            electroRepository.findSessionByPairUser(target)?.let { sid ->
                updateState { it.copy(sid = sid) }
                messenger.setSessionId(sid)
            }
        }
    }

    private fun getUser(target: Long) {
        viewModelScope.launch {
            electroRepository.getUser(target).onSuccess { (_, _, user) ->
                user?.let {
                    println(it)
                    updateState { it.copy(targetUser = user) }
                }
            }
        }
        viewModelScope.launch {
            messenger.listen(target)
        }
    }

    private fun onMenuClick(menu: PairMenuItem) {
        viewModelScope.launch {
            val target = state.value.target
            runCatching {
                when (menu) {
                    PairMenuItem.CONTACT_ADD -> {
                        contactApi.addContact(target)
                    }

                    PairMenuItem.CONTACT_DELETE -> {
                        contactApi.deleteContact(target)
                    }

                    PairMenuItem.USER_BLOCK -> {
                        userApi.blockUser(target)
                    }

                    PairMenuItem.USER_UNBLOCK -> {
                        userApi.unblockUser(target)
                    }
                }
            }.onSuccess {
                getRelationState(target)
            }
        }
    }

    override fun onCleared() {
        messenger.readMessage()
        viewModelScope.launch {
            messenger.unlisten(state.value.target)
            viewModelScope.cancel()
        }
        super.onCleared()
    }

    private fun call() {
        val intent = Intent(context, CallActivity::class.java).apply {
            action = OFFER_ACTION
            putExtra("src", "${uidProvider.getUid()}")
            putExtra("dst", "${state.value.target}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

enum class PairMenuItem(
    @StringRes val text: Int,
    val icon: ImageVector
) {
    CONTACT_ADD(R.string.add_contact, Icons.Rounded.PersonAdd),
    CONTACT_DELETE(R.string.delete_contact, Icons.Rounded.Delete),
    USER_BLOCK(R.string.block_user, Icons.Rounded.Block),
    USER_UNBLOCK(R.string.unblock_user, Icons.Rounded.Block),
}

package cn.tabidachi.electro.ui.pair

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
import cn.tabidachi.electro.CallActivity
import cn.tabidachi.electro.OFFER_ACTION
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.database.entity.RelationState
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.ktx.TAG
import cn.tabidachi.electro.model.BaseMessenger
import cn.tabidachi.electro.model.Messenger
import cn.tabidachi.electro.ui.common.MessageManager
import cn.tabidachi.electro.ui.common.MessageManagerImpl
import cn.tabidachi.electro.ui.pair.PairContract.Event
import cn.tabidachi.electro.ui.pair.PairContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PairViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val ktor: Ktor,
    private val repository: Repository,
    savedStateHandle: SavedStateHandle
) : PairContract.ViewModel(
    initialState = State(
        uid = ktor.uid,
        target = savedStateHandle.toRoute<PairRoute>().target
    )
) {
    val messenger: Messenger = object : BaseMessenger(
        repository = repository,
        ktor = ktor,
        scope = viewModelScope,
        sid = null
    ) {
        override suspend fun getSessionId(): Long? {
            Log.d(TAG, "getSessionId: target = ${state.value.target}")
            Log.d(TAG, "super.getSessionId() = ${super.getSessionId()}")
            return super.getSessionId() ?: repository.createSessionByPairUser(state.value.target)
                .getOrNull()?.data.also { sid ->
                    updateState { it.copy(sid = sid) }
                    sid?.let(::setSessionId)
                }
        }
    }

    val messageManager: MessageManager = MessageManagerImpl(
        context = context,
        repository = repository,
        ktor = ktor,
        scope = viewModelScope
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
            repository.getRelationState(target).onSuccess {
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
            repository.findSessionByPairUser(target).collect { sid ->
                updateState { it.copy(sid = sid) }
                messenger.setSessionId(sid)
            }
        }
    }

    private fun getUser(target: Long) {
        viewModelScope.launch {
            repository.getUser(target).onSuccess { (_, _, user) ->
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
            when (menu) {
                PairMenuItem.CONTACT_ADD -> {
                    repository.addContact(target)
                }

                PairMenuItem.CONTACT_DELETE -> {
                    repository.deleteContact(target)
                }

                PairMenuItem.USER_BLOCK -> {
                    repository.blockUser(target)
                }

                PairMenuItem.USER_UNBLOCK -> {
                    repository.unblockUser(target)
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
            putExtra("src", "${ktor.uid}")
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

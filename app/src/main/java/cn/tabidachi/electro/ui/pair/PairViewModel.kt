package cn.tabidachi.electro.ui.pair

import android.app.Application
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.CallActivity
import cn.tabidachi.electro.OFFER_ACTION
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.database.entity.Message
import cn.tabidachi.electro.data.database.entity.RelationState
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.model.Messenger
import cn.tabidachi.electro.model.attachment.Attachment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PairViewModel @Inject constructor(
    private val application: Application,
    override val ktor: Ktor,
    override val repository: Repository
) : Messenger(repository, ktor) {
    private val _viewState = MutableStateFlow(PairViewState())
    val viewState = _viewState.asStateFlow()
    private val job1 = initWebSocket()
    private val job2 = initMessage()


    fun setTarget(target: Long) {
        if (target == ktor.uid) return
        _viewState.update { it.copy(target = target) }
        findSessionId(target)
        getUser(target)
        getRelationState(target)
    }

    private fun getRelationState(target: Long) {
        viewModelScope.launch {
            repository.getRelationState(target).onSuccess {
                val filter = _viewState.value.menu.filter {
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
                    _viewState.update {
                        it.copy(menu = menu.apply {
                            addAll(filter)
                        })
                    }
                }
            }
        }
    }

    private fun findSessionId(target: Long) {
        viewModelScope.launch {
            repository.findSessionByPairUser(target).collect { sid ->
                _viewState.update { it.copy(sid = sid) }
                this@PairViewModel.sid.value = sid
            }
        }
    }

    fun getUser(target: Long) {
        viewModelScope.launch {
            repository.getUser(target).onSuccess { (_, _, user) ->
                user?.let {
                    println(it)
                    _viewState.update { it.copy(targetUser = user) }
                }
            }
        }
        viewModelScope.launch {
            listen(target)
        }
    }

    override suspend fun getSessionId(): Long? {
        val target = _viewState.value.target ?: return null
        return _viewState.value.sid ?: repository.createSessionByPairUser(target)
            .getOrNull()?.data.also { sid ->
                _viewState.update { it.copy(sid = sid) }
                sid?.let {
                    this.sid.value = it
                }
            }
    }

    fun onMenuClick(menu: PairMenuItem) {
        viewModelScope.launch {
            val target = _viewState.value.target ?: return@launch
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
        readMessage()
        viewModelScope.cancel()
        viewModelScope.launch {
            _viewState.value.target?.let { unlisten(it) }
        }
        job1.cancel()
        job2.cancel()
        super.onCleared()
    }

    fun call() {
        val intent = Intent(application, CallActivity::class.java).apply {
            action = OFFER_ACTION
            putExtra("src", "${ktor.uid}")
            putExtra("dst", "${_viewState.value.target}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        application.startActivity(intent)
    }
}

data class PairViewState(
    val sid: Long? = null,
    val target: Long? = null,
    val messages: List<Triple<Boolean, Message, Attachment?>> = emptyList(),
    val attachments: SnapshotStateList<Attachment> = mutableStateListOf(),
    val text: String = "",
    val isProcessing: Boolean = false,
    val targetUser: User? = null,
    val isRefresh: Boolean = false,
    val menu: List<PairMenuItem> = emptyList(),
    val scrollTo: Int = 0,
    val newMessage: Long? = null,
)

enum class PairMenuItem(
    @StringRes val text: Int,
    val icon: ImageVector
) {
    CONTACT_ADD(R.string.add_contact, Icons.Rounded.PersonAdd),
    CONTACT_DELETE(R.string.delete_contact, Icons.Rounded.Delete),
    USER_BLOCK(R.string.block_user, Icons.Rounded.Block),
    USER_UNBLOCK(R.string.unblock_user, Icons.Rounded.Block),
}
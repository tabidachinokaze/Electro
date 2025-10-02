package cn.tabidachi.electro.ui.pair

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import cn.tabidachi.electro.data.database.entity.Message
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.model.attachment.Attachment
import moe.tabidachi.compose.mvi.BaseViewModel

interface PairContract {
    abstract class ViewModel(
        initialState: State
    ) : BaseViewModel<State, Event, Effect>(initialState)

    data class State(
        val uid: Long,
        val target: Long,
        val sid: Long? = null,
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

    sealed interface Event {
        data object Initialize : Event
        data object NavigateUp : Event
        data object Call : Event
        data class OnMenuClick(val value: PairMenuItem) : Event
    }

    sealed interface Effect
}

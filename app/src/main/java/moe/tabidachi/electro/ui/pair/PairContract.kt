package moe.tabidachi.electro.ui.pair

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dagger.assisted.AssistedFactory
import moe.tabidachi.compose.mvi.BaseViewModel
import moe.tabidachi.electro.data.database.entity.Message
import moe.tabidachi.electro.data.database.entity.User
import moe.tabidachi.electro.model.attachment.Attachment

interface PairContract {
    abstract class ViewModel(
        initialState: State
    ) : BaseViewModel<State, Event, Effect>(initialState)

    @AssistedFactory
    interface Factory {
        fun create(route: PairRoute): PairViewModel
    }

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

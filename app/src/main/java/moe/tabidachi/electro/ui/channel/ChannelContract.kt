package moe.tabidachi.electro.ui.channel

import android.graphics.Bitmap
import dagger.assisted.AssistedFactory
import moe.tabidachi.compose.mvi.BaseViewModel
import moe.tabidachi.electro.data.database.entity.Dialog
import moe.tabidachi.electro.data.database.entity.Session
import moe.tabidachi.electro.data.database.entity.User
import moe.tabidachi.electro.model.response.ChannelRole

interface ChannelContract {
    abstract class ViewModel(initialState: State) :
        BaseViewModel<State, Event, Effect>(initialState)

    @AssistedFactory
    interface Factory {
        fun create(route: ChannelRoute): ChannelViewModel
    }

    data class State(
        val dialog: Dialog? = null,
        val isExit: Boolean = false,
        val canSendMessage: Boolean = false,
        val users: List<User> = emptyList(),
        val roles: List<ChannelRole> = emptyList(),
        val isAdmin: Boolean = false,
        val owner: Long = 0,
        val filter: String = "",
        val contacts: List<User> = emptyList(),
        val reply: Long? = null,
        val image: Bitmap? = null,
        val title: String = "",
        val isTitleError: Boolean = false,
        val description: String = "",
        val processing: Boolean = false,
        val session: Session? = null
    )

    sealed interface Event {
        data class AddAdmin(val uid: Long) : Event
        data class GetAdmin(val uid: Long) : Event
        data class RemoveAdmin(val uid: Long) : Event
        data class RemoveMember(val uid: Long) : Event
        data object NavigateUp : Event
        data class NavigateToPair(val uid: Long) : Event
        data object ExitGroup : Event
        data class NavigateToChannelDetail(val sid: Long) : Event
        data class NavigateToChannelEdit(val sid: Long) : Event
        data class NavigateToChannelInvite(val sid: Long) : Event
        data class NavigateToChannelAdmin(val sid: Long) : Event
        data object GetSessionUser : Event
        data object GetSessionInfo : Event
        data object GetContact : Event
        data class OnFilterChange(val value: String) : Event
        data class Invite(val uid: Long) : Event
        data class OnCropSuccess(val value: Bitmap) : Event
        data object FindSession : Event
        data object OnDone : Event
        data class OnTitleChange(val value: String) : Event
        data class OnDescriptionChange(val value: String) : Event
    }

    sealed interface Effect {
        data object NavigateUp : Effect
    }
}

data class UserState(
    val user: User,
    val isOnline: Boolean
)

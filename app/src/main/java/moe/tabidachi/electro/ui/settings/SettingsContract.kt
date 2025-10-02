package moe.tabidachi.electro.ui.settings

import androidx.compose.ui.graphics.ImageBitmap
import moe.tabidachi.electro.data.database.entity.User
import moe.tabidachi.electro.ui.theme.DarkLight
import moe.tabidachi.electro.ui.theme.Theme
import moe.tabidachi.compose.mvi.BaseViewModel

interface SettingsContract {
    abstract class ViewModel(initialState: State) :
        BaseViewModel<State, Event, Effect>(initialState)

    data class State(
        val theme: Theme = Theme.Dynamic,
        val darkLight: DarkLight = DarkLight.SYSTEM,
        val language: Language = Language.SYSTEM,
        val user: User = User(-1, "", "", ""),
        val isMenuExpanded: Boolean = false,
        val isEmailDialogVisible: Boolean = false,
        val newEmail: String = "",
        val isDayNightMenuExpanded: Boolean = false,
        val isThemeMenuExpanded: Boolean = false,
    )

    sealed interface Event {
        data class UpdateAvatar(val value: ImageBitmap) : Event
        data object GetUser : Event
        data object NavigateUp : Event
        data object OnMenuExpand : Event
        data object OnMenuDismiss : Event
        data object Logout : Event
        data object NavigateToProfile : Event
        data class OnDayNightMenuVisible(val value: Boolean) : Event
        data class OnDayNightModeChange(val value: DarkLight) : Event
        data class OnThemeMenuVisible(val value: Boolean) : Event
        data class OnThemeChange(val value: Theme) : Event
        data object NavigateToLocaleSettings : Event
    }

    sealed interface Effect
}

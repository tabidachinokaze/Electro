package moe.tabidachi.electro.ui.server

import moe.tabidachi.compose.mvi.BaseViewModel

interface ServerContract {
    abstract class ViewModel(initialState: State) :
        BaseViewModel<State, Event, Effect>(initialState)

    data class State(
        val url: String = "",
        val port: String = "",
        val minioUrl: String = "",
        val minioPort: String = "",
        val dialogVisible: Boolean = false,
        val dialogType: ServerDialogType = ServerDialogType.ElectroUrl,
        val dialogValue: String = ""
    )

    data class Action(
        val showDialog: (ServerDialogType) -> Unit = {},
        val hideDialog: () -> Unit = {},
        val onSave: () -> Unit = {},
        val onDialogValueChange: (String) -> Unit = {},
        val onNavigateUp: () -> Unit = {}
    )

    sealed interface Event {
        data class ShowDialog(val value: ServerDialogType) : Event
        data object HideDialog : Event
        data object OnSave : Event
        data class OnDialogValueChange(val value: String) : Event
    }

    sealed interface Effect
}

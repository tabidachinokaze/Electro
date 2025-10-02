package moe.tabidachi.electro.ui.server

import moe.tabidachi.electro.data.network.Ktor
import moe.tabidachi.electro.ui.server.ServerContract.Event
import moe.tabidachi.electro.ui.server.ServerContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.defaultRequest
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
    val ktor: Ktor
) : ServerContract.ViewModel(State()) {
    override fun event(event: Event) = when (event) {
        Event.HideDialog -> hideDialog()
        is Event.OnDialogValueChange -> onDialogValueChange(event.value)
        Event.OnSave -> onSave()
        is Event.ShowDialog -> showDialog(event.value)
    }

    private fun showDialog(type: ServerDialogType) {
        updateState {
            it.copy(
                dialogType = type,
                dialogVisible = true,
                dialogValue = when (type) {
                    ServerDialogType.ElectroUrl -> state.value.url
                    ServerDialogType.ElectroPort -> state.value.port
                    ServerDialogType.MinioUrl -> state.value.minioUrl
                    ServerDialogType.MinioPort -> state.value.minioPort
                }
            )
        }
    }

    private fun hideDialog() = updateState { it.copy(dialogVisible = false, dialogValue = "") }

    private fun onSave() {
        when (state.value.dialogType) {
            ServerDialogType.ElectroUrl -> updateState { it.copy(url = it.dialogValue) }
            ServerDialogType.ElectroPort -> updateState { it.copy(port = it.dialogValue) }
            ServerDialogType.MinioUrl -> updateState { it.copy(minioUrl = it.dialogValue) }
            ServerDialogType.MinioPort -> updateState { it.copy(minioPort = it.dialogValue) }
        }

        hideDialog()

        ktor.client.config {
            defaultRequest {
                this.host = state.value.url
                this.port = state.value.port.toInt()
            }
        }
    }

    private fun onDialogValueChange(value: String) {
        updateState {
            it.copy(dialogValue = value)
        }
    }
}

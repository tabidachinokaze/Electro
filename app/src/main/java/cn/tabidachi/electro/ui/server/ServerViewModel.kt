package cn.tabidachi.electro.ui.server

import androidx.lifecycle.ViewModel
import cn.tabidachi.electro.data.network.Ktor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.defaultRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
    val ktor: Ktor
) : ViewModel() {

    private val _state: MutableStateFlow<ServerState> = MutableStateFlow(ServerState())

    val state: StateFlow<ServerState> = _state.asStateFlow()

    fun showDialog(type: ServerDialogType) {
        _state.update {
            it.copy(
                dialogType = type,
                dialogVisible = true,
                dialogValue = when (type) {
                    ServerDialogType.ElectroUrl -> _state.value.url
                    ServerDialogType.ElectroPort -> _state.value.port
                    ServerDialogType.MinioUrl -> _state.value.minioUrl
                    ServerDialogType.MinioPort -> _state.value.minioPort
                }
            )
        }
    }

    fun hideDialog() = _state.update { it.copy(dialogVisible = false, dialogValue = "") }

    fun onSave() {
        when (_state.value.dialogType) {
            ServerDialogType.ElectroUrl -> _state.update { it.copy(url = it.dialogValue) }
            ServerDialogType.ElectroPort -> _state.update { it.copy(port = it.dialogValue) }
            ServerDialogType.MinioUrl -> _state.update { it.copy(minioUrl = it.dialogValue) }
            ServerDialogType.MinioPort -> _state.update { it.copy(minioPort = it.dialogValue) }
        }

        hideDialog()

        ktor.client.config {
            defaultRequest {
                this.host = _state.value.url
                this.port = _state.value.port.toInt()
            }
        }
    }

    fun onDialogValueChange(value: String) {
        _state.update {
            it.copy(dialogValue = value)
        }
    }
}
package moe.tabidachi.electro.ui.server

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.http.URLBuilder
import kotlinx.coroutines.launch
import moe.tabidachi.electro.Prefs
import moe.tabidachi.electro.data.repository.SharedRepository
import moe.tabidachi.electro.ext.dataStore
import moe.tabidachi.electro.ui.server.ServerContract.Event
import moe.tabidachi.electro.ui.server.ServerContract.State
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val sharedRepository: SharedRepository,
) : ServerContract.ViewModel(State()) {
    init {
        updateState {
            val sharedState = sharedRepository.state.value
            val baseUrl = URLBuilder(sharedState.baseUrl)
            val minioUrl = URLBuilder(sharedState.minioUrl)
            it.copy(
                url = "${baseUrl.protocol.name}://${baseUrl.host}",
                port = baseUrl.port.toString(),
                minioUrl = "${minioUrl.protocol.name}://${minioUrl.host}",
                minioPort = minioUrl.port.toString(),
            )
        }
    }

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
        sharedRepository.updateState {
            val state = state.value
            val baseUrl = URLBuilder(state.url).apply {
                state.port.toIntOrNull()?.let {
                    this.port = it
                }
            }.buildString()

            val minioUrl = URLBuilder(state.minioUrl).apply {
                state.minioPort.toIntOrNull()?.let {
                    this.port = it
                }
            }.buildString()
            viewModelScope.launch {
                context.dataStore.edit {
                    it[Prefs.BASE_URL] = baseUrl
                    it[Prefs.MINIO_URL] = minioUrl
                }
            }
            it.copy(
                baseUrl = baseUrl,
                minioUrl = minioUrl,
            )
        }
    }

    private fun onDialogValueChange(value: String) {
        updateState {
            it.copy(dialogValue = value)
        }
    }
}

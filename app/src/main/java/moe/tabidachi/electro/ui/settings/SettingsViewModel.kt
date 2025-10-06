package moe.tabidachi.electro.ui.settings

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.util.generateNonce
import io.minio.GetPresignedObjectUrlArgs
import io.minio.http.Method
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tabidachi.electro.Prefs
import moe.tabidachi.electro.data.Repository
import moe.tabidachi.electro.data.network.Ktor
import moe.tabidachi.electro.data.network.MinIO
import moe.tabidachi.electro.ext.MINIO
import moe.tabidachi.electro.ext.dataStore
import moe.tabidachi.electro.model.request.UserUpdateRequest
import moe.tabidachi.electro.ui.settings.SettingsContract.Event
import moe.tabidachi.electro.ui.settings.SettingsContract.State
import moe.tabidachi.electro.ui.theme.DarkLight
import moe.tabidachi.electro.ui.theme.Theme
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val ktor: Ktor,
    private val minio: MinIO,
    private val repository: Repository
) : SettingsContract.ViewModel(State()) {
    override fun event(event: Event) = when (event) {
        Event.GetUser -> getUser()
        Event.Logout -> logout()
        Event.NavigateToProfile -> Unit
        Event.NavigateUp -> Unit
        is Event.OnDayNightMenuVisible -> onDayNightMenuVisible(event.value)
        is Event.OnDayNightModeChange -> onDayNightModeChange(event.value)
        Event.OnMenuDismiss -> onMenuDismiss()
        Event.OnMenuExpand -> onMenuExpand()
        is Event.OnThemeChange -> onThemeChange(event.value)
        is Event.OnThemeMenuVisible -> onThemeMenuVisible(event.value)
        is Event.UpdateAvatar -> updateAvatar(event.value)
        Event.NavigateToLocaleSettings -> Unit
    }

    init {
        viewModelScope.launch {
            application.dataStore.data.map {
                it[Prefs.DARK_LIGHT]
            }.filterNotNull().collect { value ->
                updateState { it.copy(darkLight = DarkLight.valueOf(value)) }
            }
        }
        viewModelScope.launch {
            application.dataStore.data.map {
                it[Prefs.THEME]
            }.filterNotNull().filter { it.isNotBlank() }.collect { value ->
                updateState { it.copy(theme = Theme.valueOf(value)) }
            }
        }
    }

    private fun onMenuExpand() {
        updateState { it.copy(isMenuExpanded = true) }
    }

    private fun onMenuDismiss() {
        updateState { it.copy(isMenuExpanded = false) }
    }

    private fun getUser() {
        viewModelScope.launch {
            ktor.getUser(ktor.uid).onSuccess {
                it.data?.let { user ->
                    updateState { it.copy(user = user) }
                }
            }
        }
    }

    private fun onEmailDialogDismiss() {
        updateState { it.copy(isEmailDialogVisible = false) }
    }

    private fun onEmailDialogShow() {
        updateState { it.copy(isEmailDialogVisible = true) }
    }

    private fun onEmailDialogConfirm() {
    }

    private fun onNewEmailValueChange(value: String) {
        updateState { it.copy(newEmail = value) }
    }

    private fun updateAvatar(bitmap: ImageBitmap) {
        viewModelScope.launch {
            minio.checkOrCreateBucket(MinIO.AVATAR)
            val filename = generateNonce()
            val url = minio.client.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(MinIO.AVATAR)
                    .`object`(filename)
                    .build()
            )
            withContext(Dispatchers.IO) {
                val outputStream = ByteArrayOutputStream()
                kotlin.runCatching {
                    bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    ktor.upload.put(url) {
                        setBody(outputStream.toByteArray())
                    }
                }.onSuccess {
                    if (it.status == HttpStatusCode.OK) {
                        val url = Url(
                            URLBuilder(
                                protocol = URLProtocol.MINIO,
                                pathSegments = listOf(MinIO.AVATAR, filename)
                            )
                        ).toString()
                        ktor.userUpdate(UserUpdateRequest(null, null, null, url)).onSuccess {
                            getUser()
                        }
                    }
                }
                outputStream.close()
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            repository.removeAccount(ktor.uid)
            application.dataStore.edit {
                it[Prefs.THEME] = ""
                it[Prefs.UID] = 0
            }
        }
    }

    private fun onDayNightModeChange(darkLight: DarkLight) {
        viewModelScope.launch {
            application.dataStore.edit {
                it[Prefs.DARK_LIGHT] = darkLight.name
            }
        }
    }

    private fun onDayNightMenuVisible(value: Boolean) {
        updateState { it.copy(isDayNightMenuExpanded = value) }
    }

    private fun onThemeChange(theme: Theme) {
        viewModelScope.launch {
            application.dataStore.edit {
                it[Prefs.THEME] = theme.name
            }
        }
    }

    private fun onThemeMenuVisible(value: Boolean) {
        updateState { it.copy(isThemeMenuExpanded = value) }
    }
}

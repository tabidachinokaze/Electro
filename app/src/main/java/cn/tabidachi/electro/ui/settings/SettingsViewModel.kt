package cn.tabidachi.electro.ui.settings

import android.app.Application
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.PreferenceConstant
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.data.network.MinIO
import cn.tabidachi.electro.ext.MINIO
import cn.tabidachi.electro.ext.dataStore
import cn.tabidachi.electro.model.request.UserUpdateRequest
import cn.tabidachi.electro.ui.theme.DarkLight
import cn.tabidachi.electro.ui.theme.Theme
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val ktor: Ktor,
    private val minio: MinIO,
    private val repository: Repository
) : ViewModel() {
    private val _viewState = MutableStateFlow(SettingViewState())
    val viewState = _viewState.asStateFlow()
    var theme by mutableStateOf(Theme.Dynamic)
    var darkLight by mutableStateOf(DarkLight.SYSTEM)
    var language by mutableStateOf(Language.SYSTEM)

    init {
        viewModelScope.launch {
            application.dataStore.data.map {
                it[PreferenceConstant.Key.DARK_LIGHT]
            }.filterNotNull().collect {
                darkLight = DarkLight.valueOf(it)
            }
        }
        viewModelScope.launch {
            application.dataStore.data.map {
                it[PreferenceConstant.Key.THEME]
            }.filterNotNull().collect {
                theme = Theme.valueOf(it)
            }
        }
    }

    fun onMenuExpand() {
        _viewState.update { it.copy(isMenuExpanded = true) }
    }

    fun onMenuDismiss() {
        _viewState.update { it.copy(isMenuExpanded = false) }
    }

    fun getUser() {
        viewModelScope.launch {
            ktor.getUser(ktor.uid).onSuccess {
                it.data?.let { user ->
                    _viewState.update { it.copy(user = user) }
                }
            }
        }
    }

    fun onEmailDialogDismiss() {
        _viewState.update { it.copy(isEmailDialogVisible = false) }
    }

    fun onEmailDialogShow() {
        _viewState.update { it.copy(isEmailDialogVisible = true) }
    }

    fun onEmailDialogConfirm() {

    }

    fun onNewEmailValueChange(value: String) {
        _viewState.update { it.copy(newEmail = value) }
    }

    fun updateAvatar(bitmap: Bitmap) {
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
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
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

    fun logout() {
        viewModelScope.launch {
            repository.removeAccount(ktor.uid)
            application.dataStore.edit {
                it[PreferenceConstant.Key.THEME] = ""
                it[PreferenceConstant.Key.UID] = 0
            }
        }
    }

    fun onDayNightModeChange(darkLight: DarkLight) {
        viewModelScope.launch {
            application.dataStore.edit {
                it[PreferenceConstant.Key.DARK_LIGHT] = darkLight.name
            }
        }
    }

    fun onDayNightMenuVisible(value: Boolean) {
        _viewState.update { it.copy(isDayNightMenuExpanded = value) }
    }

    fun onThemeChange(theme: Theme) {
        viewModelScope.launch {
            application.dataStore.edit {
                it[PreferenceConstant.Key.THEME] = theme.name
            }
        }
    }

    fun onLanguageChange(language: Language) {
        viewModelScope.launch {
            val languageTags = LocaleListCompat.forLanguageTags(language.tag)
            AppCompatDelegate.setApplicationLocales(languageTags)
        }
    }

    fun onLanguageMenuVisible(value: Boolean) {
        _viewState.update { it.copy(isLanguageMenuExpanded = value) }
    }

    fun onThemeMenuVisible(value: Boolean) {
        _viewState.update { it.copy(isThemeMenuExpanded = value) }
    }
}

data class SettingViewState(
    val user: User = User(-1, "", "", ""),
    val isMenuExpanded: Boolean = false,
    val isEmailDialogVisible: Boolean = false,
    val newEmail: String = "",
    val isDayNightMenuExpanded: Boolean = false,
    val isLanguageMenuExpanded: Boolean = false,
    val isThemeMenuExpanded: Boolean = false,
)
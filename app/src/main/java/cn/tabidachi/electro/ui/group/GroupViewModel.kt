package cn.tabidachi.electro.ui.group

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.database.entity.Dialog
import cn.tabidachi.electro.data.database.entity.Message
import cn.tabidachi.electro.data.database.entity.Session
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.data.network.MinIO
import cn.tabidachi.electro.ext.MINIO
import cn.tabidachi.electro.model.Messenger
import cn.tabidachi.electro.model.attachment.Attachment
import cn.tabidachi.electro.model.request.GroupUpdateRequest
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    override val repository: Repository,
    override val ktor: Ktor,
    private val minio: MinIO
) : Messenger(repository, ktor) {
    private val _viewState = MutableStateFlow(GroupViewState())
    val viewState = _viewState.asStateFlow()
    private val job1 = initMessage()
    private val job2 = initWebSocket()

    fun setSessionId(sid: Long) {
        this.sid.value = sid
        _viewState.update { it.copy(sid = sid) }
        getSessionInfo(sid)
        getSessionUser(sid)
    }

    fun getSessionUser(sid: Long) {
        viewModelScope.launch {
            repository.getSessionUser(sid).collect {
                it.mapNotNull {
                    repository.getUser(it).getOrNull()?.data
                }.also { users ->
                    _viewState.update { it.copy(users = users) }
                }.forEach {
                    listen(it.uid)
                }
            }
        }
    }

    fun getSessionInfo(sid: Long) {
        viewModelScope.launch {
            repository.getDialog(sid).collect { dialog ->
                _viewState.update { it.copy(dialog = dialog) }
            }
        }
    }

    override suspend fun getSessionId(): Long? {
        return _viewState.value.sid
    }

    fun onCropSuccess(bitmap: Bitmap) {
        _viewState.update { it.copy(image = bitmap) }
    }

    fun onTitleChange(value: String) {
        _viewState.update { it.copy(title = value, isTitleError = false) }
    }

    fun onDescriptionChange(value: String) {
        _viewState.update { it.copy(description = value) }
    }

    fun onDone(callback: () -> Unit) {
        viewModelScope.launch {
            val viewState = _viewState.value
            val sid = viewState.sid ?: return@launch
            val image = viewState.image?.let {
                uploadImage(it) ?: return@launch
            }
            val title = viewState.title.ifBlank { null }
            if (title.isNullOrBlank()) {
                _viewState.update { it.copy(isTitleError = true) }
                return@launch
            }
            val description = viewState.description.ifBlank { null }
            repository.updateGroupInfo(sid, GroupUpdateRequest(image, title, description))
                .onSuccess {
                    it.data?.let {
                        callback()
                    }
                }
        }
    }

    suspend fun uploadImage(bitmap: Bitmap): String? {
        minio.checkOrCreateBucket(MinIO.AVATAR)
        val filename = generateNonce()
        val url = minio.client.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(MinIO.AVATAR)
                .`object`(filename)
                .build()
        )
        return withContext(Dispatchers.IO) {
            ByteArrayOutputStream().use { outputStream ->
                if (
                    kotlin.runCatching {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        ktor.upload.put(url) {
                            setBody(outputStream.toByteArray())
                        }
                    }.getOrNull()?.status == HttpStatusCode.OK
                ) {
                    Url(
                        URLBuilder(
                            protocol = URLProtocol.MINIO,
                            pathSegments = listOf(MinIO.AVATAR, filename)
                        )
                    ).toString()
                } else null
            }
        }
    }

    fun findSession() {
        viewModelScope.launch {
            _viewState.value.sid?.let {
                repository.findSession(it).collect { session ->
                    _viewState.update {
                        it.copy(
                            session = session,
                            title = session.title ?: "",
                            description = session.description ?: ""
                        )
                    }
                }
            }
        }
    }

    fun exitGroup(sid: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.exitSession(sid).onSuccess {
                it.data?.let {
                    _viewState.update { it.copy(isExit = true) }
                    onSuccess()
                }
            }
        }
    }

    fun getContact() {
        viewModelScope.launch {
            repository.contact().onSuccess {
                it.data?.mapNotNull {
                    repository.getUser(it).getOrNull()?.data
                }?.let { contacts ->
                    _viewState.update { it.copy(contacts = contacts) }
                }
            }
        }
    }

    fun invite(target: Long) {
        val sid = _viewState.value.sid ?: return
        viewModelScope.launch {
            repository.invite(sid, target).onSuccess {
                it.data?.let {
                    getSessionUser(sid)
                }
            }
        }
    }

    fun onFilterChange(filter: String) {
        _viewState.update { it.copy(filter = filter) }
    }

    fun getAdmin(sid: Long) {
        viewModelScope.launch {
            repository.getGroupAdmin(sid).onSuccess {
                it.data?.let { admin ->
                    val isAdmin = admin.any { it.uid == ktor.uid }
                    _viewState.update { it.copy(admin = admin, isAdmin = isAdmin) }
                }
            }
        }
    }

    override fun onCleared() {
        job1.cancel()
        job2.cancel()
        _viewState.value.users.forEach {
            unlisten(it.uid)
        }
        super.onCleared()
    }
}

data class GroupViewState(
    val sid: Long? = null,
    val dialog: Dialog? = null,
    val reply: Long? = null,
    val users: List<User> = emptyList(),
    val image: Bitmap? = null,
    val title: String = "",
    val isTitleError: Boolean = false,
    val description: String = "",
    val processing: Boolean = false,
    val session: Session? = null,
    val isExit: Boolean = false,
    val filter: String = "",
    val contacts: List<User> = emptyList(),
    val admin: List<User> = emptyList(),
    val isAdmin: Boolean = false
)
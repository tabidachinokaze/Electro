package cn.tabidachi.electro.ui.channel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.data.network.MinIO
import cn.tabidachi.electro.ext.MINIO
import cn.tabidachi.electro.model.Messenger
import cn.tabidachi.electro.model.request.ChannelUpdateRequest
import cn.tabidachi.electro.model.response.ChannelRoleType
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.reflect.KFunction0

@HiltViewModel
class ChannelViewModel @Inject constructor(
    override val repository: Repository,
    override val ktor: Ktor,
    private val minio: MinIO
) : Messenger(repository, ktor) {

    private val _viewState: MutableStateFlow<ChannelState> = MutableStateFlow(ChannelState())

    val viewState: StateFlow<ChannelState> = _viewState.asStateFlow()

    private val job1 = initMessage()
    private val job2 = initWebSocket()

    init {
        viewModelScope.launch {
            sid.collect { sid ->
                getSessionInfo(sid)
                getSessionUser(sid)
            }
        }
    }

    override suspend fun getSessionId(): Long {
        return sid.value
    }

    fun getSessionInfo(sid: Long) {
        viewModelScope.launch {
            repository.getDialog(sid).collect { dialog ->
                _viewState.update { it.copy(dialog = dialog) }
            }
        }
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

    fun getAdmin(sid: Long) = viewModelScope.launch {
        repository.getChannelAdmins(sid).onSuccess {
            it.data?.also { roles ->
                Log.d("ChannelViewModel", "getAdmin: ${roles.size}")
                val isAdmin = roles.any { it.uid == ktor.uid }
                val owner = roles.firstOrNull { it.type == ChannelRoleType.OWNER }
                _viewState.update { it.copy(roles = roles, isAdmin = isAdmin, canSendMessage = isAdmin) }
                if (owner != null) {
                    _viewState.update { it.copy(owner = owner.uid) }
                }
            }
        }
    }

    fun removeAdmin(target: Long) {
        viewModelScope.launch {
            repository.removeChannelAdmin(sid.value, target).onSuccess {
                if (it.status == HttpStatusCode.OK.value) it.data?.let { target ->
                    _viewState.update {
                        it.copy(
                            roles = it.roles.toMutableList().apply {
                                removeIf {
                                    it.uid == target
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    fun addAdmin(target: Long) {
        viewModelScope.launch {
            repository.addChannelAdmin(sid.value, target).onSuccess {
                if (it.status == HttpStatusCode.OK.value) it.data?.let { role ->
                    _viewState.update {
                        it.copy(
                            roles = it.roles.toMutableList().apply {
                                add(role)
                            }
                        )
                    }
                }
            }
        }
    }

    fun removeMember(target: Long) {
        viewModelScope.launch {
            repository.removeChannelMember(sid.value, target).onSuccess {
                if (it.status == HttpStatusCode.OK.value) it.data?.let { target ->
                    _viewState.update {
                        it.copy(
                            users = it.users.toMutableList().apply {
                                removeIf {
                                    it.uid == target
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    fun onFilterChange(filter: String) {
        _viewState.update { it.copy(filter = filter) }
    }

    fun invite(target: Long) {
        val sid = sid.value
        viewModelScope.launch {
            repository.invite(sid, target).onSuccess {
                it.data?.let {
                    getSessionUser(sid)
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

    fun exitGroup(sid: Long, onSuccess: KFunction0<Unit>) {
        viewModelScope.launch {
            repository.exitSession(sid).onSuccess {
                it.data?.let {
                    _viewState.update { it.copy(isExit = true) }
                    onSuccess()
                }
            }
        }
    }

    fun onCropSuccess(bitmap: Bitmap) {
        _viewState.update { it.copy(image = bitmap) }
    }

    fun onDone(callback: () -> Unit) {
        viewModelScope.launch {
            val viewState = _viewState.value
            val sid = this@ChannelViewModel.sid.value
            val image = viewState.image?.let {
                uploadImage(it) ?: return@launch
            }
            val title = viewState.title.ifBlank { null }
            if (title.isNullOrBlank()) {
                _viewState.update { it.copy(isTitleError = true) }
                return@launch
            }
            val description = viewState.description.ifBlank { null }
            repository.updateChannelInfo(sid, ChannelUpdateRequest(image, title, description))
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
            sid.value.let {
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

    fun onTitleChange(value: String) {
        _viewState.update { it.copy(title = value, isTitleError = false) }
    }

    fun onDescriptionChange(value: String) {
        _viewState.update { it.copy(description = value) }
    }

    override fun onCleared() {
        job1.cancel()
        job2.cancel()
        super.onCleared()
    }
}
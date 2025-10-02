package moe.tabidachi.electro.ui.channel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import moe.tabidachi.electro.data.Repository
import moe.tabidachi.electro.data.network.Ktor
import moe.tabidachi.electro.data.network.MinIO
import moe.tabidachi.electro.ext.MINIO
import moe.tabidachi.electro.model.BaseMessenger
import moe.tabidachi.electro.model.Messenger
import moe.tabidachi.electro.model.request.ChannelUpdateRequest
import moe.tabidachi.electro.model.response.ChannelRoleType
import moe.tabidachi.electro.ui.channel.ChannelContract.Effect
import moe.tabidachi.electro.ui.channel.ChannelContract.Event
import moe.tabidachi.electro.ui.channel.ChannelContract.State
import moe.tabidachi.electro.ui.common.MessageManager
import moe.tabidachi.electro.ui.common.MessageManagerImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val repository: Repository,
    private val ktor: Ktor,
    private val minio: MinIO,
    savedStateHandle: SavedStateHandle
) : ChannelContract.ViewModel(initialState = State()) {
    private val route = savedStateHandle.toRoute<ChannelRoute>()
    val messenger: Messenger = BaseMessenger(
        repository = repository,
        ktor = ktor,
        scope = viewModelScope,
        sid = route.sid
    )
    val messageManager: MessageManager = MessageManagerImpl(
        context = context,
        repository = repository,
        ktor = ktor,
        scope = viewModelScope
    )

    init {
        viewModelScope.launch {
            getSessionInfo(route.sid)
            getSessionUser(route.sid)
        }
        event(Event.GetAdmin(route.sid))
    }

    override fun event(event: Event) = when (event) {
        is Event.AddAdmin -> addAdmin(event.uid)
        is Event.RemoveAdmin -> removeAdmin(event.uid)
        is Event.RemoveMember -> removeMember(event.uid)
        is Event.GetAdmin -> handleOneTimeEvent(event) { getAdmin(event.uid) }
        is Event.NavigateToPair -> Unit
        Event.NavigateUp -> Unit
        Event.ExitGroup -> exitGroup(route.sid)
        is Event.NavigateToChannelDetail -> Unit
        Event.GetSessionInfo -> handleOneTimeEvent(event) { getSessionInfo(route.sid) }
        Event.GetSessionUser -> handleOneTimeEvent(event) { getSessionUser(route.sid) }
        is Event.NavigateToChannelAdmin -> Unit
        is Event.NavigateToChannelEdit -> Unit
        is Event.NavigateToChannelInvite -> Unit
        Event.FindSession -> findSession()
        Event.GetContact -> getContact()
        is Event.Invite -> invite(event.uid)
        is Event.OnCropSuccess -> onCropSuccess(event.value)
        Event.OnDone -> onDone()
        is Event.OnFilterChange -> onFilterChange(event.value)
        is Event.OnDescriptionChange -> onDescriptionChange(event.value)
        is Event.OnTitleChange -> onTitleChange(event.value)
    }

    private fun getSessionInfo(sid: Long) {
        viewModelScope.launch {
            repository.getDialog(sid).collect { dialog ->
                updateState { it.copy(dialog = dialog) }
            }
        }
    }

    private fun getSessionUser(sid: Long) {
        viewModelScope.launch {
            repository.getSessionUser(sid).collect {
                it.mapNotNull {
                    repository.getUser(it).getOrNull()?.data
                }.also { users ->
                    updateState { it.copy(users = users) }
                }.forEach {
                    messenger.listen(it.uid)
                }
            }
        }
    }

    private fun getAdmin(sid: Long) {
        viewModelScope.launch {
            repository.getChannelAdmins(sid).onSuccess {
                it.data?.also { roles ->
                    Log.d("ChannelViewModel", "getAdmin: ${roles.size}")
                    val isAdmin = roles.any { it.uid == ktor.uid }
                    val owner = roles.firstOrNull { it.type == ChannelRoleType.OWNER }
                    updateState {
                        it.copy(
                            roles = roles,
                            isAdmin = isAdmin,
                            canSendMessage = isAdmin
                        )
                    }
                    if (owner != null) {
                        updateState { it.copy(owner = owner.uid) }
                    }
                }
            }
        }
    }

    private fun removeAdmin(target: Long) {
        viewModelScope.launch {
            repository.removeChannelAdmin(route.sid, target).onSuccess {
                if (it.status == HttpStatusCode.OK.value) {
                    it.data?.let { target ->
                        updateState {
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
    }

    private fun addAdmin(target: Long) {
        viewModelScope.launch {
            repository.addChannelAdmin(route.sid, target).onSuccess {
                if (it.status == HttpStatusCode.OK.value) {
                    it.data?.let { role ->
                        updateState {
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
    }

    private fun removeMember(target: Long) {
        viewModelScope.launch {
            repository.removeChannelMember(route.sid, target).onSuccess {
                if (it.status == HttpStatusCode.OK.value) {
                    it.data?.let { target ->
                        updateState {
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
    }

    private fun onFilterChange(filter: String) {
        updateState { it.copy(filter = filter) }
    }

    private fun invite(target: Long) {
        val sid = route.sid
        viewModelScope.launch {
            repository.invite(sid, target).onSuccess {
                it.data?.let {
                    getSessionUser(sid)
                }
            }
        }
    }

    private fun getContact() {
        viewModelScope.launch {
            repository.contact().onSuccess {
                it.data?.mapNotNull {
                    repository.getUser(it).getOrNull()?.data
                }?.let { contacts ->
                    updateState { it.copy(contacts = contacts) }
                }
            }
        }
    }

    private fun exitGroup(sid: Long) {
        viewModelScope.launch {
            repository.exitSession(sid).onSuccess {
                it.data?.let {
                    updateState { it.copy(isExit = true) }
                    emitEffect(Effect.NavigateUp)
                }
            }
        }
    }

    private fun onCropSuccess(bitmap: Bitmap) {
        updateState { it.copy(image = bitmap) }
    }

    private fun onDone() {
        viewModelScope.launch {
            val viewState = state.value
            val sid = route.sid
            val image = viewState.image?.let {
                uploadImage(it) ?: return@launch
            }
            val title = viewState.title.ifBlank { null }
            if (title.isNullOrBlank()) {
                updateState { it.copy(isTitleError = true) }
                return@launch
            }
            val description = viewState.description.ifBlank { null }
            repository.updateChannelInfo(sid, ChannelUpdateRequest(image, title, description))
                .onSuccess {
                    it.data?.let {
                        emitEffect(Effect.NavigateUp)
                    }
                }
        }
    }

    private suspend fun uploadImage(bitmap: Bitmap): String? {
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
                    runCatching {
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
                } else {
                    null
                }
            }
        }
    }

    private fun findSession() {
        viewModelScope.launch {
            repository.findSession(route.sid).collect { session ->
                updateState {
                    it.copy(
                        session = session,
                        title = session.title ?: "",
                        description = session.description ?: ""
                    )
                }
            }
        }
    }

    private fun onTitleChange(value: String) {
        updateState { it.copy(title = value, isTitleError = false) }
    }

    private fun onDescriptionChange(value: String) {
        updateState { it.copy(description = value) }
    }
}

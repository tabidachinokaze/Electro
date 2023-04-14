package cn.tabidachi.electro.model

import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.database.entity.MessageSendRequest
import cn.tabidachi.electro.data.database.entity.MessageType
import cn.tabidachi.electro.model.attachment.Attachment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UploadMessageItem(
    val message: MessageSendRequest,
    val attachment: Attachment?,
    override val scope: CoroutineScope,
    val repository: Repository
) : Playable(scope) {
    var state by mutableStateOf<UploadState>(UploadState.Pause)
    var path by mutableStateOf<String?>(null)
    private var uploadJob: Job? = null

    init {
        scope.launch {
            path = repository.findResource(message.identification())?.path
        }
        scope.launch {
            upload()
        }
    }

    fun upload() {
        if (state is UploadState.Uploading) return
        uploadJob?.cancel()
        uploadJob = scope.launch {
            state = UploadState.Uploading(0f)
            repository.sendMessage(
                message, onFailure = {
                    state = UploadState.Failure
                }, onProgress = {
                    state = UploadState.Uploading(it)
                }
            )
        }
    }

    override fun play() {
        if (player == null) {
            path?.let {
                if (message.type == MessageType.VOICE) {
                    onClose()
                    player = MediaPlayer().apply {
                        setDataSource(it)
                        setOnCompletionListener {
                            it.seekTo(0)
                            this@UploadMessageItem.isPlaying = false
                        }
                        prepare()
                    }
                }
            }
        }
        super.play()
    }

    fun cancel() {
        uploadJob?.cancel()
        scope.launch {
            delay(500)
            state = UploadState.Pause
        }
    }

    fun cancelMessage() {
        scope.launch {
            // TODO 还需处理，文件缓存，云端缓存
            repository.cancelSendingMessage(message.id)
        }
    }
}

sealed interface UploadState {
    object Pause : UploadState
    data class Uploading(val progress: Float) : UploadState
    object Failure : UploadState
}
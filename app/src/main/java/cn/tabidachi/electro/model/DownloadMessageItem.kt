package cn.tabidachi.electro.model

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.database.entity.Message
import cn.tabidachi.electro.data.database.entity.MessageType
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.ext.addIfAbsent
import cn.tabidachi.electro.model.attachment.Attachment
import cn.tabidachi.electro.model.attachment.DocumentAttachment
import cn.tabidachi.electro.model.attachment.LocationAttachment
import cn.tabidachi.electro.model.attachment.WebRTCAttachment
import cn.tabidachi.electro.ui.common.BubbleType
import cn.tabidachi.electro.ui.common.MessageMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DownloadMessageItem(
    val type: BubbleType,
    val message: Message,
    val attachment: Attachment?,
    val repository: Repository,
    override val scope: CoroutineScope
) : Playable(scope) {
    var path by mutableStateOf<String?>(null)
    var user by mutableStateOf<User?>(null)
    var downloading by mutableStateOf(false)
    private var downloadJob: Job? = null
    val menus = mutableStateListOf<MessageMenu>()
    val interactionSource = MutableInteractionSource()
    var artwork by mutableStateOf<Any?>(null)

    init {
        scope.launch {
            find()
            download()
        }
        scope.launch {
            findUser()
        }
        scope.launch {
            if (!message.text.isNullOrBlank()) {
                menus.addIfAbsent(MessageMenu.Copy)
            }
//            menus.add(MessageMenu.Edit)
            if (message.uid == repository.ktor.uid) {
                menus.add(MessageMenu.Delete)
            }
//            menus.add(MessageMenu.Forward)
            menus.add(MessageMenu.Reply)
        }
        scope.launch {
            playFlow.collect {
                if (it != message.mid) pause()
            }
        }
    }


    override fun play() {
        playFlow.value = message.mid
        super.play()
    }

    suspend fun findUser() {
        if (user != null) return
        repository.getUser(message.uid).onSuccess {
            user = it.data
        }
    }

    suspend fun find() {
        if (path != null) return
        path = repository.findResource(message.identification())?.path?.also {
            if (message.type == MessageType.AUDIO) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(it)
                retriever.embeddedPicture?.let {
                    artwork = BitmapFactory.decodeByteArray(it, 0, it.size)
                }
            }
            if (message.type == MessageType.VOICE) {
                onClose()
                player = MediaPlayer().apply {
                    setDataSource(it)
                    setOnCompletionListener {
                        it.seekTo(0)
                        this@DownloadMessageItem.isPlaying = false
                    }
                    prepare()
                }
            }
        }
    }

    fun download(auto: Boolean = true) {
        if (path != null) return
        if (downloading) return
        downloadJob?.cancel()
        downloadJob = scope.launch {
            when (attachment) {
                is DocumentAttachment -> {
                    if (auto && attachment.size >= 1024 * 1024) {
                        return@launch
                    }
                    downloading = true
                    when {
                        attachment.url != null -> {
                            repository.download(
                                message.identification(),
                                attachment.url!!,
                                onSuccess = {
                                    downloading = false
                                    scope.launch {
                                        find()
                                    }
                                }, onFailure = {
                                    downloading = false
                                }
                            ) { sent, length ->
                                length?.let { progress = sent.toFloat() / it.toFloat() }
                            }
                        }

                        attachment.bucket != null && attachment.`object` != null -> {
                            repository.download(
                                id = message.identification(),
                                bucket = attachment.bucket!!,
                                `object` = attachment.`object`!!,
                                onSuccess = {
                                    downloading = false
                                    scope.launch {
                                        find()
                                    }
                                }, onFailure = {
                                    downloading = false
                                },
                                onProgress = ::progress::set
                            )
                        }
                    }
                }

                is LocationAttachment -> {

                }

                is WebRTCAttachment -> {

                }

                else -> {

                }
            }
        }
    }

    fun cancel() {
        downloadJob?.cancel()
        downloading = false
        progress = 0f
    }

    suspend fun emitInteraction() {
        val press = PressInteraction.Press(Offset.Zero)
        interactionSource.emit(press)
        delay(500)
        interactionSource.emit(PressInteraction.Release(press))
    }
}
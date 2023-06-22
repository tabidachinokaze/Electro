package cn.tabidachi.electro.ui.common

import android.Manifest
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Pair
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.ContentInfoCompat
import androidx.core.view.OnReceiveContentListener
import androidx.hilt.navigation.compose.hiltViewModel
import cn.tabidachi.electro.coil.BlurTransformation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.LocationActivity
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.database.entity.Message
import cn.tabidachi.electro.data.database.entity.MessageSendRequest
import cn.tabidachi.electro.data.database.entity.MessageType
import cn.tabidachi.electro.data.database.entity.Path
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.ext.checkPermission
import cn.tabidachi.electro.ext.longTimeFormat
import cn.tabidachi.electro.ext.openableColumns
import cn.tabidachi.electro.model.Playable
import cn.tabidachi.electro.model.attachment.Attachment
import cn.tabidachi.electro.model.attachment.AudioAttachment
import cn.tabidachi.electro.model.attachment.FileAttachment
import cn.tabidachi.electro.model.attachment.ImageAttachment
import cn.tabidachi.electro.model.attachment.LocationAttachment
import cn.tabidachi.electro.model.attachment.VideoAttachment
import cn.tabidachi.electro.model.attachment.VoiceAttachment
import cn.tabidachi.electro.model.attachment.WebRTCAttachment
import cn.tabidachi.electro.model.attachment.convert
import cn.tabidachi.electro.model.attachment.deserialize
import cn.tabidachi.electro.model.attachment.serialize
import coil.executeBlocking
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Scale
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.PoiItemV2
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.http.ContentType
import io.ktor.util.generateNonce
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import javax.inject.Inject

@Composable
fun BottomMessageField(
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false,
    attachments: List<Attachment>,
    onAttachmentRemove: (Attachment) -> Unit,
    text: String,
    onTextValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onSelect: (AttachmentType) -> Unit
) {
    Surface(
        modifier = modifier,
        tonalElevation = 2.dp
    ) {
        Column {
            AttachmentRow(
                attachments = attachments,
                onAttachmentRemove = onAttachmentRemove,
                modifier = Modifier.padding(top = 8.dp)
            )
            MessageTextField(
                text = text,
                onTextValueChange = onTextValueChange
            )
            AttachmentSelector(
                sendButtonEnabled = attachments.isNotEmpty() || text.isNotEmpty(),
                isProcessing = isProcessing,
                onSend = onSend,
                onSelect = onSelect,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomMessageField(
    modifier: Modifier = Modifier,
    viewModel: MessageViewModel = hiltViewModel(),
    sessionIdRequest: suspend () -> Long?,
    replyRequest: suspend () -> Long?,
    replyContent: @Composable () -> Unit,
    onSuccess: () -> Unit,
) {
    val viewState by viewModel.viewState.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = {
            it.forEach(viewModel::onFileAttachment)
        }
    )
    val context = LocalContext.current
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            if (it.resultCode == 114514) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    viewModel.sendLocationMessage(
                        sessionIdRequest,
                        it.data?.extras?.getParcelable("attachment", LocationAttachment::class.java)
                    )
                }
            }
        }
    )
    Surface(
        modifier = modifier,
        tonalElevation = 2.dp
    ) {
        Column {
            replyContent()
            AttachmentRow(
                attachments = viewState.attachments,
                onAttachmentRemove = viewModel::onAttachmentRemove,
                modifier = Modifier.padding(top = 8.dp)
            )
            AnimatedContent(targetState = viewState.isRecording, label = "") {
                if (it) {
                    Recording(
                        duration = viewState.duration,
                        onCancel = viewModel::onRecordStop,
                        onCompleted = {
                            viewModel.onRecordCompleted(sessionIdRequest, replyRequest, onSuccess)
                        }, modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(vertical = 8.dp)
                    )
                } else {
                    MessageTextField(
                        text = viewState.text,
                        onTextValueChange = viewModel::onTextValueChange
                    )
                }
            }
            AttachmentSelector(
                sendButtonEnabled = viewState.attachments.isNotEmpty() || viewState.text.isNotEmpty(),
                isProcessing = viewState.isProcessing,
                onSend = {
                    viewModel.onSend(
                        sessionIdRequest = sessionIdRequest,
                        replyRequest = replyRequest,
                        onSuccess = onSuccess
                    )
                },
                onSelect = {
                    when (it) {
                        AttachmentType.Audio -> launcher.launch(ContentType.Audio.Any.toString())
                        AttachmentType.Video -> launcher.launch(ContentType.Video.Any.toString())
                        AttachmentType.Image -> launcher.launch(ContentType.Image.Any.toString())
                        AttachmentType.Location ->
                            locationLauncher.launch(Intent(context, LocationActivity::class.java))

                        AttachmentType.File -> launcher.launch(ContentType.Any.toString())
                    }
                },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                isRecording = viewState.isRecording,
                onRecording = viewModel::onRecording,
            )
        }
    }
}

@Composable
fun Recording(
    duration: Long,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onCompleted: () -> Unit
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Rounded.RadioButtonChecked,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(text = duration.longTimeFormat())
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onCancel) {
            Text(text = stringResource(id = R.string.cancel))
        }
        TextButton(onClick = onCompleted) {
            Text(text = stringResource(id = R.string.send))
        }
    }
}

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val application: Application,
    private val repository: Repository,
    private val ktor: Ktor,
) : ViewModel(), OnReceiveContentListener {
    private val _viewState = MutableStateFlow(MessageViewState())
    val viewState = _viewState.asStateFlow()
    private var recorder: MediaRecorder? = null
    private var recordedFile: File? = null
    private var startTime: Long = 0
    private var stopTime: Long = 0
    private var recordingJob: Job? = null

    fun onTextValueChange(text: String) {
        _viewState.update { it.copy(text = text) }
    }

    fun onEditing(message: Message) {
        val attachment = Attachment.deserialize(message.type, message.attachment)
        _viewState.update {
            it.copy(
                text = message.text ?: "",
                isEditing = true
            )
        }
        attachment?.let(_viewState.value.attachments::add)
    }

    fun onRecording() {
        Playable.playFlow.value = 0
        if (_viewState.value.isRecording) return
        onRecordStop()
        if (!application.checkPermission(Manifest.permission.RECORD_AUDIO)) return
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(application)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            recordedFile = File(application.getExternalFilesDir(null), generateNonce())
            setOutputFile(recordedFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            kotlin.runCatching {
                prepare()
                start()
                startTime = System.currentTimeMillis()
                _viewState.update { it.copy(isRecording = true) }
                Playable.enabled = false
                recordingJob = viewModelScope.launch {
                    while (_viewState.value.isRecording) {
                        _viewState.update { it.copy(duration = System.currentTimeMillis() - startTime) }
                        delay(250)
                    }
                    _viewState.update { it.copy(duration = 0) }
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun onRecordCompleted(
        sessionIdRequest: suspend () -> Long?,
        replyRequest: suspend () -> Long?,
        onSuccess: () -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        onRecordStop()
        val attachment = VoiceAttachment {
            application.contentResolver.getType(Uri.fromFile(recordedFile))?.let(::contentType::set)
            recordedFile?.length()?.let(::size::set)
            duration = stopTime - startTime
        }
        val reply = replyRequest()
        val sid = sessionIdRequest() ?: return@launch
        val message = createAttachmentMessage(sid, reply, attachment)
        repository.saveResource(Path(message.identification(), recordedFile.toString()))
        repository.addMessageRequest(message)
        onSuccess()
    }

    fun onRecordStop() {
        stopTime = System.currentTimeMillis()
        recordingJob?.cancel()
        recorder?.stop()
        recorder?.release()
        recorder = null
        _viewState.update { it.copy(isRecording = false) }
        Playable.enabled = true
    }

    fun onFileAttachment(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        if (_viewState.value.attachments.any {
                when (val attachment = it) {
                    is AudioAttachment -> attachment.uri == uri.toString()
                    is FileAttachment -> attachment.uri == uri.toString()
                    is ImageAttachment -> attachment.uri == uri.toString()
                    is VideoAttachment -> attachment.uri == uri.toString()
                    is VoiceAttachment -> attachment.uri == uri.toString()
                    is LocationAttachment -> false
                    is WebRTCAttachment -> false
                }
            }) {
            return@launch
        }
        val type = application.contentResolver.getType(uri)
        val uriDetail = uri.openableColumns(application.contentResolver).getOrNull()
        val uriString = uri.toString()
        when {
            type == null -> {
                FileAttachment {
                    contentType = "*/*"
                    this.uri = uriString
                    uriDetail?.name?.let(this::filename::set)
                    uriDetail?.size?.let(this::size::set)
                }
            }

            ContentType.parse(type).match(ContentType.Audio.Any) -> {
                AudioAttachment {
                    contentType = type
                    this.uri = uriString
                    uriDetail?.name?.let(this::filename::set)
                    uriDetail?.size?.let(this::size::set)
                    kotlin.runCatching {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(application, uri)
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                            ?.let(::title::set)
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                            ?.let(::artist::set)
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            ?.toLongOrNull()?.let(::duration::set)
                        retriever.embeddedPicture?.let(::loadInSize)?.let(::blur)?.let(::blur)
                            ?.let(::blur)?.let(::quality)?.let(::artwork::set)
                    }.onFailure {
                        it.printStackTrace()
                    }
                }
            }

            ContentType.parse(type).match(ContentType.Image.Any) -> {
                val bounds = getImageBounds(uri)
                ImageAttachment {
                    contentType = type
                    this.uri = uriString
                    uriDetail?.name?.let(this::filename::set)
                    uriDetail?.size?.let(this::size::set)
                    width = bounds.outWidth
                    height = bounds.outHeight
                    thumb = uri.let(::loadInSize).let(::blur).let(::blur).let(::blur).let(::blur)
                        .let(::quality)
                }
            }

            ContentType.parse(type).match(ContentType.Video.Any) -> {
                VideoAttachment {
                    contentType = type
                    this.uri = uriString
                    uriDetail?.name?.let(this::filename::set)
                    uriDetail?.size?.let(this::size::set)
                    kotlin.runCatching {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(application, uri)
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                            ?.toIntOrNull()?.let(this::width::set)
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                            ?.toIntOrNull()?.let(this::height::set)
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            ?.toLongOrNull()?.let(this::duration::set)
                        this.thumb =
                            (retriever.embeddedPicture ?: retriever.frameAtTime)?.let(::loadInSize)
                                ?.let(::blur)?.let(::blur)?.let(::blur)?.let(::quality)
                        retriever.release()
                    }.onFailure {
                        it.printStackTrace()
                    }
                }
            }

            else -> {
                FileAttachment {
                    contentType = type
                    this.uri = uriString
                    uriDetail?.name?.let(this::filename::set)
                    uriDetail?.size?.let(this::size::set)
                }
            }
        }.let { attachment ->
            _viewState.value.attachments.add(attachment)
        }
    }

    private fun getImageBounds(uri: Uri): BitmapFactory.Options {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        application.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
        return options
    }

    private fun loadInSize(data: Any): Bitmap {
        val request = ImageRequest.Builder(application)
            .data(data)
            .size(320, 320)
            .scale(Scale.FIT)
            .build()
        return (application.imageLoader.executeBlocking(request).drawable as BitmapDrawable).bitmap.copy(
            Bitmap.Config.ARGB_8888,
            true
        )
    }

    private fun blur(bitmap: Bitmap): Bitmap {
        val request = ImageRequest.Builder(application)
            .data(bitmap)
            .transformations(BlurTransformation(25f, 1f))
            .build()
        return (application.imageLoader.executeBlocking(request).drawable.also {
            println("Drawable $it")
        } as BitmapDrawable).bitmap.copy(
            Bitmap.Config.ARGB_8888,
            true
        )
    }

    private fun quality(bitmap: Bitmap): ByteArray {
        return ByteArrayOutputStream().use {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 90, it)
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }
            it.toByteArray()
        }
    }

    fun onAttachmentRemove(attachment: Attachment) {
        _viewState.value.attachments.remove(attachment)
    }

    private fun changeProcessingState(value: Boolean) {
        _viewState.update { it.copy(isProcessing = value) }
    }

    private fun createTextMessage(sid: Long, reply: Long?, text: String): MessageSendRequest {
        return MessageSendRequest(
            UUID.randomUUID().toString(),
            sid,
            ktor.uid,
            null,
            reply,
            type = MessageType.TEXT,
            text,
            null,
            System.currentTimeMillis()
        )
    }

    private fun createMessage(
        sid: Long,
        reply: Long?,
        text: String,
        attachment: Attachment
    ): MessageSendRequest {
        return MessageSendRequest(
            UUID.randomUUID().toString(),
            sid,
            ktor.uid,
            null,
            reply,
            type = attachment.convert(),
            text,
            attachment.serialize(),
            System.currentTimeMillis()
        )
    }

    private fun createAttachmentMessage(
        sid: Long,
        reply: Long?,
        attachment: Attachment
    ): MessageSendRequest {
        return MessageSendRequest(
            UUID.randomUUID().toString(),
            sid,
            ktor.uid,
            null,
            reply,
            type = attachment.convert(),
            null,
            attachment.serialize(),
            System.currentTimeMillis()
        )
    }

    private fun cleanMessageField() {
        _viewState.update { it.copy(text = "") }
        _viewState.value.attachments.clear()
    }

    fun onSend(
        sessionIdRequest: suspend () -> Long?,
        replyRequest: suspend () -> Long?,
        onSuccess: () -> Unit
    ) {
        if (_viewState.value.isProcessing) return
        viewModelScope.launch {
            changeProcessingState(true)
            val attachments = _viewState.value.attachments
            val reply = replyRequest()
            val text = _viewState.value.text
            val sid = sessionIdRequest() ?: return@launch
            when (attachments.size) {
                0 -> {
                    createTextMessage(sid, reply, text).let(::listOf)
                }

                1 -> {
                    createMessage(sid, reply, text, attachments.first()).let(::listOf)
                }

                else -> {
                    mutableListOf<MessageSendRequest>().apply {
                        createAttachmentMessage(sid, reply, attachments.first()).let(::add)
                        attachments.drop(1).map {
                            createAttachmentMessage(sid, null, it)
                        }.let(::addAll)
                        text.takeIf { it.isNotBlank() }?.let {
                            createTextMessage(sid, null, text)
                        }?.let(::add)
                    }
                }
            }.forEach {
                repository.addMessageRequest(it)
            }.run {
                cleanMessageField()
                onSuccess()
            }
        }.run {
            changeProcessingState(false)
        }
    }

    fun sendLocationMessage(sessionIdRequest: suspend () -> Long?, poiItem: PoiItemV2) =
        viewModelScope.launch {
            val sid = sessionIdRequest() ?: return@launch
            val attachment = LocationAttachment {
                latitude = poiItem.latLonPoint.latitude
                longitude = poiItem.latLonPoint.longitude
                title = poiItem.title
                city = poiItem.cityName
                address = poiItem.adName
                snippet = poiItem.snippet
            }
            repository.addMessageRequest(createAttachmentMessage(sid, null, attachment))
        }

    fun sendLocationMessage(sessionIdRequest: suspend () -> Long?, latLng: LatLng) =
        viewModelScope.launch {
            val sid = sessionIdRequest() ?: return@launch
            val attachment = LocationAttachment {
                latitude = latLng.latitude
                longitude = latLng.longitude
            }
            repository.addMessageRequest(createAttachmentMessage(sid, null, attachment))
        }

    fun sendLocationMessage(
        sessionIdRequest: suspend () -> Long?,
        attachment: LocationAttachment?
    ) = viewModelScope.launch {
        val sid = sessionIdRequest() ?: return@launch
        val attachment = attachment ?: return@launch
        repository.addMessageRequest(createAttachmentMessage(sid, null, attachment))
    }

    override fun onReceiveContent(view: View, payload: ContentInfoCompat): ContentInfoCompat? {
        println(payload)
        val pair: Pair<ContentInfoCompat, ContentInfoCompat> = payload.partition {
            it.uri != null
        }
        val uriContent = pair.first
        val remaining = pair.second
        if (uriContent != null) {
            val clip = uriContent.clip
            for (i in 0..clip.itemCount) {
                val uri = clip.getItemAt(i)
                println(uri)
            }
        }
        return remaining
    }
}

data class MessageViewState(
    val text: String = "",
    val attachments: SnapshotStateList<Attachment> = mutableStateListOf(),
    val isProcessing: Boolean = false,
    val isRecording: Boolean = false,
    val duration: Long = 0,
    val isEditing: Boolean = false,
    val editMessage: Message? = null
)






















package cn.tabidachi.electro

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.ext.longTimeFormat
import cn.tabidachi.electro.ui.theme.ElectroTheme
import coil3.compose.AsyncImage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AudioPreviewActivity : ComponentActivity() {
    private val vm: AudioPreviewViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.data?.let(vm::onUri)
        setContent {
            ElectroTheme {
                AudioPreviewDialog(
                    visible = true,
                    title = vm.title,
                    artist = vm.artist,
                    artwork = vm.artwork,
                    isPlaying = vm.isPlaying,
                    onPlayPause = vm::playPause,
                    duration = vm.duration,
                    progress = vm.progress,
                    onSlide = vm::onSlide,
                    onSlideFinished = vm::setProgress,
                    onDismissRequest = ::finishAndRemoveTask
                )
            }
        }
    }
}

@HiltViewModel
class AudioPreviewViewModel @Inject constructor(
    private val application: Application,
) : ViewModel() {
    var artwork by mutableStateOf<Bitmap?>(null)
    var title by mutableStateOf("")
    var artist by mutableStateOf("")
    var duration by mutableStateOf(0L)
    var progress by mutableStateOf(0L)
    var isPlaying by mutableStateOf(false)
    private var sliding by mutableStateOf(false)
    private var player: MediaPlayer? = null
    private var initialized: Boolean = false
    fun onUri(uri: Uri) = viewModelScope.launch {
        if (initialized) return@launch
        onClose()
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(application, uri)
        retriever.embeddedPicture?.let {
            artwork = BitmapFactory.decodeByteArray(it, 0, it.size)
        }
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.let(::title::set)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.let(::artist::set)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            ?.let(::duration::set)
        player = MediaPlayer().apply {
            setDataSource(application, uri)
            setOnCompletionListener {
                it.seekTo(0)
                this@AudioPreviewViewModel.isPlaying = false
            }
            prepare()
        }
        play()
        initialized = true
    }

    private var job: Job? = null

    private fun play() {
        player?.let { player ->
            player.start()
            isPlaying = true
            job?.cancel()
            job = viewModelScope.launch {
                while (isPlaying) {
                    if (!sliding) {
                        progress = player.currentPosition.toLong()
                    }
                    delay(200)
                }
                progress = player.currentPosition.toLong()
                isPlaying = false
            }
        }
    }

    private fun pause() {
        player?.pause()
        job?.cancel()
        isPlaying = false
    }

    fun playPause() = viewModelScope.launch {
        player?.let { player ->
            if (player.isPlaying) {
                pause()
            } else {
                play()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        onClose()
    }

    private fun onClose() {
        job?.cancel()
        player?.stop()
        player?.release()
    }

    fun onSlide(progress: Float) = viewModelScope.launch {
        sliding = true
        this@AudioPreviewViewModel.progress = progress.toLong()
    }

    fun setProgress(progress: Float) = viewModelScope.launch {
        sliding = false
        player?.seekTo(progress.toInt())
    }
}

@Composable
fun AudioPreviewDialog(
    visible: Boolean,
    title: String,
    artist: String,
    artwork: Any?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    duration: Long,
    progress: Long,
    onSlide: (Float) -> Unit,
    onSlideFinished: (Float) -> Unit,
    onDismissRequest: () -> Unit = {}
) {
    if (visible) Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            shape = AlertDialogDefaults.shape
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onPlayPause,
                    ) {
                        if (artwork != null) {
                            Surface(
                                shape = CircleShape,
                                modifier = Modifier.aspectRatio(1f)
                            ) {
                                AsyncImage(
                                    model = artwork,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.25f),
                            modifier = Modifier.fillMaxSize()
                        ) {

                        }
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Slider(
                    value = progress.toFloat(),
                    valueRange = 0f..duration.toFloat(),
                    onValueChange = onSlide,
                    onValueChangeFinished = {
                        onSlideFinished(progress.toFloat())
                    }
                )
                Row {
                    Text(
                        text = progress.longTimeFormat(),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = duration.longTimeFormat(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
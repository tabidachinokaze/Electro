package cn.tabidachi.electro.model

import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

open class Playable(
    open val scope: CoroutineScope
) {
    var player: MediaPlayer? = null
    var isPlaying by mutableStateOf(false)
    var playingJob: Job? = null
    var progress by mutableStateOf(0F)
    open fun play() {
        if (!enabled) return
        player?.let { player ->
            player.start()
            isPlaying = true
            playingJob?.cancel()
            playingJob = scope.launch {
                while (isPlaying) {
                    progress = player.currentPosition.toFloat()
                    delay(200)
                }
                progress = player.currentPosition.toFloat()
                isPlaying = false
            }
        }
    }

    fun pause() {
        if (player?.isPlaying == true) {
            player?.pause()
            playingJob?.cancel()
            isPlaying = false
        }
    }

    fun playPause() = scope.launch {
        player?.let { player ->
            if (player.isPlaying) {
                pause()
            } else {
                play()
            }
        }
    }

    fun onSlide(progress: Float) {
        this.progress = progress
        player?.seekTo(progress.toInt())
    }

    fun onClose() {
        playingJob?.cancel()
        player?.stop()
        player?.release()
    }

    companion object {
        val playFlow = MutableStateFlow<Long>(0)
        var enabled: Boolean = true
    }
}
package cn.tabidachi.electro

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.media.AudioManager
import android.util.Log
import androidx.core.content.getSystemService
import cn.tabidachi.electro.ui.call.PeerConnectionObserver
import cn.tabidachi.electro.ui.call.RemoteSessionDescription
import cn.tabidachi.electro.ui.call.SessionDescriptionObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.webrtc.AudioSource
import org.webrtc.Camera2Capturer
import org.webrtc.Camera2Enumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.EglBase
import org.webrtc.HardwareVideoEncoderFactory
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Factory(
    private val context: Context
) : PeerConnectionObserver() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        val options =
            PeerConnectionFactory.InitializationOptions.builder(context)
                .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    val eglBaseContext = EglBase.create().eglBaseContext
    private val config = PeerConnection.RTCConfiguration(
        listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
    ).apply {
        sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
    }
    val factory = PeerConnectionFactory.builder()
        .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
        .setVideoEncoderFactory(HardwareVideoEncoderFactory(eglBaseContext, true, true))
        .createPeerConnectionFactory()

    // <video>
    private val cameraManager by lazy { context.getSystemService<CameraManager>()!! }
    private val videoCapturer: VideoCapturer by lazy {
        cameraManager.cameraIdList.filter {
            cameraManager.getCameraCharacteristics(it)
                .get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_FRONT
        }.map {
            Camera2Capturer(context, it, null)
        }.first()
    }
    private val cameraEnumerator by lazy { Camera2Enumerator(context) }
    private val resolution by lazy {
        val frontCamera = cameraEnumerator.deviceNames.first { cameraName ->
            cameraEnumerator.isFrontFacing(cameraName)
        }
        val supportedFormats = cameraEnumerator.getSupportedFormats(frontCamera) ?: emptyList()
        supportedFormats.firstOrNull {
            (it.width == 720 || it.width == 480 || it.width == 360)
        } ?: error("There is no matched resolution!")
    }
    private val surfaceTextureHelper by lazy {
        SurfaceTextureHelper.create(
            "SurfaceTextureHelper",
            eglBaseContext
        )
    }
    private val videoSource: VideoSource by lazy {
        factory.createVideoSource(videoCapturer.isScreencast).apply {
            videoCapturer.initialize(surfaceTextureHelper, context, this.capturerObserver)
            videoCapturer.startCapture(resolution.width, resolution.height, 30)
        }
    }
    private val videoTrack: VideoTrack by lazy {
        factory.createVideoTrack("Video${UUID.randomUUID()}", videoSource)
    }
    private val _localVideoTrack = MutableSharedFlow<VideoTrack?>()
    val localVideoTrack = _localVideoTrack.asSharedFlow()
    private val _remoteVideoTrack = MutableSharedFlow<VideoTrack?>()
    val remoteVideoTrack = _remoteVideoTrack.asSharedFlow()

    // </video>
    // <audio>
    private val audioSource: AudioSource by lazy {
        val items = listOf(
            "googEchoCancellation" to true,
            "googAutoGainControl" to true,
            "googHighpassFilter" to true,
            "googNoiseSuppression" to true,
            "googTypingNoiseDetection" to true,
        ).map { MediaConstraints.KeyValuePair(it.first, it.second.toString()) }
        val constraints = MediaConstraints().apply {
            with(optional) {
                add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
                addAll(items)
            }
        }
        factory.createAudioSource(constraints)
    }
    private val audioTrack by lazy {
        factory.createAudioTrack("Audio${UUID.randomUUID()}", audioSource)
    }

    // </audio>
    private val audioManager by lazy { context.getSystemService<AudioManager>() }
    fun setupAudio() {
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
    }

    fun setSpeakerphoneOn(isSpeakerphoneOn: Boolean) {
        audioManager?.isSpeakerphoneOn = isSpeakerphoneOn
    }

    fun setupLocalVideoTrack(peerConnection: PeerConnection) {
        peerConnection.addTrack(videoTrack)
        peerConnection.addTrack(audioTrack)
        scope.launch {
            _localVideoTrack.emit(videoTrack)
        }
    }

    fun createPeerConnection(
        callback: Callback
    ): Connection {
        val connection = factory.createPeerConnection(
            config, object : PeerConnectionObserver() {
                override fun onIceCandidate(candidate: IceCandidate?) {
                    super.onIceCandidate(candidate)
                    candidate?.let(callback.onIceCandidate)
                }

                override fun onTrack(transceiver: RtpTransceiver?) {
                    super.onTrack(transceiver)
                    transceiver?.receiver?.track()
                        ?.takeIf { it.kind() == MediaStreamTrack.VIDEO_TRACK_KIND }?.let {
                        scope.launch {
                            _remoteVideoTrack.emit(it as VideoTrack)
                        }
                    }
                    transceiver?.let(callback.onTrack)
                }

                override fun onRenegotiationNeeded() {
                    super.onRenegotiationNeeded()
                    callback.onRenegotiationNeeded()
                }
            }
        )!!
        return Connection(connection)
    }

    fun switchCamera() {
        (videoCapturer as Camera2Capturer).switchCamera(null)
    }

    fun microphone(enabled: Boolean) {
        audioManager?.isMicrophoneMute = !enabled
        audioTrack.setEnabled(enabled)
    }

    fun camera(enabled: Boolean) {
        videoTrack.setEnabled(enabled)
        scope.launch {
            when (videoTrack.enabled()) {
                true -> videoCapturer.startCapture(resolution.width, resolution.height, 30)
                false -> {
                    delay(1000)
                    videoCapturer.stopCapture()
                }
            }
        }
    }

    fun disconnect() {
        _remoteVideoTrack.replayCache.forEach {
            it?.dispose()
        }
        _localVideoTrack.replayCache.forEach {
            it?.dispose()
        }
        videoTrack.dispose()
        audioTrack.dispose()
        videoCapturer.stopCapture()
        videoCapturer.dispose()
    }

    data class Callback(
        val onDescription: (SessionDescription) -> Unit,
        val onIceCandidate: (IceCandidate) -> Unit,
        val onTrack: (RtpTransceiver) -> Unit,
        val onRenegotiationNeeded: () -> Unit
    )
}

class Connection(
    val connection: PeerConnection,
) : SessionDescriptionObserver() {
    suspend fun offer() = getDescription {
        connection.createOffer(it, MediaConstraints())
    }.onSuccess(::setLocalDescription)

    suspend fun answer() = getDescription {
        connection.createAnswer(it, MediaConstraints())
    }.onSuccess(::setLocalDescription)

    private fun setLocalDescription(sessionDescription: SessionDescription) {
        connection.setLocalDescription(this, sessionDescription)
    }

    fun setRemoteDescription(sessionDescription: SessionDescription) {
        connection.setRemoteDescription(this, sessionDescription)
    }

    fun addIceCandidate(iceCandidate: IceCandidate) {
        connection.addIceCandidate(iceCandidate)
    }
}


suspend inline fun getDescription(
    crossinline callback: (SdpObserver) -> Unit
): Result<SessionDescription> = suspendCoroutine {
    val TAG = "getDescription"
    object : SdpObserver {
        override fun onCreateSuccess(description: SessionDescription?) = kotlin.runCatching {
            Log.d(
                TAG, "onCreateSuccess: ${
                    description?.let { it1 ->
                        RemoteSessionDescription(
                            it1.type, description.description
                        )
                    }
                }"
            )
            description ?: throw RuntimeException("SessionDescription is null!")
        }.let(it::resume)

        override fun onSetSuccess() {
            Log.d(TAG, "onSetSuccess: ")
        }

        override fun onCreateFailure(p0: String?) =
            Result.failure<SessionDescription>(RuntimeException(p0)).let(it::resume).also {
                Log.d(TAG, "onCreateFailure: $p0")
            }

        override fun onSetFailure(message: String?) = Unit.also {
            Log.d(TAG, "onSetFailure: $message")
        }
    }.let(callback)
}

suspend inline fun setDescription(
    crossinline callback: (SdpObserver) -> Unit
): Result<Unit> = suspendCoroutine {
    val TAG = "setDescription"
    object : SdpObserver {
        override fun onCreateSuccess(description: SessionDescription?) {
            Log.d(
                TAG, "onCreateSuccess: ${
                    description?.let { it1 ->
                        RemoteSessionDescription(
                            it1.type, description.description
                        )
                    }
                }"
            )
        }

        override fun onSetSuccess() {
            Log.d(TAG, "onSetSuccess: ")
            Result.success(Unit).let(it::resume)
        }

        override fun onCreateFailure(p0: String?) {
            Log.d(TAG, "onCreateFailure: $p0")
        }

        override fun onSetFailure(message: String?) {
            Log.d(TAG, "onSetFailure: $message")
            Result.failure<Unit>(RuntimeException(message)).let(it::resume)
        }

    }.let(callback)
}
package cn.tabidachi.electro.ui.call

import android.Manifest
import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.ANSWER_ACTION
import cn.tabidachi.electro.Factory
import cn.tabidachi.electro.OFFER_ACTION
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.network.ElectroWebSocket
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.data.network.MessageType
import cn.tabidachi.electro.ext.checkPermission
import cn.tabidachi.electro.model.WebSocketMessage
import cn.tabidachi.electro.model.header
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.webrtc.IceCandidate
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val application: Application,
    private val repository: Repository,
    val ktor: Ktor
) : ViewModel() {
    private val _viewState = MutableStateFlow(CallViewState())
    val viewState = _viewState.asStateFlow()
    private var isCalled = false
    val factory = Factory(application)
    private val TAG = "CallViewModel"

    private lateinit var ws: ElectroWebSocket
    private val _onCallEnd = MutableStateFlow(false)
    val onCallEnd = _onCallEnd.asStateFlow()

    private val connection = factory.createPeerConnection(
        Factory.Callback(
            onDescription = {
                Log.d(TAG, "onDescription: $it")
            }, onIceCandidate = {
                sendIce(
                    messageConvert(
                        MessageType.WebRTC.Ice,
                        RemoteIceCandidate(it).let(Json::encodeToString)
                    )
                )
            }, onTrack = {
                Log.d(TAG, "onTrack: $it")
            }, onRenegotiationNeeded = {

            }
        )
    )


    val offerSdp = mutableStateOf<RemoteSessionDescription?>(null)
    val answerSdp = mutableStateOf<RemoteSessionDescription?>(null)
    val localIce = mutableStateOf<RemoteIceCandidate?>(null)
    val remoteIce = mutableStateOf<RemoteIceCandidate?>(null)

    private fun messageConvert(type: MessageType, message: String): WebSocketMessage {
        return WebSocketMessage {
            header = header {
                this.type = type.toString()
            }
            body = WebRTCMessage(_viewState.value.target!!, message).let(Json::encodeToString)
                .toByteArray()
        }
    }

    private fun sendIce(webSocketMessage: WebSocketMessage) {
        viewModelScope.launch {
            ws.connected.takeWhile { !it }.collect()
            ws.send(
                webSocketMessage, onFailure = {
                    sendIce(webSocketMessage)
                }
            )
        }
    }

    private fun initWebSocket() {
        viewModelScope.launch {
            ws.onWebSocketMessage.collect {
                when (it.header.type) {
                    MessageType.WebRTC.Response.toString() -> {
                        launch {
                            createOffer()
                            delay(1000)
                            createOffer()
                        }
                    }

                    MessageType.WebRTC.Offer.toString() -> {
                        val (_, message) = String(it.body).let<String, WebRTCMessage>(Json::decodeFromString)
                        val description =
                            message.let<String, RemoteSessionDescription>(Json::decodeFromString)
                        offerSdp.value = description
                        connection.setRemoteDescription(description.toLocal())
                        connection.answer().onSuccess {
                            sendSdpToRemote(MessageType.WebRTC.Answer, RemoteSessionDescription(it))
                        }
                    }

                    MessageType.WebRTC.Answer.toString() -> {
                        val (_, message) = String(it.body).let<String, WebRTCMessage>(Json::decodeFromString)
                        val description =
                            message.let<String, RemoteSessionDescription>(Json::decodeFromString)
                        answerSdp.value = description
                        connection.setRemoteDescription(description.toLocal())
                    }

                    MessageType.WebRTC.Ice.toString() -> {
                        val (_, message) = String(it.body)
                            .let<String, WebRTCMessage>(Json::decodeFromString)
                        val candidate =
                            message.let<String, RemoteIceCandidate>(Json::decodeFromString)
                        remoteIce.value = candidate
                        connection.addIceCandidate(candidate.toLocal())
                    }

                    MessageType.WebRTC.End.toString() -> {
                        _onCallEnd.value = true
                    }
                }
            }
        }
    }

    private var newKtor: Ktor? = null
    fun init(offer: Long, answer: Long, action: String) {
        if (isCalled) return
        _viewState.update {
            it.copy(
                target = when (action) {
                    OFFER_ACTION -> answer
                    else -> offer
                }
            )
        }
        factory.setupAudio()
        factory.setupLocalVideoTrack(connection.connection)
        viewModelScope.launch {
            when (action) {
                OFFER_ACTION -> {
                    repository.getUser(answer).onSuccess {
                        it.data?.let { user ->
                            _viewState.update { it.copy(user = user) }
                        }
                    }
                    ws = ktor.ws
                }

                ANSWER_ACTION -> {
                    repository.getUser(offer).onSuccess {
                        it.data?.let { user ->
                            _viewState.update { it.copy(user = user) }
                        }
                    }
                    val account = repository.findAccount(answer) ?: return@launch
                    println(account)
                    newKtor = Ktor(application).apply {
                        this.token = account.token
                        this.uid = account.uid
                    }
                    ws = newKtor!!.ws
                }
            }
            ws.pingPong.isCall = true
            initWebSocket()
            ws.connected.takeWhile { !it }.collect()
            if (!isCalled) {
                when (action) {
                    OFFER_ACTION -> request(answer)
                    ANSWER_ACTION -> response(offer)
                }
            }
        }
    }

    private fun request(target: Long) {
        val message = WebSocketMessage {
            header = header {
                type = MessageType.WebRTC.Request.toString()
            }
            body = "$target".toByteArray()
        }
        ws.send(
            message,
            onSuccess = {
                isCalled = true
            }, onFailure = {
                viewModelScope.launch {
                    delay(2000)
                    request(target)
                }
            }
        )
    }

    private fun response(target: Long) {
        val message = WebSocketMessage {
            header = header {
                type = MessageType.WebRTC.Response.toString()
            }
            body = "$target".toByteArray()
        }
        ws.send(
            message, onSuccess = {
                isCalled = true
            }, onFailure = {
                viewModelScope.launch {
                    delay(2000)
                    response(target)
                }
            }
        )
    }

    fun onMicEnabled(enabled: Boolean) {
        factory.microphone(enabled)
        _viewState.update { it.copy(mic = enabled) }
    }

    fun onCameraEnabled(enabled: Boolean) {
        factory.camera(enabled)
        _viewState.update { it.copy(camera = enabled) }
    }

    fun onCallEnd(callback: () -> Unit) {
        ws.send(
            WebSocketMessage {
                header = header {
                    type = MessageType.WebRTC.End.toString()
                }
                body = "${_viewState.value.target}".toByteArray()
            }
        )
        stop()
        callback()
    }

    fun stop() {
        factory.disconnect()
        connection.connection.dispose()
    }

    fun flipCamera() {
        factory.switchCamera()
    }

    private fun sendSdpToRemote(type: MessageType, description: RemoteSessionDescription) {
        ws.send(
            messageConvert(
                type,
                description.let(Json::encodeToString)
            )
        )
    }

    private fun sendIceToRemote(iceCandidate: RemoteIceCandidate) {
        ws.send(
            messageConvert(
                MessageType.WebRTC.Ice,
                iceCandidate.let(Json::encodeToString)
            )
        )
    }

    suspend fun createOffer() {
        connection.offer().onSuccess {
            offerSdp.value = RemoteSessionDescription(it.type, it.description)
            sendSdpToRemote(MessageType.WebRTC.Offer, RemoteSessionDescription(it))
        }
    }

    fun isSpeakerphone(isSpeakerphone: Boolean) {
        _viewState.update { it.copy(isSpeakerphone = isSpeakerphone) }
        factory.setSpeakerphoneOn(isSpeakerphone)
    }

    fun changeVisible() {
        _viewState.update { it.copy(barsVisible = !it.barsVisible) }
    }

    override fun onCleared() {
        super.onCleared()
        ws.pingPong.isCall = false
        newKtor?.close()
    }
}


@Serializable
data class WebRTCMessage(
    val target: Long,
    val message: String
)

@Serializable
data class RemoteIceCandidate(
    val sdpMid: String,
    val sdpMLineIndex: Int,
    val sdp: String,
) {
    constructor(iceCandidate: IceCandidate) : this(
        iceCandidate.sdp,
        iceCandidate.sdpMLineIndex,
        iceCandidate.sdp
    )
}
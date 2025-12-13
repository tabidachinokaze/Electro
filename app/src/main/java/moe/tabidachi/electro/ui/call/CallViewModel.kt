package moe.tabidachi.electro.ui.call

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import moe.tabidachi.compose.mvi.BaseViewModel
import moe.tabidachi.electro.ANSWER_ACTION
import moe.tabidachi.electro.Factory
import moe.tabidachi.electro.OFFER_ACTION
import moe.tabidachi.electro.data.ElectroRepository
import moe.tabidachi.electro.data.network.ElectroWebSocket
import moe.tabidachi.electro.data.network.MessageType
import moe.tabidachi.electro.data.provider.BaseUrlProvider
import moe.tabidachi.electro.data.repository.SharedRepository
import moe.tabidachi.electro.ktx.TAG
import moe.tabidachi.electro.model.WebSocketMessage
import moe.tabidachi.electro.model.header
import moe.tabidachi.electro.shared.SharedHttpClient
import moe.tabidachi.electro.shared.SharedJson
import moe.tabidachi.electro.ui.call.CallContract.Effect
import moe.tabidachi.electro.ui.call.CallContract.Event
import moe.tabidachi.electro.ui.call.CallContract.State
import org.webrtc.IceCandidate
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val electroRepository: ElectroRepository,
    private val client: HttpClient,
    private val sharedRepository: SharedRepository,
    private val webSocket: ElectroWebSocket,
    private val baseUrlProvider: BaseUrlProvider
) : BaseViewModel<State, Event, Effect>(State()), CallContract.ViewModel {
    private var isCalled = false
    val factory = Factory(context)
    private var ws: ElectroWebSocket = webSocket

    private val connection = factory.createPeerConnection(
        Factory.Callback(
            onDescription = {
                Log.d(TAG, "onDescription: $it")
            },
            onIceCandidate = {
                sendIce(
                    messageConvert(
                        MessageType.WebRTC.Ice,
                        RemoteIceCandidate(it).let(Json::encodeToString)
                    )
                )
            },
            onTrack = {
                Log.d(TAG, "onTrack: $it")
            },
            onRenegotiationNeeded = {
            }
        )
    )

    private val offerSdp = mutableStateOf<RemoteSessionDescription?>(null)
    private val answerSdp = mutableStateOf<RemoteSessionDescription?>(null)
    private val localIce = mutableStateOf<RemoteIceCandidate?>(null)
    private val remoteIce = mutableStateOf<RemoteIceCandidate?>(null)

    override fun event(event: Event) = when (event) {
        is Event.InitCall -> handleOneTimeEvent(event) {
            val state = state.value
            onMicEnabled(state.mic)
            onCameraEnabled(state.camera)
            isSpeakerphone(state.isSpeakerphone)
            init(offer = event.offer, answer = event.answer, action = event.action)
        }

        Event.ToggleImmersive -> changeVisible()
        Event.OnCallEnd -> onCallEnd()
        Event.FlipCamera -> flipCamera()
        is Event.OnMicEnabled -> onMicEnabled(event.value)
        is Event.OnCameraEnabled -> onCameraEnabled(event.value)
        is Event.IsSpeakerphone -> isSpeakerphone(event.value)
    }

    private fun messageConvert(type: MessageType, message: String): WebSocketMessage {
        return WebSocketMessage {
            header = header {
                this.type = type.toString()
            }
            body = WebRTCMessage(state.value.target!!, message).let(Json::encodeToString)
                .toByteArray()
        }
    }

    private fun sendIce(webSocketMessage: WebSocketMessage) {
        viewModelScope.launch {
            ws.connected.takeWhile { !it }.collect()
            ws.send(
                webSocketMessage,
                onFailure = {
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
                        stop()
                        emitEffect(Effect.OnCallEnd)
                    }
                }
            }
        }
    }

    private fun init(offer: Long, answer: Long, action: String) {
        if (isCalled) return
        updateState {
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
                    electroRepository.getUser(answer).onSuccess {
                        it.data?.let { user ->
                            updateState { it.copy(user = user) }
                        }
                    }
                }

                ANSWER_ACTION -> {
                    electroRepository.getUser(offer).onSuccess {
                        it.data?.let { user ->
                            updateState { it.copy(user = user) }
                        }
                    }
                    val account = electroRepository.findAccount(answer) ?: return@launch
                    println(account)
                    val client = SharedHttpClient(
                        json = SharedJson(),
                        tokenProvider = { account.token },
                        onUrlConvertConfig = {}
                    )
                    ws = ElectroWebSocket(
                        client = client,
                        baseUrlProvider = baseUrlProvider
                    )
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
            },
            onFailure = {
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
            message,
            onSuccess = {
                isCalled = true
            },
            onFailure = {
                viewModelScope.launch {
                    delay(2000)
                    response(target)
                }
            }
        )
    }

    private fun onMicEnabled(enabled: Boolean) {
        factory.microphone(enabled)
        updateState { it.copy(mic = enabled) }
    }

    private fun onCameraEnabled(enabled: Boolean) {
        factory.camera(enabled)
        updateState { it.copy(camera = enabled) }
    }

    private fun onCallEnd() {
        ws.send(
            WebSocketMessage {
                header = header {
                    type = MessageType.WebRTC.End.toString()
                }
                body = "${state.value.target}".toByteArray()
            }
        )
        stop()
        emitEffect(Effect.OnCallEnd)
    }

    private fun stop() {
        factory.disconnect()
        connection.connection.dispose()
    }

    private fun flipCamera() {
        factory.switchCamera()
    }

    private fun sendSdpToRemote(type: MessageType, description: RemoteSessionDescription) {
        val message = messageConvert(type, description.let(Json::encodeToString))
        ws.send(message)
    }

    private fun sendIceToRemote(iceCandidate: RemoteIceCandidate) {
        val message = messageConvert(MessageType.WebRTC.Ice, iceCandidate.let(Json::encodeToString))
        ws.send(message)
    }

    private suspend fun createOffer() {
        connection.offer().onSuccess {
            offerSdp.value = RemoteSessionDescription(it.type, it.description)
            sendSdpToRemote(MessageType.WebRTC.Offer, RemoteSessionDescription(it))
        }
    }

    private fun isSpeakerphone(isSpeakerphone: Boolean) {
        updateState { it.copy(isSpeakerphone = isSpeakerphone) }
        factory.setSpeakerphoneOn(isSpeakerphone)
    }

    private fun changeVisible() {
        updateState { it.copy(barsVisible = !it.barsVisible) }
    }

    override fun onCleared() {
        super.onCleared()
        ws.pingPong.isCall = false
        runBlocking { ws.close() }
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

fun RemoteIceCandidate.toLocal() = IceCandidate(this.sdpMid, this.sdpMLineIndex, this.sdp)

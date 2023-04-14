package cn.tabidachi.electro.data.network

import android.util.Log
import cn.tabidachi.electro.model.WebSocketMessage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.client.plugins.websocket.cio.webSocketRawSession
import io.ktor.client.request.port
import io.ktor.http.takeFrom
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koitharu.pausingcoroutinedispatcher.launchPausing
import java.util.concurrent.atomic.AtomicInteger

interface WebSocket {
    val onWebSocketMessage: SharedFlow<WebSocketMessage>
    fun send(webSocketMessage: WebSocketMessage, onSuccess: () -> Unit = {}, onFailure: (WebSocketMessage) -> Unit = {})
}

class ElectroWebSocket(
    private val client: HttpClient,
    private val port: Int
) : WebSocket {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var session: ClientWebSocketSession? = null
    val pingPong = PingPong(5000)
    private var messageJob: Job? = null
    private val _onWebSocketMessage = MutableSharedFlow<WebSocketMessage>()
    override val onWebSocketMessage = _onWebSocketMessage.asSharedFlow()
    private var isClosed = false
    val connected = MutableStateFlow(false)
    private val isPause = MutableStateFlow(false)
    private val connectActiveJob = scope.launchPausing {
        while (!isClosed) {
            isPause.takeWhile { it }.collect {
                close()
            }
            pingPong.ping(
                onPing = {
                    Log.i(TAG, "onPing: $it")
                    launch {
                        kotlin.runCatching {
                            session?.send(Frame.Ping(it.toByteArray()))
                        }.onFailure {
                            Log.e(TAG, "Socket: send failure", it)
                        }
                    }
                }, onConnect = {
                    connected.value = true
                }, onTimeout = {
                    connected.value = false
                    Log.i(TAG, "onTimeout")
                    launch {
                        close()
                        connect().onSuccess {
                            session = it
                            closeMessage()
                            messageJob = scope.launch {
                                onMessage(it)
                            }
                        }.onFailure {
                            Log.e(TAG, "connect: failure", it)
                        }
                    }
                    5000
                }
            )
        }
    }

    fun pause() {
        isPause.value = true
    }

    fun resume() {
        isPause.value = false
    }

    override fun send(webSocketMessage: WebSocketMessage, onSuccess: () -> Unit, onFailure: (WebSocketMessage) -> Unit) {
        Log.d(TAG, "$webSocketMessage")
        kotlin.runCatching {
            Json.encodeToString(webSocketMessage)
        }.mapCatching {
            scope.launch {
                session?.send(it)
                onSuccess()
            }
        }.onFailure {
            Log.e(TAG, "send: $webSocketMessage", it)
            onFailure(webSocketMessage)
        }
    }

    private suspend fun onMessage(session: ClientWebSocketSession) {
        for (frame in session.incoming) {
            when (frame) {
                is Frame.Pong -> {
                    Log.i(TAG, "onPong: ${String(frame.data)}")
                    pingPong.pong(String(frame.data))
                }

                is Frame.Text -> {
                    kotlin.runCatching {
                        Json.decodeFromString<WebSocketMessage>(frame.readText())
                    }.mapCatching {
                        _onWebSocketMessage.emit(it)
                    }.onFailure {
                        Log.e(TAG, "onMessage: ${frame.readText()}", it)
                    }
                }

                else -> {

                }
            }
        }
    }

    private fun closeMessage() {
        messageJob?.cancel()
    }

    private suspend fun connect(): Result<ClientWebSocketSession> {
        return kotlin.runCatching {
            client.webSocketRawSession {
                url.takeFrom("/")
                port = this@ElectroWebSocket.port
            }
        }
    }

    suspend fun close() {
        session?.cancel()
        session?.close()
    }

    fun stop() {
        isClosed = true
        scope.launch {
            close()
        }
        connectActiveJob.cancel()
        messageJob?.cancel()
    }

    class PingPong(private val timeout: Long) {
        private var ping: String = "ping"
        private var pong: String = "pong"
        private val counter = AtomicInteger(0)
        var isCall: Boolean = false

        suspend fun ping(onPing: (String) -> Unit, onConnect: () -> Unit, onTimeout: () -> Long) {
            ping = counter.getAndIncrement().toString().also(onPing)
            delay(timeout)
            if (ping != pong) {
                delay(onTimeout())
            } else {
                onConnect()
            }
        }

        fun pong(pong: String) {
            this.pong = pong
        }
    }

    companion object {
        const val TAG = "ElectroWebSocket"
    }
}

@Serializable
class MessageType(
    val type: String, val subtype: String
) {
    override fun equals(other: Any?): Boolean {
        return other is MessageType &&
                type.equals(other.type, ignoreCase = true) &&
                subtype.equals(other.subtype, ignoreCase = true)
    }

    override fun hashCode(): Int = type.lowercase().hashCode() + subtype.lowercase().hashCode()

    override fun toString(): String = "$type/$subtype"

    fun match(pattern: MessageType): Boolean {
        return equals(pattern)
    }

    companion object {
        fun parse(value: String): MessageType {
            val slash = value.indexOf('/')
            if (slash == -1) {
                return Unknown
            }
            val type = value.substring(0, slash).trim()
            if (type.isEmpty()) {
                return Unknown
            }
            val subtype = value.substring(slash + 1).trim()
            if (type.contains(' ') || subtype.contains(' ')) {
                return Unknown
            }
            if (subtype.isEmpty() || subtype.contains('/')) {
                return Unknown
            }
            return MessageType(type, subtype)
        }
        val Unknown = MessageType("*", "*")
    }

    object Dialog {
        val New = MessageType("dialog", "new")
    }

    object Message {
        val New = MessageType("message", "new")
        val Delete = MessageType("message", "delete")
        val Update = MessageType("message", "update")
    }

    object WebRTC {
        val Request = MessageType("webrtc", "request")
        val Response = MessageType("webrtc", "response")
        val Offer = MessageType("webrtc", "offer")
        val Answer = MessageType("webrtc", "answer")
        val Ice = MessageType("webrtc", "ice")
        val End = MessageType("webrtc", "end")
    }

    object OnlineStatus {
        val Status = MessageType("online_status", "status")
        val Listen = MessageType("online_status", "listen")
        val Unlisten = MessageType("online_status", "unlisten")
    }
}
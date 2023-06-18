package cn.tabidachi.electro.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.database.entity.Message
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.data.network.MessageType
import cn.tabidachi.electro.model.attachment.Attachment
import cn.tabidachi.electro.model.attachment.deserialize
import cn.tabidachi.electro.ui.common.BubbleType
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

abstract class Messenger(
    open val repository: Repository,
    open val ktor: Ktor
) : ViewModel() {
    val sid = MutableStateFlow<Long>(-1)
    val messages = mutableStateListOf<DownloadMessageItem>()
    val uploadMessages = mutableStateListOf<UploadMessageItem>()
    var isRefresh by mutableStateOf(false)
    private val listens = mutableStateMapOf<Long, Boolean>()

    fun online(uid: Long): Boolean {
        return listens[uid] ?: false
    }

    fun online(): Int {
        return listens.values.count { it }
    }

    private fun send(webSocketMessage: WebSocketMessage, retry: Boolean = false) {
        viewModelScope.launch {
            ktor.ws.connected.takeWhile { !it }.collect()
            ktor.ws.send(
                webSocketMessage,
                onFailure = {
                    if (retry) {
                        viewModelScope.launch {
                            ktor.ws.connected.takeWhile { !it }.collect()
                            send(it)
                        }
                    }
                }
            )
        }
    }

    fun listen(target: Long) {
        if (!listens.containsKey(target)) {
            val message = WebSocketMessage {
                header = header {
                    type = MessageType.OnlineStatus.Listen.toString()
                }
                body = "$target".toByteArray()
            }
            send(message, true)
        }
    }

    fun unlisten(target: Long) {
        val message = WebSocketMessage {
            header = header {
                type = MessageType.OnlineStatus.Unlisten.toString()
            }
            body = "$target".toByteArray()
        }
        send(message)
    }


    fun initMessage() = viewModelScope.launch {
        sid.filter { it != -1L }.collect {
            onRefresh()
            launch {
                messageSendingQueue(it)
            }
            launch {
                remoteAfterMessage(it)
            }
        }
    }

    fun initWebSocket() = viewModelScope.launch {
        ktor.ws.onWebSocketMessage.collectLatest { message ->
            println(message)
            when (message.header.type) {
                MessageType.Message.New.toString() -> {
                    val pair =
                        String(message.body).let<String, Pair<Long, Long>>(Json::decodeFromString)
                    if (pair.first == sid.value) {
                        repository.message(pair.second).onSuccess {
                            it.data?.let { it1 ->
                                insert(messageToItem(it1))
                            }
                        }
                    }
                }

                MessageType.Message.Update.toString() -> {

                }

                MessageType.Message.Delete.toString() -> {
                    val pair =
                        String(message.body).let<String, Pair<Long, Long>>(Json::decodeFromString)
                    if (pair.first == sid.value) {
                        repository.deleteLocalMessage(pair.second)
                        messages.removeIf { it.message.mid == pair.second }
                    }
                }

                MessageType.OnlineStatus.Status.toString() -> {
                    val (target, isOnline) = String(message.body).let<String, OnlineStatus>(
                        Json::decodeFromString
                    )
                    listens[target] = isOnline
                }
            }
        }
    }

    fun onRefresh() {
        localBeforeMessage(
            sid.value,
            messages.lastOrNull()?.message?.createTime ?: System.currentTimeMillis()
        ) { state ->
            isRefresh = state
        }
    }

    private fun remoteAfterMessage(sid: Long) {
        viewModelScope.launch {
            val time =
                repository.getLatestMessageInSession(sid)?.createTime ?: System.currentTimeMillis()
            repository.remoteAfterMessage(sid, time).onSuccess {
                it.data?.forEach {
                    insert(messageToItem(it))
                }
            }
        }
    }

    private fun localBeforeMessage(sid: Long, time: Long, onProcess: (Boolean) -> Unit) {
        viewModelScope.launch {
            onProcess(true)
            repository.localBeforeMessage(sid, time, 10).onEach {
                insert(messageToItem(it))
            }.let {
                if (it.isEmpty()) {
                    repository.remoteBeforeMessage(sid, time, 10).onSuccess { (_, _, data) ->
                        data?.forEach {
                            insert(messageToItem(it))
                        }
                    }
                }
                onProcess(false)
            }
        }
    }

    fun insert(item: DownloadMessageItem) {
        val index = messages.indexOfFirst { it.message.mid == item.message.mid }
        if (index >= 0) {
            messages[index] = item
        } else {
            messages.binarySearch {
                item.message.createTime.compareTo(it.message.createTime)
            }.let {
                if (it < 0) -it - 1 else it
            }.let {
                messages.add(it, item)
            }
        }
    }

    fun messageToItem(message: Message): DownloadMessageItem {
        return DownloadMessageItem(
            type = if (message.uid == ktor.uid) BubbleType.Outgoing else BubbleType.Incoming,
            message = message,
            attachment = Attachment.deserialize(message.type, message.attachment),
            repository = repository,
            viewModelScope
        )
    }

    private var job: Job? = null
    private fun messageSendingQueue(sid: Long) {
        job?.cancel()
        job = viewModelScope.launch {
            repository.messageSendingQueue(sid).collect {
                val news = it.map { it.id }
                val olds = uploadMessages.map { it.message.id }
                olds.forEach { old ->
                    if (old !in news) {
                        uploadMessages.removeIf { it.message.id == old }
                    }
                }
                news.forEach { new ->
                    if (new !in olds) {
                        val message = it.first { it.id == new }
                        insert(
                            UploadMessageItem(
                                message,
                                Attachment.deserialize(message.type, message.attachment),
                                viewModelScope,
                                repository
                            )
                        )
                    }
                }
            }
        }
    }

    fun insert(item: UploadMessageItem) {
        val index = uploadMessages.indexOfFirst { it.message.id == item.message.id }
        if (index >= 0) {
            uploadMessages[index] = item
        } else {
            uploadMessages.binarySearch {
                item.message.createTime.compareTo(it.message.createTime)
            }.let {
                if (it < 0) -it - 1 else it
            }.let {
                uploadMessages.add(it, item)
            }
        }
    }

    open suspend fun getSessionId(): Long? = null

    fun deleteMessage(mid: Long) {
        viewModelScope.launch {
            repository.deleteMessage(mid)
        }
    }

    var reply by mutableStateOf<Long?>(null)

    open fun getReplyId(): Long? {
        return reply
    }

    fun onReply(mid: Long) {
        this.reply = mid
    }

    fun onReplyClear() {
        this.reply = null
    }

    fun onMessageSendSuccess() {
        onReplyClear()
    }

    fun readMessage() {
        messages.firstOrNull()?.let {
            viewModelScope.launch {
                repository.readMessage(sid.value, it.message.createTime)
            }
        }
    }
}
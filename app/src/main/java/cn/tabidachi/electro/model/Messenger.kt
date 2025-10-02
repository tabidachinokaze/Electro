package cn.tabidachi.electro.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.database.entity.Message
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.data.network.MessageType
import cn.tabidachi.electro.model.attachment.Attachment
import cn.tabidachi.electro.model.attachment.deserialize
import cn.tabidachi.electro.ui.common.BubbleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

interface Messenger {
    //val repository: Repository
    //val ktor: Ktor
    //val scope: CoroutineScope
    val sid: StateFlow<Long?>
    val messages: SnapshotStateList<DownloadMessageItem>
    val uploadMessages: SnapshotStateList<UploadMessageItem>
    val isRefresh: Boolean
    suspend fun getSessionId(): Long?
    fun setSessionId(sid: Long)
    fun online(uid: Long): Boolean
    fun online(): Int
    fun listen(target: Long)
    fun unlisten(target: Long)
    fun initMessage()
    fun initWebSocket()
    fun onRefresh()
    fun insert(item: DownloadMessageItem)
    fun insert(item: UploadMessageItem)
    fun deleteMessage(mid: Long)
    val reply: Long?
    fun getReplyId(): Long?
    fun onReply(mid: Long)
    fun onReplyClear()
    fun onMessageSendSuccess()
    fun readMessage()
}

object EmptyMessenger : Messenger {
    override val sid: StateFlow<Long?> = MutableStateFlow(0)
    override val messages: SnapshotStateList<DownloadMessageItem> = mutableStateListOf()
    override val uploadMessages: SnapshotStateList<UploadMessageItem> = mutableStateListOf()
    override val isRefresh: Boolean = false
    override suspend fun getSessionId(): Long? = sid.value

    override fun setSessionId(sid: Long) {}

    override fun online(uid: Long): Boolean = false

    override fun online(): Int = 0

    override fun listen(target: Long) {}

    override fun unlisten(target: Long) {}

    override fun initMessage() {}

    override fun initWebSocket() {}

    override fun onRefresh() {}

    override fun insert(item: DownloadMessageItem) {}

    override fun insert(item: UploadMessageItem) {}

    override fun deleteMessage(mid: Long) {}

    override val reply: Long? = null

    override fun getReplyId(): Long? = null

    override fun onReply(mid: Long) {}

    override fun onReplyClear() {}

    override fun onMessageSendSuccess() {}

    override fun readMessage() {}
}

open class BaseMessenger(
    private val repository: Repository,
    private val ktor: Ktor,
    private val scope: CoroutineScope,
    sid: Long?
) : Messenger {
    override val messages: SnapshotStateList<DownloadMessageItem> = mutableStateListOf()
    override val uploadMessages: SnapshotStateList<UploadMessageItem> = mutableStateListOf()
    override var isRefresh: Boolean by mutableStateOf(false)
    private val listens = mutableStateMapOf<Long, Boolean>()
    private val _sid = MutableStateFlow(sid)
    override val sid: StateFlow<Long?> = _sid.asStateFlow()

    init {
        initMessage()
        initWebSocket()
    }

    override suspend fun getSessionId(): Long? {
        return _sid.value
    }

    override fun setSessionId(sid: Long) {
        _sid.value = sid
    }

    override fun online(uid: Long): Boolean {
        return listens[uid] ?: false
    }

    override fun online(): Int {
        return listens.values.count { it }
    }

    private fun send(webSocketMessage: WebSocketMessage, retry: Boolean = false) {
        scope.launch {
            ktor.ws.connected.takeWhile { !it }.collect()
            ktor.ws.send(
                webSocketMessage,
                onFailure = {
                    if (retry) {
                        scope.launch {
                            ktor.ws.connected.takeWhile { !it }.collect()
                            send(it)
                        }
                    }
                }
            )
        }
    }

    override fun listen(target: Long) {
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

    override fun unlisten(target: Long) {
        val message = WebSocketMessage {
            header = header {
                type = MessageType.OnlineStatus.Unlisten.toString()
            }
            body = "$target".toByteArray()
        }
        send(message)
    }

    override fun initMessage() {
        sid.filterNotNull().onEach { sid ->
            onRefresh()
            messageSendingQueue(sid)
            remoteAfterMessage(sid)
        }.launchIn(scope)
    }

    override fun initWebSocket() {
        scope.launch {
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
    }

    override fun onRefresh() {
        localBeforeMessage(
            sid.value ?: return,
            messages.lastOrNull()?.message?.createTime ?: System.currentTimeMillis()
        ) { state ->
            isRefresh = state
        }
    }

    private fun remoteAfterMessage(sid: Long) {
        scope.launch {
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
        scope.launch {
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

    override fun insert(item: DownloadMessageItem) {
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

    private fun messageToItem(message: Message): DownloadMessageItem {
        return DownloadMessageItem(
            type = if (message.uid == ktor.uid) BubbleType.Outgoing else BubbleType.Incoming,
            message = message,
            attachment = Attachment.deserialize(message.type, message.attachment),
            repository = repository,
            scope
        )
    }

    private var job: Job? = null
    private fun messageSendingQueue(sid: Long) {
        job?.cancel()
        job = scope.launch {
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
                                scope,
                                repository
                            )
                        )
                    }
                }
            }
        }
    }

    override fun insert(item: UploadMessageItem) {
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

    override fun deleteMessage(mid: Long) {
        scope.launch {
            repository.deleteMessage(mid)
        }
    }

    override var reply: Long? by mutableStateOf<Long?>(null)

    override fun getReplyId(): Long? {
        return reply
    }

    override fun onReply(mid: Long) {
        this.reply = mid
    }

    override fun onReplyClear() {
        this.reply = null
    }

    override fun onMessageSendSuccess() {
        onReplyClear()
    }

    override fun readMessage() {
        val sid = sid.value ?: return
        messages.firstOrNull()?.let {
            scope.launch {
                repository.readMessage(sid, it.message.createTime)
            }
        }
    }
}

package moe.tabidachi.electro.data

import android.content.Context
import android.net.Uri
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.util.generateNonce
import io.minio.ComposeSource
import io.minio.ObjectWriteResponse
import io.minio.http.Method
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import moe.tabidachi.electro.ElectroStorage
import moe.tabidachi.electro.data.database.ElectroDatabase
import moe.tabidachi.electro.data.database.entity.Account
import moe.tabidachi.electro.data.database.entity.Chunk
import moe.tabidachi.electro.data.database.entity.Dialog
import moe.tabidachi.electro.data.database.entity.Download
import moe.tabidachi.electro.data.database.entity.Message
import moe.tabidachi.electro.data.database.entity.MessageSendRequest
import moe.tabidachi.electro.data.database.entity.Path
import moe.tabidachi.electro.data.database.entity.Search
import moe.tabidachi.electro.data.database.entity.Session
import moe.tabidachi.electro.data.database.entity.SessionSearch
import moe.tabidachi.electro.data.database.entity.SessionType
import moe.tabidachi.electro.data.database.entity.SessionUser
import moe.tabidachi.electro.data.database.entity.User
import moe.tabidachi.electro.data.network.MinIO
import moe.tabidachi.electro.data.repository.SharedState
import moe.tabidachi.electro.data.service.DialogApi
import moe.tabidachi.electro.data.service.FileApi
import moe.tabidachi.electro.data.service.MessageApi
import moe.tabidachi.electro.data.service.SessionApi
import moe.tabidachi.electro.data.service.UserApi
import moe.tabidachi.electro.ext.md5
import moe.tabidachi.electro.ext.md5WithCopy
import moe.tabidachi.electro.ext.regex
import moe.tabidachi.electro.model.UserQuery
import moe.tabidachi.electro.model.attachment.Attachment
import moe.tabidachi.electro.model.attachment.AudioAttachment
import moe.tabidachi.electro.model.attachment.DocumentAttachment
import moe.tabidachi.electro.model.attachment.FileAttachment
import moe.tabidachi.electro.model.attachment.ImageAttachment
import moe.tabidachi.electro.model.attachment.LocationAttachment
import moe.tabidachi.electro.model.attachment.VideoAttachment
import moe.tabidachi.electro.model.attachment.VoiceAttachment
import moe.tabidachi.electro.model.attachment.WebRTCAttachment
import moe.tabidachi.electro.model.attachment.deserialize
import moe.tabidachi.electro.model.attachment.serialize
import moe.tabidachi.electro.model.request.MessageRequest
import moe.tabidachi.electro.model.request.MessageSyncRequest
import moe.tabidachi.electro.model.response.DialogResponse
import moe.tabidachi.electro.model.response.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import kotlin.math.ceil

interface ElectroRepository {
    suspend fun findResource(id: String): Path?
    suspend fun saveResource(path: Path)
    suspend fun download(
        id: String,
        url: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {},
        progressListener: suspend (Long, Long?) -> Unit = { _: Long, _: Long? -> }
    )

    suspend fun download(
        id: String,
        bucket: String,
        `object`: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {},
        onProgress: (Float) -> Unit
    )

    fun sessionsFlow(): Flow<List<User>>
    suspend fun findSessionByPairUser(target: Long): Long?
    suspend fun localBeforeMessage(sid: Long, before: Long, size: Int): List<Message>
    suspend fun getLatestMessageInSession(sid: Long): Message?
    suspend fun remoteAfterMessage(sid: Long, after: Long): Result<Response<List<Message>>>
    suspend fun remoteBeforeMessage(
        sid: Long,
        before: Long,
        size: Int
    ): Result<Response<List<Message>>>

    suspend fun message(mid: Long): Result<Response<Message>>
    suspend fun readMessage(sid: Long, time: Long): Result<Response<Long>>
    suspend fun queryUserFlow(query: String): List<UserQuery>
    suspend fun getUser(target: Long): Result<Response<User>>
    suspend fun sendMessage(
        body: MessageSendRequest,
        onFailure: () -> Unit,
        onProgress: (Float) -> Unit
    )

    suspend fun addMessageRequest(
        body: MessageSendRequest,
    )

    suspend fun createSessionByPairUser(target: Long): Result<Response<Long>>
    fun dialogsFlow(): Flow<List<Dialog>>
    suspend fun pullDialogs(): Result<Response<List<DialogResponse>>>
    suspend fun pullDialog(sid: Long): Result<Response<DialogResponse>>
    suspend fun getDialog(sid: Long): Dialog?
    suspend fun dialogsFlow(query: String): List<Dialog>
    suspend fun sessionSearch(title: String): List<SessionSearch>
    suspend fun getSessionUser(sid: Long): List<Long>
    fun messageSendingQueue(sid: Long): Flow<List<MessageSendRequest>>
    suspend fun deleteMessage(mid: Long): Result<Response<Long>>
    suspend fun deleteLocalMessage(mid: Long)
    suspend fun cancelSendingMessage(id: String)
    suspend fun findSession(sid: Long): Session?
    suspend fun saveSearch(query: String)
    suspend fun loadSearch(): String?
    suspend fun findAccount(uid: Long): Account?
    suspend fun removeAccount(uid: Long)
}

class ElectroRepositoryImpl(
    val context: Context,
    private val client: HttpClient,
    private val database: ElectroDatabase,
    private val sharedState: StateFlow<SharedState>,
    private val storage: ElectroStorage,
    private val minio: MinIO,
    private val userApi: UserApi,
    private val sessionApi: SessionApi,
    private val messageApi: MessageApi,
    private val dialogApi: DialogApi,
    private val fileApi: FileApi,
) : ElectroRepository {
    private val dialogDao = database.dialogDao()
    private val messageDao = database.messageDao()
    private val messageSendRequestDao = database.messageSendRequestDao()
    private val sessionSearchDao = database.sessionSearchDao()
    private val pathDao = database.pathDao()
    private val sessionUserDao = database.sessionUserDao()
    private val chunkDao = database.chunkDao()
    private val downloadDao = database.downloadDao()
    private val accountDao = database.accountDao()
    private val userDao = database.userDao()
    private val sessionDao = database.sessionDao()
    private val queryUserDao = database.queryUserDao()
    private val searchDao = database.searchDao()

    override suspend fun findResource(id: String) = withContext(Dispatchers.IO) {
        pathDao.find(id)
    }

    override suspend fun saveResource(path: Path) = withContext(Dispatchers.IO) {
        pathDao.upsert(path)
    }

    override suspend fun download(
        id: String,
        url: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        progressListener: suspend (Long, Long?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            runCatching {
                val readChannel = fileApi.download(url, progressListener)
                storage.store(url, readChannel)?.let {
                    pathDao.upsert(Path(id, it.toString()))
                    onSuccess()
                }
            }.onFailure {
                onFailure()
            }
        }
    }

    override suspend fun download(
        id: String,
        bucket: String,
        `object`: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        onProgress: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        runCatching {
            var offset = 0L
            val download = downloadDao.find(id)
            val file: File
            when {
                download == null -> {
                    file = File(storage.directory, generateNonce()).also {
                        it.createNewFile()
                    }
                    downloadDao.upsert(Download(id, file.toString(), false))
                }

                download.completed -> {
                    return@withContext
                }

                else -> {
                    file = File(download.path)
                    offset = file.length()
                }
            }
            val statObject = minio.statObject(bucket, `object`)!!

            val length = statObject.size()
            val inputStream = minio.getObject(bucket, `object`, offset)!!
            val outputStream = FileOutputStream(file, true)
            val buffer = ByteArray(1024)
            var bytesRead = inputStream.read(buffer)
            while (bytesRead != -1 && isActive) {
                outputStream.write(buffer, 0, bytesRead)
                offset += bytesRead
                onProgress(offset.toFloat() / length.toFloat())
                bytesRead = inputStream.read(buffer)
            }
            if (isActive) {
                pathDao.upsert(Path(id, file.toString()))
                downloadDao.delete(id)
            }
            outputStream.close()
            inputStream.close()
        }.onSuccess {
            onSuccess()
        }.onFailure {
            onFailure()
            it.printStackTrace()
        }
    }

    override fun sessionsFlow(): Flow<List<User>> = accountDao.allFlow().map {
        it.mapNotNull { userDao.find(it.uid) }
    }.flowOn(Dispatchers.IO)

    override suspend fun findSessionByPairUser(target: Long): Long? = withContext(Dispatchers.IO) {
        val currentUserId = sharedState.value.currentUserId
        runCatching { sessionApi.findSessionByPairUser(target) }.onSuccess {
            it.data?.let { sid ->
                sessionUserDao.upsert(
                    SessionUser(sid, SessionType.P2P, listOf(currentUserId, target))
                )
                return@withContext sid
            }
        }.onFailure {
            sessionUserDao.find(SessionType.P2P).firstOrNull {
                it.users.all { it == currentUserId || it == target }
            }?.let {
                return@withContext it.sid
            }
        }
        return@withContext null
    }

    override suspend fun localBeforeMessage(sid: Long, before: Long, size: Int) =
        withContext(Dispatchers.IO) {
            messageDao.getMessageBeforeTime(sid, before, size).let { messages ->
                messages.map { MessageSyncRequest(it.mid, it.updateTime) }.let { request ->
                    runCatching { messageApi.messageSync(request) }.getOrNull()?.data?.let { response ->
                        withContext(Dispatchers.IO) {
                            response.updates.forEach(messageDao::upsert)
                            response.deletes.forEach(messageDao::delete)
                        }
                        val updates = response.updates.map { it.mid }
                        messages.filter { it.mid !in response.deletes }.map { message ->
                            if (message.mid in updates) {
                                response.updates.first { it.mid == message.mid }
                            } else {
                                message
                            }
                        }
                    } ?: messages
                }
            }
        }

    override suspend fun getLatestMessageInSession(sid: Long) = withContext(Dispatchers.IO) {
        messageDao.getLatestMessageInSession(sid)
    }

    override suspend fun remoteAfterMessage(
        sid: Long,
        after: Long
    ): Result<Response<List<Message>>> = withContext(Dispatchers.IO) {
        runCatching {
            messageApi.messages(
                MessageRequest(
                    sid = sid,
                    between = after to null,
                    type = MessageRequest.Type.BETWEEN,
                    limit = 0
                )
            )
        }.onSuccess { (_, _, list) ->
            list?.forEach(messageDao::upsert)
        }
    }

    override suspend fun remoteBeforeMessage(
        sid: Long,
        before: Long,
        size: Int
    ): Result<Response<List<Message>>> = withContext(Dispatchers.IO) {
        runCatching {
            messageApi.messages(
                MessageRequest(
                    sid = sid,
                    between = null to before,
                    type = MessageRequest.Type.BETWEEN,
                    limit = size
                )
            )
        }.onSuccess { (_, _, list) ->
            list?.forEach(messageDao::upsert)
        }
    }

    override suspend fun message(mid: Long): Result<Response<Message>> =
        withContext(Dispatchers.IO) {
            runCatching {
                messageApi.message(mid)
            }.onSuccess { (_, _, message) ->
                message?.let {
                    messageDao.upsert(it)
                }
            }
        }

    override suspend fun readMessage(sid: Long, time: Long): Result<Response<Long>> {
        return withContext(Dispatchers.IO) { runCatching { messageApi.readMessage(sid, time) } }
    }

    override suspend fun queryUserFlow(query: String): List<UserQuery> =
        withContext(Dispatchers.IO) {
            runCatching { userApi.queryUser(query) }.getOrNull()?.data?.also {
                it.forEach(queryUserDao::upsert)
            } ?: buildList {
                val cursor = database.query(
                    "select * from userquery where username regexp ?",
                    arrayOf(query.regex().pattern)
                )
                while (cursor.moveToNext()) {
                    UserQuery(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2)
                    ).let(::add)
                }
            }
        }

    override suspend fun getUser(target: Long): Result<Response<User>> =
        withContext(Dispatchers.IO) {
            runCatching { userApi.getUser(target) }.onSuccess {
                it.data?.let { it1 -> userDao.upsert(it1) }
            }
        }

    private suspend fun compose(path: String): ObjectWriteResponse? = withContext(Dispatchers.IO) {
        if (chunkDao.unloaded(path) != 0) return@withContext null
        val chunks = chunkDao.getChunk(path)
        if (chunks.isEmpty()) return@withContext null
        val composeSources = chunks.map { chunk ->
            ComposeSource.builder()
                .bucket(MinIO.UPLOAD)
                .`object`(chunk.filename)
                .build()
        }
        if (!minio.checkOrCreateBucket(MinIO.ELECTRO)) return@withContext null
        minio.composeObject(MinIO.ELECTRO, composeSources, chunks.first().md5)
    }

    private suspend fun upload(path: String, onProgress: (Float) -> Unit) =
        withContext(Dispatchers.IO) {
            val file = File(path)
            var uploaded = 0L
            val length = file.length()
            FileInputStream(file).use { inputStream ->
                val buffer = ByteArray(CHUNK_SIZE)
                var bytes: Int
                chunkDao.getChunk(path).forEach { chunk ->
                    println(chunk)
                    if (chunk.uploaded) {
                        uploaded += inputStream.skip(CHUNK_SIZE.toLong())
                    } else {
                        bytes = inputStream.read(buffer)

                        runCatching {
                            if (!minio.checkOrCreateBucket(MinIO.UPLOAD)) {
                                return@withContext
                            }
                            val url = minio.getPresignedObjectUrl(
                                Method.PUT,
                                MinIO.UPLOAD,
                                chunk.filename
                            )
                            client.put(url!!) {
                                setBody(buffer.copyOfRange(0, bytes))
                            }
                        }.onSuccess {
                            println(it.status)
                            if (it.status == HttpStatusCode.OK) {
                                chunkDao.upsert(chunk.copy(uploaded = true))
                            }
                            uploaded += bytes
                        }.onFailure {
                            it.printStackTrace()
                        }
                    }
                    onProgress(uploaded.toFloat() / length.toFloat())
                }
            }
        }

    override suspend fun sendMessage(
        body: MessageSendRequest,
        onFailure: () -> Unit,
        onProgress: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        val path = findResource(body.identification())?.path
        path?.let {
            upload(it, onProgress)
        }
        val request = if (path == null) body else when (val attachment =
            Attachment.deserialize(body.type, body.attachment)) {
            is DocumentAttachment -> {
                val pair = path.runCatching {
                    compose(this) ?: throw Exception("null")
                }.getOrNull() ?: return@withContext
                when (attachment) {
                    is AudioAttachment -> attachment.copy(
                        bucket = pair.bucket(),
                        `object` = pair.`object`()
                    )

                    is FileAttachment -> attachment.copy(
                        bucket = pair.bucket(),
                        `object` = pair.`object`()
                    )

                    is ImageAttachment -> attachment.copy(
                        bucket = pair.bucket(),
                        `object` = pair.`object`()
                    )

                    is VideoAttachment -> attachment.copy(
                        bucket = pair.bucket(),
                        `object` = pair.`object`()
                    )

                    is VoiceAttachment -> attachment.copy(
                        bucket = pair.bucket(),
                        `object` = pair.`object`()
                    )
                }
            }

            is LocationAttachment -> attachment
            is WebRTCAttachment -> attachment
            null -> null
        }.let {
            body.copy(attachment = it?.serialize()).also {
                messageSendRequestDao.upsert(it)
            }
        }
        runCatching {
            messageApi.sendMessage(request)
        }.onSuccess { (_, _, messageSendResponse) ->
            messageSendResponse?.let {
                messageSendRequestDao.deleteMessageById(it.id)
                val message = Message(it)
                path?.let { path ->
                    pathDao.delete(request.identification())
                    pathDao.upsert(Path(message.identification(), path))
                    chunkDao.delete(path)
                }
                messageDao.upsert(message)
            }
        }.onFailure {
            onFailure()
        }
    }

    override suspend fun addMessageRequest(
        body: MessageSendRequest,
    ) = withContext(Dispatchers.IO) {
        when (val attachment = Attachment.deserialize(body.type, body.attachment)) {
            is DocumentAttachment -> {
                when (attachment) {
                    is AudioAttachment, is FileAttachment, is ImageAttachment, is VideoAttachment -> {
                        val file =
                            File(context.getExternalFilesDir(null), generateNonce()).also {
                                if (!it.exists()) {
                                    it.createNewFile()
                                }
                            }
                        val md5sum =
                            context.contentResolver.openInputStream(Uri.parse(attachment.uri))
                                ?.use { inputStream ->
                                    context.contentResolver.openOutputStream(file.toUri())
                                        ?.use { outputStream ->
                                            inputStream.md5WithCopy(outputStream)
                                        }
                                }
                        val count = ceil(file.length().toDouble() / CHUNK_SIZE).toInt()
                        repeat(count) {
                            chunkDao.upsert(
                                Chunk(
                                    path = file.toString(),
                                    offset = it,
                                    uploaded = false,
                                    md5 = BigInteger(1, md5sum).toString(16)
                                )
                            )
                        }
                        pathDao.upsert(Path(body.identification(), file.toString()))
                    }

                    is VoiceAttachment -> {
                        val file = pathDao.find(body.identification())?.path?.let(::File)
                            ?: return@withContext
                        val md5sum = file.let(::FileInputStream).use {
                            it.md5()
                        }
                        val count = ceil(file.length().toDouble() / CHUNK_SIZE).toInt()
                        repeat(count) {
                            chunkDao.upsert(
                                Chunk(
                                    path = file.toString(),
                                    offset = it,
                                    uploaded = false,
                                    md5 = BigInteger(1, md5sum).toString(16)
                                )
                            )
                        }
                    }
                }
                when (attachment) {
                    is AudioAttachment -> attachment.copy(uri = null)
                    is FileAttachment -> attachment.copy(uri = null)
                    is ImageAttachment -> attachment.copy(uri = null)
                    is VideoAttachment -> attachment.copy(uri = null)
                    is VoiceAttachment -> attachment
                }
            }

            is LocationAttachment -> attachment
            is WebRTCAttachment -> attachment
            null -> null
        }.let {
            body.copy(attachment = it?.serialize())
        }.let {
            messageSendRequestDao.upsert(it)
        }
    }

    override suspend fun createSessionByPairUser(target: Long): Result<Response<Long>> =
        withContext(Dispatchers.IO) {
            runCatching { sessionApi.createSessionByPairUser(target) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun dialogsFlow(): Flow<List<Dialog>> =
        sharedState.map { it.currentUserId }.filterNotNull().flatMapLatest {
            dialogDao.dialogsFlow(uid = it)
        }.flowOn(Dispatchers.IO)

    override suspend fun pullDialogs(): Result<Response<List<DialogResponse>>> =
        withContext(Dispatchers.IO) {
            runCatching { dialogApi.dialogs() }.onSuccess { (_, _, dialogs) ->
                dialogs?.map(::Dialog)?.onEach(dialogDao::upsert)?.map {
                    it.sid
                }?.let { remotes ->
                    sharedState.value.currentUserId?.let { uid ->
                        val locals = dialogDao.dialogs(uid).map { it.sid }
                        locals.forEach { local ->
                            if (local !in remotes) {
                                dialogDao.delete(local)
                            }
                        }
                    }
                }
            }
        }

    override suspend fun pullDialog(sid: Long): Result<Response<DialogResponse>> =
        withContext(Dispatchers.IO) {
            runCatching { dialogApi.dialog(sid) }.onSuccess { (_, _, dialog) ->
                dialog?.let(::Dialog)?.let(dialogDao::upsert)
            }
        }

    override suspend fun getDialog(sid: Long): Dialog? = withContext(Dispatchers.IO) {
        pullDialog(sid).onSuccess {
            it.data?.run {
                return@withContext Dialog(this)
            }
        }.onFailure {
            return@withContext dialogDao.dialog(sid, sharedState.value.currentUserId)
        }
        null
    }

    override suspend fun dialogsFlow(query: String): List<Dialog> = withContext(Dispatchers.IO) {
        buildList {
            val cursor = database.query(
                "select * from dialog where title regexp ?",
                arrayOf(query.regex().pattern)
            )
            while (cursor.moveToNext()) {
                Dialog(
                    cursor.getLong(0),
                    cursor.getLong(1),
                    SessionType.valueOf(cursor.getString(2)),
                    cursor.getStringOrNull(3),
                    cursor.getStringOrNull(4),
                    cursor.getStringOrNull(5),
                    cursor.getLongOrNull(6),
                    cursor.getIntOrNull(7),
                    cursor.getStringOrNull(8),
                ).let { add(it) }
            }
        }
    }

    override suspend fun sessionSearch(title: String): List<SessionSearch> =
        withContext(Dispatchers.IO) {
            runCatching { sessionApi.sessionSearch(title) }.onSuccess { (status, message, sessionSearchList) ->
                sessionSearchList?.let {
                    it.forEach(sessionSearchDao::upsert)
                    return@withContext it
                }
            }.onFailure {
                val sessionSearches = buildList {
                    val cursor = database.query(
                        "select * from sessionsearch where title regexp ?",
                        arrayOf(title.regex().pattern)
                    )
                    while (cursor.moveToNext()) {
                        val sessionSearch = SessionSearch(
                            cursor.getLong(0),
                            SessionType.valueOf(cursor.getString(1)),
                            cursor.getStringOrNull(2),
                            cursor.getStringOrNull(3),
                            cursor.getStringOrNull(4),
                            cursor.getLong(5),
                            cursor.getInt(6)
                        )
                        add(sessionSearch)
                    }
                }
                return@withContext sessionSearches
            }
            emptyList()
        }

    override suspend fun getSessionUser(sid: Long): List<Long> = withContext(Dispatchers.IO) {
        runCatching { sessionApi.getSessionUser(sid) }.onSuccess {
            it.data?.let {
                if (sessionUserDao.find(sid) == null) {
                    sessionUserDao.insert(SessionUser(sid = sid, it.first, it.second))
                } else {
                    sessionUserDao.update(SessionUser(sid = sid, it.first, it.second))
                }
                return@withContext it.second
            }
        }.onFailure {
            sessionUserDao.find(sid)?.let {
                return@withContext it.users
            }
        }
        emptyList()
    }

    override fun messageSendingQueue(sid: Long): Flow<List<MessageSendRequest>> {
        return messageSendRequestDao.getMessagesFlow(sid).flowOn(Dispatchers.IO)
    }

    override suspend fun deleteMessage(mid: Long): Result<Response<Long>> =
        withContext(Dispatchers.IO) {
            runCatching { messageApi.deleteMessage(mid) }.onSuccess {
                it.data?.let { it1 -> deleteLocalMessage(it1) }
            }
        }

    override suspend fun deleteLocalMessage(mid: Long) = withContext(Dispatchers.IO) {
        messageDao.delete(mid)
    }

    override suspend fun cancelSendingMessage(id: String) = withContext(Dispatchers.IO) {
        messageSendRequestDao.deleteMessageById(id)
    }

    override suspend fun findSession(sid: Long): Session? = withContext(Dispatchers.IO) {
        runCatching { sessionApi.findSession(sid) }.getOrNull()?.data?.let {
            sessionDao.upsert(it)
            return@withContext it
        } ?: sessionDao.find(sid)
    }

    override suspend fun saveSearch(query: String) {
        withContext(Dispatchers.IO) {
            searchDao.upsert(Search(sharedState.value.currentUserId, query))
        }
    }

    override suspend fun loadSearch(): String? = withContext(Dispatchers.IO) {
        searchDao.find(sharedState.value.currentUserId)?.query
    }

    override suspend fun findAccount(uid: Long) = withContext(Dispatchers.IO) {
        accountDao.findByUser(uid)
    }

    override suspend fun removeAccount(uid: Long) = withContext(Dispatchers.IO) {
        accountDao.delete(uid)
    }
}

private const val CHUNK_SIZE = 1024 * 1024 * 5
package cn.tabidachi.electro.data

import android.app.Application
import android.net.Uri
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import cn.tabidachi.electro.ElectroStorage
import cn.tabidachi.electro.data.database.ElectroDatabase
import cn.tabidachi.electro.data.database.entity.Chunk
import cn.tabidachi.electro.data.database.entity.Dialog
import cn.tabidachi.electro.data.database.entity.Download
import cn.tabidachi.electro.data.database.entity.Message
import cn.tabidachi.electro.data.database.entity.MessageSendRequest
import cn.tabidachi.electro.data.database.entity.Path
import cn.tabidachi.electro.data.database.entity.RelationState
import cn.tabidachi.electro.data.database.entity.Search
import cn.tabidachi.electro.data.database.entity.SessionSearch
import cn.tabidachi.electro.data.database.entity.SessionType
import cn.tabidachi.electro.data.database.entity.SessionUser
import cn.tabidachi.electro.data.database.entity.SessionUserState
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.data.network.MinIO
import cn.tabidachi.electro.ext.md5
import cn.tabidachi.electro.ext.md5WithCopy
import cn.tabidachi.electro.ext.regex
import cn.tabidachi.electro.model.UserQuery
import cn.tabidachi.electro.model.attachment.Attachment
import cn.tabidachi.electro.model.attachment.AudioAttachment
import cn.tabidachi.electro.model.attachment.DocumentAttachment
import cn.tabidachi.electro.model.attachment.FileAttachment
import cn.tabidachi.electro.model.attachment.ImageAttachment
import cn.tabidachi.electro.model.attachment.LocationAttachment
import cn.tabidachi.electro.model.attachment.VideoAttachment
import cn.tabidachi.electro.model.attachment.VoiceAttachment
import cn.tabidachi.electro.model.attachment.WebRTCAttachment
import cn.tabidachi.electro.model.attachment.deserialize
import cn.tabidachi.electro.model.attachment.serialize
import cn.tabidachi.electro.model.request.ChannelUpdateRequest
import cn.tabidachi.electro.model.request.GroupUpdateRequest
import cn.tabidachi.electro.model.request.MessageRequest
import cn.tabidachi.electro.model.request.MessageSyncRequest
import cn.tabidachi.electro.model.request.UserUpdateRequest
import cn.tabidachi.electro.model.response.ChannelRole
import cn.tabidachi.electro.model.response.GroupRole
import cn.tabidachi.electro.model.response.Response
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.util.generateNonce
import io.minio.ComposeObjectArgs
import io.minio.ComposeSource
import io.minio.GetObjectArgs
import io.minio.GetPresignedObjectUrlArgs
import io.minio.ObjectWriteResponse
import io.minio.StatObjectArgs
import io.minio.http.Method
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.math.BigInteger
import kotlin.math.ceil

class Repository(
    val application: Application,
    private val database: ElectroDatabase,
    val ktor: Ktor,
    private val storage: ElectroStorage,
    private val minio: MinIO
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
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

    suspend fun findResource(id: String) = withContext(Dispatchers.IO) {
        pathDao.find(id)
    }

    suspend fun saveResource(path: Path) = withContext(Dispatchers.IO) {
        pathDao.upsert(path)
    }

    fun upload() {

    }

    suspend fun download(
        id: String,
        url: String,
        progressListener: suspend (Long, Long) -> Unit = { _: Long, _: Long -> }
    ) {
        val request = ktor.client.head {
            url(url)
        }
        val totalLength = request.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: -1
        var downloaded = 0L
        val file = pathDao.find(id)?.path?.let(::File)?.takeIf {
            downloaded = it.length()
            it.exists()
        } ?: kotlin.run {
            File(storage.directory, storage.filename(url)).also {
                it.createNewFile()
                pathDao.upsert(Path(id, it.toString()))
            }
        }
        val response = ktor.client.get {
            url(url)
            header(HttpHeaders.Range, "bytes=$downloaded-")
            method = HttpMethod.Get
        }
        when (response.status) {
            HttpStatusCode.OK -> {
                response.readBytes().let { bytes ->
                    file.writeBytes(bytes)
                    downloaded += bytes.size
                    progressListener(downloaded, totalLength)
                }
            }

            HttpStatusCode.PartialContent -> {
                response.headers[HttpHeaders.ContentLength]?.let { contentRange ->
                    val (rangeStart, rangeEnd, rangeTotal) = parseContentRange(contentRange)
                    response.readBytes().let { bytes ->
                        RandomAccessFile(file, "rw").use { randomAccessFile ->
                            randomAccessFile.seek(rangeStart)
                            randomAccessFile.write(bytes)
                            downloaded += bytes.size
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun parseContentRange(contentRange: String): Triple<Long, Long, Long> {
        val (unit, range, total) = contentRange.substringAfter(' ').split('/')
        val (rangeStart, rangeEnd) = range.split('-')
        return Triple(rangeStart.toLong(), rangeEnd.toLong(), total.toLong())
    }

    suspend fun download(
        id: String,
        url: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {},
        progressListener: suspend (Long, Long) -> Unit = { _: Long, _: Long -> }
    ) = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            val readChannel = ktor.download(url, progressListener)
            storage.store(url, readChannel)?.let {
                pathDao.upsert(Path(id, it.toString()))
                onSuccess()
            }
        }.onFailure {
            onFailure()
        }
    }

    suspend fun download(
        id: String,
        bucket: String,
        `object`: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {},
        onProgress: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        kotlin.runCatching {
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
            val statObject = minio.client.statObject(
                StatObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(`object`)
                    .build()
            )

            val length = statObject.size()
            val inputStream = minio.client.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(`object`)
                    .offset(offset)
                    .build()
            )
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
            it.printStackTrace()
        }
    }

    suspend fun sessions() {
        ktor.sessions().onSuccess { (_, _, sidList) ->
            sidList?.let {
                val local = dialogDao.dialogs(ktor.uid).map { it.sid }
                it.forEach {
                    if (it !in local) {
                        withContext(Dispatchers.IO) {
                            dialogDao.delete(it)
                        }
                    }
                }
            }
        }
    }

    fun sessionsFlow(): Flow<List<User>> {
        return accountDao.allFlow().map {
            it.mapNotNull {
                withContext(Dispatchers.IO) {
                    userDao.find(it.uid)
                }
            }
        }
    }

    suspend fun findSessionByPairUser(target: Long) = flow<Long> {
        ktor.findSessionByPairUser(target).onSuccess {
            it.data?.let { sid ->
                emit(sid)
                sessionUserDao.upsert(SessionUser(sid, SessionType.P2P, listOf(ktor.uid, target)))
            }
        }.onFailure {
            sessionUserDao.find(SessionType.P2P).firstOrNull {
                it.users.all { it == ktor.uid || it == target }
            }?.let {
                emit(it.sid)
            }
        }
    }.flowOn(Dispatchers.IO)

    fun messages(sid: Long, force: Boolean = false): Flow<List<Message>> = flow {
        database.messageDao().getMessageListBySessionId(sid).let {
            emit(it)
        }
        if (force) {
            ktor.messages(MessageRequest(sid, null to null, MessageRequest.Type.NONE, 0))
                .onSuccess { (_, _, messageList) ->
                    messageList?.also {
                        emit(it)
                    }?.forEach {
                        messageDao.upsert(it)
                    }
                }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun localBeforeMessage(sid: Long, before: Long, size: Int) =
        withContext(Dispatchers.IO) {
            messageDao.getMessageBeforeTime(sid, before, size).let { messages ->
                messages.map { MessageSyncRequest(it.mid, it.updateTime) }.let { request ->
                    ktor.messageSync(request).getOrNull()?.data?.let { response ->
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

    suspend fun getLatestMessageInSession(sid: Long) = withContext(Dispatchers.IO) {
        messageDao.getLatestMessageInSession(sid)
    }

    suspend fun remoteAfterMessage(sid: Long, after: Long): Result<Response<List<Message>>> {
        return ktor.messages(
            MessageRequest(
                sid = sid,
                between = after to null,
                type = MessageRequest.Type.BETWEEN,
                limit = 0
            )
        ).onSuccess { (_, _, list) ->
            withContext(Dispatchers.IO) {
                list?.forEach(messageDao::upsert)
            }
        }
    }

    suspend fun remoteBeforeMessage(
        sid: Long,
        before: Long,
        size: Int
    ): Result<Response<List<Message>>> {
        return ktor.messages(
            MessageRequest(
                sid = sid,
                between = null to before,
                type = MessageRequest.Type.BETWEEN,
                limit = size
            )
        ).onSuccess { (_, _, list) ->
            withContext(Dispatchers.IO) {
                list?.forEach(messageDao::upsert)
            }
        }
    }

    fun message(sid: Long, mid: Long): Flow<Message> = flow {
        ktor.message(mid).onSuccess { (_, _, message) ->
            message?.let {
                emit(it)
                if (messageDao.getMessageById(it.mid) == null) {
                    messageDao.insert(it)
                } else {
                    messageDao.update(it)
                }
            }
        }.onFailure {
            messageDao.getMessageById(mid)?.let {
                emit(it)
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun message(mid: Long): Result<Response<Message>> {
        return ktor.message(mid).onSuccess { (_, _, message) ->
            message?.let {
                withContext(Dispatchers.IO) {
                    messageDao.upsert(it)
                }
            }
        }
    }

    suspend fun readMessage(sid: Long, time: Long): Result<Response<Long>> {
        return ktor.readMessage(sid, time)
    }

    suspend fun queryUser(query: String): Result<Response<List<UserQuery>>> {
        return ktor.queryUser(query)
    }

    suspend fun queryUserFlow(query: String): List<UserQuery> = withContext(Dispatchers.IO) {
        ktor.queryUser(query).getOrNull()?.data?.also {
            it.forEach(queryUserDao::upsert)
        } ?: flow {
            val cursor = database.query(
                "select * from userquery where username regexp ?",
                arrayOf(query.regex().pattern)
            )
            while (cursor.moveToNext()) {
                UserQuery(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2)
                ).let { emit(it) }
            }
        }.toList()
    }

    suspend fun getUser(target: Long): Result<Response<User>> {
        return ktor.getUser(target).onSuccess {
            withContext(Dispatchers.IO) {
                it.data?.let { it1 -> userDao.upsert(it1) }
            }
        }
    }

    suspend fun compose(path: String): ObjectWriteResponse? = withContext(Dispatchers.IO) {
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
        minio.client.composeObject(
            ComposeObjectArgs.builder()
                .bucket(MinIO.ELECTRO)
                .sources(composeSources)
                .`object`(chunks.first().md5)
                .build()
        )
    }

    suspend fun upload(path: String, onProgress: (Float) -> Unit) = withContext(Dispatchers.IO) {
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

                    kotlin.runCatching {
                        if (!minio.checkOrCreateBucket(MinIO.UPLOAD)) {
                            return@withContext
                        }
                        val url = minio.client.getPresignedObjectUrl(
                            GetPresignedObjectUrlArgs.builder()
                                .method(Method.PUT)
                                .bucket(MinIO.UPLOAD)
                                .`object`(chunk.filename)
                                .build()
                        )
                        ktor.upload.put(url) {
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

    suspend fun sendMessage(
        body: MessageSendRequest,
        onFailure: () -> Unit,
        onProgress: (Float) -> Unit
    ) {
        val path = findResource(body.identification())?.path
        path?.let {
            upload(it, onProgress)
        }
        val request = if (path == null) body else when (val attachment =
            Attachment.deserialize(body.type, body.attachment)) {
            is DocumentAttachment -> {
                val pair = path.let {
                    kotlin.runCatching {
                        compose(it) ?: throw Exception("null")
                    }.getOrNull() ?: return
                }
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
                withContext(Dispatchers.IO) {
                    messageSendRequestDao.upsert(it)
                }
            }
        }
        ktor.sendMessage(request).onSuccess { (_, _, messageSendResponse) ->
            messageSendResponse?.let {
                withContext(Dispatchers.IO) {
                    messageSendRequestDao.deleteMessageById(it.id)
                    val message = Message(it)
                    path?.let { path ->
                        pathDao.delete(request.identification())
                        pathDao.upsert(Path(message.identification(), path))
                        chunkDao.delete(path)
                    }
                    messageDao.upsert(message)
                }
            }
        }.onFailure {
            onFailure()
        }
    }

    suspend fun addMessageRequest(
        body: MessageSendRequest,
    ) = withContext(Dispatchers.IO) {
        when (val attachment = Attachment.deserialize(body.type, body.attachment)) {
            is DocumentAttachment -> {
                when (attachment) {
                    is AudioAttachment, is FileAttachment, is ImageAttachment, is VideoAttachment -> {
                        val file =
                            File(application.getExternalFilesDir(null), generateNonce()).also {
                                if (!it.exists()) {
                                    it.createNewFile()
                                }
                            }
                        val md5sum =
                            application.contentResolver.openInputStream(Uri.parse(attachment.uri))
                                ?.use { inputStream ->
                                    application.contentResolver.openOutputStream(file.toUri())
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

    suspend fun createSessionByPairUser(target: Long): Result<Response<Long>> {
        return ktor.createSessionByPairUser(target)
    }

    fun dialogsFlow() = dialogDao.dialogsFlow(ktor.uid)

    suspend fun pullDialogs() = ktor.dialogs().onSuccess { (_, _, dialogs) ->
        withContext(Dispatchers.IO) {
            dialogs?.map(::Dialog)?.onEach(dialogDao::upsert)?.map {
                it.sid
            }?.let { remotes ->
                val locals = dialogDao.dialogs(ktor.uid).map { it.sid }
                locals.forEach { local ->
                    if (local !in remotes) {
                        dialogDao.delete(local)
                    }
                }
            }
        }
    }

    suspend fun pullDialog(sid: Long) = ktor.dialog(sid).onSuccess { (_, _, dialog) ->
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                dialog?.let(::Dialog)?.let(dialogDao::upsert)
            }
        }
    }

    suspend fun getDialog(sid: Long) = flow {
        pullDialog(sid).onSuccess {
            it.data?.run {
                emit(Dialog(this))
            }
        }.onFailure {
            dialogDao.dialog(sid, ktor.uid)?.let {
                emit(it)
            }
        }
    }.flowOn(Dispatchers.IO)

    fun dialogsFlow(query: String): Flow<List<Dialog>> = flow {
        flow {
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
                ).let { emit(it) }
            }
        }.toList().let { emit(it) }
    }.flowOn(Dispatchers.IO)

    fun sessionSearch(title: String): Flow<List<SessionSearch>> = flow {
        flow {
            val cursor = database.query(
                "select * from sessionsearch where title regexp ?",
                arrayOf(title.regex().pattern)
            )
            while (cursor.moveToNext()) {
                SessionSearch(
                    cursor.getLong(0),
                    SessionType.valueOf(cursor.getString(1)),
                    cursor.getStringOrNull(2),
                    cursor.getStringOrNull(3),
                    cursor.getStringOrNull(4),
                    cursor.getLong(5),
                    cursor.getInt(6)
                ).let { emit(it) }
            }
        }.toList().let {
            emit(it)
        }
        ktor.sessionSearch(title).onSuccess { (status, message, sessionSearchList) ->
            sessionSearchList?.let {
                emit(it)
                it.forEach(sessionSearchDao::upsert)
            }
        }
    }.flowOn(Dispatchers.IO)

    fun getSessionUser(sid: Long): Flow<List<Long>> = flow {
        ktor.getSessionUser(sid).onSuccess {
            it.data?.let {
                emit(it.second)
                if (sessionUserDao.find(sid) == null) {
                    sessionUserDao.insert(SessionUser(sid = sid, it.first, it.second))
                } else {
                    sessionUserDao.update(SessionUser(sid = sid, it.first, it.second))
                }
            }
        }.onFailure {
            sessionUserDao.find(sid)?.let {
                emit(it.users)
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getRelationState(target: Long): Result<Response<RelationState>> {
        return ktor.getRelationState(target)
    }

    suspend fun addContact(target: Long): Result<Response<Boolean>> {
        return ktor.addContact(target)
    }

    suspend fun deleteContact(target: Long): Result<Response<Boolean>> {
        return ktor.deleteContact(target)
    }

    suspend fun blockUser(target: Long): Result<Response<Boolean>> {
        return ktor.blockUser(target)
    }

    suspend fun unblockUser(target: Long): Result<Response<Boolean>> {
        return ktor.unblockUser(target)
    }

    suspend fun contact(): Result<Response<List<Long>>> {
        return ktor.contact()
    }

    suspend fun invite(sid: Long, target: Long): Result<Response<Boolean>> {
        return ktor.invite(sid, target)
    }

    fun messageSendingQueue(sid: Long): Flow<List<MessageSendRequest>> {
        return messageSendRequestDao.getMessagesFlow(sid)
    }

    suspend fun deleteMessage(mid: Long): Result<Response<Long>> {
        return ktor.deleteMessage(mid).onFailure {

        }.onSuccess {
            it.data?.let { it1 -> deleteLocalMessage(it1) }
        }
    }

    suspend fun deleteLocalMessage(mid: Long) = withContext(Dispatchers.IO) {
        messageDao.delete(mid)
    }

    suspend fun cancelSendingMessage(id: String) = withContext(Dispatchers.IO) {
        messageSendRequestDao.deleteMessageById(id)
    }

    suspend fun updateUserInfo(userUpdateRequest: UserUpdateRequest): Boolean {
        return ktor.userUpdate(userUpdateRequest).getOrNull()?.data != null
    }

    fun findSession(sid: Long) = flow {
        ktor.findSession(sid).getOrNull()?.data?.let {
            emit(it)
            sessionDao.upsert(it)
        } ?: sessionDao.find(sid)
    }.flowOn(Dispatchers.IO)

    suspend fun updateGroupInfo(sid: Long, request: GroupUpdateRequest): Result<Response<Long>> {
        return ktor.updateGroupInfo(sid, request)
    }

    suspend fun onSessionJoinRequest(sid: Long): Result<Response<SessionUserState>> {
        return ktor.onSessionJoinRequest(sid)
    }

    suspend fun saveSearch(query: String) = withContext(Dispatchers.IO) {
        searchDao.upsert(Search(ktor.uid, query))
    }

    suspend fun loadSearch(): String? = withContext(Dispatchers.IO) {
        searchDao.find(ktor.uid)?.query
    }

    suspend fun exitSession(sid: Long): Result<Response<Long>> {
        return ktor.exitSession(sid)
    }

    suspend fun findAccount(uid: Long) = withContext(Dispatchers.IO) {
        accountDao.findByUser(uid)
    }

    suspend fun removeAccount(uid: Long) = withContext(Dispatchers.IO) {
        accountDao.delete(uid)
    }

    suspend fun getGroupAdmins(sid: Long): Result<Response<List<GroupRole>>> {
        return ktor.getGroupAdmins(sid)
    }

    suspend fun removeGroupAdmin(sid: Long, target: Long): Result<Response<Long>> {
        return ktor.removeGroupAdmin(sid, target)
    }

    suspend fun addGroupAdmin(sid: Long, target: Long): Result<Response<GroupRole>> {
        return ktor.addGroupAdmin(sid, target)
    }

    suspend fun removeGroupMember(sid: Long, target: Long): Result<Response<Long>> {
        return ktor.removeGroupMember(sid, target)
    }

    suspend fun getGroupAdmin(sid: Long, target: Long): Result<Response<GroupRole>> {
        return ktor.getGroupAdmin(sid, target)
    }

    suspend fun getChannelAdmins(sid: Long): Result<Response<List<ChannelRole>>> {
        return ktor.getChannelAdmins(sid)
    }

    suspend fun removeChannelAdmin(sid: Long, target: Long): Result<Response<Long>> {
        return ktor.removeChannelAdmin(sid, target)
    }

    suspend fun addChannelAdmin(sid: Long, target: Long): Result<Response<ChannelRole>> {
        return ktor.addChannelAdmin(sid, target)
    }

    suspend fun removeChannelMember(sid: Long, target: Long): Result<Response<Long>> {
        return ktor.removeChannelMember(sid, target)
    }

    suspend fun getChannelAdmin(sid: Long, target: Long): Result<Response<ChannelRole>> {
        return ktor.getChannelAdmin(sid, target)
    }

    suspend fun updateChannelInfo(sid: Long, request: ChannelUpdateRequest): Result<Response<Long>> {
        return ktor.updateChannelInfo(sid, request)
    }
}

private const val CHUNK_SIZE = 1024 * 1024 * 5
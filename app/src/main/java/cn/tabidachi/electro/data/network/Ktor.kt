package cn.tabidachi.electro.data.network

import android.app.Application
import android.util.Log
import cn.tabidachi.electro.BuildConfig
import cn.tabidachi.electro.data.database.entity.Message
import cn.tabidachi.electro.data.database.entity.MessageSendRequest
import cn.tabidachi.electro.data.database.entity.RelationState
import cn.tabidachi.electro.data.database.entity.Session
import cn.tabidachi.electro.data.database.entity.SessionSearch
import cn.tabidachi.electro.data.database.entity.SessionType
import cn.tabidachi.electro.data.database.entity.SessionUserState
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.ext.ELECTRO
import cn.tabidachi.electro.ext.MINIO
import cn.tabidachi.electro.model.UserQuery
import cn.tabidachi.electro.model.request.CaptchaRequest
import cn.tabidachi.electro.model.request.ChannelUpdateRequest
import cn.tabidachi.electro.model.request.GroupUpdateRequest
import cn.tabidachi.electro.model.request.InviteRequest
import cn.tabidachi.electro.model.request.LoginRequest
import cn.tabidachi.electro.model.request.MessageRequest
import cn.tabidachi.electro.model.request.MessageSyncRequest
import cn.tabidachi.electro.model.request.MessageSyncResponse
import cn.tabidachi.electro.model.request.RegisterRequest
import cn.tabidachi.electro.model.request.SessionCreateRequest
import cn.tabidachi.electro.model.request.UserUpdateRequest
import cn.tabidachi.electro.model.response.AuthResponse
import cn.tabidachi.electro.model.response.ChannelRole
import cn.tabidachi.electro.model.response.GroupRole
import cn.tabidachi.electro.model.response.MessageSendResponse
import cn.tabidachi.electro.model.response.Response
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.FileStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.plugin
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.nio.charset.Charset

class Ktor(
    private val application: Application,
    val host: String = BuildConfig.ELECTRO_SERVER_HOST,
    val port: Int = URLProtocol.ELECTRO.defaultPort,
) {
    val okHttpClient = OkHttpClient()
    val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json = Json {
                prettyPrint = true
                isLenient = true
            })
        }
        install(WebSockets)
        defaultRequest {
            this.host = this@Ktor.host
            this.port = this@Ktor.port
        }
        install(HttpCache) {
            publicStorage(FileStorage(application.cacheDir!!))
        }
    }

    val upload: HttpClient = HttpClient(CIO)

    var token: String? = null
    var uid: Long = 0

    init {
        client.plugin(HttpSend).intercept { request ->
            execute(
                request.apply {
                    token?.let(::bearerAuth)
                }
            )
        }
    }

    val ws = ElectroWebSocket(client, port)

    fun close() {
        ws.stop()
    }

    fun convert(urlBuilder: URLBuilder): URLBuilder {
        return urlBuilder.apply {
            when (protocol.name) {
                URLProtocol.ELECTRO.name -> {
                    protocol = URLProtocol.HTTP
                    host = this@Ktor.host
                    port = this@Ktor.port
                }

                URLProtocol.MINIO.name -> {
                    protocol = URLProtocol.HTTP
                    host = this@Ktor.host
                    port = URLProtocol.MINIO.defaultPort
                }
            }
        }
    }

    fun convert(url: String): URLBuilder {
        return convert(URLBuilder(url))
    }

    fun getUrl(url: String): String {
        return "http://$host:$port$url"
    }

    private suspend inline fun <reified T, reified R> get(
        url: String,
        body: T
    ): Result<Response<R>> = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            val requestBody = Json.encodeToString(body)
                .toRequestBody(ContentType.Application.Json.toString().toMediaTypeOrNull())
            val request = Request.Builder()
                .url(getUrl(url))
                .method(HttpMethod.Get.value, requestBody)
                .header("Authorization", "Bearer $token")
                .build()
            okHttpClient.newCall(request).execute()
//            client.get(url) {
//                contentType(ContentType.Application.Json)
//                setBody(body)
//            }
        }.onFailure {
            Log.e(TAG, "get: $url", it)
        }.mapCatching {
            Json.decodeFromString<Response<R>>(it.bodyAsText())
        }.onFailure {
            Log.e(TAG, "get: $url", it)
        }
    }

    private suspend inline fun <reified R> get(url: String): Result<Response<R>> =
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val request = Request.Builder()
                    .url(getUrl(url))
                    .method(HttpMethod.Get.value, null)
                    .header("Authorization", "Bearer $token")
                    .build()
                okHttpClient.newCall(request).execute()
//            client.get(url) {
//                contentType(ContentType.Application.Json)
//                setBody(Any())
//            }
            }.onFailure {
                Log.e(TAG, "get: $url", it)
            }.mapCatching {
//                val text = it.bodyAsText()
//                println(text)
                Json.decodeFromString<Response<R>>(it.bodyAsText())
            }.onFailure {
                Log.e(TAG, "get: $url", it)
            }
        }

    private suspend inline fun <reified T, reified R> post(
        url: String,
        body: T
    ): Result<Response<R>> = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            Log.e(TAG, "post body: $body")
            // TODO okhttp 实现
            val requestBody = Json.encodeToString(body)
                .toRequestBody(ContentType.Application.Json.toString().toMediaTypeOrNull())
            val request = Request.Builder()
                .url(getUrl(url))
                .method(HttpMethod.Post.value, requestBody)
                .header("Authorization", "Bearer $token")
                .build()
            okHttpClient.newCall(request).execute()
            /* TODO ktor 实现
    TODO    client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            */
        }.onFailure {
            Log.e(TAG, "post: $url", it)
        }.mapCatching {
            Json.decodeFromString<Response<R>>(it.bodyAsText())
        }.onFailure {
            Log.e(TAG, "post: $url", it)
        }
    }

    private suspend inline fun <reified R> post(url: String): Result<Response<R>> =
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val request = Request.Builder()
                    .url(getUrl(url))
                    .method(HttpMethod.Post.value, byteArrayOf().toRequestBody())
                    .header("Authorization", "Bearer $token")
                    .build()
                okHttpClient.newCall(request).execute()
//            client.post(url) {
//                contentType(ContentType.Application.Json)
//            }
            }.onFailure {
                Log.e(TAG, "post: $url", it)
            }.mapCatching {
                Json.decodeFromString<Response<R>>(it.bodyAsText())
            }.onFailure {
                Log.e(TAG, "post: $url", it)
            }
        }

    private suspend inline fun <reified T, reified R> delete(
        url: String,
        body: T
    ): Result<Response<R>> = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            val requestBody = Json.encodeToString(body)
                .toRequestBody(ContentType.Application.Json.toString().toMediaTypeOrNull())
            val request = Request.Builder()
                .url(getUrl(url))
                .method(HttpMethod.Delete.value, requestBody)
                .header("Authorization", "Bearer $token")
                .build()
            okHttpClient.newCall(request).execute()
//            client.delete(url) {
//                contentType(ContentType.Application.Json)
//                setBody(body)
//            }
        }.onFailure {
            Log.e(TAG, "delete: $url", it)
        }.mapCatching {
            Json.decodeFromString<Response<R>>(it.bodyAsText())
        }.onFailure {
            Log.e(TAG, "delete: $url", it)
        }
    }

    private suspend inline fun <reified R> delete(url: String): Result<Response<R>> =
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val request = Request.Builder()
                    .url(getUrl(url))
                    .method(HttpMethod.Delete.value, null)
                    .header("Authorization", "Bearer $token")
                    .build()
                okHttpClient.newCall(request).execute()
//            client.delete(url) {
//                contentType(ContentType.Application.Json)
//            }
            }.onFailure {
                Log.e(TAG, "delete: $url", it)
            }.mapCatching {
                Json.decodeFromString<Response<R>>(it.bodyAsText())
            }.onFailure {
                Log.e(TAG, "delete: $url", it)
            }
        }

    private suspend inline fun <reified T, reified R> patch(
        url: String,
        body: T
    ): Result<Response<R>> = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            val requestBody = Json.encodeToString(body)
                .toRequestBody(ContentType.Application.Json.toString().toMediaTypeOrNull())
            val request = Request.Builder()
                .url(getUrl(url))
                .method(HttpMethod.Patch.value, requestBody)
                .header("Authorization", "Bearer $token")
                .build()
            okHttpClient.newCall(request).execute()
//            client.patch(url) {
//                contentType(ContentType.Application.Json)
//                setBody(body)
//            }
        }.onFailure {
            Log.e(TAG, "patch: $url", it)
        }.mapCatching {
            Json.decodeFromString<Response<R>>(it.bodyAsText())
        }.onFailure {
            Log.e(TAG, "patch: $url", it)
        }
    }

    private suspend inline fun <reified R> patch(url: String): Result<Response<R>> =
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val request = Request.Builder()
                    .url(getUrl(url))
                    .method(HttpMethod.Patch.value, null)
                    .header("Authorization", "Bearer $token")
                    .build()
                okHttpClient.newCall(request).execute()
//            client.patch(url) {
//                contentType(ContentType.Application.Json)
//            }
            }.onFailure {
                Log.e(TAG, "patch: $url", it)
            }.mapCatching {
                Json.decodeFromString<Response<R>>(it.bodyAsText())
            }.onFailure {
                Log.e(TAG, "patch: $url", it)
            }
        }

    suspend fun captcha(body: CaptchaRequest): Result<Response<String?>> {
        return post("/captcha", body)
    }

    suspend fun login(body: LoginRequest): Result<Response<AuthResponse>> = post("/login", body)

    suspend fun register(body: RegisterRequest): Result<Response<AuthResponse>> =
        post("/register", body)

    suspend fun checkUserExist(email: String): Result<Response<String?>> = get("/check/$email")

    suspend fun messages(body: MessageRequest): Result<Response<List<Message>>> {
        return post("/messages", body)
    }

    suspend fun message(mid: Long): Result<Response<Message>> {
        return get("/messages/$mid")
    }

    suspend fun sendMessage(body: MessageSendRequest): Result<Response<MessageSendResponse>> {
        println("Send Message: $body")
        return post("/message", body)
    }

    suspend fun findSessionByPairUser(target: Long): Result<Response<Long?>> {
        return get("/sessions/p2p/$target")
    }

    suspend fun sessions(): Result<Response<List<Long>>> {
        return get("/sessions")
    }

    suspend fun uploadFile(
        bytes: ByteArray,
        contentType: String,
        filename: String
    ): Result<Response<String?>> {
        return kotlin.runCatching {
            client.submitFormWithBinaryData(
                "/files",
                formData = formData {
                    append(
                        "file",
                        bytes,
                        Headers.build {
                            append(HttpHeaders.ContentType, contentType)
                            append(HttpHeaders.ContentDisposition, "filename=$filename")
                        }
                    )
                }
            )
        }.mapCatching {
            Json.decodeFromString(it.bodyAsText())
        }
    }

    suspend fun queryUser(query: String): Result<Response<List<UserQuery>>> {
        return get<List<UserQuery>>("/user/query/${query.encodeURLParameter()}").map {
            it.copy(data = it.data?.map {
                it.copy(avatar = convert(it.avatar).buildString())
            })
        }
    }

    suspend fun getUser(target: Long): Result<Response<User>> {
        return get<User>("/user/$target").map {
            it.copy(data = it.data?.let {
                it.copy(avatar = convert(it.avatar).buildString())
            })
        }
    }

    suspend fun createSessionByPairUser(target: Long): Result<Response<Long>> {
        return post("/sessions/p2p/$target")
    }

    suspend fun dialogs(): Result<Response<List<DialogResponse>>> {
        return get<List<DialogResponse>>("/dialogs").map {
            it.copy(
                data = it.data?.map {
                    it.copy(
                        image = it.image?.let(::convert)?.buildString()
                    )
                }
            )
        }
    }

    suspend fun dialog(sid: Long): Result<Response<DialogResponse>> {
        return get<DialogResponse>("/dialogs/$sid").map {
            it.copy(
                data = it.data?.let {
                    it.copy(image = it.image?.let(::convert)?.buildString())
                }
            )
        }
    }

    suspend fun sessionSearch(title: String): Result<Response<List<SessionSearch>>> {
        return get<List<SessionSearch>>("/search/session/${title.encodeURLParameter()}").map {
            it.copy(
                data = it.data?.map {
                    it.copy(
                        image = it.image?.let(::convert)?.buildString()
                    )
                }
            )
        }
    }

    suspend fun createSession(body: SessionCreateRequest): Result<Response<Long>> {
        return post("/session", body)
    }

    suspend fun download(
        url: String,
        progressListener: suspend (bytesSentTotal: Long, contentLength: Long) -> Unit
    ): ByteReadChannel {
        return client.get(url) {
            onDownload(progressListener)
        }.bodyAsChannel()
    }

    suspend fun getSessionUser(sid: Long): Result<Response<Pair<SessionType, List<Long>>>> {
        return get("/sessions/$sid/users")
    }

    suspend fun getRelationState(target: Long): Result<Response<RelationState>> {
        return get("/relation/$target")
    }

    suspend fun addContact(target: Long): Result<Response<Boolean>> {
        return post("/relation/${target}/contact")
    }

    suspend fun deleteContact(target: Long): Result<Response<Boolean>> {
        return delete("/relation/${target}/contact")
    }

    suspend fun blockUser(target: Long): Result<Response<Boolean>> {
        return post("/relation/${target}/block")
    }

    suspend fun unblockUser(target: Long): Result<Response<Boolean>> {
        return delete("/relation/${target}/block")
    }

    suspend fun contact(): Result<Response<List<Long>>> {
        return get("/relation/contact")
    }

    suspend fun invite(sid: Long, target: Long): Result<Response<Boolean>> {
        return post("/session/invite", InviteRequest(sid, target))
    }

    suspend fun messageSync(body: List<MessageSyncRequest>): Result<Response<MessageSyncResponse>> {
        return post("/messages/sync", body)
    }

    suspend fun deleteMessage(mid: Long): Result<Response<Long>> {
        return delete("/message/$mid")
    }

    suspend fun userUpdate(request: UserUpdateRequest): Result<Response<Long>> {
        return patch("/user", request)
    }

    suspend fun findSession(sid: Long): Result<Response<Session>> {
        return get("/sessions/$sid")
    }

    suspend fun updateGroupInfo(sid: Long, request: GroupUpdateRequest): Result<Response<Long>> {
        return patch("/group/$sid", request)
    }

    suspend fun onSessionJoinRequest(sid: Long): Result<Response<SessionUserState>> {
        return post("/session/$sid/request")
    }

    suspend fun exitSession(sid: Long): Result<Response<Long>> {
        return post("/session/$sid/exit")
    }

    suspend fun readMessage(sid: Long, time: Long): Result<Response<Long>> {
        return post("/messages/$sid/read/$time")
    }

    suspend fun getGroupAdmins(sid: Long): Result<Response<List<GroupRole>>> {
        return get("/group/$sid/admins")
    }

    suspend fun removeGroupAdmin(sid: Long, target: Long): Result<Response<Long>> {
        Log.d(TAG, "removeGroupAdmin: $sid, $target")
        return delete<Long>("/group/$sid/admin/$target")
    }

    suspend fun addGroupAdmin(sid: Long, target: Long): Result<Response<GroupRole>> {
        return post<GroupRole>("/group/$sid/admin/$target")
    }

    suspend fun removeGroupMember(sid: Long, target: Long): Result<Response<Long>> {
        return delete<Long>("/group/$sid/member/$target")
    }

    suspend fun getGroupAdmin(sid: Long, target: Long): Result<Response<GroupRole>> {
        return get<GroupRole>("/group/$sid/admin/$target")
    }

    suspend fun getChannelAdmins(sid: Long): Result<Response<List<ChannelRole>>> {
        return get<List<ChannelRole>>("/channel/$sid/admins").onSuccess {
            Log.d(TAG, "getChannelAdmins: ${it.data?.size}")
        }
    }

    suspend fun removeChannelAdmin(sid: Long, target: Long): Result<Response<Long>> {
        return delete<Long>("/channel/$sid/admin/$target")
    }

    suspend fun addChannelAdmin(sid: Long, target: Long): Result<Response<ChannelRole>> {
        return post("/channel/$sid/admin/$target")
    }

    suspend fun removeChannelMember(sid: Long, target: Long): Result<Response<Long>> {
        return delete<Long>("/channel/$sid/member/$target")
    }

    suspend fun getChannelAdmin(sid: Long, target: Long): Result<Response<ChannelRole>> {
        return get("/channel/$sid/admin/$target")
    }

    suspend fun updateChannelInfo(sid: Long, request: ChannelUpdateRequest): Result<Response<Long>> {
        return patch("/channel/$sid", request)
    }

    companion object {
        val TAG = Ktor::class.simpleName
    }
}

private fun okhttp3.Response.bodyAsText(): String = runBlocking {
    this@bodyAsText.body?.byteString()?.string(Charset.defaultCharset())!!
}.also {
    Log.d("bodyAsText", "bodyAsText: $it")
}

@Serializable
data class DialogResponse(
    val sid: Long,
    val uid: Long,
    val type: SessionType,
    val image: String?,
    val title: String?,
    val subtitle: String?,
    val latest: Long?,
    val unread: Int?,
    val extras: String?
)
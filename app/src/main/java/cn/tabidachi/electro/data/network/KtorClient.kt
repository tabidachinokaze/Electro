package cn.tabidachi.electro.data.network

import android.util.Log
import cn.tabidachi.electro.model.response.Response
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.FileStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

interface Client {
    suspend fun <T, R> get(url: String, body: T): Result<Response<R>>
}

class KtorClient {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                json = Json {
                    prettyPrint = true
                    isLenient = true
                }
            )
        }
        install(WebSockets)
        defaultRequest {
            this.host = "127.0.0.1"
            this.port = 114514
        }
        install(HttpCache) {
            publicStorage(FileStorage(File("")))
        }
    }

    suspend inline fun <reified T, reified R> get(
        url: String,
    ): Result<Response<R>> = withContext(Dispatchers.IO) {
        runCatching {
            client.get(url) {
                contentType(ContentType.Application.Json)
            }
        }.mapCatching {
            Json.decodeFromString<Response<R>>(it.bodyAsText())
        }.onFailure {
            Log.e(TAG, "get: $url", it)
        }
    }

    suspend inline fun <reified T, reified R> get(
        url: String,
        body: T
    ): Result<Response<R>> = withContext(Dispatchers.IO) {
        runCatching {
            client.get(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }.mapCatching {
            Json.decodeFromString<Response<R>>(it.bodyAsText())
        }.onFailure {
            Log.e(TAG, "get: $url", it)
        }
    }

    suspend inline fun <reified T, reified R> post(
        url: String,
    ): Result<Response<R>> = withContext(Dispatchers.IO) {
        runCatching {
            client.post(url) {
                contentType(ContentType.Application.Json)
            }
        }.mapCatching {
            Json.decodeFromString<Response<R>>(it.bodyAsText())
        }.onFailure {
            Log.e(TAG, "post: $url", it)
        }
    }

    suspend inline fun <reified T, reified R> post(
        url: String,
        body: T
    ): Result<Response<R>> = withContext(Dispatchers.IO) {
        runCatching {
            client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }.mapCatching {
            Json.decodeFromString<Response<R>>(it.bodyAsText())
        }.onFailure {
            Log.d(TAG, "post: $url", it)
        }
    }

    suspend inline fun <reified T, reified R> put(
        url: String,
    ): Result<Response<R>> = withContext(Dispatchers.IO) {
        runCatching {
            client.put(url) {
                contentType(ContentType.Application.Json)
            }
        }.mapCatching {
            Json.decodeFromString<Response<R>>(it.bodyAsText())
        }.onFailure {
            Log.d(TAG, "put: $url", it)
        }
    }

    suspend inline fun <reified T, reified R> put(
        url: String,
        body: T
    ): Result<Response<R>> = withContext(Dispatchers.IO) {
        runCatching {
            client.put(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }.mapCatching {
            Json.decodeFromString<Response<R>>(it.bodyAsText())
        }.onFailure {
            Log.e(TAG, "put: $url", it)
        }
    }

    suspend inline fun <reified T, reified R> delete(
        url: String,
    ): Result<Response<R>> = withContext(Dispatchers.IO) {
        runCatching {
            client.delete(url) {
                contentType(ContentType.Application.Json)
            }
        }.mapCatching {
            Json.decodeFromString<Response<R>>(it.bodyAsText())
        }.onFailure {
            Log.d(TAG, "delete: $url", it)
        }
    }

    suspend inline fun <reified T, reified R> delete(
        url: String,
        body: T
    ): Result<Response<R>> = withContext(Dispatchers.IO) {
        runCatching {
            client.delete(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }.mapCatching {
            Json.decodeFromString<Response<R>>(it.bodyAsText())
        }.onFailure {
            Log.e(TAG, "delete: $url", it)
        }
    }

    suspend inline fun <reified T, reified R> patch(
        url: String,
    ): Result<Response<R>> = withContext(Dispatchers.IO) {
        runCatching {
            client.patch(url) {
                contentType(ContentType.Application.Json)
            }
        }.mapCatching {
            Json.decodeFromString<Response<R>>(it.bodyAsText())
        }.onFailure {
            Log.d(TAG, "patch: $url", it)
        }
    }

    suspend inline fun <reified T, reified R> patch(
        url: String,
        body: T
    ): Result<Response<R>> = withContext(Dispatchers.IO) {
        runCatching {
            client.patch(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }.mapCatching {
            Json.decodeFromString<Response<R>>(it.bodyAsText())
        }.onFailure {
            Log.e(TAG, "patch: $url", it)
        }
    }

    companion object {
        val TAG = KtorClient::class.java.simpleName
    }
}

class ClientImpl {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                json = Json {
                    prettyPrint = true
                    isLenient = true
                }
            )
        }
        install(WebSockets)
        defaultRequest {
            this.host = "127.0.0.1"
            this.port = 114514
        }
        install(HttpCache) {
            publicStorage(FileStorage(File("")))
        }
    }
}

class OkHtpClient(
    private val client: Client
) {
    init {
    }
}


interface Json2BeanFactory<I, O> {
    fun to(input: I, type: Class<O>): O
}
package moe.tabidachi.electro.data.service

import io.ktor.client.HttpClient
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.ByteReadChannel

interface FileApi {
    suspend fun download(
        url: String,
        progressListener: suspend (bytesSentTotal: Long, contentLength: Long?) -> Unit
    ): ByteReadChannel
}

class FileApiImpl(
    private val client: HttpClient
) : FileApi {
    override suspend fun download(
        url: String,
        progressListener: suspend (bytesSentTotal: Long, contentLength: Long?) -> Unit
    ): ByteReadChannel {
        return client.get(url) {
            onDownload(progressListener)
        }.bodyAsChannel()
    }
}
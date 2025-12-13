package moe.tabidachi.electro.data.service

import io.ktor.client.HttpClient
import moe.tabidachi.electro.data.database.entity.Message
import moe.tabidachi.electro.data.database.entity.MessageSendRequest
import moe.tabidachi.electro.model.request.MessageRequest
import moe.tabidachi.electro.model.request.MessageSyncRequest
import moe.tabidachi.electro.model.request.MessageSyncResponse
import moe.tabidachi.electro.model.response.MessageSendResponse
import moe.tabidachi.electro.model.response.Response
import moe.tabidachi.electro.data.provider.BaseUrlProvider

interface MessageApi {
    suspend fun messages(body: MessageRequest): Response<List<Message>>
    suspend fun message(mid: Long): Response<Message>
    suspend fun sendMessage(body: MessageSendRequest): Response<MessageSendResponse>
    suspend fun messageSync(body: List<MessageSyncRequest>): Response<MessageSyncResponse>
    suspend fun deleteMessage(mid: Long): Response<Long>
    suspend fun readMessage(sid: Long, time: Long): Response<Long>
}

class MessageApiImpl(
    client: HttpClient,
    baseUrlProvider: BaseUrlProvider
) : MessageApi, BaseKtor(client, baseUrlProvider) {
    override suspend fun messages(body: MessageRequest): Response<List<Message>> {
        return post("messages", body = body)
    }

    override suspend fun message(mid: Long): Response<Message> {
        return get("messages", "$mid")
    }

    override suspend fun sendMessage(body: MessageSendRequest): Response<MessageSendResponse> {
        return post("message", body = body)
    }

    override suspend fun messageSync(body: List<MessageSyncRequest>): Response<MessageSyncResponse> {
        return post("messages", "sync", body = body)
    }

    override suspend fun deleteMessage(mid: Long): Response<Long> {
        return delete("message", "$mid")
    }

    override suspend fun readMessage(
        sid: Long,
        time: Long
    ): Response<Long> {
        return post("messages", "$sid", "read", "$time")
    }
}
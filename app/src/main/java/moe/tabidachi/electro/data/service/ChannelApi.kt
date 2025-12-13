package moe.tabidachi.electro.data.service

import io.ktor.client.HttpClient
import moe.tabidachi.electro.model.request.ChannelUpdateRequest
import moe.tabidachi.electro.model.response.ChannelRole
import moe.tabidachi.electro.model.response.Response
import moe.tabidachi.electro.data.provider.BaseUrlProvider

interface ChannelApi {
    suspend fun getChannelAdmins(sid: Long): Response<List<ChannelRole>>
    suspend fun removeChannelAdmin(sid: Long, target: Long): Response<Long>
    suspend fun addChannelAdmin(sid: Long, target: Long): Response<ChannelRole>
    suspend fun removeChannelMember(sid: Long, target: Long): Response<Long>
    suspend fun getChannelAdmin(sid: Long, target: Long): Response<ChannelRole>
    suspend fun updateChannelInfo(sid: Long, request: ChannelUpdateRequest): Response<Long>
}

class ChannelApiImpl(
    client: HttpClient,
    baseUrlProvider: BaseUrlProvider
) : ChannelApi, BaseKtor(client, baseUrlProvider) {
    override suspend fun getChannelAdmins(sid: Long): Response<List<ChannelRole>> {
        return get("channel", "$sid", "admins")
    }

    override suspend fun removeChannelAdmin(sid: Long, target: Long): Response<Long> {
        return delete("channel", "$sid", "admin", "$target")
    }

    override suspend fun addChannelAdmin(sid: Long, target: Long): Response<ChannelRole> {
        return post("channel", "$sid", "admin", "$target")
    }

    override suspend fun removeChannelMember(sid: Long, target: Long): Response<Long> {
        return delete("channel", "$sid", "member", "$target")
    }

    override suspend fun getChannelAdmin(sid: Long, target: Long): Response<ChannelRole> {
        return get("channel", "$sid", "admin", "$target")
    }

    override suspend fun updateChannelInfo(
        sid: Long,
        request: ChannelUpdateRequest
    ): Response<Long> {
        return patch("channel", "$sid", body = request)
    }
}

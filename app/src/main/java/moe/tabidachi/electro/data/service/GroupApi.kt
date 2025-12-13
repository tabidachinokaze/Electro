package moe.tabidachi.electro.data.service

import io.ktor.client.HttpClient
import moe.tabidachi.electro.model.request.GroupUpdateRequest
import moe.tabidachi.electro.model.response.GroupRole
import moe.tabidachi.electro.model.response.Response
import moe.tabidachi.electro.data.provider.BaseUrlProvider

interface GroupApi {
    suspend fun updateGroupInfo(sid: Long, request: GroupUpdateRequest): Response<Long>
    suspend fun getGroupAdmins(sid: Long): Response<List<GroupRole>>
    suspend fun removeGroupAdmin(sid: Long, target: Long): Response<Long>
    suspend fun addGroupAdmin(sid: Long, target: Long): Response<GroupRole>
    suspend fun removeGroupMember(sid: Long, target: Long): Response<Long>
    suspend fun getGroupAdmin(sid: Long, target: Long): Response<GroupRole>
}

class GroupApiImpl(
    client: HttpClient,
    baseUrlProvider: BaseUrlProvider
) : GroupApi, BaseKtor(client, baseUrlProvider) {
    override suspend fun updateGroupInfo(sid: Long, request: GroupUpdateRequest): Response<Long> {
        return patch("group", "$sid", body = request)
    }

    override suspend fun getGroupAdmins(sid: Long): Response<List<GroupRole>> {
        return get("group", "$sid", "admins")
    }

    override suspend fun removeGroupAdmin(sid: Long, target: Long): Response<Long> {
        return delete("group", "$sid", "admin", "$target")
    }

    override suspend fun addGroupAdmin(sid: Long, target: Long): Response<GroupRole> {
        return post("group", "$sid", "admin", "$target")
    }

    override suspend fun removeGroupMember(sid: Long, target: Long): Response<Long> {
        return delete("group", "$sid", "member", "$target")
    }

    override suspend fun getGroupAdmin(sid: Long, target: Long): Response<GroupRole> {
        return get("group", "$sid", "admin", "$target")
    }
}

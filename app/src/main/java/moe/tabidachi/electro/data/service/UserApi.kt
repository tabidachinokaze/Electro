package moe.tabidachi.electro.data.service

import io.ktor.client.HttpClient
import io.ktor.http.encodeURLPathPart
import moe.tabidachi.electro.data.database.entity.User
import moe.tabidachi.electro.model.UserQuery
import moe.tabidachi.electro.model.request.UserUpdateRequest
import moe.tabidachi.electro.model.response.Response
import moe.tabidachi.electro.data.provider.BaseUrlProvider

interface UserApi {
    suspend fun checkUserExist(email: String): Response<String>
    suspend fun queryUser(query: String): Response<List<UserQuery>>
    suspend fun getUser(target: Long): Response<User>
    suspend fun blockUser(target: Long): Response<Boolean>
    suspend fun unblockUser(target: Long): Response<Boolean>
    suspend fun userUpdate(request: UserUpdateRequest): Response<Long>
}

class UserApiImpl(
    client: HttpClient,
    baseUrlProvider: BaseUrlProvider
) : UserApi, BaseKtor(client, baseUrlProvider) {
    override suspend fun checkUserExist(email: String): Response<String> {
        return get("check", email)
    }

    override suspend fun queryUser(query: String): Response<List<UserQuery>> {
        return get("user", "query", query.encodeURLPathPart())
    }

    override suspend fun getUser(target: Long): Response<User> {
        return get("user", "$target")
    }

    override suspend fun blockUser(target: Long): Response<Boolean> {
        return post("relation", "$target", "block")
    }

    override suspend fun unblockUser(target: Long): Response<Boolean> {
        return delete("relation", "$target", "block")
    }

    override suspend fun userUpdate(request: UserUpdateRequest): Response<Long> {
        return patch("user", body = request)
    }
}

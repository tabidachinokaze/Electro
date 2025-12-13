package moe.tabidachi.electro.data.service

import io.ktor.client.HttpClient
import io.ktor.http.encodeURLPathPart
import moe.tabidachi.electro.data.database.entity.Session
import moe.tabidachi.electro.data.database.entity.SessionSearch
import moe.tabidachi.electro.data.database.entity.SessionType
import moe.tabidachi.electro.data.database.entity.SessionUserState
import moe.tabidachi.electro.model.request.InviteRequest
import moe.tabidachi.electro.model.request.SessionCreateRequest
import moe.tabidachi.electro.model.response.Response
import moe.tabidachi.electro.data.provider.BaseUrlProvider

interface SessionApi {
    suspend fun createSessionByPairUser(target: Long): Response<Long>
    suspend fun findSessionByPairUser(target: Long): Response<Long>
    suspend fun sessions(): Response<List<Long>>
    suspend fun sessionSearch(title: String): Response<List<SessionSearch>>
    suspend fun createSession(body: SessionCreateRequest): Response<Long>
    suspend fun getSessionUser(sid: Long): Response<Pair<SessionType, List<Long>>>
    suspend fun findSession(sid: Long): Response<Session>
    suspend fun exitSession(sid: Long): Response<Long>
    suspend fun onSessionJoinRequest(sid: Long): Response<SessionUserState>
    suspend fun invite(sid: Long, target: Long): Response<Boolean>

}

class SessionApiImpl(
    client: HttpClient,
    baseUrlProvider: BaseUrlProvider
) : SessionApi, BaseKtor(client, baseUrlProvider) {
    override suspend fun createSessionByPairUser(target: Long): Response<Long> {
        return post("sessions", "p2p", "$target")
    }

    override suspend fun findSessionByPairUser(target: Long): Response<Long> {
        return get("sessions", "p2p", "$target")
    }

    override suspend fun sessions(): Response<List<Long>> {
        return get("sessions")
    }

    override suspend fun sessionSearch(title: String): Response<List<SessionSearch>> {
        return get("search", "session", title.encodeURLPathPart())
    }

    override suspend fun createSession(body: SessionCreateRequest): Response<Long> {
        return post("session", body = body)
    }

    override suspend fun getSessionUser(sid: Long): Response<Pair<SessionType, List<Long>>> {
        return get("sessions", "$sid", "users")
    }

    override suspend fun findSession(sid: Long): Response<Session> {
        return get("sessions", "$sid")
    }

    override suspend fun exitSession(sid: Long): Response<Long> {
        return post("session", "$sid", "exit")
    }

    override suspend fun onSessionJoinRequest(sid: Long): Response<SessionUserState> {
        return post("session", "$sid", "request")
    }

    override suspend fun invite(sid: Long, target: Long): Response<Boolean> {
        return post("session", "$target", body = InviteRequest(sid, target))
    }
}
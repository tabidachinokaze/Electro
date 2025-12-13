package moe.tabidachi.electro.data.service

import io.ktor.client.HttpClient
import moe.tabidachi.electro.data.database.entity.RelationState
import moe.tabidachi.electro.model.response.Response
import moe.tabidachi.electro.data.provider.BaseUrlProvider

interface RelationApi {
    suspend fun getRelationState(target: Long): Response<RelationState>
}

class RelationApiImpl(
    client: HttpClient,
    baseUrlProvider: BaseUrlProvider
) : RelationApi, BaseKtor(client, baseUrlProvider) {
    override suspend fun getRelationState(target: Long): Response<RelationState> {
        return get("relation", "$target")
    }
}
package moe.tabidachi.electro.data.service

import io.ktor.client.HttpClient
import moe.tabidachi.electro.model.response.Response
import moe.tabidachi.electro.data.provider.BaseUrlProvider

interface ContactApi {
    suspend fun addContact(target: Long): Response<Boolean>
    suspend fun deleteContact(target: Long): Response<Boolean>
    suspend fun contact(): Response<List<Long>>
}

class ContactApiImpl(
    client: HttpClient,
    baseUrlProvider: BaseUrlProvider
) : ContactApi, BaseKtor(client, baseUrlProvider) {
    override suspend fun addContact(target: Long): Response<Boolean> {
        return post("relation", "$target", "contact")
    }

    override suspend fun deleteContact(target: Long): Response<Boolean> {
        return delete("relation", "$target", "contact")
    }

    override suspend fun contact(): Response<List<Long>> {
        return get("relation", "contact")
    }
}
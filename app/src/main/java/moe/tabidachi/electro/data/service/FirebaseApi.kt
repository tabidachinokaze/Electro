package moe.tabidachi.electro.data.service

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.path
import moe.tabidachi.electro.data.provider.BaseUrlProvider

interface FirebaseApi {
    suspend fun firebase(result: String)
}

class FirebaseApiImpl(
    private val client: HttpClient,
    private val baseUrlProvider: BaseUrlProvider
) : FirebaseApi {
    override suspend fun firebase(result: String) {
        client.post(baseUrlProvider.getBaseUrl()) {
            url { path("firebase") }
            setBody(result)
        }
    }
}
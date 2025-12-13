package moe.tabidachi.electro.data.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import moe.tabidachi.electro.model.response.Response
import moe.tabidachi.electro.data.provider.BaseUrlProvider

abstract class BaseKtor(
    val client: HttpClient,
    private val baseUrlProvider: BaseUrlProvider
) {
    val baseUrl: String get() = baseUrlProvider.getBaseUrl()

    suspend inline fun <reified R> get(
        vararg path: String,
    ): Response<R> {
        val response = client.get(baseUrl) {
            url {
                path(*path)
            }
            contentType(ContentType.Application.Json)
        }
        return response.body()
    }

    suspend inline fun <reified T, reified R> get(
        vararg path: String,
        body: T
    ): Response<R> {
        val response = client.get(baseUrl) {
            url {
                path(*path)
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        return response.body()
    }

    suspend inline fun <reified R> post(
        vararg path: String,
    ): Response<R> {
        val response = client.post(baseUrl) {
            url {
                path(*path)
            }
            contentType(ContentType.Application.Json)
        }
        return response.body()
    }

    suspend inline fun <reified T, reified R> post(
        vararg path: String,
        body: T
    ): Response<R> {
        val response = client.post(baseUrl) {
            url {
                path(*path)
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        return response.body()
    }

    suspend inline fun <reified R> delete(
        vararg path: String,
    ): Response<R> {
        val response = client.delete(baseUrl) {
            url {
                path(*path)
            }
            contentType(ContentType.Application.Json)
        }
        return response.body()
    }

    suspend inline fun <reified T, reified R> patch(
        vararg path: String,
        body: T
    ): Response<R> {
        val response = client.patch(baseUrl) {
            url {
                path(*path)
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        return response.body()
    }
}

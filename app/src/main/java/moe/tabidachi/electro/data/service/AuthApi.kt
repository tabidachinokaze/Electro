package moe.tabidachi.electro.data.service

import io.ktor.client.HttpClient
import moe.tabidachi.electro.model.request.CaptchaRequest
import moe.tabidachi.electro.model.request.LoginRequest
import moe.tabidachi.electro.model.request.RegisterRequest
import moe.tabidachi.electro.model.response.AuthResponse
import moe.tabidachi.electro.model.response.Response
import moe.tabidachi.electro.data.provider.BaseUrlProvider

interface AuthApi {
    suspend fun captcha(body: CaptchaRequest): Response<String>
    suspend fun login(body: LoginRequest): Response<AuthResponse>
    suspend fun register(body: RegisterRequest): Response<AuthResponse>
}

class AuthApiImpl(
    client: HttpClient,
    baseUrlProvider: BaseUrlProvider
) : AuthApi, BaseKtor(client, baseUrlProvider) {
    override suspend fun captcha(body: CaptchaRequest): Response<String> {
        return post("captcha", body = body)
    }

    override suspend fun login(body: LoginRequest): Response<AuthResponse> {
        return post("login", body = body)
    }

    override suspend fun register(body: RegisterRequest): Response<AuthResponse> {
        return post("register", body = body)
    }
}

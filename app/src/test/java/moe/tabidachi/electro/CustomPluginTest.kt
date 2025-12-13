package moe.tabidachi.electro

import kotlinx.coroutines.runBlocking
import moe.tabidachi.electro.data.service.AuthApi
import moe.tabidachi.electro.data.service.AuthApiImpl
import moe.tabidachi.electro.model.request.LoginRequest
import moe.tabidachi.electro.shared.SharedHttpClient
import moe.tabidachi.electro.shared.SharedJson
import org.junit.Test

class CustomPluginTest {
    private val client = SharedHttpClient(SharedJson()) { null }
    private val authApi: AuthApi = AuthApiImpl(client) {
        "http://tabidachi.lan:23333"
    }

    @Test
    fun test() {
        runBlocking {
            val response = authApi.login(LoginRequest("kaze@tabidachi.moe", "kaze2025"))
            println(response)
        }
    }
}

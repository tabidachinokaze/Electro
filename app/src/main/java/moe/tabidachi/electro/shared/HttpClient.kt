package moe.tabidachi.electro.shared

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LoggingFormat
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.plugin
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.request.bearerAuth
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import moe.tabidachi.electro.data.database.entity.SessionSearch
import moe.tabidachi.electro.data.database.entity.User
import moe.tabidachi.electro.data.provider.TokenProvider
import moe.tabidachi.electro.model.UserQuery
import moe.tabidachi.electro.model.response.DialogResponse
import moe.tabidachi.electro.model.response.Response
import kotlin.time.Duration.Companion.seconds

@Suppress("FunctionName")
fun SharedHttpClient(
    json: Json,
    tokenProvider: TokenProvider,
    onUrlConvertConfig: (UrlConvertConfig) -> Unit
): HttpClient {
    return HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                json = json
            )
        }
        install(WebSockets) {
            pingInterval = 10.seconds
            contentConverter = KotlinxWebsocketSerializationConverter(
                format = json
            )
        }
        install(Logging) {
            format = LoggingFormat.Default
            level = LogLevel.ALL
            logger = Logger.SIMPLE
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
        install(UrlConvertPlugin) {
            onUrlConvertConfig(this)
            transform<Response<List<UserQuery>>> {
                it.copy(data = it.data?.map { it.copy(avatar = convert(it.avatar)) })
            }
            transform<Response<User>> {
                it.copy(data = it.data?.let { it.copy(avatar = convert(it.avatar)) })
            }
            transform<Response<List<SessionSearch>>> {
                it.copy(data = it.data?.map { it.copy(image = it.image?.let(::convert)) })
            }
            transform<Response<List<DialogResponse>>> {
                it.copy(data = it.data?.map { it.copy(image = it.image?.let(::convert)) })
            }
            transform<Response<DialogResponse>> {
                it.copy(data = it.data?.copy(image = it.data.image?.let(::convert)))
            }
        }
    }.also {
        it.plugin(HttpSend).intercept { request ->
            tokenProvider.getToken()?.let(request::bearerAuth)
            execute(request)
        }
    }
}

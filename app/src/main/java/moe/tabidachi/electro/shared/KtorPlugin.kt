package moe.tabidachi.electro.shared

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.ClientHook
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.HttpResponseContainer
import io.ktor.client.statement.HttpResponsePipeline
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.content.NullBody
import io.ktor.util.pipeline.PipelinePhase
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import moe.tabidachi.electro.ext.ELECTRO
import moe.tabidachi.electro.ext.MINIO
import moe.tabidachi.electro.ktx.TAG

class UrlConvertConfig {
    var electroUrl: URLBuilder? = null
    var minioUrl: URLBuilder? = null
    val transforms = mutableMapOf<TypeInfo, Function1<Any, Any>>()

    inline fun <reified T> transform(noinline block: Function1<T, T>) {
        val typeInfo: TypeInfo = typeInfo<T>()
        transforms[typeInfo] = { input ->
            require(input is T) { "Expected type ${T::class}, but got ${input::class}" }
            block(input) as Any
        }
    }

    fun convert(urlBuilder: URLBuilder): URLBuilder {
        return urlBuilder.apply {
            when (protocol.name) {
                URLProtocol.ELECTRO.name -> {
                    electroUrl?.let { electroUrl ->
                        protocol = electroUrl.protocol
                        host = electroUrl.host
                        port = electroUrl.port
                    }
                }

                URLProtocol.MINIO.name -> {
                    minioUrl?.let { minioUrl ->
                        protocol = minioUrl.protocol
                        host = minioUrl.host
                        port = minioUrl.port
                    }
                }
            }
        }
    }

    fun convert(url: String): String {
        return convert(URLBuilder(url)).buildString().also {
            Log.d(TAG, "convert: $url -> $it")
        }
    }
}

val UrlConvertPlugin = createClientPlugin("UrlConvertPlugin", ::UrlConvertConfig) {
    this.on(
        hook = UrlConvertClientHook,
        handler = { response, requestedType: TypeInfo ->
            pluginConfig.transforms[requestedType]?.invoke(response) ?: response
        }
    )
}

object UrlConvertClientHook : ClientHook<suspend (response: Any, requestedType: TypeInfo) -> Any?> {
    override fun install(
        client: HttpClient,
        handler: suspend (
            response: Any,
            requestedType: TypeInfo
        ) -> Any?
    ) {
        val afterTransform = PipelinePhase("AfterTransform")
        client.responsePipeline.insertPhaseAfter(HttpResponsePipeline.After, afterTransform)
        client.responsePipeline.intercept(afterTransform) {
            val (typeInfo, content) = subject
            val newContent = handler(it.response, it.expectedType)
                ?: return@intercept
            if (newContent !is NullBody && !typeInfo.type.isInstance(newContent)) {
                error("transformResponseBody returned $newContent but expected value of type $typeInfo")
            }
            proceedWith(HttpResponseContainer(typeInfo, newContent))
        }
    }
}

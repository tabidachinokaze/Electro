package moe.tabidachi.electro.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.http.URLBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import moe.tabidachi.electro.data.network.ElectroWebSocket
import moe.tabidachi.electro.data.provider.BaseUrlProvider
import moe.tabidachi.electro.data.provider.TokenProvider
import moe.tabidachi.electro.data.provider.UidProvider
import moe.tabidachi.electro.data.repository.SharedRepository
import moe.tabidachi.electro.initializer.Initializer
import moe.tabidachi.electro.shared.SharedHttpClient
import moe.tabidachi.electro.shared.SharedJson
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SharedModule {
    @Provides
    @Singleton
    @GlobalCoroutineScope
    fun provideGlobalCoroutineScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

    @Provides
    @Singleton
    fun provideInitializer(
        @ApplicationContext
        context: Context,
        sharedRepository: SharedRepository
    ): Initializer {
        return Initializer(
            context = context,
            sharedRepository = sharedRepository
        )
    }

    @Provides
    @Singleton
    fun provideJson(): Json {
        return SharedJson()
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        json: Json,
        tokenProvider: TokenProvider,
        sharedRepository: SharedRepository,
        @GlobalCoroutineScope
        scope: CoroutineScope
    ): HttpClient {
        return SharedHttpClient(
            json = json, tokenProvider = tokenProvider,
            onUrlConvertConfig = { config ->
                sharedRepository.state.map {
                    it.baseUrl to it.minioUrl
                }.onEach {
                    config.electroUrl = URLBuilder(it.first)
                    config.minioUrl = URLBuilder(it.second)
                }.launchIn(scope)
            }
        )
    }

    @Provides
    @Singleton
    fun provideWebSocket(
        client: HttpClient,
        baseUrlProvider: BaseUrlProvider
    ): ElectroWebSocket {
        return ElectroWebSocket(
            client = client,
            baseUrlProvider = baseUrlProvider
        )
    }

    @Provides
    @Singleton
    fun provideBaseUrlProvider(
        sharedRepository: SharedRepository
    ): BaseUrlProvider {
        return BaseUrlProvider { sharedRepository.state.value.baseUrl }
    }

    @Provides
    @Singleton
    fun provideTokenProvider(
        sharedRepository: SharedRepository
    ): TokenProvider {
        return TokenProvider { with(sharedRepository.state.value) { tokens[currentUserId] } }
    }

    @Provides
    @Singleton
    fun provideUidProvider(
        sharedRepository: SharedRepository
    ): UidProvider {
        return UidProvider { sharedRepository.state.value.currentUserId }
    }
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class GlobalCoroutineScope

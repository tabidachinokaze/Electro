package moe.tabidachi.electro.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import moe.tabidachi.electro.data.provider.BaseUrlProvider
import moe.tabidachi.electro.data.service.AuthApi
import moe.tabidachi.electro.data.service.AuthApiImpl
import moe.tabidachi.electro.data.service.ChannelApi
import moe.tabidachi.electro.data.service.ChannelApiImpl
import moe.tabidachi.electro.data.service.ContactApi
import moe.tabidachi.electro.data.service.ContactApiImpl
import moe.tabidachi.electro.data.service.DialogApi
import moe.tabidachi.electro.data.service.DialogApiImpl
import moe.tabidachi.electro.data.service.FileApi
import moe.tabidachi.electro.data.service.FileApiImpl
import moe.tabidachi.electro.data.service.FirebaseApi
import moe.tabidachi.electro.data.service.FirebaseApiImpl
import moe.tabidachi.electro.data.service.GroupApi
import moe.tabidachi.electro.data.service.GroupApiImpl
import moe.tabidachi.electro.data.service.MessageApi
import moe.tabidachi.electro.data.service.MessageApiImpl
import moe.tabidachi.electro.data.service.RelationApi
import moe.tabidachi.electro.data.service.RelationApiImpl
import moe.tabidachi.electro.data.service.SessionApi
import moe.tabidachi.electro.data.service.SessionApiImpl
import moe.tabidachi.electro.data.service.UserApi
import moe.tabidachi.electro.data.service.UserApiImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideAuthApi(
        client: HttpClient,
        baseUrlProvider: BaseUrlProvider
    ): AuthApi {
        return AuthApiImpl(
            client = client,
            baseUrlProvider = baseUrlProvider
        )
    }

    @Provides
    @Singleton
    fun provideUserApi(
        client: HttpClient,
        baseUrlProvider: BaseUrlProvider
    ): UserApi {
        return UserApiImpl(
            client = client,
            baseUrlProvider = baseUrlProvider
        )
    }

    @Provides
    @Singleton
    fun provideSessionApi(
        client: HttpClient,
        baseUrlProvider: BaseUrlProvider
    ): SessionApi {
        return SessionApiImpl(
            client = client,
            baseUrlProvider = baseUrlProvider
        )
    }

    @Provides
    @Singleton
    fun provideContactApi(
        client: HttpClient,
        baseUrlProvider: BaseUrlProvider
    ): ContactApi = ContactApiImpl(
        client = client,
        baseUrlProvider = baseUrlProvider
    )

    @Provides
    @Singleton
    fun provideGroupApi(
        client: HttpClient,
        baseUrlProvider: BaseUrlProvider
    ): GroupApi = GroupApiImpl(
        client = client,
        baseUrlProvider = baseUrlProvider
    )

    @Provides
    @Singleton
    fun provideChannelApi(
        client: HttpClient,
        baseUrlProvider: BaseUrlProvider
    ): ChannelApi = ChannelApiImpl(
        client = client,
        baseUrlProvider = baseUrlProvider
    )

    @Provides
    @Singleton
    fun provideMessageApi(
        client: HttpClient,
        baseUrlProvider: BaseUrlProvider
    ): MessageApi = MessageApiImpl(
        client = client,
        baseUrlProvider = baseUrlProvider
    )

    @Provides
    @Singleton
    fun provideDialogApi(
        client: HttpClient,
        baseUrlProvider: BaseUrlProvider
    ): DialogApi = DialogApiImpl(
        client = client,
        baseUrlProvider = baseUrlProvider
    )

    @Provides
    @Singleton
    fun provideRelationApi(
        client: HttpClient,
        baseUrlProvider: BaseUrlProvider
    ): RelationApi = RelationApiImpl(
        client = client,
        baseUrlProvider = baseUrlProvider
    )

    @Provides
    @Singleton
    fun provideFileApi(
        client: HttpClient,
    ): FileApi = FileApiImpl(client = client)

    @Provides
    @Singleton
    fun provideFirebaseApi(
        client: HttpClient,
        baseUrlProvider: BaseUrlProvider
    ): FirebaseApi {
        return FirebaseApiImpl(
            client = client,
            baseUrlProvider = baseUrlProvider,
        )
    }
}

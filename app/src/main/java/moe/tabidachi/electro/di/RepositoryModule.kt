package moe.tabidachi.electro.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import moe.tabidachi.electro.ElectroStorage
import moe.tabidachi.electro.data.ElectroRepository
import moe.tabidachi.electro.data.ElectroRepositoryImpl
import moe.tabidachi.electro.data.database.ElectroDatabase
import moe.tabidachi.electro.data.network.MinIO
import moe.tabidachi.electro.data.repository.SharedRepository
import moe.tabidachi.electro.data.repository.SharedRepositoryImpl
import moe.tabidachi.electro.data.service.DialogApi
import moe.tabidachi.electro.data.service.FileApi
import moe.tabidachi.electro.data.service.MessageApi
import moe.tabidachi.electro.data.service.SessionApi
import moe.tabidachi.electro.data.service.UserApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideSharedRepository(): SharedRepository {
        return SharedRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideRepository(
        @ApplicationContext
        context: Context,
        database: ElectroDatabase,
        storage: ElectroStorage,
        minio: MinIO,
        userApi: UserApi,
        sessionApi: SessionApi,
        messageApi: MessageApi,
        dialogApi: DialogApi,
        sharedRepository: SharedRepository,
        fileApi: FileApi,
        client: HttpClient
    ): ElectroRepository {
        return ElectroRepositoryImpl(
            context = context,
            database = database,
            storage = storage,
            minio = minio,
            userApi = userApi,
            sessionApi = sessionApi,
            messageApi = messageApi,
            dialogApi = dialogApi,
            sharedState = sharedRepository.state,
            fileApi = fileApi,
            client = client,
        )
    }
}
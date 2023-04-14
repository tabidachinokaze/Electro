package cn.tabidachi.electro.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import cn.tabidachi.electro.ElectroStorage
import cn.tabidachi.electro.data.Repository
import cn.tabidachi.electro.data.database.ElectroDatabase
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.data.network.MinIO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ElectroModule {
    @Singleton
    @Provides
    fun provideKtorClient(
        application: Application
    ): Ktor {
        return Ktor(application)
    }

    @Singleton
    @Provides
    fun provideMinio(): MinIO {
        return MinIO()
    }

    @Singleton
    @Provides
    fun provideStorage(
        application: Application,
        ktor: Ktor
    ): ElectroStorage {
        return ElectroStorage(application = application, ktor = ktor)
    }
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class RemoteDataSource

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class LocalDataSource

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideRepository(
        application: Application,
        database: ElectroDatabase,
        ktor: Ktor,
        storage: ElectroStorage,
        minio: MinIO
    ) = Repository(application, database, ktor, storage, minio)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext
        context: Context
    ): ElectroDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ElectroDatabase::class.java,
            "electro.db"
        ).build()
    }
}
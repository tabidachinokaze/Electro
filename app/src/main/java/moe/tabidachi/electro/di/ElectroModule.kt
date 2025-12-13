package moe.tabidachi.electro.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import moe.tabidachi.electro.ElectroStorage
import moe.tabidachi.electro.data.database.ElectroDatabase
import moe.tabidachi.electro.data.network.MinIO
import moe.tabidachi.electro.data.repository.SharedRepository
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ElectroModule {
    @Singleton
    @Provides
    fun provideMinio(
        sharedRepository: SharedRepository
    ): MinIO {
        return MinIO(sharedRepository)
    }

    @Singleton
    @Provides
    fun provideStorage(
        application: Application,
    ): ElectroStorage {
        return ElectroStorage(application = application)
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
package moe.tabidachi.electro.initializer

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import moe.tabidachi.electro.BuildConfig
import moe.tabidachi.electro.Prefs
import moe.tabidachi.electro.data.repository.SharedRepository
import moe.tabidachi.electro.ext.dataStore

class Initializer(
    private val context: Context,
    private val sharedRepository: SharedRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) {
    operator fun invoke() {
        context.dataStore.data.map {
            val baseUrl = it[Prefs.BASE_URL] ?: BuildConfig.ELECTRO_SERVER_URL
            val minioUrl = it[Prefs.MINIO_URL] ?: BuildConfig.MINIO_URL
            sharedRepository.updateState {
                it.copy(
                    baseUrl = baseUrl,
                    minioUrl = minioUrl,
                    accessKey = BuildConfig.MINIO_ACCESS_KEY,
                    secretKey = BuildConfig.MINIO_SECRET_KEY
                )
            }
        }.launchIn(scope)
    }
}
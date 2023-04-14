package cn.tabidachi.electro

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import cn.tabidachi.electro.data.network.Ktor
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpMethod
import io.ktor.util.hex
import io.ktor.util.sha1
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ElectroStorage(
    private val application: Application,
    val directory: File = application.getExternalFilesDir(null)!!,
    private val ktor: Ktor,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    init {
        directory.mkdirs()
    }

    fun filename(url: String): String {
        return url.toByteArray().let(::sha1).let(::hex)
    }

    suspend fun store(url: String, readChannel: ByteReadChannel): File? =
        withContext(Dispatchers.IO) {
            val filename = url.toByteArray().let(::sha1).let(::hex)
            val file = File(directory, filename)
            val exists = file.exists()
            if (!exists && !file.createNewFile()) {
                return@withContext null
            }
            application.contentResolver.openOutputStream(file.toUri())?.use { outputStream ->
                readChannel.copyTo(outputStream)
            }
            file
        }

    suspend fun store(
        url: String,
        strategy: StoreConflictStrategy = StoreConflictStrategy.IGNORE
    ): File? = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            val filename = url.toByteArray().let(::sha1).let(::hex)
            val file = File(directory, filename)
            val exists = file.exists()
            if (!exists && !file.createNewFile()) {
                return@withContext file
            }
            when (strategy) {
                StoreConflictStrategy.REPLACE -> {}
                StoreConflictStrategy.IGNORE -> {
                    if (exists) {
                        return@withContext file
                    }
                }
            }
            val channel = ktor.client.get {
                url("")
                method = HttpMethod.Get
            }.bodyAsChannel()
            application.contentResolver.openOutputStream(file.toUri())?.use { outputStream ->
                channel.copyTo(outputStream)
            }
//            URL(url).openStream()?.use { inputStream ->
//                application.contentResolver.openOutputStream(file.toUri())?.use { outputStream ->
//                    inputStream.copyTo(outputStream)
//                }
//            }
            file
        }.getOrNull()
    }

    suspend fun store(uri: Uri, strategy: StoreConflictStrategy = StoreConflictStrategy.IGNORE) =
        withContext(Dispatchers.IO) {
            val filename = uri.toString().toByteArray().let(::sha1).let(::hex)
            val file = File(directory, filename)
            val exists = file.exists()
            if (!exists && !file.createNewFile()) {
                return@withContext file
            }
            when (strategy) {
                StoreConflictStrategy.REPLACE -> {}
                StoreConflictStrategy.IGNORE -> {
                    if (exists) {
                        return@withContext file
                    }
                }
            }
            application.contentResolver.openInputStream(uri)?.use { inputStream ->
                application.contentResolver.openOutputStream(file.toUri())?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        }

    suspend fun store(filename: String, uri: Uri, force: Boolean = false): File =
        withContext(Dispatchers.IO) {
            val file = File(directory, filename)
            val exists = file.exists()
            if (!exists) {
                file.createNewFile()
            } else if (!force) {
                return@withContext file
            }
            application.contentResolver.openInputStream(uri)?.use { inputStream ->
                application.contentResolver.openOutputStream(file.toUri())?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        }

    suspend fun store(filename: String, url: String, force: Boolean = false) =
        withContext(Dispatchers.IO) {
            val file = File(directory, filename)
            val exists = file.exists()
            if (!exists) {
                file.createNewFile()
            } else if (!force) {
                return@withContext file
            }
            val channel = ktor.client.get {
                url(url)
                method = HttpMethod.Get
            }.bodyAsChannel()
            application.contentResolver.openOutputStream(file.toUri())?.use { outputStream ->
                channel.copyTo(outputStream)
            }
            file
        }

    fun find(uri: Uri): File {
        val filename = uri.toString().toByteArray().let(::sha1).let(::hex)
        return File(directory, filename)
    }

    fun find(url: String): File {
        val filename = url.toByteArray().let(::sha1).let(::hex)
        return File(directory, filename)
    }
}

enum class StoreConflictStrategy {
    REPLACE, IGNORE
}
package moe.tabidachi.electro

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
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
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    init {
        directory.mkdirs()
    }

    fun filename(url: String): String {
        return url.toByteArray().let(::sha1).let(::hex)
    }

    suspend fun store(url: String, readChannel: ByteReadChannel): File? = withContext(dispatcher) {
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

    fun find(uri: Uri): File {
        val filename = uri.toString().toByteArray().let(::sha1).let(::hex)
        return File(directory, filename)
    }

    fun find(url: String): File {
        val filename = url.toByteArray().let(::sha1).let(::hex)
        return File(directory, filename)
    }
}

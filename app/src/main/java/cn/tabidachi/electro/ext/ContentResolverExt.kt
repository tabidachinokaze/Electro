package cn.tabidachi.electro.ext

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toFile

fun Uri.openableColumns(contentResolver: ContentResolver): Result<UriDetail> {
    return when (this.scheme) {
        ContentResolver.SCHEME_CONTENT -> {
            kotlin.runCatching {
                contentResolver.query(
                    this,
                    arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    cursor.moveToFirst()
                    UriDetail(
                        cursor.getString(0),
                        cursor.getLong(1)
                    )
                }!!
            }
        }
        else -> {
            Result.success(
                UriDetail(
                    toFile().name,
                    toFile().length()
                )
            )
        }
    }
}

class UriDetail(
    val name: String,
    val size: Long
)
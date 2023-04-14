package cn.tabidachi.electro.ext

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import cn.tabidachi.electro.AudioPreviewActivity
import java.io.File

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "electro")

fun Context.openExternal(file: File, type: String) {
    val uri = FileProvider.getUriForFile(
        this,
        "cn.tabidachi.electro.FileProvider",
        file
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, type)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    this.startActivity(intent)
}

fun Context.fileProvider(path: String): Uri {
    return FileProvider.getUriForFile(
        this,
        "cn.tabidachi.electro.FileProvider",
        File(path)
    )
}

fun Context.openExternal(path: String, type: String) {
    val uri = FileProvider.getUriForFile(
        this,
        "cn.tabidachi.electro.FileProvider",
        File(path)
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, type)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    this.startActivity(intent)
}

fun Context.audioPreview(path: String) {
    val uri = FileProvider.getUriForFile(
        this,
        "cn.tabidachi.electro.FileProvider",
        File(path)
    )
    val intent = Intent(this, AudioPreviewActivity::class.java).apply {
        setDataAndType(uri, type)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    this.startActivity(intent)
}

fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}


fun Context.applicationSettings() {
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.data =
        Uri.fromParts("package", this.packageName, null)
    this.startActivity(intent)
}

fun Context.checkPermission(permission: String): Boolean {
    return if (
        ActivityCompat.checkSelfPermission(this, permission)
        != PackageManager.PERMISSION_GRANTED
    ) {
        this.applicationSettings()
        false
    } else {
        true
    }
}
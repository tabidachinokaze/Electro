package moe.tabidachi.electro

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object Prefs {
    val THEME = stringPreferencesKey("theme")
    val DARK_LIGHT = stringPreferencesKey("dark_light")
    val TOKEN = stringPreferencesKey("token")
    val UID = longPreferencesKey("uid")
    val BASE_URL = stringPreferencesKey("base_url")
    val MINIO_URL = stringPreferencesKey("minio_url")
}

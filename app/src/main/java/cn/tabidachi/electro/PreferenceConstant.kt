package cn.tabidachi.electro

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceConstant {
    const val TOKEN = "token"
    const val UID = "uid"
    const val DAY_NIGHT = "day_night"
    const val THEME = "theme"
    object Key {
        val THEME = stringPreferencesKey(PreferenceConstant.THEME)
        val DARK_LIGHT = stringPreferencesKey("dark_light")
        val TOKEN = stringPreferencesKey(PreferenceConstant.TOKEN)
        val UID = longPreferencesKey(PreferenceConstant.UID)
    }
}
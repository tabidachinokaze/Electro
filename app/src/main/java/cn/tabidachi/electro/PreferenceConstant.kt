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
        val DAY_NIGHT = stringPreferencesKey(PreferenceConstant.DAY_NIGHT)
        val TOKEN = stringPreferencesKey(PreferenceConstant.TOKEN)
        val UID = longPreferencesKey(PreferenceConstant.UID)
    }
}
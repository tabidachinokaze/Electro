package cn.tabidachi.electro.ext

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val TextStyle.Companion.AuthButtonText: TextStyle
    get() {
        return TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
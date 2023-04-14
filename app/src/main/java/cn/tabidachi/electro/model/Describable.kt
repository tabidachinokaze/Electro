package cn.tabidachi.electro.model

import androidx.compose.runtime.Composable

interface Describable {
    @Composable
    fun description(): String
}
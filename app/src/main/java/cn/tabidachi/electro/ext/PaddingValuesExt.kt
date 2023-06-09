package cn.tabidachi.electro.ext

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection

@Composable
operator fun PaddingValues.plus(paddingValues: PaddingValues): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = this.calculateStartPadding(direction) + paddingValues.calculateStartPadding(direction),
        top = this.calculateTopPadding() + paddingValues.calculateTopPadding(),
        end = this.calculateEndPadding(direction) + paddingValues.calculateEndPadding(direction),
        bottom = this.calculateBottomPadding() + paddingValues.calculateBottomPadding(),
    )
}
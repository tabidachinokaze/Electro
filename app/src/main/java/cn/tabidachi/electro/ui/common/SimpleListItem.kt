package cn.tabidachi.electro.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier

@Composable
fun SimpleListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) = Row(modifier) {
    leadingContent?.let {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            it()
        }
    }
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.weight(1f)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            ProvideTextStyle(MaterialTheme.typography.bodyLarge, headlineContent)
        }
        supportingContent?.let {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                ProvideTextStyle(MaterialTheme.typography.bodyMedium, it)
            }
        }
    }
    trailingContent?.let {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            ProvideTextStyle(MaterialTheme.typography.labelSmall, it)
        }
    }
}

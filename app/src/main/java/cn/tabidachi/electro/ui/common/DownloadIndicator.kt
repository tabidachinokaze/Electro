package cn.tabidachi.electro.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.model.DownloadMessageItem

@Composable
fun DownloadIndicator(
    item: DownloadMessageItem,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    FilledIconButton(
        onClick = {
            if (item.path == null) {
                if (item.downloading) {
                    item.cancel()
                } else {
                    item.download(false)
                }
            }
            onClick()
        }, colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            )
        )
    ) {
        icon?.invoke()
        if (item.path == null) {
            Icon(
                imageVector = if (item.downloading) Icons.Rounded.Clear else Icons.Rounded.ArrowDownward,
                contentDescription = null
            )
            if (item.downloading) {
                CircularProgressIndicator(
                    progress = item.progress,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(40.dp),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}
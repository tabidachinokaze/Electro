package cn.tabidachi.electro.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.model.DownloadMessageItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun EditContent(
    item: DownloadMessageItem,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
    color: Color,
    onScrollTo: suspend () -> Unit,
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Max)
            .clickable {
                scope.launch {
                    onScrollTo()
                    item.emitInteraction()
                }
            }.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .width(3.dp)
                .fillMaxHeight()
                .background(
                    color = color,
                    shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                )
        )
        Column(
            modifier = Modifier.fillMaxHeight().weight(1f)
        ) {
            item.user?.username?.let {
                Text(
                    text = it,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = item.message.description(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
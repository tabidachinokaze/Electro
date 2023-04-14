package cn.tabidachi.electro.ui.common.attachment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.ext.sizeStrWithAuto
import cn.tabidachi.electro.model.attachment.AudioAttachment
import cn.tabidachi.electro.ui.common.SimpleListItem

@Composable
fun AudioAttachment(
    leadingContent: @Composable () -> Unit,
    attachment: AudioAttachment,
    modifier: Modifier = Modifier
) {
    SimpleListItem(
        headlineContent = {
            Text(
                text = attachment.title ?: attachment.filename,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }, supportingContent = {
            Text(
                text = attachment.artist ?: attachment.size.sizeStrWithAuto(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }, leadingContent = {
            Box(
                modifier = Modifier
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .padding(end = 8.dp), contentAlignment = Alignment.Center
            ) {
                leadingContent()
            }
        },
        modifier = modifier.padding(8.dp)
    )
}
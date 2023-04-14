package cn.tabidachi.electro.ui.common.attachment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.ext.longTimeFormat
import cn.tabidachi.electro.model.attachment.VoiceAttachment
import cn.tabidachi.electro.ui.common.SimpleListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAttachment(
    leadingContent: @Composable () -> Unit,
    attachment: VoiceAttachment,
    modifier: Modifier = Modifier,
    progress: Float,
    onProgressChange: (Float) -> Unit,
) {
    val configuration = LocalConfiguration.current
    SimpleListItem(
        headlineContent = {
            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                Slider(
                    value = progress,
                    onValueChange = onProgressChange,
                    modifier = Modifier.width(configuration.screenWidthDp.dp / 3),
                    thumb = {

                    },
                    valueRange = 0f..attachment.duration.toFloat(),
                )
            }
        }, supportingContent = {
            Text(
                text = progress.toLong().longTimeFormat(),
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
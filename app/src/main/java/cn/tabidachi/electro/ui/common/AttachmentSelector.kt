package cn.tabidachi.electro.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AttachmentSelector(
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false,
    sendButtonEnabled: Boolean,
    onSend: () -> Unit,
    onSelect: (AttachmentType) -> Unit,
    isRecording: Boolean = false,
    onRecording: () -> Unit = {},
    isEditing: Boolean = false,
    onEdited: () -> Unit = {}
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f)
        ) {
            AttachmentType.values().forEach {
                item {
                    FilledTonalIconButton(
                        onClick = {
                            onSelect(it)
                        }, enabled = !isRecording
                    ) {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        if (isProcessing) {
            CircularProgressIndicator(strokeWidth = 3.dp, strokeCap = StrokeCap.Round, modifier = Modifier.size(24.dp))
        } else
        if (isEditing) {
            FilledIconButton(onClick = onEdited) {
                Icon(imageVector = Icons.Rounded.Done, contentDescription = null)
            }
        } else if (sendButtonEnabled) {
            IconButton(onClick = onSend) {
                Icon(
                    imageVector = Icons.Rounded.Send, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Box(
                modifier = modifier
                    .size(40.0.dp)
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = {

                        }, onLongClick = onRecording,
                        enabled = !isRecording,
                        role = Role.Button,
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = rememberRipple(
                            bounded = false,
                            radius = 40.0.dp / 2
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
                    Icon(
                        imageVector = Icons.Rounded.Mic, contentDescription = null,
                    )
                }
            }
        }
    }
}
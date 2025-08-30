package cn.tabidachi.electro.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.ext.openExternal
import cn.tabidachi.electro.model.UploadMessageItem
import cn.tabidachi.electro.model.UploadState
import cn.tabidachi.electro.model.attachment.AudioAttachment
import cn.tabidachi.electro.model.attachment.DocumentAttachment
import cn.tabidachi.electro.model.attachment.FileAttachment
import cn.tabidachi.electro.model.attachment.ImageAttachment
import cn.tabidachi.electro.model.attachment.LocationAttachment
import cn.tabidachi.electro.model.attachment.VideoAttachment
import cn.tabidachi.electro.model.attachment.VoiceAttachment
import cn.tabidachi.electro.model.attachment.WebRTCAttachment
import cn.tabidachi.electro.ui.common.attachment.AudioAttachment
import cn.tabidachi.electro.ui.common.attachment.FileAttachment
import cn.tabidachi.electro.ui.common.attachment.LocationAttachment
import cn.tabidachi.electro.ui.common.attachment.VoiceAttachment
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.video.VideoFrameDecoder

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun UploadMessage(
    item: UploadMessageItem
) {
    val attachment = item.attachment
    val message = item.message
    val density = LocalDensity.current
    val context = LocalContext.current
    Column(modifier = Modifier.width(IntrinsicSize.Max)) {
        when (attachment) {
            is DocumentAttachment -> {
                when (attachment) {
                    is AudioAttachment -> {
                        AudioAttachment(
                            leadingContent = {
                                UploadIndicator(item)
                            }, attachment = attachment
                        )
                    }

                    is FileAttachment -> {
                        FileAttachment(
                            leadingContent = {
                                UploadIndicator(item)
                            }, attachment = attachment
                        )
                    }

                    is ImageAttachment -> {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            val aspect = if (attachment.width == 0 || attachment.height == 0) {
                                Modifier
                            } else {
                                Modifier.aspectRatio(attachment.width.toFloat() / attachment.height.toFloat())
                            }
                            AsyncImage(
                                model = item.path,
                                contentDescription = null,
                                modifier = Modifier
                                    .then(aspect)
                                    .fillMaxWidth()
                                    .height(with(density) {
                                        attachment.height.toDp()
                                    })
                                    .then(aspect)
                                    .clickable {
                                        item.path?.let {
                                            context.openExternal(
                                                it,
                                                attachment.contentType
                                            )
                                        }
                                    }, contentScale = ContentScale.Crop
                            )
                            UploadIndicator(item)
                        }
                    }

                    is VideoAttachment -> {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            val aspect = if (attachment.width == 0 || attachment.height == 0) {
                                Modifier
                            } else {
                                Modifier.aspectRatio(attachment.width.toFloat() / attachment.height.toFloat())
                            }
                            AnimatedContent(
                                targetState = item.path?.let {
                                    ImageRequest.Builder(context)
                                        .data(it)
                                        .decoderFactory { result, options, _ ->
                                            VideoFrameDecoder(result.source, options)
                                        }
                                        .build()
                                } ?: attachment.thumb,
                                label = ""
                            ) {
                                AsyncImage(
                                    model = it,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .then(aspect)
                                        .fillMaxWidth()
                                        .height(with(density) {
                                            attachment.height.toDp()
                                        })
                                        .then(aspect)
                                        .clickable {
                                            item.path?.let {
                                                context.openExternal(
                                                    it,
                                                    attachment.contentType
                                                )
                                            }
                                        }
                                )
                            }
                            UploadIndicator(item)
                        }
                    }

                    is VoiceAttachment -> {
                        VoiceAttachment(
                            leadingContent = {
                                IconButton(
                                    onClick = item::playPause,
                                    enabled = item.path != null
                                ) {
                                    Icon(
                                        imageVector = if (item.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                        contentDescription = null
                                    )
                                }
                            },
                            attachment = attachment,
                            progress = item.progress,
                            onProgressChange = item::onSlide,
                        )
                    }
                }
            }

            is LocationAttachment -> {
                LocationAttachment(attachment = attachment)
            }

            is WebRTCAttachment -> {
                Box(
                    modifier = Modifier.sizeIn(
                        minWidth = 160.dp,
                        minHeight = 90.dp
                    )
                )
            }

            null -> {}
        }
        if (!message.text.isNullOrBlank()) {
            Text(text = message.text, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun UploadIndicator(item: UploadMessageItem) {
    FilledIconButton(
        onClick = {
            when (item.state) {
                is UploadState.Uploading -> {
                    item.cancel()
                }

                else -> {
                    item.upload()
                }
            }
        }, colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            )
        )
    ) {
        when (val state = item.state) {
            UploadState.Failure -> {
                Icon(
                    imageVector = Icons.Rounded.ArrowUpward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }

            UploadState.Pause -> {
                Icon(
                    imageVector = Icons.Rounded.ArrowUpward,
                    contentDescription = null,
                )
            }

            is UploadState.Uploading -> {
                Icon(
                    imageVector = Icons.Rounded.Clear,
                    contentDescription = null
                )
                CircularProgressIndicator(
                    progress = state.progress,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(40.dp),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}
package cn.tabidachi.electro.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilePresent
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.database.entity.Message
import cn.tabidachi.electro.ext.audioPreview
import cn.tabidachi.electro.ext.openExternal
import cn.tabidachi.electro.model.DownloadMessageItem
import cn.tabidachi.electro.model.UserQuery
import cn.tabidachi.electro.model.attachment.Attachment
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
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AttachmentMessage(
    item: DownloadMessageItem,
    replyContent: @Composable (() -> Unit)? = null
) {
    val attachment = item.attachment
    val message = item.message
    val density = LocalDensity.current
    val context = LocalContext.current
    Column(modifier = Modifier.width(IntrinsicSize.Max)) {
        replyContent?.invoke()
        when (attachment) {
            is DocumentAttachment -> {
                when (attachment) {
                    is AudioAttachment -> {
                        AudioAttachment(
                            leadingContent = {
                                DownloadIndicator(
                                    item = item,
                                    icon = {
                                        AsyncImage(
                                            model = item.artwork ?: attachment.artwork,
                                            contentDescription = null
                                        )
                                        item.path?.let {
                                            Icon(
                                                imageVector = Icons.Rounded.PlayArrow,
                                                contentDescription = null
                                            )
                                        }
                                    }, onClick = {
                                        item.path?.let(context::audioPreview)
                                    }
                                )
                            }, attachment = attachment,
                            modifier = Modifier.clickable {
                                item.path?.let {
                                    context.openExternal(it, attachment.contentType)
                                }
                            }
                        )
                    }

                    is FileAttachment -> {
                        FileAttachment(
                            leadingContent = {
                                if (item.path == null) {
                                    DownloadIndicator(item = item)
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.FilePresent,
                                        contentDescription = null
                                    )
                                }
                            }, attachment = attachment,
                            modifier = Modifier.clickable {
                                item.path?.let {
                                    context.openExternal(it, attachment.contentType)
                                }
                            }
                        )
                    }

                    is ImageAttachment -> {
                        Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            val aspect = if (attachment.width == 0 || attachment.height == 0) {
                                Modifier
                            } else {
                                Modifier.aspectRatio(attachment.width.toFloat() / attachment.height.toFloat())
                            }
                            AnimatedContent(
                                targetState = item.path ?: attachment.thumb,
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
                                        .clickable {
                                            if (it is String) {
                                                context.openExternal(it, attachment.contentType)
                                            }
                                        }, contentScale = ContentScale.Crop
                                )
                            }
                            if (item.path == null) {
                                DownloadIndicator(item = item)
                            }
                        }
                    }

                    is VideoAttachment -> {
                        val function: () -> Unit = {
                            item.path?.let { context.openExternal(it, attachment.contentType) }
                        }
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
                                        .clickable(onClick = function),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            if (item.path == null) {
                                DownloadIndicator(item = item)
                            } else {
                                FilledIconButton(
                                    onClick = function,
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.8f
                                        )
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }

                    is VoiceAttachment -> {
                        VoiceAttachment(
                            leadingContent = {
                                item.path?.let {
                                    IconButton(onClick = item::playPause) {
                                        Icon(
                                            imageVector = if (item.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                            contentDescription = null
                                        )
                                    }
                                } ?: DownloadIndicator(item = item)
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
            Text(
                text = message.text, modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun AttachmentMessage(
    isIncoming: Boolean,
    userQuery: UserQuery? = null,
    onDownload: () -> Unit = {},
    message: Message,
    attachment: Attachment?
) {
    Column {
        if (isIncoming && userQuery != null) {
            Text(text = userQuery.username, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
        }
        when (attachment) {
            is AudioAttachment -> {
                FileAttachment(leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.FilePresent,
                        contentDescription = null
                    )
                }, attachment)
            }

            is FileAttachment -> {
                FileAttachment(leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.FilePresent,
                        contentDescription = null
                    )
                }, attachment)
            }

            is ImageAttachment -> {
                Column {
                    attachment.url?.let {
                        AsyncImage(
                            model = attachment.url,
                            contentDescription = null,
                            modifier = Modifier
                                .sizeIn(
                                    minWidth = 160.dp,
                                    minHeight = 90.dp
                                )
                                .clickable {

                                }
                        )
                    }
                }
            }

            is VideoAttachment -> {
                FileAttachment(leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.FilePresent,
                        contentDescription = null
                    )
                }, attachment)
            }

            is VoiceAttachment -> {
                FileAttachment(leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.FilePresent,
                        contentDescription = null
                    )
                }, attachment)
            }

            is LocationAttachment -> {
                Box(
                    modifier = Modifier.sizeIn(
                        minWidth = 160.dp,
                        minHeight = 90.dp
                    )
                )
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
fun ImageText() {
    Column {
        Image(
            painter = painterResource(id = R.drawable.raiden_shogun_background),
            contentDescription = null
        )
        Text(text = "Some Text")
    }
}


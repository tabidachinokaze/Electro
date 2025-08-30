package cn.tabidachi.electro.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.FilePresent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.model.attachment.Attachment
import cn.tabidachi.electro.model.attachment.AudioAttachment
import cn.tabidachi.electro.model.attachment.FileAttachment
import cn.tabidachi.electro.model.attachment.ImageAttachment
import cn.tabidachi.electro.model.attachment.LocationAttachment
import cn.tabidachi.electro.model.attachment.VideoAttachment
import cn.tabidachi.electro.model.attachment.VoiceAttachment
import cn.tabidachi.electro.model.attachment.WebRTCAttachment
import cn.tabidachi.electro.ui.common.attachment.AudioAttachment
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.video.VideoFrameDecoder

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AttachmentRow(
    attachments: List<Attachment>,
    modifier: Modifier = Modifier,
    onAttachmentRemove: (Attachment) -> Unit,
) {
    val context = LocalContext.current
    AnimatedVisibility(visible = attachments.isNotEmpty()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.height(AttachmentRowHeight)
        ) {
            item {
                Spacer(modifier = Modifier)
            }
            items(
                attachments,
                key = {
                    it.hashCode()
                }
            ) { attachment ->
                Box(modifier = Modifier.animateItem()) {
                    Box(
                        modifier = Modifier
                            .clip(ItemShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary, ItemShape)
                    ) {
                        when (attachment) {
                            is AudioAttachment -> {
                                AudioAttachment(
                                    leadingContent = {
                                        Icon(
                                            imageVector = Icons.Rounded.AudioFile,
                                            contentDescription = null
                                        )
                                    },
                                    attachment = attachment,
                                    modifier = Modifier.widthIn(
                                        AttachmentRowHeight,
                                        AttachmentRowHeight * 2
                                    )
                                )
                            }

                            is FileAttachment -> {
                                cn.tabidachi.electro.ui.common.attachment.FileAttachment(
                                    leadingContent = {
                                        Icon(
                                            imageVector = Icons.Rounded.FilePresent,
                                            contentDescription = null
                                        )
                                    },
                                    attachment = attachment,
                                    modifier = Modifier.widthIn(
                                        AttachmentRowHeight,
                                        AttachmentRowHeight * 2
                                    )
                                )
                            }

                            is ImageAttachment -> {
                                AsyncImage(
                                    model = attachment.uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .widthIn(
                                            AttachmentRowHeight,
                                            AttachmentRowHeight * 2
                                        )
                                )
                            }

                            is VideoAttachment -> {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(attachment.uri)
                                        .decoderFactory { result, options, _ ->
                                            VideoFrameDecoder(result.source, options)
                                        }
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .widthIn(
                                            AttachmentRowHeight,
                                            AttachmentRowHeight * 2
                                        )
                                )
                            }

                            is VoiceAttachment -> {

                            }

                            is LocationAttachment -> {

                            }

                            is WebRTCAttachment -> {

                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .offset(8.dp, (-8).dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = null,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    onAttachmentRemove(attachment)
                                }
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.5f
                                    )
                                )
                                .scale(0.8f),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier)
            }
        }
    }
}

val ItemShape = RoundedCornerShape(12.dp)
val AttachmentRowHeight = 80.dp

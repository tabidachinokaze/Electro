package cn.tabidachi.electro.ui.call

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.SpeakerPhone
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.call.CallContract.Event
import cn.tabidachi.electro.ui.call.CallContract.State
import cn.tabidachi.electro.ui.common.VideoRenderer
import cn.tabidachi.electro.ui.preview.PreviewSurface
import cn.tabidachi.electro.ui.preview.Previews
import coil3.compose.AsyncImage
import org.webrtc.EglBase
import org.webrtc.VideoTrack

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CallScreen(
    state: State,
    event: (Event) -> Unit,
    localVideoTrack: VideoTrack?,
    remoteVideoTrack: VideoTrack?,
    eglBaseContext: EglBase.Context?
) {
    var parentSize: IntSize by remember { mutableStateOf(IntSize(0, 0)) }
    Box(
        modifier = Modifier
    ) {
        AsyncImage(
            model = state.user?.avatar,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        event(Event.ToggleImmersive)
                    },
                    interactionSource = remember {
                        MutableInteractionSource()
                    },
                    indication = null
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged {
                        parentSize = it
                    }
            ) {
                remoteVideoTrack?.let { videoTrack ->
                    eglBaseContext?.let { eglBaseContext ->
                        VideoRenderer(
                            videoTrack = videoTrack,
                            modifier = Modifier.fillMaxSize(),
                            eglBaseContext = eglBaseContext
                        )
                    }
                }
            }
            if (localVideoTrack != null && eglBaseContext != null && state.camera) {
                FloatingVideoRenderer(
                    videoTrack = localVideoTrack!!,
                    parentBounds = parentSize,
                    paddingValues = PaddingValues(0.dp),
                    modifier = Modifier
                        .size(width = 150.dp, height = 210.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .align(Alignment.TopEnd),
                    eglBaseContext = eglBaseContext
                )
            }
        }
        AnimatedVisibility(
            visible = state.barsVisible,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopAppBar(
                title = {
                },
                navigationIcon = {
                    IconButton(onClick = {
                        event(Event.OnCallEnd)
                    }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { event(Event.FlipCamera) }) {
                        Icon(imageVector = Icons.Rounded.Cameraswitch, contentDescription = null)
                    }
                },
                colors = topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            startY = 0f,
                        )
                    )
            )
        }
        AnimatedVisibility(
            visible = state.barsVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.surface
                            ),
                            startY = 0f,
                        )
                    )
                    .navigationBarsPadding()
            ) {
                CallAction.entries.forEach {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        when (it) {
                            CallAction.CallEnd -> {
                                FilledIconButton(
                                    onClick = {
                                        event(Event.OnCallEnd)
                                    },
                                    modifier = Modifier.size(48.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(imageVector = it.active, contentDescription = null)
                                }
                            }

                            CallAction.Mic -> {
                                FilledIconToggleButton(
                                    checked = state.mic,
                                    onCheckedChange = {
                                        event(Event.OnMicEnabled(it))
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (state.mic) it.active else it.inactive,
                                        contentDescription = null
                                    )
                                }
                            }

                            CallAction.Camera -> {
                                FilledIconToggleButton(
                                    checked = state.camera,
                                    onCheckedChange = {
                                        event(Event.OnCameraEnabled(it))
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (state.camera) it.active else it.inactive,
                                        contentDescription = null
                                    )
                                }
                            }

                            CallAction.Speakerphone -> {
                                FilledIconToggleButton(
                                    checked = state.isSpeakerphone,
                                    onCheckedChange = {
                                        event(Event.IsSpeakerphone(it))
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (state.isSpeakerphone) it.active else it.inactive,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        Text(
                            text = stringResource(id = it.text),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Previews
@Composable
private fun CallScreenPreview() {
    PreviewSurface {
        CallScreen(
            state = State(),
            event = {},
            localVideoTrack = null,
            remoteVideoTrack = null,
            eglBaseContext = null,
        )
    }
}

enum class CallAction(
    val active: ImageVector,
    val inactive: ImageVector,
    val text: Int
) {
    Speakerphone(Icons.Rounded.SpeakerPhone, Icons.Rounded.SpeakerPhone, R.string.handsfree),
    Mic(Icons.Rounded.Mic, Icons.Rounded.MicOff, R.string.microphone),
    Camera(Icons.Rounded.Videocam, Icons.Rounded.VideocamOff, R.string.camera),
    CallEnd(Icons.Rounded.CallEnd, Icons.Rounded.CallEnd, R.string.call_end),
}

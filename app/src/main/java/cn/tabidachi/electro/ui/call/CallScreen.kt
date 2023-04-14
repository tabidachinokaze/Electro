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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.ui.common.VideoRenderer
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.webrtc.IceCandidate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CallScreen(
    offer: Long,
    answer: Long,
    action: String,
    onCallEnd: () -> Unit = {},
) {
    val viewModel: CallViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()
    val localVideoTrack by viewModel.factory.localVideoTrack.collectAsState(null)
    val remoteVideoTrack by viewModel.factory.remoteVideoTrack.collectAsState(null)
    LaunchedEffect(Unit) {
        viewModel.init(offer, answer, action)
    }
    LaunchedEffect(key1 = Unit, block = {
        viewModel.onMicEnabled(viewState.mic)
        viewModel.onCameraEnabled(viewState.camera)
        viewModel.isSpeakerphone(viewState.isSpeakerphone)
    })
    LaunchedEffect(key1 = Unit, block = {
        viewModel.onCallEnd.collect {
            if (it) {
                viewModel.stop()
                onCallEnd()
            }
        }
    })
    var parentSize: IntSize by remember { mutableStateOf(IntSize(0, 0)) }
    val view = LocalView.current
    DisposableEffect(key1 = Unit, effect = {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
        }
    })
    val systemUiController = rememberSystemUiController()
    LaunchedEffect(key1 = viewState.barsVisible, block = {
        systemUiController.isSystemBarsVisible = viewState.barsVisible
    })
    Box(
        modifier = Modifier
    ) {
        AsyncImage(
            model = viewState.user?.avatar,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        viewModel.changeVisible()
                    }, onLongClick = {

                    }, onDoubleClick = {

                    }, interactionSource = remember {
                        MutableInteractionSource()
                    }, indication = null
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged {
                        parentSize = it
                    }
            ) {
                remoteVideoTrack?.let {
                    VideoRenderer(
                        videoTrack = it,
                        modifier = Modifier.fillMaxSize(),
                        eglBaseContext = viewModel.factory.eglBaseContext
                    )
                }
            }
            if (localVideoTrack != null && viewState.camera) {
                FloatingVideoRenderer(
                    videoTrack = localVideoTrack!!,
                    parentBounds = parentSize,
                    paddingValues = PaddingValues(0.dp),
                    modifier = Modifier
                        .size(width = 150.dp, height = 210.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .align(Alignment.TopEnd),
                    eglBaseContext = viewModel.factory.eglBaseContext
                )
            }
        }
        AnimatedVisibility(
            visible = viewState.barsVisible,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopAppBar(
                title = {

                }, navigationIcon = {
                    IconButton(onClick = {
                        viewModel.onCallEnd(onCallEnd)
                    }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }, actions = {
                    IconButton(onClick = viewModel::flipCamera) {
                        Icon(imageVector = Icons.Rounded.Cameraswitch, contentDescription = null)
                    }
                }, colors = topAppBarColors(containerColor = Color.Transparent),
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
            visible = viewState.barsVisible,
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
                CallAction.values().forEach {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        when (it) {
                            CallAction.CallEnd -> {
                                FilledIconButton(
                                    onClick = {
                                        viewModel.onCallEnd(onCallEnd)
                                    }, modifier = Modifier.size(48.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(imageVector = it.active, contentDescription = null)
                                }
                            }

                            CallAction.Mic -> {
                                FilledIconToggleButton(
                                    checked = viewState.mic,
                                    onCheckedChange = viewModel::onMicEnabled,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (viewState.mic) it.active else it.inactive,
                                        contentDescription = null
                                    )
                                }
                            }

                            CallAction.Camera -> {
                                FilledIconToggleButton(
                                    checked = viewState.camera,
                                    onCheckedChange = {
                                        viewModel.onCameraEnabled(it)
                                    }, modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (viewState.camera) it.active else it.inactive,
                                        contentDescription = null
                                    )
                                }
                            }

                            CallAction.Speakerphone -> {
                                FilledIconToggleButton(
                                    checked = viewState.isSpeakerphone,
                                    onCheckedChange = {
                                        viewModel.isSpeakerphone(it)
                                    }, modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (viewState.isSpeakerphone) it.active else it.inactive,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        Text(
                            text = stringResource(id = it.text),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
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

fun RemoteIceCandidate.toLocal() = IceCandidate(this.sdpMid, this.sdpMLineIndex, this.sdp)

data class CallViewState(
    val mic: Boolean = true,
    val camera: Boolean = true,
    val isSpeakerphone: Boolean = false,
    val isFrontCamera: Boolean = true,
    val target: Long? = null,
    val user: User? = null,
    val barsVisible: Boolean = true
)
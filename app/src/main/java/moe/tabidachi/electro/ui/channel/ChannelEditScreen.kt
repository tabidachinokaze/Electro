package moe.tabidachi.electro.ui.channel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import moe.tabidachi.electro.R
import moe.tabidachi.electro.ui.channel.ChannelContract.Event
import moe.tabidachi.electro.ui.channel.ChannelContract.State
import moe.tabidachi.electro.ui.common.SimpleTextField
import moe.tabidachi.electro.ui.pair.navigateToPair
import moe.tabidachi.electro.ui.preview.PreviewSurface
import moe.tabidachi.electro.ui.preview.Previews
import coil3.compose.AsyncImage
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data object ChannelEditRoute

context(navController: NavHostController)
fun NavGraphBuilder.channelEdit() = composable<ChannelEditRoute> {
    val viewModel: ChannelViewModel =
        hiltViewModel(navController.getBackStackEntry(ChannelRoute::class))
    val (state, event) = viewModel.observe {
        when (it) {
            ChannelContract.Effect.NavigateUp -> navController.navigateUp()
        }
    }
    ChannelEditScreen(
        state = state.value,
        event = {
            when (it) {
                is Event.NavigateToChannelAdmin -> navController.navigateToChannelAdmin()
                is Event.NavigateToChannelDetail -> navController.navigateToChannelDetail()
                is Event.NavigateToChannelEdit -> navController.navigateToChannelEdit()
                is Event.NavigateToChannelInvite -> navController.navigateToChannelInvite()
                is Event.NavigateToPair -> navController.navigateToPair(it.uid)
                Event.NavigateUp -> navController.navigateUp()
                else -> event(it)
            }
        }
    )
}

fun NavHostController.navigateToChannelEdit() {
    navigate(ChannelEditRoute)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelEditScreen(
    state: State,
    event: (Event) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val cropper = rememberImageCropper()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            println(it)
            it?.let {
                scope.launch {
                    when (val cropResult = cropper.crop(it, context)) {
                        is CropResult.Success -> {
                            event(Event.OnCropSuccess(cropResult.bitmap.asAndroidBitmap()))
                        }

                        else -> {
                        }
                    }
                }
            }
        }
    )
    cropper.cropState?.let {
        ImageCropperDialog(state = it)
    }
    LaunchedEffect(Unit) {
        event(Event.FindSession)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.edit))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            event(Event.NavigateUp)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    event(Event.OnDone)
                },
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                if (state.processing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeCap = StrokeCap.Round
                    )
                } else {
                    Icon(imageVector = Icons.Rounded.Done, contentDescription = null)
                }
            }
        },
        contentWindowInsets = WindowInsets.statusBars
    ) {
        Column(
            modifier = Modifier
                .padding(top = it.calculateTopPadding())
                .navigationBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                        .clickable {
                            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    state.image?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    } ?: state.dialog?.image?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    } ?: run {
                        Image(imageVector = Icons.Rounded.AddAPhoto, contentDescription = null)
                    }
                }
                SimpleTextField(
                    value = state.title,
                    onValueChange = {
                        event(Event.OnTitleChange(it))
                    },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    placeholder = {
                        Text(text = stringResource(id = R.string.channel_name))
                    },
                    isError = state.isTitleError
                )
            }
            OutlinedTextField(
                value = state.description,
                onValueChange = {
                    event(Event.OnDescriptionChange(it))
                },
                label = {
                    Text(text = stringResource(id = R.string.channel_description))
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Previews
@Composable
private fun ChannelEditScreenPreview() {
    PreviewSurface {
        ChannelEditScreen(
            state = State(),
            event = {}
        )
    }
}

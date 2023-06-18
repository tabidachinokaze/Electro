package cn.tabidachi.electro.ui.channel

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
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.ElectroNavigationActions
import cn.tabidachi.electro.ui.common.SimpleTextField
import coil.compose.AsyncImage
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelEditScreen(
    sid: Long,
    navigationActions: ElectroNavigationActions,
    viewModel: ChannelViewModel,
) {
    val viewState by viewModel.viewState.collectAsState()
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
                            viewModel.onCropSuccess(cropResult.bitmap.asAndroidBitmap())
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
    LaunchedEffect(key1 = Unit, block = {
        viewModel.findSession()
    })
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.edit))
                },
                navigationIcon = {
                    IconButton(
                        onClick = navigationActions::navigateUp
                    ) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                },
            )
        }, floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onDone {
                        navigationActions.navigateUp()
                    }
                }, modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                if (viewState.processing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeCap = StrokeCap.Round)
                } else {
                    Icon(imageVector = Icons.Rounded.Done, contentDescription = null)
                }
            }
        }, contentWindowInsets = WindowInsets.statusBars
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
                    viewState.image?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    } ?: viewState.dialog?.image?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    } ?: kotlin.run {
                        Image(imageVector = Icons.Rounded.AddAPhoto, contentDescription = null)
                    }
                }
                SimpleTextField(
                    value = viewState.title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    placeholder = {
                        Text(text = stringResource(id = R.string.channel_name))
                    }, isError = viewState.isTitleError
                )
            }
            OutlinedTextField(
                value = viewState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = {
                    Text(text = stringResource(id = R.string.channel_description))
                }, modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
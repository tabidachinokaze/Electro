package cn.tabidachi.electro.ui.channel

import android.graphics.Bitmap
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.database.entity.SessionType
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.data.network.MinIO
import cn.tabidachi.electro.ext.MINIO
import cn.tabidachi.electro.model.request.SessionCreateRequest
import cn.tabidachi.electro.ui.channel.CreateChannelContract.Effect
import cn.tabidachi.electro.ui.channel.CreateChannelContract.Event
import cn.tabidachi.electro.ui.channel.CreateChannelContract.State
import cn.tabidachi.electro.ui.common.SimpleTextField
import cn.tabidachi.electro.ui.preview.PreviewSurface
import cn.tabidachi.electro.ui.preview.Previews
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.util.generateNonce
import io.minio.GetPresignedObjectUrlArgs
import io.minio.http.Method
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.BaseViewModel
import moe.tabidachi.compose.mvi.observe
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@Serializable
data object ChannelCreateRoute

context(navController: NavHostController)
fun NavGraphBuilder.channelCreate() = composable<ChannelCreateRoute> {
    val viewModel: ChannelCreateViewModel = hiltViewModel()
    val (state, event) = viewModel.observe {
        when (it) {
            is Effect.NavigateUpAndNavigateToChannel -> {
                navController.navigateUp()
                navController.navigateToChannel(it.sid)
            }
        }
    }
    ChannelCreateScreen(
        state = state.value,
        event = {
            when (it) {
                Event.NavigateUp -> navController.navigateUp()
                else -> event(it)
            }
        }
    )
}

fun NavHostController.navigateToChannelCreate() {
    navigate(ChannelCreateRoute)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelCreateScreen(
    state: State,
    event: (Event) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cropper = rememberImageCropper()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            it?.let {
                scope.launch {
                    when (val cropResult = cropper.crop(it, context)) {
                        is CropResult.Success -> {
                            event(Event.OnCropSuccess(cropResult.bitmap))
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.create_new_channel))
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
                        Image(
                            bitmap = it,
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
private fun ChannelCreateScreenPreview() {
    PreviewSurface {
        ChannelCreateScreen(
            state = State(),
            event = {}
        )
    }
}

@HiltViewModel
class ChannelCreateViewModel @Inject constructor(
    private val ktor: Ktor,
    private val minio: MinIO
) : CreateChannelContract.ViewModel(State()) {
    override fun event(event: Event) = when (event) {
        is Event.OnDescriptionChange -> onDescriptionChange(event.value)
        Event.OnDone -> onDone()
        is Event.OnTitleChange -> onTitleChange(event.value)
        Event.NavigateUp -> Unit
        is Event.OnCropSuccess -> onCropSuccess(event.value)
    }

    private fun onDone() {
        val state = state.value
        if (state.processing) return
        changeProcessing(true)
        val title = state.title
        if (title.isBlank()) {
            updateState { it.copy(isTitleError = true) }
            changeProcessing(false)
            return
        }
        val image = state.image
        val description = state.description
        viewModelScope.launch {
            val url = image?.asAndroidBitmap()?.let {
                uploadImage(it) ?: return@launch changeProcessing(false)
            }
            val request = SessionCreateRequest(
                type = SessionType.CHANNEL,
                title = title,
                description = description,
                image = url
            )
            ktor.createSession(request).onSuccess {
                it.data?.let {
                    emitEffect(Effect.NavigateUpAndNavigateToChannel(it))
                }
            }.onFailure {
                it.printStackTrace()
            }
            changeProcessing(false)
        }
    }

    private suspend fun uploadImage(bitmap: Bitmap): String? {
        minio.checkOrCreateBucket(MinIO.Companion.AVATAR)
        val filename = generateNonce()
        val url = minio.client.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(MinIO.Companion.AVATAR)
                .`object`(filename)
                .build()
        )
        return withContext(Dispatchers.IO) {
            ByteArrayOutputStream().use { outputStream ->
                if (
                    runCatching {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        ktor.upload.put(url) {
                            setBody(outputStream.toByteArray())
                        }
                    }.getOrNull()?.status == HttpStatusCode.Companion.OK
                ) {
                    Url(
                        URLBuilder(
                            protocol = URLProtocol.Companion.MINIO,
                            pathSegments = listOf(MinIO.Companion.AVATAR, filename)
                        )
                    ).toString()
                } else {
                    null
                }
            }
        }
    }

    private fun onTitleChange(value: String) {
        updateState { it.copy(title = value, isTitleError = false) }
    }

    private fun onDescriptionChange(value: String) {
        updateState { it.copy(description = value) }
    }

    private fun onCropSuccess(bitmap: ImageBitmap) {
        updateState { it.copy(image = bitmap) }
    }

    private fun changeProcessing(value: Boolean) {
        updateState { it.copy(processing = value) }
    }
}

interface CreateChannelContract {
    abstract class ViewModel(initialState: State) :
        BaseViewModel<State, Event, Effect>(initialState)

    data class State(
        val title: String = "",
        val description: String = "",
        val image: ImageBitmap? = null,
        val processing: Boolean = false,
        val isTitleError: Boolean = false
    )

    sealed interface Event {
        data class OnDescriptionChange(val value: String) : Event
        data class OnTitleChange(val value: String) : Event
        data object OnDone : Event
        data object NavigateUp : Event
        data class OnCropSuccess(val value: ImageBitmap) : Event
    }

    sealed interface Effect {
        data class NavigateUpAndNavigateToChannel(val sid: Long) : Effect
    }
}

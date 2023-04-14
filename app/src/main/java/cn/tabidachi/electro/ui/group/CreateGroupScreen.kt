package cn.tabidachi.electro.ui.group

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.database.entity.SessionType
import cn.tabidachi.electro.data.network.Ktor
import cn.tabidachi.electro.data.network.MinIO
import cn.tabidachi.electro.ext.MINIO
import cn.tabidachi.electro.model.request.SessionCreateRequest
import cn.tabidachi.electro.ui.ElectroNavigationActions
import cn.tabidachi.electro.ui.common.SimpleTextField
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    navigationActions: ElectroNavigationActions,
    navHostController: NavHostController
) {
    val viewModel: CreateGroupViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()
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
                            viewModel.onCropSuccess(cropResult.bitmap)
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
                    Text(text = stringResource(id = R.string.create_new_group))
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
                        navigationActions.navigateToGroup(it)
                    }
                }, modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                if (viewState.processing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeCap = StrokeCap.Round
                    )
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
                        Image(
                            bitmap = it,
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
                        Text(text = stringResource(id = R.string.group_name))
                    }, isError = viewState.isTitleError
                )
            }
            OutlinedTextField(
                value = viewState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = {
                    Text(text = stringResource(id = R.string.group_description))
                }, modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val ktor: Ktor,
    private val minio: MinIO
) : ViewModel() {
    private val _viewState = MutableStateFlow(CreateGroupViewState())
    val viewState = _viewState.asStateFlow()

    fun onDone(onSuccess: (Long) -> Unit) {
        if (_viewState.value.processing) return
        changeProcessing(true)
        val title = _viewState.value.title
        if (title.isBlank()) {
            _viewState.update { it.copy(isTitleError = true) }
            changeProcessing(false)
            return
        }
        val image = _viewState.value.image
        val description = _viewState.value.description
        viewModelScope.launch {
            image?.let { image ->
                val url = uploadImage(image.asAndroidBitmap()) ?: return@let Unit
                SessionCreateRequest(
                    type = SessionType.ROOM,
                    title = title,
                    description = description,
                    image = url
                ).let {
                    ktor.createSession(it).onSuccess {
                        it.data?.let {
                            onSuccess(it)
                        }
                    }.onFailure {
                        it.printStackTrace()
                    }
                }
            } ?: kotlin.run {
                SessionCreateRequest(
                    SessionType.ROOM,
                    title,
                    description,
                    null
                ).let {
                    ktor.createSession(it).onSuccess {
                        it.data?.let {
                            onSuccess(it)
                        }
                    }
                }
            }
            changeProcessing(false)
        }
    }

    suspend fun uploadImage(bitmap: Bitmap): String? {
        minio.checkOrCreateBucket(MinIO.AVATAR)
        val filename = generateNonce()
        val url = minio.client.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(MinIO.AVATAR)
                .`object`(filename)
                .build()
        )
        return withContext(Dispatchers.IO) {
            ByteArrayOutputStream().use { outputStream ->
                if (
                    kotlin.runCatching {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        ktor.upload.put(url) {
                            setBody(outputStream.toByteArray())
                        }
                    }.getOrNull()?.status == HttpStatusCode.OK
                ) {
                    Url(
                        URLBuilder(
                            protocol = URLProtocol.MINIO,
                            pathSegments = listOf(MinIO.AVATAR, filename)
                        )
                    ).toString()
                } else null
            }
        }
    }

    fun onTitleChange(value: String) {
        _viewState.update { it.copy(title = value, isTitleError = false) }
    }

    fun onDescriptionChange(value: String) {
        _viewState.update { it.copy(description = value) }
    }

    fun onCropSuccess(bitmap: ImageBitmap) {
        _viewState.update { it.copy(image = bitmap) }
    }

    fun changeProcessing(value: Boolean) {
        _viewState.update { it.copy(processing = value) }
    }
}

data class CreateGroupViewState(
    val title: String = "",
    val description: String = "",
    val image: ImageBitmap? = null,
    val processing: Boolean = false,
    val isTitleError: Boolean = false
)
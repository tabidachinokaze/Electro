package cn.tabidachi.electro.ui.imagepreview

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileCopy
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import cn.tabidachi.electro.R


/**
 * UI State that represents ImagePreviewScreen
 **/
data class ImagePreviewState(
    val title: String = ""
)

/**
 * ImagePreview Actions emitted from the UI Layer
 * passed to the coordinator to handle
 **/
data class ImagePreviewActions(
    val onClick: () -> Unit = {}
)

/**
 * Compose Utility to retrieve actions from nested components
 **/
val LocalImagePreviewActions = staticCompositionLocalOf<ImagePreviewActions> {
    error("{NAME} Actions Were not provided, make sure ProvideImagePreviewActions is called")
}

@Composable
fun ProvideImagePreviewActions(actions: ImagePreviewActions, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalImagePreviewActions provides actions) {
        content.invoke()
    }
}

enum class ImagePreviewMenu(@StringRes val text: Int, val icon: ImageVector) {
    SAVE(R.string.save, Icons.Rounded.Save),
    SHARE(R.string.share, Icons.Rounded.Share),
    COPY(R.string.copy, Icons.Rounded.FileCopy),
}
package cn.tabidachi.electro.ui.preview

import android.content.Context
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.theme.ElectroTheme
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler

@OptIn(ExperimentalCoilApi::class)
@Composable
fun PreviewSurface(content: @Composable () -> Unit) {
    val context: Context = LocalContext.current
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(Color.Red.toArgb())
        context.getDrawable(R.drawable.transparent_akkarin)!!.asImage()
    }

    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        ElectroTheme {
            Surface(content = content)
        }
    }
}

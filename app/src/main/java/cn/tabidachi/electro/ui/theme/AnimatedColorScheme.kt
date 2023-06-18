package cn.tabidachi.electro.ui.theme

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun AnimatedColorScheme(colorScheme: ColorScheme, content: @Composable (ColorScheme) -> Unit) {
    val viewModel: ColorSchemeViewModel = viewModel()
    val colors = viewModel.colors
    LaunchedEffect(colorScheme) {
        val colorArray = colorScheme.toColorArray()
        colors.forEachIndexed { index, colorAnimation ->
            launch {
                colorAnimation.animateTo(colorArray[index], ColorSchemeAnimationSpec)
            }
        }
    }
    content(
        ColorScheme(
            colors[0].value,
            colors[1].value,
            colors[2].value,
            colors[3].value,
            colors[4].value,
            colors[5].value,
            colors[6].value,
            colors[7].value,
            colors[8].value,
            colors[9].value,
            colors[10].value,
            colors[11].value,
            colors[12].value,
            colors[13].value,
            colors[14].value,
            colors[15].value,
            colors[16].value,
            colors[17].value,
            colors[18].value,
            colors[19].value,
            colors[20].value,
            colors[21].value,
            colors[22].value,
            colors[23].value,
            colors[24].value,
            colors[25].value,
            colors[26].value,
            colors[27].value,
            colors[28].value,
        )
    )
}

class ColorSchemeViewModel : ViewModel() {
    val colors = Array(29) { Animatable(Color.Unspecified) }
}

private fun ColorScheme.toColorArray(): Array<Color> = arrayOf(
    primary,
    onPrimary,
    primaryContainer,
    onPrimaryContainer,
    inversePrimary,
    secondary,
    onSecondary,
    secondaryContainer,
    onSecondaryContainer,
    tertiary,
    onTertiary,
    tertiaryContainer,
    onTertiaryContainer,
    background,
    onBackground,
    surface,
    onSurface,
    surfaceVariant,
    onSurfaceVariant,
    surfaceTint,
    inverseSurface,
    inverseOnSurface,
    error,
    onError,
    errorContainer,
    onErrorContainer,
    outline,
    outlineVariant,
    scrim,
)

private val ColorSchemeAnimationSpec = tween<Color>(1000)
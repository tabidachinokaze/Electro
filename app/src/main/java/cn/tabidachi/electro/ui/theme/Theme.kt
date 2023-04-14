package cn.tabidachi.electro.ui.theme

import android.Manifest
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.settings.Themes
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ElectroTheme(
    dayNight: DayNight = if (isSystemInDarkTheme()) DayNight.NIGHT else DayNight.DAY,
    theme: Themes = Themes.Dynamic,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val darkMode = when (dayNight) {
        DayNight.DAY -> false
        DayNight.NIGHT -> true
        DayNight.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = when {
        Themes.Dynamic == theme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> when (darkMode) {
            true -> dynamicDarkColorScheme(context)
            else -> dynamicLightColorScheme(context)
        }

        else -> {
            if (darkMode) theme.nightColorScheme else theme.lightColorScheme
        }
    }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )
    )

    LaunchedEffect(key1 = Unit, block = {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    })

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(color = Color.Transparent, darkIcons = !darkMode)
        systemUiController.isNavigationBarContrastEnforced = false
    }
    AnimateColorScheme(colorScheme = colorScheme) {
        MaterialTheme(
            colorScheme = it,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun AnimateColorScheme(colorScheme: ColorScheme, content: @Composable (ColorScheme) -> Unit) {
    val viewModel: ColorSchemeViewModel = viewModel()
    LaunchedEffect(colorScheme) {
        viewModel.update(this, colorScheme)
    }
    content(
        ColorScheme(
            primary = viewModel.primary.value,
            onPrimary = viewModel.onPrimary.value,
            primaryContainer = viewModel.primaryContainer.value,
            onPrimaryContainer = viewModel.onPrimaryContainer.value,
            inversePrimary = viewModel.inversePrimary.value,
            secondary = viewModel.secondary.value,
            onSecondary = viewModel.onSecondary.value,
            secondaryContainer = viewModel.secondaryContainer.value,
            onSecondaryContainer = viewModel.onSecondaryContainer.value,
            tertiary = viewModel.tertiary.value,
            onTertiary = viewModel.onTertiary.value,
            tertiaryContainer = viewModel.tertiaryContainer.value,
            onTertiaryContainer = viewModel.onTertiaryContainer.value,
            background = viewModel.background.value,
            onBackground = viewModel.onBackground.value,
            surface = viewModel.surface.value,
            onSurface = viewModel.onSurface.value,
            surfaceVariant = viewModel.surfaceVariant.value,
            onSurfaceVariant = viewModel.onSurfaceVariant.value,
            surfaceTint = viewModel.surfaceTint.value,
            inverseSurface = viewModel.inverseSurface.value,
            inverseOnSurface = viewModel.inverseOnSurface.value,
            error = viewModel.error.value,
            onError = viewModel.onError.value,
            errorContainer = viewModel.errorContainer.value,
            onErrorContainer = viewModel.onErrorContainer.value,
            outline = viewModel.outline.value,
            outlineVariant = viewModel.outlineVariant.value,
            scrim = viewModel.scrim.value,
        )
    )
}

class ColorSchemeViewModel : ViewModel() {
    val primary = Animatable(Color.Unspecified)
    val onPrimary = Animatable(Color.Unspecified)
    val primaryContainer = Animatable(Color.Unspecified)
    val onPrimaryContainer = Animatable(Color.Unspecified)
    val inversePrimary = Animatable(Color.Unspecified)
    val secondary = Animatable(Color.Unspecified)
    val onSecondary = Animatable(Color.Unspecified)
    val secondaryContainer = Animatable(Color.Unspecified)
    val onSecondaryContainer = Animatable(Color.Unspecified)
    val tertiary = Animatable(Color.Unspecified)
    val onTertiary = Animatable(Color.Unspecified)
    val tertiaryContainer = Animatable(Color.Unspecified)
    val onTertiaryContainer = Animatable(Color.Unspecified)
    val background = Animatable(Color.Unspecified)
    val onBackground = Animatable(Color.Unspecified)
    val surface = Animatable(Color.Unspecified)
    val onSurface = Animatable(Color.Unspecified)
    val surfaceVariant = Animatable(Color.Unspecified)
    val onSurfaceVariant = Animatable(Color.Unspecified)
    val surfaceTint = Animatable(Color.Unspecified)
    val inverseSurface = Animatable(Color.Unspecified)
    val inverseOnSurface = Animatable(Color.Unspecified)
    val error = Animatable(Color.Unspecified)
    val onError = Animatable(Color.Unspecified)
    val errorContainer = Animatable(Color.Unspecified)
    val onErrorContainer = Animatable(Color.Unspecified)
    val outline = Animatable(Color.Unspecified)
    val outlineVariant = Animatable(Color.Unspecified)
    val scrim = Animatable(Color.Unspecified)

    fun update(scope: CoroutineScope, colorScheme: ColorScheme) = scope.launch {
        launch { primary.animateTo(colorScheme.primary, ColorSchemeAnimationSpec) }
        launch { onPrimary.animateTo(colorScheme.onPrimary, ColorSchemeAnimationSpec) }
        launch {
            primaryContainer.animateTo(
                colorScheme.primaryContainer,
                ColorSchemeAnimationSpec
            )
        }
        launch {
            onPrimaryContainer.animateTo(
                colorScheme.onPrimaryContainer,
                ColorSchemeAnimationSpec
            )
        }
        launch { inversePrimary.animateTo(colorScheme.inversePrimary, ColorSchemeAnimationSpec) }
        launch { secondary.animateTo(colorScheme.secondary, ColorSchemeAnimationSpec) }
        launch { onSecondary.animateTo(colorScheme.onSecondary, ColorSchemeAnimationSpec) }
        launch {
            secondaryContainer.animateTo(
                colorScheme.secondaryContainer,
                ColorSchemeAnimationSpec
            )
        }
        launch {
            onSecondaryContainer.animateTo(
                colorScheme.onSecondaryContainer,
                ColorSchemeAnimationSpec
            )
        }
        launch { tertiary.animateTo(colorScheme.tertiary, ColorSchemeAnimationSpec) }
        launch { onTertiary.animateTo(colorScheme.onTertiary, ColorSchemeAnimationSpec) }
        launch {
            tertiaryContainer.animateTo(
                colorScheme.tertiaryContainer,
                ColorSchemeAnimationSpec
            )
        }
        launch {
            onTertiaryContainer.animateTo(
                colorScheme.onTertiaryContainer,
                ColorSchemeAnimationSpec
            )
        }
        launch { background.animateTo(colorScheme.background, ColorSchemeAnimationSpec) }
        launch { onBackground.animateTo(colorScheme.onBackground, ColorSchemeAnimationSpec) }
        launch { surface.animateTo(colorScheme.surface, ColorSchemeAnimationSpec) }
        launch { onSurface.animateTo(colorScheme.onSurface, ColorSchemeAnimationSpec) }
        launch { surfaceVariant.animateTo(colorScheme.surfaceVariant, ColorSchemeAnimationSpec) }
        launch {
            onSurfaceVariant.animateTo(
                colorScheme.onSurfaceVariant,
                ColorSchemeAnimationSpec
            )
        }
        launch { surfaceTint.animateTo(colorScheme.surfaceTint, ColorSchemeAnimationSpec) }
        launch { inverseSurface.animateTo(colorScheme.inverseSurface, ColorSchemeAnimationSpec) }
        launch {
            inverseOnSurface.animateTo(
                colorScheme.inverseOnSurface,
                ColorSchemeAnimationSpec
            )
        }
        launch { error.animateTo(colorScheme.error, ColorSchemeAnimationSpec) }
        launch { onError.animateTo(colorScheme.onError, ColorSchemeAnimationSpec) }
        launch { errorContainer.animateTo(colorScheme.errorContainer, ColorSchemeAnimationSpec) }
        launch {
            onErrorContainer.animateTo(
                colorScheme.onErrorContainer,
                ColorSchemeAnimationSpec
            )
        }
        launch { outline.animateTo(colorScheme.outline, ColorSchemeAnimationSpec) }
        launch { outlineVariant.animateTo(colorScheme.outlineVariant, ColorSchemeAnimationSpec) }
        launch { scrim.animateTo(colorScheme.scrim, ColorSchemeAnimationSpec) }
    }
}

private val ColorSchemeAnimationSpec = tween<Color>(1000)

enum class DayNight(@StringRes val text: Int) {
    DAY(R.string.always_off),
    NIGHT(R.string.always_on),
    SYSTEM(R.string.follow_system),
}
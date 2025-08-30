package cn.tabidachi.electro.ui.theme

import android.Manifest
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import cn.tabidachi.electro.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ElectroTheme(
    darkLight: DarkLight = if (isSystemInDarkTheme()) DarkLight.DARK else DarkLight.LIGHT,
    theme: Theme = Theme.Dynamic,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val darkMode = when (darkLight) {
        DarkLight.SYSTEM -> isSystemInDarkTheme()
        DarkLight.DARK -> true
        DarkLight.LIGHT -> false
    }
    val colorScheme = when {
        Theme.Dynamic == theme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> when (darkMode) {
            true -> dynamicDarkColorScheme(context)
            else -> dynamicLightColorScheme(context)
        }

        else -> {
            if (darkMode) theme.dark else theme.light
        }
    }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(key1 = Unit, block = {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    })

    AnimatedColorScheme(colorScheme = colorScheme) {
        MaterialTheme(
            colorScheme = it,
            typography = Typography,
            content = content
        )
    }
}

enum class DarkLight(@StringRes val text: Int) {
    SYSTEM(R.string.dark_light_system),
    DARK(R.string.dark_light_on),
    LIGHT(R.string.dark_light_off)
}

enum class Theme(
    val dark: ColorScheme,
    val light: ColorScheme,
    @StringRes val text: Int
) {
    Dynamic(AnemoDarkColors, AnemoLightColors, R.string.dynamic),
    Pyro(PyroDarkColors, PyroLightColors, R.string.pyro),
    Hydro(HydroDarkColors, HydroLightColors, R.string.hydro),
    Anemo(AnemoDarkColors, AnemoLightColors, R.string.anemo),
    Electro(ElectroDarkColors, ElectroLightColors, R.string.electro),
    Dendro(DendroDarkColors, DendroLightColors, R.string.dendro),
    Cryo(CryoDarkColors, CryoLightColors, R.string.cryo),
    Geo(GeoDarkColors, GeoLightColors, R.string.geo),
}
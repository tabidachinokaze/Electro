package cn.tabidachi.electro.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.settings.components.SettingsCategory
import cn.tabidachi.electro.ui.theme.DarkColorScheme
import cn.tabidachi.electro.ui.theme.DayNight
import cn.tabidachi.electro.ui.theme.LightColorScheme
import cn.tabidachi.electro.ui.theme.colors.DefaultColor
import cn.tabidachi.electro.ui.theme.colors.GreenappleColor
import cn.tabidachi.electro.ui.theme.colors.MidnightduskColor
import cn.tabidachi.electro.ui.theme.colors.StrawberryColor
import cn.tabidachi.electro.ui.theme.colors.TakoColor
import cn.tabidachi.electro.ui.theme.colors.TealturqoiseColor

fun LazyListScope.Theme(viewModel: SettingsViewModel) {
    item {
        val viewState by viewModel.viewState.collectAsState()
        val isDark = when (viewModel.dayNight) {
            DayNight.DAY -> false
            DayNight.NIGHT -> true
            DayNight.SYSTEM -> isSystemInDarkTheme()
        }
        SettingsCategory(stringResource(id = R.string.theme)) {
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.DarkMode,
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(text = stringResource(id = R.string.dark_mode))
                }, supportingContent = {
                    Text(text = stringResource(id = viewModel.dayNight.text))
                    DropdownMenu(
                        expanded = viewState.isDayNightMenuExpanded,
                        onDismissRequest = {
                            viewModel.onDayNightMenuVisible(false)
                        }) {
                        DayNight.values().forEach {
                            DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = it.text))
                                }, onClick = {
                                    viewModel.onDayNightModeChange(it)
                                    viewModel.onDayNightMenuVisible(false)
                                }
                            )
                        }
                    }
                }, modifier = Modifier.clickable {
                    viewModel.onDayNightMenuVisible(true)
                }
            )
            ListItem(
                leadingContent = {
                    Icon(imageVector = Icons.Rounded.Palette, contentDescription = null)
                },
                headlineContent = {
                    Text(text = stringResource(id = R.string.theme))
                }, supportingContent = {
                    Text(text = stringResource(id = viewModel.theme.text))
                    DropdownMenu(
                        expanded = viewState.isThemeMenuExpanded,
                        onDismissRequest = {
                            viewModel.onThemeMenuVisible(false)
                        }) {
                        Themes.values().forEach {
                            DropdownMenuItem(
                                leadingIcon = {
                                    val color = when (it) {
                                        Themes.Dynamic -> MaterialTheme.colorScheme.primary
                                        else -> when (isDark) {
                                            true -> it.nightColorScheme.primary
                                            else -> it.lightColorScheme.primary
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = color,
                                                shape = CircleShape
                                            )
                                            .size(24.dp)
                                    )
                                },
                                text = {
                                    Text(text = stringResource(id = it.text))
                                }, onClick = {
                                    viewModel.onThemeChange(it)
                                    viewModel.onThemeMenuVisible(false)
                                }
                            )
                        }
                    }
                }, modifier = Modifier.clickable {
                    viewModel.onThemeMenuVisible(true)
                }, trailingContent = {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .size(40.dp)
                    )
                }
            )
        }
    }
}

enum class Themes(val lightColorScheme: ColorScheme, val nightColorScheme: ColorScheme, @StringRes val text: Int) {
    Dynamic(LightColorScheme, DarkColorScheme, R.string.dynamic),
    Default(DefaultColor.Light.colorScheme, DefaultColor.Dark.colorScheme, R.string.theme_default),
    Greenapple(GreenappleColor.Light.colorScheme, GreenappleColor.Dark.colorScheme, R.string.greenapple),
    Midnightdusk(MidnightduskColor.Light.colorScheme, MidnightduskColor.Dark.colorScheme, R.string.midnightdusk),
    Strawberry(StrawberryColor.Light.colorScheme, StrawberryColor.Dark.colorScheme, R.string.strawberry),
    Tako(TakoColor.Light.colorScheme, TakoColor.Dark.colorScheme, R.string.tako),
    Tealturqoise(TealturqoiseColor.Light.colorScheme, TealturqoiseColor.Dark.colorScheme, R.string.tealturqoise),
}
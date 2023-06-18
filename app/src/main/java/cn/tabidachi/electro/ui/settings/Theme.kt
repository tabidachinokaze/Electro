package cn.tabidachi.electro.ui.settings

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
import cn.tabidachi.electro.ui.theme.DarkLight
import cn.tabidachi.electro.ui.theme.Theme

fun LazyListScope.Theme(viewModel: SettingsViewModel) {
    item {
        val viewState by viewModel.viewState.collectAsState()
        val isDark = when (viewModel.darkLight) {
            DarkLight.SYSTEM -> isSystemInDarkTheme()
            DarkLight.DARK -> true
            DarkLight.LIGHT -> false
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
                    Text(text = stringResource(id = viewModel.darkLight.text))
                    DropdownMenu(
                        expanded = viewState.isDayNightMenuExpanded,
                        onDismissRequest = {
                            viewModel.onDayNightMenuVisible(false)
                        }) {
                        DarkLight.values().forEach {
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
                        Theme.values().forEach {
                            DropdownMenuItem(
                                leadingIcon = {
                                    val color = when (it) {
                                        Theme.Dynamic -> MaterialTheme.colorScheme.primary
                                        else -> when (isDark) {
                                            true -> it.dark.primary
                                            else -> it.light.primary
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

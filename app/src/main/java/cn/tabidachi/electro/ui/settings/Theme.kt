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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.settings.SettingsContract.Event
import cn.tabidachi.electro.ui.settings.SettingsContract.State
import cn.tabidachi.electro.ui.settings.components.SettingsCategory
import cn.tabidachi.electro.ui.theme.DarkLight
import cn.tabidachi.electro.ui.theme.Theme

fun LazyListScope.Theme(
    state: State,
    event: (Event) -> Unit
) {
    item {
        val isDark = when (state.darkLight) {
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
                },
                supportingContent = {
                    Box {
                        Text(text = stringResource(id = state.darkLight.text))
                        DropdownMenu(
                            expanded = state.isDayNightMenuExpanded,
                            onDismissRequest = {
                                event(Event.OnDayNightMenuVisible(false))
                            }
                        ) {
                            DarkLight.entries.forEach {
                                DropdownMenuItem(
                                    text = {
                                        Text(text = stringResource(id = it.text))
                                    },
                                    onClick = {
                                        event(Event.OnDayNightModeChange(it))
                                        event(Event.OnDayNightMenuVisible(false))
                                    }
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.clickable {
                    event(Event.OnDayNightMenuVisible(true))
                }
            )
            ListItem(
                leadingContent = {
                    Icon(imageVector = Icons.Rounded.Palette, contentDescription = null)
                },
                headlineContent = {
                    Text(text = stringResource(id = R.string.theme))
                },
                supportingContent = {
                    Box {
                        Text(text = stringResource(id = state.theme.text))
                        DropdownMenu(
                            expanded = state.isThemeMenuExpanded,
                            onDismissRequest = {
                                event(Event.OnThemeMenuVisible(false))
                            }
                        ) {
                            Theme.entries.forEach {
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
                                    },
                                    onClick = {
                                        event(Event.OnThemeChange(it))
                                        event(Event.OnThemeMenuVisible(false))
                                    }
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.clickable {
                    event(Event.OnThemeMenuVisible(true))
                },
                trailingContent = {
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

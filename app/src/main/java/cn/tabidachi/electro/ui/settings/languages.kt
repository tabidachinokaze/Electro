package cn.tabidachi.electro.ui.settings

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.settings.components.SettingsCategory

fun LazyListScope.Languages(viewModel: SettingsViewModel) {
    item {
        val viewState by viewModel.viewState.collectAsState()
        SettingsCategory(stringResource(id = R.string.languages)) {
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Language,
                        contentDescription = null
                    )
                }, headlineContent = {
                    Text(text = stringResource(id = R.string.languages))
                }, supportingContent = {
                    Text(text = stringResource(id = viewModel.language.text))
                    DropdownMenu(
                        expanded = viewState.isLanguageMenuExpanded,
                        onDismissRequest = {
                            viewModel.onLanguageMenuVisible(false)
                        }) {
                        Language.values().forEach {
                            DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = it.text))
                                }, onClick = {
                                    viewModel.onLanguageChange(it)
                                    viewModel.onLanguageMenuVisible(false)
                                }
                            )
                        }
                    }
                }, modifier = Modifier.clickable {
                    viewModel.onLanguageMenuVisible(true)
                }
            )
        }
    }
}

enum class Language(@StringRes val text: Int, val tag: String) {
    SYSTEM(R.string.follow_system, ""),
    ENGLISH(R.string.english, "en"),
    CHINESE(R.string.chinese, "zh-Hans-CN"),
}
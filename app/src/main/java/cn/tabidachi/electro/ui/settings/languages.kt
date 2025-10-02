package cn.tabidachi.electro.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.settings.SettingsContract.Event
import cn.tabidachi.electro.ui.settings.SettingsContract.State
import cn.tabidachi.electro.ui.settings.components.SettingsCategory

fun LazyListScope.Languages(
    state: State,
    event: (Event) -> Unit
) {
    item {
        SettingsCategory(stringResource(id = R.string.languages)) {
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Language,
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(text = stringResource(id = R.string.languages))
                },
                supportingContent = {
                    Text(text = stringResource(id = state.language.text))
                },
                modifier = Modifier.clickable {
                    event(Event.NavigateToLocaleSettings)
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

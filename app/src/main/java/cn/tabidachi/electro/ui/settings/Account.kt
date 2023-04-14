package cn.tabidachi.electro.ui.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.ui.settings.components.SettingsCategory

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.Account(user: User) {
    item {
        val clipboardManager = LocalClipboardManager.current
        SettingsCategory(stringResource(id = R.string.account)) {
            ListItem(
                headlineContent = {
                    Text(text = user.email)
                },
                supportingContent = {
                    Text(text = stringResource(id = R.string.email))
                }, modifier = Modifier.combinedClickable(
                    onClick = {

                    },
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(text = user.email))
                    }
                )
            )
            ListItem(
                headlineContent = {
                    Text(text = user.username)
                },
                supportingContent = {
                    Text(text = stringResource(id = R.string.username))
                }, modifier = Modifier.combinedClickable(
                    onClick = { },
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(text = user.username))
                    }
                )
            )
        }
    }
}
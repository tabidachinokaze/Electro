package cn.tabidachi.electro.ui.settings

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.ui.settings.components.SettingsCategory
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.Account(user: User) {
    item {
        val scope = rememberCoroutineScope()
        val clipboard = LocalClipboard.current
        SettingsCategory(stringResource(id = R.string.account)) {
            ListItem(
                headlineContent = {
                    Text(text = user.email)
                },
                supportingContent = {
                    Text(text = stringResource(id = R.string.email))
                },
                modifier = Modifier.combinedClickable(
                    onClick = {
                    },
                    onLongClick = {
                        scope.launch {
                            clipboard.setClipEntry(
                                ClipData.newPlainText(user.email, user.email).toClipEntry()
                            )
                        }
                    }
                )
            )
            ListItem(
                headlineContent = {
                    Text(text = user.username)
                },
                supportingContent = {
                    Text(text = stringResource(id = R.string.username))
                },
                modifier = Modifier.combinedClickable(
                    onClick = { },
                    onLongClick = {
                        scope.launch {
                            clipboard.setClipEntry(
                                ClipData.newPlainText(user.username, user.username).toClipEntry()
                            )
                        }
                    }
                )
            )
        }
    }
}

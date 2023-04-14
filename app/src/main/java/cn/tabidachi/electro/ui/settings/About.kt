package cn.tabidachi.electro.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import cn.tabidachi.electro.AppCenter
import cn.tabidachi.electro.BuildConfig
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.settings.components.SettingsCategory
import com.microsoft.appcenter.distribute.Distribute
import compose.icons.TablerIcons
import compose.icons.tablericons.BrandGithub
import compose.icons.tablericons.BrandTelegram

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.About() {
    item {
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current
        SettingsCategory(stringResource(id = R.string.about)) {
            About.values().forEach {
                when (it) {
                    About.VERSION -> {
                        ListItem(
                            headlineContent = {
                                Text(text = stringResource(id = it.text))
                            }, supportingContent = {
                                Text(text = BuildConfig.VERSION_NAME)
                            }, leadingContent = {
                                Icon(imageVector = it.icon, contentDescription = null)
                            }, modifier = Modifier.combinedClickable(
                                onClick = {
                                    AppCenter.allowToast = true
                                    Distribute.checkForUpdate()
                                }, onLongClick = {
                                    clipboardManager.setText(AnnotatedString(BuildConfig.VERSION_NAME))
                                }
                            ), trailingContent = {
                                IconButton(onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.data))
                                    context.startActivity(intent)
                                }) {
                                    Icon(imageVector = Icons.Rounded.OpenInNew, contentDescription = null)
                                }
                            }
                        )
                    }
                    else -> {
                        ListItem(
                            headlineContent = {
                                Text(text = stringResource(id = it.text))
                            }, supportingContent = {
                                Text(text = it.data)
                            }, leadingContent = {
                                Icon(imageVector = it.icon, contentDescription = null)
                            }, modifier = Modifier.combinedClickable(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.data))
                                    context.startActivity(intent)
                                }, onLongClick = {
                                    clipboardManager.setText(AnnotatedString(it.data))
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}

enum class About(@StringRes val text: Int, val icon: ImageVector, val data: String) {
    VERSION(R.string.version, Icons.Rounded.NewReleases, "https://install.appcenter.ms/users/tabidachinokaze/apps/electro/distribution_groups/electro"),
    TELEGRAM(R.string.telegram, TablerIcons.BrandTelegram, "https://t.me/tabidachinokaze"),
    GITHUB(R.string.github, TablerIcons.BrandGithub, "https://github.com/tabidachinokaze")
}
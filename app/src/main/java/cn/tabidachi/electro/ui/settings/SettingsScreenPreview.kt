package cn.tabidachi.electro.ui.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.ui.common.ImageTopAppBar
import cn.tabidachi.electro.ui.common.SimpleListItem
import cn.tabidachi.electro.ui.settings.components.SettingsCategory
import cn.tabidachi.electro.ui.theme.DarkLight
import cn.tabidachi.electro.ui.theme.Theme
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.rememberImagePicker
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(device = "spec:width=1080px,height=1920px,dpi=440", locale = "zh-rCN")
fun SettingsScreenPreview(modifier: Modifier = Modifier) {
    val viewState = SettingViewState(user = User(114514, "kaze", "kaze@tabidachi.cn", ""))
    val user = viewState.user
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imageCropper = rememberImageCropper()
    val imagePicker = rememberImagePicker(onImage = { uri ->
        scope.launch {
            when (val result = imageCropper.crop(uri, context)) {
                is CropResult.Success -> {
                }

                else -> {

                }
            }
        }
    })


    if (imageCropper.cropState != null) {
        ImageCropperDialog(state = imageCropper.cropState!!)
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ImageTopAppBar(
                image = {
                    Image(
                        painter = painterResource(id = R.drawable.transparent_akkarin),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                },
                title = {
                    SimpleListItem(
                        headlineContent = {
                            Text(text = user.username)
                        }, supportingContent = {
                            if (user.uid != -1L) {
                                Text(text = "UID: ${user.uid}")
                            }
                        }
                    )
                }, navigationIcon = {
                    IconButton(onClick = {
                    }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }, actions = {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = viewState.isMenuExpanded,
                        onDismissRequest = {}
                    ) {
                        SettingsDropdownMenuItem.values().forEach {
                            DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = it.stringRes))
                                }, onClick = {
                                    when (it) {
                                        SettingsDropdownMenuItem.AVATAR -> {
                                            imagePicker.pick()
                                        }

                                        SettingsDropdownMenuItem.LOGOUT -> {
                                        }

                                        SettingsDropdownMenuItem.PROFILE -> {
                                        }
                                    }
                                }, leadingIcon = {
                                    Icon(imageVector = it.leadingIcon, contentDescription = null)
                                }
                            )
                        }
                    }
                }, scrollBehavior = scrollBehavior
            )
        }
    ) {
        LazyColumn(
            contentPadding = it,
        ) {
            AccountPreview(user)
            item {
                Divider()
            }
            ThemePreview(viewState)
            item {
                Divider()
            }
            LanguagesPreview(viewState)
            item {
                Divider()
            }
            Permissions()
            item {
                Divider()
            }
            About()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.AccountPreview(user: User) {
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


fun LazyListScope.ThemePreview(viewState: SettingViewState) {
    item {
        val isDark = isSystemInDarkTheme()
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
                    Text(text = stringResource(id = DarkLight.SYSTEM.text))
                    DropdownMenu(
                        expanded = viewState.isDayNightMenuExpanded,
                        onDismissRequest = {
                        }) {
                        DarkLight.values().forEach {
                            DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = it.text))
                                }, onClick = {
                                }
                            )
                        }
                    }
                }, modifier = Modifier.clickable {
                }
            )
            ListItem(
                leadingContent = {
                    Icon(imageVector = Icons.Rounded.Palette, contentDescription = null)
                },
                headlineContent = {
                    Text(text = stringResource(id = R.string.theme))
                }, supportingContent = {
                    Text(text = stringResource(id = Theme.Dynamic.text))
                    DropdownMenu(
                        expanded = viewState.isThemeMenuExpanded,
                        onDismissRequest = {
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
                                }
                            )
                        }
                    }
                }, modifier = Modifier.clickable {
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

fun LazyListScope.LanguagesPreview(viewState: SettingViewState) {
    item {
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
                    Text(text = stringResource(id = Language.SYSTEM.text))
                    DropdownMenu(
                        expanded = viewState.isLanguageMenuExpanded,
                        onDismissRequest = {
                        }) {
                        Language.values().forEach {
                            DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = it.text))
                                }, onClick = {

                                }
                            )
                        }
                    }
                }, modifier = Modifier.clickable {
                }
            )
        }
    }
}
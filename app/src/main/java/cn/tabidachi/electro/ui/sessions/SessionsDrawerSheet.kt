package cn.tabidachi.electro.ui.sessions

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.sessions.components.UserCard
import coil.compose.AsyncImage

@Composable
fun SessionsDrawerSheet(
    modifier: Modifier = Modifier,
    onAvatarClick: () -> Unit,
    onDrawerItemClick: (DrawerSheetItem) -> Unit,
    addAccount: () -> Unit,
    switchAccount: () -> Unit
) {
    val viewModel: SessionsViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()
    var isExpand by remember {
        mutableStateOf(false)
    }
    val users by viewModel.sessions.collectAsState(initial = emptyList())
    ModalDrawerSheet(
        windowInsets = WindowInsets.statusBars.only(WindowInsetsSides.Start),
        drawerShape = RectangleShape,
    ) {
        Column(
            modifier = modifier
        ) {
            UserCard(user = viewState.user, onAvatarClick = onAvatarClick, isExpand = isExpand) {
                isExpand = !isExpand
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
                    .navigationBarsPadding()
            ) {
                AnimatedVisibility(visible = isExpand) {
                    Column {
                        Column {
                            users.forEach {
                                ListItem(
                                    headlineContent = {
                                        Text(text = it.username)
                                    }, leadingContent = {
                                        AsyncImage(
                                            model = it.avatar,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .size(32.dp)
                                        )
                                    }, modifier = Modifier.clickable {
                                        viewModel.switchAccount(it.uid) {
                                            switchAccount()
                                        }
                                    }
                                )
                            }
                        }
                        ListItem(
                            headlineContent = {
                                Text(text = stringResource(id = R.string.add_account))
                            }, leadingContent = {
                                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                            }, modifier = Modifier.clickable {
                                addAccount()
                            }
                        )
                        Divider()
                    }
                }
                DrawerSheetItem.values().forEach {
                    ListItem(
                        headlineContent = {
                            Text(text = stringResource(id = it.text))
                        }, leadingContent = {
                            Icon(imageVector = it.icon, contentDescription = null)
                        }, modifier = Modifier.clickable {
                            onDrawerItemClick(it)
                        }
                    )
                }
            }
        }
    }
}

enum class DrawerSheetItem(
    val icon: ImageVector, @StringRes val text: Int
) {
    CONTACT(Icons.Rounded.Contacts, R.string.contact),
    NEW_GROUP(Icons.Rounded.GroupAdd, R.string.create_new_group),
    NEW_CHANNEL(Icons.Rounded.Podcasts, R.string.create_channel),
    FAVORITE(Icons.Rounded.Bookmark, R.string.favorites),
    SETTINGS(Icons.Rounded.Settings, R.string.settings),
}
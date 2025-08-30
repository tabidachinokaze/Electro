package cn.tabidachi.electro.ui.channel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.ElectroNavigationActions
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelAdminScreen(
    sid: Long,
    navigationActions: ElectroNavigationActions,
    viewModel: ChannelViewModel,
) {
    val viewState by viewModel.viewState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "成员管理")
                }, navigationIcon = {
                    IconButton(onClick = navigationActions::navigateUp) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = it.calculateTopPadding())
        ) {
            items(viewState.users, key = {
                it.uid
            }) { user ->
                var menuVisible by remember {
                    mutableStateOf(false)
                }
                ListItem(
                    headlineContent = {
                        Text(text = user.username)
                    }, supportingContent = {
                        if (viewModel.online(user.uid)) {
                            Text(
                                text = stringResource(id = R.string.online),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(text = stringResource(id = R.string.offline))
                        }
                    }, leadingContent = {
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp),
                        ) {
                            AsyncImage(model = user.avatar, contentDescription = null)
                        }
                    }, modifier = Modifier.clickable {
                        navigationActions.navigateToPair(user.uid)
                    }, trailingContent = {
                        if (viewState.owner != user.uid) IconButton(onClick = { menuVisible = true }) {
                            Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = menuVisible,
                            onDismissRequest = { menuVisible = false }
                        ) {
                            if (!viewState.roles.any { it.uid == user.uid }) DropdownMenuItem(
                                text = {
                                    Text(text = "设置为管理员")
                                }, onClick = {
                                    viewModel.addAdmin(user.uid)
                                }
                            ) else DropdownMenuItem(
                                text = {
                                    Text(text = "取消管理员权限")
                                }, onClick = {
                                    viewModel.removeAdmin(user.uid)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(text = "从频道中移除")
                                }, onClick = {
                                    viewModel.removeMember(user.uid)
                                }
                            )
                        }
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}
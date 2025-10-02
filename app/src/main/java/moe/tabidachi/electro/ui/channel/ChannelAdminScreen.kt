package moe.tabidachi.electro.ui.channel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import moe.tabidachi.electro.R
import moe.tabidachi.electro.data.database.entity.User
import moe.tabidachi.electro.model.EmptyMessenger
import moe.tabidachi.electro.model.Messenger
import moe.tabidachi.electro.ui.channel.ChannelContract.Effect
import moe.tabidachi.electro.ui.channel.ChannelContract.Event
import moe.tabidachi.electro.ui.channel.ChannelContract.State
import moe.tabidachi.electro.ui.pair.navigateToPair
import moe.tabidachi.electro.ui.preview.PreviewSurface
import moe.tabidachi.electro.ui.preview.Previews
import coil3.compose.AsyncImage
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data object ChannelAdminRoute

context(navController: NavHostController)
fun NavGraphBuilder.channelAdmin() = composable<ChannelAdminRoute> {
    val viewModel: ChannelViewModel =
        hiltViewModel(navController.getBackStackEntry(ChannelRoute::class))
    val (state, event) = viewModel.observe {
        when (it) {
            Effect.NavigateUp -> navController.navigateUp()
        }
    }
    ChannelAdminScreen(
        state = state.value,
        event = {
            when (it) {
                is Event.NavigateToChannelAdmin -> navController.navigateToChannelAdmin()
                is Event.NavigateToChannelDetail -> navController.navigateToChannelDetail()
                is Event.NavigateToChannelEdit -> navController.navigateToChannelEdit()
                is Event.NavigateToChannelInvite -> navController.navigateToChannelInvite()
                is Event.NavigateToPair -> navController.navigateToPair(it.uid)
                Event.NavigateUp -> navController.navigateUp()
                else -> event(it)
            }
        },
        messenger = viewModel.messenger
    )
}

fun NavHostController.navigateToChannelAdmin() {
    navigate(ChannelAdminRoute)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelAdminScreen(
    state: State,
    event: (Event) -> Unit,
    messenger: Messenger
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "成员管理")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            event(Event.NavigateUp)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = it.calculateTopPadding())
        ) {
            items(
                items = state.users,
                key = { it.uid }
            ) { user ->
                var menuVisible by remember {
                    mutableStateOf(false)
                }
                ListItem(
                    headlineContent = {
                        Text(text = user.username)
                    },
                    supportingContent = {
                        if (messenger.online(user.uid)) {
                            Text(
                                text = stringResource(id = R.string.online),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(text = stringResource(id = R.string.offline))
                        }
                    },
                    leadingContent = {
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp),
                        ) {
                            AsyncImage(model = user.avatar, contentDescription = null)
                        }
                    },
                    modifier = Modifier.clickable {
                        event(Event.NavigateToPair(user.uid))
                    },
                    trailingContent = {
                        if (state.owner != user.uid) {
                            IconButton(onClick = { menuVisible = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = null
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = menuVisible,
                            onDismissRequest = { menuVisible = false }
                        ) {
                            if (!state.roles.any { it.uid == user.uid }) {
                                DropdownMenuItem(
                                    text = {
                                        Text(text = "设置为管理员")
                                    },
                                    onClick = {
                                        event(Event.AddAdmin(user.uid))
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = {
                                        Text(text = "取消管理员权限")
                                    },
                                    onClick = {
                                        event(Event.RemoveAdmin(user.uid))
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(text = "从频道中移除")
                                },
                                onClick = {
                                    event(Event.RemoveMember(user.uid))
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

@Composable
@Previews
private fun ChannelAdminScreenPreview() {
    PreviewSurface {
        ChannelAdminScreen(
            state = State(
                users = List(10) {
                    User(
                        uid = it.toLong(),
                        username = "$it",
                        email = "",
                        avatar = ""
                    )
                }
            ),
            event = {},
            messenger = EmptyMessenger
        )
    }
}

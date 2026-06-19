package moe.tabidachi.electro.ui.group

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Output
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe
import moe.tabidachi.electro.R
import moe.tabidachi.electro.model.EmptyMessenger
import moe.tabidachi.electro.model.Messenger
import moe.tabidachi.electro.ui.common.ImageTopAppBar
import moe.tabidachi.electro.ui.group.GroupContract.Event
import moe.tabidachi.electro.ui.group.GroupContract.State
import moe.tabidachi.electro.ui.pair.navigateToPair
import moe.tabidachi.electro.ui.preview.PreviewSurface
import moe.tabidachi.electro.ui.preview.Previews

@Serializable
data object GroupDetailRoute : NavKey

context(navController: NavHostController)
fun NavGraphBuilder.groupDetail() = composable<GroupDetailRoute> {
    val viewModel: GroupViewModel =
        hiltViewModel(navController.getBackStackEntry(GroupRoute::class))
    val (state, event) = viewModel.observe {
        when (it) {
            GroupContract.Effect.NavigateUp -> navController.navigateUp()
        }
    }
    GroupDetailScreen(
        state = state.value,
        event = {
            when (it) {
                is Event.NavigateToChannelAdmin -> navController.navigateToGroupAdmin()
                is Event.NavigateToChannelDetail -> navController.navigateToGroupDetail()
                is Event.NavigateToChannelEdit -> navController.navigateToGroupEdit()
                is Event.NavigateToChannelInvite -> navController.navigateToGroupInvite()
                is Event.NavigateToPair -> navController.navigateToPair(it.uid)
                Event.NavigateUp -> navController.navigateUp()
                else -> event(it)
            }
        },
        messenger = viewModel.messenger
    )
}

fun NavHostController.navigateToGroupDetail() {
    navigate(GroupDetailRoute)
}

context(backStack: NavBackStack<NavKey>)
fun EntryProviderScope<NavKey>.groupDetail(
    viewModel: GroupViewModel,
    onNavigateUp: () -> Unit
) = entry<GroupDetailRoute> {
    val (state, event) = viewModel.observe {
        when (it) {
            GroupContract.Effect.NavigateUp -> onNavigateUp()
        }
    }
    GroupDetailScreen(
        state = state.value,
        event = {
            when (it) {
                is Event.NavigateToChannelAdmin -> backStack.navigateToGroupAdmin()
                is Event.NavigateToChannelDetail -> backStack.navigateToGroupDetail()
                is Event.NavigateToChannelEdit -> backStack.navigateToGroupEdit()
                is Event.NavigateToChannelInvite -> backStack.navigateToGroupInvite()
                is Event.NavigateToPair -> backStack.navigateToPair(it.uid)
                Event.NavigateUp -> onNavigateUp()
                else -> event(it)
            }
        },
        messenger = viewModel.messenger
    )
}

fun NavBackStack<NavKey>.navigateToGroupDetail() {
    add(GroupDetailRoute)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroupDetailScreen(
    state: State,
    event: (Event) -> Unit,
    messenger: Messenger
) {
    var menuExpanded by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(Unit) {
        event(Event.GetSessionUser)
        event(Event.GetSessionInfo)
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            ImageTopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.dialog?.title ?: "",
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(id = R.string.online_count, messenger.online()),
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                },
                image = {
                    AsyncImage(
                        model = state.dialog?.image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { event(Event.NavigateUp) }
                    ) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (state.isAdmin) {
                        IconButton(
                            onClick = {
                                messenger.sid.value?.let {
                                    event(Event.NavigateToChannelEdit(it))
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Rounded.Edit, contentDescription = null)
                        }
                    }
                    IconButton(onClick = {
                        menuExpanded = true
                    }) {
                        Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = {
                        menuExpanded = false
                    }) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Output,
                                    contentDescription = null
                                )
                            },
                            text = {
                                Text(text = stringResource(id = R.string.leave_group))
                            },
                            onClick = {
                                event(Event.ExitGroup)
                                menuExpanded = false
                            }
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = it.calculateTopPadding())
        ) {
            item {
                HorizontalDivider(thickness = 8.dp)
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = {
                            Text(text = stringResource(id = R.string.add_members))
                        },
                        leadingContent = {
                            Icon(imageVector = Icons.Rounded.PersonAdd, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                messenger.sid.value?.let {
                                    event(Event.NavigateToChannelInvite(it))
                                }
                            }
                    )
                }
                HorizontalDivider(thickness = 8.dp)
            }
            stickyHeader {
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(id = R.string.members),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        if (state.isAdmin) {
                            IconButton(
                                onClick = {
                                    messenger.sid.value?.let {
                                        event(Event.NavigateToChannelAdmin(it))
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Rounded.Edit, contentDescription = null)
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
            items(state.users) {
                ListItem(
                    leadingContent = {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape
                        ) {
                            AsyncImage(model = it.avatar, contentDescription = null)
                        }
                    },
                    headlineContent = {
                        Text(text = it.username)
                    },
                    supportingContent = {
                        if (messenger.online(it.uid)) {
                            Text(
                                text = stringResource(id = R.string.online),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(text = stringResource(id = R.string.offline))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            event(Event.NavigateToPair(it.uid))
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
private fun GroupDetailScreenPreview() {
    PreviewSurface {
        GroupDetailScreen(
            state = State(),
            event = {},
            messenger = EmptyMessenger
        )
    }
}

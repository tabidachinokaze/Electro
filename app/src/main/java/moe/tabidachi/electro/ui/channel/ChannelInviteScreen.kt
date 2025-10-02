package moe.tabidachi.electro.ui.channel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import moe.tabidachi.electro.R
import moe.tabidachi.electro.ext.regex
import moe.tabidachi.electro.model.EmptyMessenger
import moe.tabidachi.electro.model.Messenger
import moe.tabidachi.electro.ui.channel.ChannelContract.Event
import moe.tabidachi.electro.ui.channel.ChannelContract.State
import moe.tabidachi.electro.ui.common.SimpleTextField
import moe.tabidachi.electro.ui.pair.navigateToPair
import moe.tabidachi.electro.ui.preview.PreviewSurface
import moe.tabidachi.electro.ui.preview.Previews
import coil3.compose.AsyncImage
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data object ChannelInviteRoute

context(navController: NavHostController)
fun NavGraphBuilder.channelInvite() = composable<ChannelInviteRoute> {
    val viewModel: ChannelViewModel =
        hiltViewModel(navController.getBackStackEntry(ChannelRoute::class))
    val (state, event) = viewModel.observe {
        when (it) {
            ChannelContract.Effect.NavigateUp -> navController.navigateUp()
        }
    }
    ChannelInviteScreen(
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

fun NavHostController.navigateToChannelInvite() {
    navigate(ChannelInviteRoute)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelInviteScreen(
    state: State,
    event: (Event) -> Unit,
    messenger: Messenger
) {
    LaunchedEffect(Unit) {
        event(Event.GetContact)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.add_members))
                },
                navigationIcon = {
                    IconButton(
                        onClick = { event(Event.NavigateUp) }
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
        Column(
            modifier = Modifier
                .padding(top = it.calculateTopPadding())
                .imePadding()
        ) {
            SimpleTextField(
                value = state.filter,
                onValueChange = {
                    event(Event.OnFilterChange(it))
                },
                placeholder = {
                    Text(text = stringResource(id = R.string.search_contacts))
                },
                maxLines = 1,
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                }
            )
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(
                    state.contacts.filter {
                        state.filter.regex().matches(it.username)
                    }
                ) { user ->
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
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape
                            ) {
                                AsyncImage(
                                    model = user.avatar,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.clickable {
                                        event(Event.NavigateToPair(user.uid))
                                    }
                                )
                            }
                        },
                        trailingContent = {
                            AnimatedVisibility(visible = !state.users.any { user.uid == it.uid }) {
                                Button(onClick = {
                                    event(Event.Invite(user.uid))
                                }) {
                                    Text(text = stringResource(id = R.string.invite))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                event(Event.NavigateToPair(user.uid))
                            }
                    )
                }
                item {
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
@Previews
private fun ChannelInviteScreenPreview() {
    PreviewSurface {
        ChannelInviteScreen(
            state = State(),
            event = {},
            messenger = EmptyMessenger
        )
    }
}

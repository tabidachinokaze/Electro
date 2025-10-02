package moe.tabidachi.electro.ui.sessions

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import moe.tabidachi.electro.R
import moe.tabidachi.electro.data.database.entity.SessionType
import moe.tabidachi.electro.ext.toast
import moe.tabidachi.electro.ui.common.DialogListItem
import moe.tabidachi.electro.ui.preview.PreviewSurface
import moe.tabidachi.electro.ui.preview.Previews
import moe.tabidachi.electro.ui.sessions.SessionsContract.Event
import moe.tabidachi.electro.ui.sessions.SessionsContract.State
import moe.tabidachi.electro.ui.sessions.components.SessionsFloatingActionButton
import moe.tabidachi.electro.ui.sessions.components.SessionsFloatingActionButtonItem
import de.charlex.compose.rememberSpeedDialFloatingActionButtonState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    state: State,
    event: (Event) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val buttonState = rememberSpeedDialFloatingActionButtonState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        event(Event.Pull)
    }
    LaunchedEffect(Unit) {
        event(Event.FindUser)
    }
    val refreshState = rememberPullToRefreshState()
    val context = LocalContext.current
    val dialogs = state.dialogs
    val scaleFraction = {
        if (state.isRefresh) {
            1f
        } else {
            LinearOutSlowInEasing.transform(refreshState.distanceFraction).coerceIn(0f, 1f)
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SessionsDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.9f),
                onAvatarClick = {
                    event(Event.NavigateToSettings)
                },
                onDrawerItemClick = {
                    scope.launch { drawerState.close() }
                    when (it) {
                        DrawerSheetItem.CONTACT -> {
                            event(Event.NavigateToContact)
                        }

                        DrawerSheetItem.NEW_GROUP -> {
                            event(Event.NavigateToGroupCreate)
                        }

                        DrawerSheetItem.NEW_CHANNEL -> {
                            event(Event.NavigateToChannelCreate)
                        }

                        DrawerSheetItem.FAVORITE -> {
                            context.toast("功能未实现")
                        }

                        DrawerSheetItem.SETTINGS -> {
                            event(Event.NavigateToSettings)
                        }
                    }
                },
                state = state,
                event = event
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(id = R.string.app_name))
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(imageVector = Icons.Rounded.Menu, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                event(Event.NavigateToSearch)
                            }
                        ) {
                            Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                SessionsFloatingActionButton(
                    buttonState = buttonState,
                    onFabItemClicked = {
                        when (it) {
                            SessionsFloatingActionButtonItem.CONTACT -> {
                                event(Event.NavigateToContact)
                            }

                            SessionsFloatingActionButtonItem.NEW_GROUP -> {
                                event(Event.NavigateToGroupCreate)
                            }

                            SessionsFloatingActionButtonItem.NEW_CHANNEL -> {
                                event(Event.NavigateToChannelCreate)
                            }
                        }
                    }
                )
            },
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .pullToRefresh(
                    state = refreshState,
                    isRefreshing = state.isRefresh,
                    onRefresh = {
                        event(Event.OnRefresh)
                    }
                )
        ) {
            Box(
                modifier = Modifier
                    // .pullRefresh(state = refreshState)
                    .fillMaxSize()
                    .padding(top = it.calculateTopPadding())
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(dialogs) {
                        DialogListItem(
                            image = it.image,
                            title = it.title,
                            subtitle = it.subtitle,
                            date = it.latest,
                            unread = it.unread,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (it.type) {
                                        SessionType.P2P -> {
                                            it.extras?.let {
                                                event(Event.NavigateToPair(it.toLong()))
                                            }
                                        }

                                        SessionType.ROOM -> {
                                            event(Event.NavigateToGroup(it.sid))
                                        }

                                        SessionType.CHANNEL -> {
                                            event(Event.NavigateToChannel(it.sid))
                                        }

                                        else -> {}
                                    }
                                }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            scaleX = scaleFraction()
                            scaleY = scaleFraction()
                        }
                ) {
                    PullToRefreshDefaults.Indicator(
                        state = refreshState,
                        isRefreshing = state.isRefresh
                    )
                }
            }
        }
    }
}

@Previews
@Composable
private fun SessionsScreenPreview() {
    PreviewSurface {
        SessionsScreen(
            state = State(),
            event = {}
        )
    }
}

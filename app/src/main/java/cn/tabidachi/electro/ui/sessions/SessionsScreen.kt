package cn.tabidachi.electro.ui.sessions

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.pullrefreshx.PullRefreshIndicator
import androidx.compose.material.pullrefreshx.pullRefresh
import androidx.compose.material.pullrefreshx.rememberPullRefreshState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.database.entity.SessionType
import cn.tabidachi.electro.ext.toast
import cn.tabidachi.electro.ui.ElectroNavigationActions
import cn.tabidachi.electro.ui.common.DialogListItem
import cn.tabidachi.electro.ui.sessions.components.SessionsFloatingActionButton
import cn.tabidachi.electro.ui.sessions.components.SessionsFloatingActionButtonItem
import de.charlex.compose.rememberSpeedDialFloatingActionButtonState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    navigationActions: ElectroNavigationActions
) {
    val viewModel: SessionsViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val buttonState = rememberSpeedDialFloatingActionButtonState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModel.pull()
    }
    LaunchedEffect(Unit) {
        viewModel.findUser()
    }
    val refreshState = rememberPullRefreshState(
        refreshing = viewState.isRefresh,
        onRefresh = {
            viewModel.onRefresh()
        }
    )
    val context = LocalContext.current
    val dialogs by viewModel.dialogs.collectAsState(initial = emptyList())
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SessionsDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.9f),
                onAvatarClick = {
                    navigationActions.navigateToSettings()
                },
                onDrawerItemClick = {
                    scope.launch { drawerState.close() }
                    when (it) {
                        DrawerSheetItem.CONTACT -> {
                            navigationActions.navigateToContact()
                        }
                        DrawerSheetItem.NEW_GROUP -> {
                            navigationActions.navigateToCreateGroup()
                        }
                        DrawerSheetItem.NEW_CHANNEL -> {
                            navigationActions.navigateToCreateChannel()
                        }
                        DrawerSheetItem.FAVORITE -> {
                            context.toast("功能未实现")
                        }
                        DrawerSheetItem.SETTINGS -> {
                            navigationActions.navigateToSettings()
                        }
                    }
                }, addAccount = {
                    navigationActions.navigateToAuth()
                }, switchAccount = {
                    navigationActions.navigateUp()
                    navigationActions.navigateToDialogs()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(id = R.string.app_name))
                    }, navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(imageVector = Icons.Rounded.Menu, contentDescription = null)
                        }
                    }, actions = {
                        IconButton(onClick = navigationActions::navigateToSearch) {
                            Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                        }
                    }, scrollBehavior = scrollBehavior
                )
            }, floatingActionButton = {
                SessionsFloatingActionButton(
                    buttonState = buttonState,
                    onFabItemClicked = {
                        when (it) {
                            SessionsFloatingActionButtonItem.CONTACT -> {
                                navigationActions.navigateToContact()
                            }

                            SessionsFloatingActionButtonItem.NEW_GROUP -> {
                                navigationActions.navigateToCreateGroup()
                            }

                            SessionsFloatingActionButtonItem.NEW_CHANNEL -> {
                                navigationActions.navigateToCreateChannel()
                            }
                        }
                    }
                )
            }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Box(
                modifier = Modifier
                    .pullRefresh(state = refreshState)
                    .fillMaxSize()
                    .padding(top = it.calculateTopPadding())
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        ListItem(
                            headlineContent = {
                                Text(text = "ChatGPT")
                            }, modifier = Modifier.clickable {
                                navigationActions.navigateToChatGPT()
                            }, leadingContent = {
                                Image(
                                    painter = painterResource(id = R.drawable.chatgpt),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp).clip(CircleShape)
                                )
                            }
                        )
                    }
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
                                            it.extras?.let { it1 ->
                                                navigationActions.navigateToPair(it1.toLong())
                                            }
                                        }

                                        SessionType.ROOM -> {
                                            navigationActions.navigateToGroup(it.sid)
                                        }

                                        SessionType.CHANNEL -> {
                                            navigationActions.navigateToChannel(it.sid)
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
                PullRefreshIndicator(
                    refreshing = viewState.isRefresh,
                    state = refreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}
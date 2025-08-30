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
//import androidx.compose.material.pullrefreshx.PullRefreshIndicator
//import androidx.compose.material.pullrefreshx.pullRefresh
//import androidx.compose.material.pullrefreshx.rememberPullRefreshState
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.database.entity.Dialog
import cn.tabidachi.electro.ui.common.DialogListItem
import cn.tabidachi.electro.ui.sessions.components.SessionsFloatingActionButton
import cn.tabidachi.electro.ui.sessions.components.SessionsFloatingActionButtonItem
import de.charlex.compose.rememberSpeedDialFloatingActionButtonState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(device = "spec:width=1080px,height=1920px,dpi=440", locale = "zh-rCN")
fun SessionsPreview(modifier: Modifier = Modifier) {
    val viewState = SessionsViewState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
    val buttonState = rememberSpeedDialFloatingActionButtonState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scope = rememberCoroutineScope()

    /*val refreshState = rememberPullRefreshState(
        refreshing = viewState.isRefresh,
        onRefresh = {
        }
    )*/
    val dialogs = remember {
        mutableStateListOf<Dialog>()
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SessionsDrawerSheetPreview(
                modifier = Modifier.fillMaxWidth(0.9f),
                onAvatarClick = {
                },
                onDrawerItemClick = {
                    scope.launch { drawerState.close() }
                    when (it) {
                        DrawerSheetItem.CONTACT -> {
                        }
                        DrawerSheetItem.NEW_GROUP -> {
                        }
                        DrawerSheetItem.NEW_CHANNEL -> {
                        }
                        DrawerSheetItem.FAVORITE -> {
                        }
                        DrawerSheetItem.SETTINGS -> {
                        }
                    }
                }, addAccount = {
                }, switchAccount = {
                }
            )
        }, modifier = modifier
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
                        IconButton(onClick = {}) {
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
                            }

                            SessionsFloatingActionButtonItem.NEW_GROUP -> {
                            }

                            SessionsFloatingActionButtonItem.NEW_CHANNEL -> {
                            }
                        }
                    }
                )
            }, modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Box(
                modifier = Modifier
                    //.pullRefresh(state = refreshState)
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

                                }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
                /*PullRefreshIndicator(
                    refreshing = viewState.isRefresh,
                    state = refreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )*/
            }
        }
    }
}
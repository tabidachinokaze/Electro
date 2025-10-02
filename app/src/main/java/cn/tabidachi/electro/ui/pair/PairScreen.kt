package cn.tabidachi.electro.ui.pair

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.R
import cn.tabidachi.electro.model.EmptyMessenger
import cn.tabidachi.electro.model.Messenger
import cn.tabidachi.electro.ui.common.EmptyMessageManager
import cn.tabidachi.electro.ui.common.MessageAction
import cn.tabidachi.electro.ui.common.MessageColumn
import cn.tabidachi.electro.ui.common.MessageManager
import cn.tabidachi.electro.ui.common.SimpleListItem
import cn.tabidachi.electro.ui.pair.PairContract.Event
import cn.tabidachi.electro.ui.pair.PairContract.State
import cn.tabidachi.electro.ui.preview.PreviewSurface
import cn.tabidachi.electro.ui.preview.Previews
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairScreen(
    state: State,
    event: (Event) -> Unit,
    messenger: Messenger,
    messageManager: MessageManager,
    action: MessageAction
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val targetUser = state.targetUser

    var menuExpanded by remember {
        mutableStateOf(false)
    }
    BackHandler {
        event(Event.NavigateUp)
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    SimpleListItem(
                        headlineContent = {
                            Text(text = targetUser?.username ?: "")
                        },
                        supportingContent = {
                            if (messenger.online(state.target)) {
                                Text(
                                    text = stringResource(id = R.string.online),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(text = stringResource(id = R.string.offline))
                            }
                        },
                        leadingContent = {
                            AsyncImage(
                                model = targetUser?.avatar,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .size(48.dp)
                                    .clip(CircleShape)
                            )
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            messenger.readMessage()
                            event(Event.NavigateUp)
                        }
                    ) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (state.uid != state.target) {
                        IconButton(onClick = { event(Event.Call) }) {
                            Icon(imageVector = Icons.Rounded.Call, contentDescription = null)
                        }
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        state.menu.forEach {
                            DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = it.text))
                                },
                                leadingIcon = {
                                    Icon(imageVector = it.icon, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    event(Event.OnMenuClick(it))
                                }
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        MessageColumn(
            messenger = messenger,
            modifier = Modifier.padding(top = it.calculateTopPadding()),
            messageManager = messageManager,
            action = action
        )
    }
}

@Composable
@Previews
private fun PairScreenPreview() {
    PreviewSurface {
        PairScreen(
            state = State(
                uid = 0,
                target = 0
            ),
            event = {},
            messenger = EmptyMessenger,
            messageManager = EmptyMessageManager,
            action = MessageAction()
        )
    }
}

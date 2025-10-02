package cn.tabidachi.electro.ui.channel

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Output
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.R
import cn.tabidachi.electro.model.EmptyMessenger
import cn.tabidachi.electro.model.Messenger
import cn.tabidachi.electro.ui.channel.ChannelContract.Event
import cn.tabidachi.electro.ui.channel.ChannelContract.State
import cn.tabidachi.electro.ui.common.EmptyMessageManager
import cn.tabidachi.electro.ui.common.MessageAction
import cn.tabidachi.electro.ui.common.MessageColumn
import cn.tabidachi.electro.ui.common.MessageManager
import cn.tabidachi.electro.ui.preview.PreviewSurface
import cn.tabidachi.electro.ui.preview.Previews
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelScreen(
    state: State,
    event: (Event) -> Unit,
    messenger: Messenger,
    messageManager: MessageManager,
    action: MessageAction
) {
    var dropMenuExpand by remember {
        mutableStateOf(false)
    }
    BackHandler {
        event(Event.NavigateUp)
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        AsyncImage(
                            model = state.dialog?.image,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                        Column {
                            Text(
                                text = state.dialog?.title ?: "",
                                maxLines = 1,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(
                                    id = R.string.online_count,
                                    messenger.online()
                                ),
                                maxLines = 1,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
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
                },
                actions = {
                    IconButton(
                        onClick = {
                            dropMenuExpand = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = dropMenuExpand,
                        onDismissRequest = { dropMenuExpand = false }
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Output,
                                    contentDescription = null
                                )
                            },
                            text = {
                                Text(text = stringResource(id = R.string.leave_channel))
                            },
                            onClick = {
                                event(Event.ExitGroup)
                                dropMenuExpand = false
                            }
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    },
                    indication = null,
                    onClick = {
                        messenger.sid.value?.let {
                            event(Event.NavigateToChannelDetail(it))
                        }
                    }
                )
            )
        }
    ) {
        MessageColumn(
            messenger = messenger,
            messageManager = messageManager,
            modifier = Modifier.padding(top = it.calculateTopPadding()),
            canSendMessage = state.canSendMessage,
            isMultiSession = true,
            action = action
        )
    }
}

@Composable
@Previews
private fun ChannelScreenPreview() {
    PreviewSurface {
        ChannelScreen(
            state = State(),
            event = {},
            messenger = EmptyMessenger,
            messageManager = EmptyMessageManager,
            action = MessageAction(),
        )
    }
}

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
import androidx.compose.material.icons.rounded.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.ElectroNavigationActions
import cn.tabidachi.electro.ui.common.MessageColumn
import cn.tabidachi.electro.ui.common.MessageViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelScreen(
    sid: Long,
    navigationActions: ElectroNavigationActions
) {
    val viewModel: ChannelViewModel = hiltViewModel()
    val mvm: MessageViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()
    LaunchedEffect(key1 = sid, block = {
        viewModel.sid.value = sid
        viewModel.getAdmin(sid)
    })
    var dropMenuExpand by remember {
        mutableStateOf(false)
    }
    BackHandler {
        viewModel.readMessage()
        navigationActions.navigateUp()
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        AsyncImage(
                            model = viewState.dialog?.image,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                        Column {
                            Text(
                                text = viewState.dialog?.title ?: "",
                                maxLines = 1,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(
                                    id = R.string.online_count,
                                    viewModel.online()
                                ), maxLines = 1,
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }, navigationIcon = {
                    IconButton(onClick = {
                        viewModel.readMessage()
                        navigationActions.navigateUp()
                    }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }, actions = {
                    IconButton(onClick = {
                        dropMenuExpand = true
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(expanded = dropMenuExpand, onDismissRequest = {
                        dropMenuExpand = false
                    }) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Output,
                                    contentDescription = null
                                )
                            }, text = {
                                Text(text = stringResource(id = R.string.leave_channel))
                            }, onClick = {
                                viewModel.exitGroup(sid, navigationActions::navigateUp)
                                dropMenuExpand = false
                            }
                        )
                    }
                }, scrollBehavior = scrollBehavior,
                modifier = Modifier.clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    }, indication = null,
                    onClick = {
                        navigationActions.navigateToChannelDetail(sid)
                    }
                )
            )
        }
    ) {
        MessageColumn(
            viewModel = viewModel,
            messageViewModel = mvm,
            navigationActions = navigationActions,
            modifier = Modifier.padding(top = it.calculateTopPadding()),
            canSendMessage = viewState.canSendMessage
        )
    }
}
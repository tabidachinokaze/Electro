package cn.tabidachi.electro.ui.group

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
import androidx.compose.material3.Divider
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.ElectroNavigationActions
import cn.tabidachi.electro.ui.common.ImageTopAppBar
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroupDetailScreen(
    sid: Long,
    navigationActions: ElectroNavigationActions,
    viewModel: GroupViewModel
) {
    val viewState by viewModel.viewState.collectAsState()
    var menuExpanded by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = Unit, block = {
        viewModel.getSessionUser(sid)
        viewModel.getSessionInfo(sid)
        viewModel.getAdmin(sid)
    })
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            ImageTopAppBar(
                title = {
                    Column {
                        Text(
                            text = viewState.dialog?.title ?: "",
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(id = R.string.online_count, viewModel.online()),
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }, image = {
                    AsyncImage(
                        model = viewState.dialog?.image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }, navigationIcon = {
                    IconButton(onClick = navigationActions::navigateUp) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }, actions = {
                    if (viewState.isAdmin) {
                        IconButton(onClick = {
                            navigationActions.navigateToGroupEdit(sid)
                        }) {
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
                            }, text = {
                                Text(text = stringResource(id = R.string.leave_group))
                            }, onClick = {
                                viewModel.exitGroup(sid, navigationActions::navigateUp)
                                menuExpanded = false
                            }
                        )
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = it.calculateTopPadding())
        ) {
            item {
                Divider(thickness = 8.dp)
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = {
                            Text(text = stringResource(id = R.string.add_members))
                        }, leadingContent = {
                            Icon(imageVector = Icons.Rounded.PersonAdd, contentDescription = null)
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navigationActions.navigateToInvite(sid)
                            }
                    )
                }
                Divider(thickness = 8.dp)
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
                    }, trailingContent = {
                        if (viewState.isAdmin) {
                            IconButton(onClick = {
                                navigationActions.navigateToGroupAdmin(sid)
                            }) {
                                Icon(imageVector = Icons.Rounded.Edit, contentDescription = null)
                            }
                        }
                    }
                )
                Divider()
            }
            items(viewState.users) {
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
                    }, supportingContent = {
                        if (viewModel.online(it.uid)) {
                            Text(text = stringResource(id = R.string.online), color = MaterialTheme.colorScheme.primary)
                        } else {
                            Text(text = stringResource(id = R.string.offline))
                        }
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navigationActions.navigateToPair(it.uid)
                        }
                )
            }
            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}
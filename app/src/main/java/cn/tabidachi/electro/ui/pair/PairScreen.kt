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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.ElectroNavigationActions
import cn.tabidachi.electro.ui.common.MessageColumn
import cn.tabidachi.electro.ui.common.MessageViewModel
import cn.tabidachi.electro.ui.common.SimpleListItem
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairScreen(
    target: Long,
    navigationActions: ElectroNavigationActions,
    navHostController: NavHostController
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val viewModel: PairViewModel = hiltViewModel()
    val messageViewModel: MessageViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()
    val targetUser = viewState.targetUser
    LaunchedEffect(target) {
        viewModel.setTarget(target)
    }

    var menuExpanded by remember {
        mutableStateOf(false)
    }
    BackHandler {
        viewModel.readMessage()
        navigationActions.navigateUp()
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    SimpleListItem(
                        headlineContent = {
                            Text(text = targetUser?.username ?: "")
                        }, supportingContent = {
                            if (viewModel.online(target)) {
                                Text(
                                    text = stringResource(id = R.string.online),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(text = stringResource(id = R.string.offline))
                            }
                        }, leadingContent = {
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
                }, navigationIcon = {
                    IconButton(onClick = {
                        viewModel.readMessage()
                        navigationActions.navigateUp()
                    }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }, actions = {
                    if (viewModel.ktor.uid != target) {
                        IconButton(onClick = {
                            viewModel.call()
                        }) {
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
                        viewState.menu.forEach {
                            DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = it.text))
                                },
                                leadingIcon = {
                                    Icon(imageVector = it.icon, contentDescription = null)
                                }, onClick = {
                                    menuExpanded = false
                                    viewModel.onMenuClick(it)
                                }
                            )
                        }
                    }
                }, scrollBehavior = scrollBehavior
            )
        }
    ) {
        MessageColumn(
            viewModel = viewModel,
            modifier = Modifier.padding(top = it.calculateTopPadding()),
            navigationActions = navigationActions,
            messageViewModel = messageViewModel,
        )
    }
}
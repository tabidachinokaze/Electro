package cn.tabidachi.electro.ui.group

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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ext.regex
import cn.tabidachi.electro.ui.ElectroNavigationActions
import cn.tabidachi.electro.ui.common.SimpleTextField
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteScreen(
    sid: Long,
    navigationActions: ElectroNavigationActions,
    viewModel: GroupViewModel = hiltViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()
    LaunchedEffect(key1 = sid, block = {
        viewModel.setSessionId(sid)
        viewModel.getContact()
    })
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.add_members))
                }, navigationIcon = {
                    IconButton(onClick = navigationActions::navigateUp) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
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
                value = viewState.filter,
                onValueChange = {
                    viewModel.onFilterChange(it)
                }, placeholder = {
                    Text(text = stringResource(id = R.string.search_contacts))
                }, maxLines = 1
            )
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(
                    viewState.contacts.filter {
                        viewState.filter.regex().matches(it.username)
                    }
                ) { user ->
                    ListItem(
                        headlineContent = {
                            Text(text = user.username)
                        }, supportingContent = {
                            Text(text = stringResource(id = R.string.online_count, viewModel.online()))
                        }, leadingContent = {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape
                            ) {
                                AsyncImage(
                                    model = user.avatar,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.clickable {
                                        navigationActions.navigateToPair(user.uid)
                                    }
                                )
                            }
                        }, trailingContent = {
                            AnimatedVisibility(visible = !viewState.users.any { user.uid == it.uid }) {
                                Button(onClick = {
                                    viewModel.invite(user.uid)
                                }) {
                                    Text(text = stringResource(id = R.string.invite))
                                }
                            }
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navigationActions.navigateToPair(user.uid)
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
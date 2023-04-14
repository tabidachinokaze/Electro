package cn.tabidachi.electro.ui.search

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.tabidachi.electro.R
import cn.tabidachi.electro.data.database.entity.Dialog
import cn.tabidachi.electro.data.database.entity.SessionSearch
import cn.tabidachi.electro.data.database.entity.SessionType
import cn.tabidachi.electro.model.UserQuery
import cn.tabidachi.electro.ui.ElectroNavigationActions
import cn.tabidachi.electro.ui.common.SearchTextField
import coil.compose.AsyncImage
import com.google.accompanist.pager.pagerTabIndicatorOffset
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    navigationActions: ElectroNavigationActions
) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    val viewModel: SearchViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchTextField(
                        value = viewState.query,
                        onValueChange = viewModel::queryValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = stringResource(id = R.string.search))
                        }, onSearch = {
                            viewModel.onSearch()
                        }
                    )
                }, navigationIcon = {
                    IconButton(onClick = navigationActions::navigateUp) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }, actions = {
                    AnimatedVisibility(visible = viewState.query.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.queryValueChange("")
                        }) {
                            Icon(imageVector = Icons.Rounded.Clear, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(top = it.calculateTopPadding())
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions: List<TabPosition> ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                    )
                }
            ) {
                SearchTab.values().forEachIndexed { index, searchTab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(text = stringResource(id = searchTab.text))
                        }
                    )
                }
            }
            HorizontalPager(pageCount = SearchTab.values().size, state = pagerState) {
                when (SearchTab.values()[it]) {
                    SearchTab.DIALOG -> when (val state = viewState.dialogs) {
                        DialogSearchState.Failure -> {}
                        DialogSearchState.None -> None()
                        is DialogSearchState.Success -> Dialogs(state.value, navigationActions)
                    }

                    SearchTab.GROUP -> when (val state = viewState.groups) {
                        SessionSearchState.Failure -> {}
                        SessionSearchState.None -> None()
                        is SessionSearchState.Success -> Groups(
                            state.value,
                            viewModel::onGroupJoinRequest
                        )
                    }

                    SearchTab.CHANNEL -> when (val state = viewState.channels) {
                        SessionSearchState.Failure -> {}
                        SessionSearchState.None -> None()
                        is SessionSearchState.Success -> Channels(state.value)
                    }

                    SearchTab.USER -> when (val state = viewState.users) {
                        UserSearchState.Failure -> {}
                        UserSearchState.None -> None()
                        is UserSearchState.Success -> Users(state.value, navigationActions)
                    }
                }
            }
        }
    }
}

@Composable
fun None() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.align(Alignment.Center)) {
            Text(text = stringResource(id = R.string.please_search))
        }
    }
}

@Composable
fun NotFound() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.align(Alignment.Center)) {
            Text(text = stringResource(id = R.string.no_results))
        }
    }
}

@Composable
fun Dialogs(
    dialogs: List<Dialog>,
    navigationActions: ElectroNavigationActions
) {
    AnimatedVisibility(visible = dialogs.isEmpty()) {
        NotFound()
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        items(dialogs) {
            ListItem(
                headlineContent = {
                    Text(text = it.title ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }, leadingContent = {
                    AsyncImage(
                        model = it.image,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                }, supportingContent = {
                    Text(text = it.subtitle ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }, modifier = Modifier.clickable {
                    when (it.type) {
                        SessionType.NONE -> {

                        }

                        SessionType.P2P -> {
                            it.extras?.toLong()
                                ?.let { it1 -> navigationActions.navigateToPair(it1) }
                        }

                        SessionType.ROOM -> {
                            navigationActions.navigateToGroup(it.sid)
                        }

                        SessionType.CHANNEL -> {}
                    }
                }
            )
        }
        item {
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Groups(
    groups: List<SessionSearch>,
    onJoinRequest: (Long) -> Unit
) {
    AnimatedVisibility(visible = groups.isEmpty()) {
        NotFound()
    }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    val (current, function) = remember {
        mutableStateOf<SessionSearch?>(null)
    }
    BackHandler(scaffoldState.bottomSheetState.isVisible) {
        scope.launch {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp)
                    .heightIn(min = 200.dp)
            ) {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(model = current?.image, contentDescription = null)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = current?.title ?: "",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }, supportingContent = {
                                Column {
                                    Text(
                                        text = stringResource(
                                            id = R.string.group_id,
                                            "${current?.sid ?: 0}"
                                        )
                                    )
                                    Text(
                                        text = stringResource(
                                            id = R.string.members_count,
                                            current?.count ?: 0
                                        )
                                    )
                                }
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            current?.sid?.let(onJoinRequest)
                        }, contentPadding = PaddingValues(8.dp),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                        Text(text = stringResource(id = R.string.join))
                    }
                }
                OutlinedTextField(
                    value = current?.description ?: "",
                    onValueChange = { },
                    label = {
                        Text(text = stringResource(id = R.string.group_description))
                    }, modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            items(groups) {
                ListItem(
                    headlineContent = {
                        Text(text = it.title ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }, leadingContent = {
                        AsyncImage(
                            model = it.image,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                function(it)
                                scaffoldState.bottomSheetState.expand()
                            }
                        },
                    supportingContent = {
                        Text(text = it.description.takeIf { !it.isNullOrBlank() } ?: stringResource(
                            id = R.string.group_id,
                            "${it.sid}"
                        ), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}


@Composable
fun Channels(
    channels: List<SessionSearch>
) {
    AnimatedVisibility(visible = channels.isEmpty()) {
        NotFound()
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        items(channels) {
            ListItem(
                headlineContent = {
                    Text(text = it.title ?: "")
                }, leadingContent = {
                    AsyncImage(
                        model = it.image,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                }
            )
        }
        item {
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
fun Users(
    users: List<UserQuery>,
    navigationActions: ElectroNavigationActions,
) {
    AnimatedVisibility(visible = users.isEmpty()) {
        NotFound()
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        items(users) {
            ListItem(
                headlineContent = {
                    Text(text = it.username)
                }, leadingContent = {
                    AsyncImage(
                        model = it.avatar,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                }, supportingContent = {
                    Text(text = "uid: ${it.uid}")
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

enum class SearchTab(@StringRes val text: Int) {
    DIALOG(R.string.dialogs),
    GROUP(R.string.groups),
    CHANNEL(R.string.channels),
    USER(R.string.users)
}
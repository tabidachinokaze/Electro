package cn.tabidachi.electro.ui.contact

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ext.regex
import cn.tabidachi.electro.ui.ElectroNavigationActions
import coil3.compose.AsyncImage

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
@Composable
fun ContactScreen(
    navHostController: NavHostController,
    navigationActions: ElectroNavigationActions
) {
    val viewModel: ContactViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()
    val isSearch = viewState.isSearch
    LaunchedEffect(Unit) {
        viewModel.getContact()
    }
    BackHandler(isSearch) {
        viewModel.changeSearchState(false)
    }
    val focusRequester = remember {
        FocusRequester()
    }
    LaunchedEffect(key1 = viewState.isSearch, block = {
        if (viewState.isSearch) {
            focusRequester.requestFocus()
        }
    })
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = isSearch,
                        modifier = Modifier.focusRequester(focusRequester), label = ""
                    ) {
                        if (it) {
                            BasicTextField(
                                value = viewState.filter,
                                onValueChange = viewModel::onQueryValueChange,
                                decorationBox = {
                                    TextFieldDefaults.DecorationBox(
                                        value = viewState.filter,
                                        innerTextField = it,
                                        enabled = true,
                                        singleLine = true,
                                        visualTransformation = VisualTransformation.None,
                                        interactionSource = remember {
                                            MutableInteractionSource()
                                        },
                                        placeholder = {
                                            Text(text = stringResource(id = R.string.search))
                                        },
                                        contentPadding = PaddingValues(),
                                        container = {},
                                        colors = TextFieldDefaults.colors(),
                                        trailingIcon = {
                                            IconButton(onClick = {
                                                viewModel.changeSearchState(false)
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Clear,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    )
                                }, keyboardActions = KeyboardActions(
                                    onSearch = {
                                        viewModel.onSearch()
                                    }
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(text = stringResource(id = R.string.contact))
                        }
                    }
                }, navigationIcon = {
                    IconButton(onClick = {
                        navHostController.navigateUp()
                    }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }, actions = {
                    AnimatedVisibility(visible = !isSearch) {
                        IconButton(onClick = {
                            viewModel.changeSearchState(true)
                        }) {
                            Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                        }
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = it.calculateTopPadding())
        ) {
            if (viewState.users.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize()) {
                        Text(text = stringResource(id = R.string.no_contact), modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
            items(viewState.users.filter {
                viewState.filter.regex().matches(it.username)
            }) {
                ListItem(
                    headlineContent = {
                        Text(text = it.username)
                    }, supportingContent = {
                        if (viewModel.online(it.uid)) {
                            Text(text = stringResource(id = R.string.online), color = MaterialTheme.colorScheme.primary)
                        } else {
                            Text(text = stringResource(id = R.string.offline))
                        }
                    }, leadingContent = {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            tonalElevation = 2.dp
                        ) {
                            AsyncImage(
                                model = it.avatar,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.clickable {
                                    navigationActions.navigateToPair(it.uid)
                                }
                            )
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
package cn.tabidachi.electro.ui.common

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import cn.tabidachi.electro.model.DownloadMessageItem
import cn.tabidachi.electro.model.Messenger
import cn.tabidachi.electro.ui.ElectroNavigationActions
import cn.tabidachi.electro.ui.component.PopupMenu
import cn.tabidachi.electro.ui.component.popupMenuAnchor
import cn.tabidachi.electro.ui.component.rememberPopupState
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageColumn(
    modifier: Modifier = Modifier,
    viewModel: Messenger,
    messageViewModel: MessageViewModel,
    navigationActions: ElectroNavigationActions,
    isMultiSession: Boolean = false,
    canSendMessage: Boolean = true,
) {
    val listState = rememberLazyListState()
    val messages = viewModel.messages
    val messageSendingQueue = viewModel.uploadMessages
    val scope = rememberCoroutineScope()
    val firstVisibleItem by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val clipboardManager = LocalClipboardManager.current
    var popupItem by remember {
        mutableStateOf<DownloadMessageItem?>(null)
    }
    val popupState = rememberPopupState()
    val refreshState = rememberPullToRefreshState()
    val scaleFraction = {
        if (viewModel.isRefresh) 1f
        else LinearOutSlowInEasing.transform(refreshState.distanceFraction).coerceIn(0f, 1f)
    }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                reverseLayout = true,
                modifier = Modifier
                    .matchParentSize()
                    .pullToRefresh(
                        state = refreshState,
                        isRefreshing = viewModel.isRefresh,
                        onRefresh = viewModel::onRefresh
                    )
            ) {
                items(messageSendingQueue) { item ->
                    val (menu, onMenuChange) = remember {
                        mutableStateOf(false)
                    }
                    MessageBubble(
                        isIncoming = false,
                        modifier = Modifier.clickable {
                            onMenuChange(true)
                        }
                    ) {
                        UploadMessage(item)
                        DropdownMenu(
                            expanded = menu,
                            onDismissRequest = {
                                onMenuChange(false)
                            },
                        ) {
                            DropdownMenuItem(text = {
                                Text(text = "取消发送")
                            }, onClick = {
                                item.cancel()
                                item.cancelMessage()
                            })
                        }
                    }
                }
                items(messages) { item ->
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.popupMenuAnchor(popupState)
                    ) {
                        if (item.type == BubbleType.Incoming && isMultiSession) {
                            Surface(
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = CircleShape,
                                modifier = Modifier
                                    .padding(start = 8.dp, bottom = 4.dp)
                                    .size(48.dp)
                            ) {
                                AsyncImage(
                                    model = item.user?.avatar,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.clickable {
                                        item.user?.let {
                                            navigationActions.navigateToPair(it.uid)
                                        }
                                    }
                                )
                            }
                        }
                        MessageBubble(
                            isIncoming = item.type == BubbleType.Incoming,
                            modifier = Modifier.clickable(
                                interactionSource = item.interactionSource,
                                indication = LocalIndication.current,
                                onClick = {
                                    popupState.show()
                                    popupItem = item
                                }
                            )
                        ) {
                            val replyItem =
                                viewModel.messages.firstOrNull { it.message.mid == item.message.reply }
                            AttachmentMessage(
                                item = item,
                                replyContent = {
                                    if (replyItem != null) ReplyContent(
                                        item = replyItem,
                                        scope = scope,
                                        color = when (item.type) {
                                            BubbleType.Incoming -> MaterialTheme.colorScheme.secondary
                                            BubbleType.Outgoing -> MaterialTheme.colorScheme.primary
                                        },
                                        onScrollTo = {
                                            listState.animateScrollToItem(
                                                viewModel.messages.indexOf(
                                                    replyItem
                                                )
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        scaleX = scaleFraction()
                        scaleY = scaleFraction()
                    }
            ) {
                PullToRefreshDefaults.Indicator(
                    state = refreshState,
                    isRefreshing = viewModel.isRefresh
                )
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = firstVisibleItem > 1 && listState.isScrollingUp(),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }, shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.inversePrimary
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
        }
        if (canSendMessage) BottomMessageField(
            sessionIdRequest = viewModel::getSessionId,
            replyRequest = viewModel::getReplyId,
            onSuccess = viewModel::onMessageSendSuccess,
            replyContent = {
                val item = viewModel.messages.firstOrNull { it.message.mid == viewModel.reply }
                androidx.compose.animation.AnimatedVisibility(
                    visible = viewModel.reply != null
                ) {
                    item ?: return@AnimatedVisibility
                    ReplyContent(
                        item = item,
                        scope = scope,
                        onReplyClear = viewModel::onReplyClear,
                        onScrollTo = {
                            listState.animateScrollToItem(viewModel.messages.indexOf(item))
                        }
                    )
                }
            },
            modifier = Modifier.imePadding(),
            viewModel = messageViewModel,
        ) else Spacer(modifier = Modifier.navigationBarsPadding())
    }
    PopupMenu(
        state = popupState,
        onDismissRequest = {
            popupState.hide()
        }
    ) {
        popupItem?.let { popupItem ->
            popupItem.menus.forEach {
                DropdownMenuItem(
                    text = {
                        Text(text = stringResource(id = it.text))
                    }, leadingIcon = {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = null
                        )
                    }, onClick = {
                        popupState.hide()
                        when (it) {
                            MessageMenu.Reply -> {
                                viewModel.onReply(popupItem.message.mid)
                            }

                            MessageMenu.Copy -> {
                                when {
                                    !popupItem.message.text.isNullOrBlank() -> {
                                        AnnotatedString(popupItem.message.text).let {
                                            clipboardManager.setText(it)
                                        }
                                    }

                                    else -> {}
                                }
                            }

                            MessageMenu.Forward -> {}
                            MessageMenu.Edit -> {}
                            MessageMenu.Delete -> {
                                viewModel.deleteMessage(popupItem.message.mid)
                            }
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun MessageDropdownMenu(
    expanded: Boolean,
    menus: List<MessageMenu>,
    onDismissRequest: (Boolean) -> Unit,
    onMenuClick: (MessageMenu) -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = {
        onDismissRequest(false)
    }) {
        menus.forEach {
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(id = it.text))
                }, leadingIcon = {
                    Icon(
                        imageVector = it.icon,
                        contentDescription = null
                    )
                }, onClick = {
                    onDismissRequest(false)
                    onMenuClick(it)
                }
            )
        }
    }
}


@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}
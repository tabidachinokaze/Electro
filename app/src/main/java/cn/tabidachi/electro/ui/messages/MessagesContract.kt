package cn.tabidachi.electro.ui.messages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.staticCompositionLocalOf
import cn.tabidachi.electro.model.DownloadMessageItem

/**
 * UI State that represents MessagesScreen
 **/
class MessagesState(

) {
    val messages = mutableStateListOf<DownloadMessageItem>()
}

/**
 * Messages Actions emitted from the UI Layer
 * passed to the coordinator to handle
 **/
data class MessagesActions(
    val onClick: () -> Unit = {}
)

/**
 * Compose Utility to retrieve actions from nested components
 **/
val LocalMessagesActions = staticCompositionLocalOf<MessagesActions> {
    error("{NAME} Actions Were not provided, make sure ProvideMessagesActions is called")
}

@Composable
fun ProvideMessagesActions(actions: MessagesActions, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalMessagesActions provides actions) {
        content.invoke()
    }
}


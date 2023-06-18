package cn.tabidachi.electro.ui.messages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MessagesRoute(
    coordinator: MessagesCoordinator = rememberMessagesCoordinator()
) {
    // State observing and declarations
    val uiState by coordinator.screenStateFlow.collectAsStateWithLifecycle(MessagesState())

    // UI Actions
    val actions = rememberMessagesActions(coordinator)

    // UI Rendering
    MessagesScreen(uiState, actions)
}


@Composable
fun rememberMessagesActions(coordinator: MessagesCoordinator): MessagesActions {
    return remember(coordinator) {
        MessagesActions(
            onClick = coordinator::doStuff
        )
    }
}
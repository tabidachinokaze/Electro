package cn.tabidachi.electro.ui.channel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ChannelRoute(
    coordinator: ChannelCoordinator = rememberChannelCoordinator()
) {
    // State observing and declarations
    val uiState by coordinator.screenStateFlow.collectAsStateWithLifecycle(ChannelState())

    // UI Actions
    val actions = rememberChannelActions(coordinator)

    // UI Rendering
//    ChannelScreen(uiState, actions)
}


@Composable
fun rememberChannelActions(coordinator: ChannelCoordinator): ChannelActions {
    return remember(coordinator) {
        ChannelActions(
            onClick = coordinator::doStuff
        )
    }
}
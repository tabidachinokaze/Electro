package cn.tabidachi.electro.ui.server

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import cn.tabidachi.electro.ui.ElectroDestinations
import cn.tabidachi.electro.ui.ElectroNavigationActions

@Composable
fun ServerRoute(
    coordinator: ServerCoordinator = rememberServerCoordinator(),
) {
    // State observing and declarations
    val state by coordinator.state.collectAsStateWithLifecycle(ServerState())

    // UI Actions
    val actions = rememberServerActions(coordinator)

    // UI Rendering
    ServerScreen(state, actions)
}

@Composable
fun rememberServerActions(coordinator: ServerCoordinator): ServerActions {
    return remember(coordinator) {
        ServerActions(
            onClick = coordinator::doStuff,
            showDialog = coordinator::showDialog,
            hideDialog = coordinator::hideDialog,
            onSave = coordinator::onSave,
            onDialogValueChange = coordinator::onDialogValueChange,
            onNavigateUp = coordinator::onNavigateUp
        )
    }
}

fun NavGraphBuilder.serverRoute(navigationActions: ElectroNavigationActions) {
    composable(ElectroDestinations.SERVER_ROUTE) {
        ServerRoute(coordinator = rememberServerCoordinator(navigationActions = navigationActions))
    }
}
package cn.tabidachi.electro.ui.imagepreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ImagePreviewRoute(
    coordinator: ImagePreviewCoordinator = rememberImagePreviewCoordinator()
) {
    // State observing and declarations
    val uiState by coordinator.screenStateFlow.collectAsStateWithLifecycle(ImagePreviewState())

    // UI Actions
    val actions = rememberImagePreviewActions(coordinator)

    // UI Rendering
    ImagePreviewScreen(uiState, actions)
}


@Composable
fun rememberImagePreviewActions(coordinator: ImagePreviewCoordinator): ImagePreviewActions {
    return remember(coordinator) {
        ImagePreviewActions(
            onClick = coordinator::doStuff
        )
    }
}
package cn.tabidachi.electro.ui.imagepreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Screen's coordinator which is responsible for handling actions from the UI layer
 * and one-shot actions based on the new UI state
 */
class ImagePreviewCoordinator(
    val viewModel: ImagePreviewViewModel
) {
    val screenStateFlow = viewModel.stateFlow

    fun doStuff() {
        // TODO Handle UI Action
    }
}

@Composable
fun rememberImagePreviewCoordinator(
    viewModel: ImagePreviewViewModel = hiltViewModel()
): ImagePreviewCoordinator {
    return remember(viewModel) {
        ImagePreviewCoordinator(
            viewModel = viewModel
        )
    }
}
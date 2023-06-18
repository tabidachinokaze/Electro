package cn.tabidachi.electro.ui.channel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Screen's coordinator which is responsible for handling actions from the UI layer
 * and one-shot actions based on the new UI state
 */
class ChannelCoordinator(
    val viewModel: ChannelViewModel
) {
    val screenStateFlow = viewModel.viewState

    fun doStuff() {
        // TODO Handle UI Action
    }
}

@Composable
fun rememberChannelCoordinator(
    viewModel: ChannelViewModel = hiltViewModel()
): ChannelCoordinator {
    return remember(viewModel) {
        ChannelCoordinator(
            viewModel = viewModel
        )
    }
}
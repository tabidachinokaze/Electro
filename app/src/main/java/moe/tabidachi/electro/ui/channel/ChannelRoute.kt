package moe.tabidachi.electro.ui.channel

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import moe.tabidachi.electro.ui.channel.ChannelContract.Effect
import moe.tabidachi.electro.ui.channel.ChannelContract.Event
import moe.tabidachi.electro.ui.common.MessageAction
import moe.tabidachi.electro.ui.pair.navigateToPair
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data class ChannelRoute(val sid: Long)

context(navController: NavHostController)
fun NavGraphBuilder.channel() = composable<ChannelRoute> {
    val viewModel: ChannelViewModel = hiltViewModel()
    val (state, event) = viewModel.observe {
        when (it) {
            Effect.NavigateUp -> navController.navigateUp()
        }
    }
    ChannelScreen(
        state = state.value,
        event = {
            when (it) {
                is Event.NavigateToChannelAdmin -> navController.navigateToChannelAdmin()
                is Event.NavigateToChannelDetail -> navController.navigateToChannelDetail()
                is Event.NavigateToChannelEdit -> navController.navigateToChannelEdit()
                is Event.NavigateToChannelInvite -> navController.navigateToChannelInvite()
                is Event.NavigateToPair -> navController.navigateToPair(it.uid)
                Event.NavigateUp -> navController.navigateUp()
                else -> event(it)
            }
        },
        messenger = viewModel.messenger,
        messageManager = viewModel.messageManager,
        action = MessageAction(
            navigateToPair = {
                navController.navigateToPair(it)
            }
        )
    )
}

fun NavHostController.navigateToChannel(sid: Long) {
    navigate(ChannelRoute(sid))
}

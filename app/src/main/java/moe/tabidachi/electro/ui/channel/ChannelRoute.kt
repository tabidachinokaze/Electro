package moe.tabidachi.electro.ui.channel

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import moe.tabidachi.electro.ui.channel.ChannelContract.Effect
import moe.tabidachi.electro.ui.channel.ChannelContract.Event
import moe.tabidachi.electro.ui.common.MessageAction
import moe.tabidachi.electro.ui.pair.navigateToPair
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data class ChannelRoute(val sid: Long): NavKey

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

context(backStack: NavBackStack<NavKey>)
fun EntryProviderScope<NavKey>.channel(viewModel: ChannelViewModel) = entry<ChannelRoute> {
    val (state, event) = viewModel.observe {
        when (it) {
            Effect.NavigateUp -> backStack.removeLastOrNull()
        }
    }
    ChannelScreen(
        state = state.value,
        event = {
            when (it) {
                is Event.NavigateToChannelAdmin -> backStack.navigateToChannelAdmin()
                is Event.NavigateToChannelDetail -> backStack.navigateToChannelDetail()
                is Event.NavigateToChannelEdit -> backStack.navigateToChannelEdit()
                is Event.NavigateToChannelInvite -> backStack.navigateToChannelInvite()
                is Event.NavigateToPair -> backStack.navigateToPair(it.uid)
                Event.NavigateUp -> backStack.removeLastOrNull()
                else -> event(it)
            }
        },
        messenger = viewModel.messenger,
        messageManager = viewModel.messageManager,
        action = MessageAction(
            navigateToPair = {
                backStack.navigateToPair(it)
            }
        )
    )
}

fun NavBackStack<NavKey>.navigateToChannel(sid: Long) {
    add(ChannelRoute(sid))
}

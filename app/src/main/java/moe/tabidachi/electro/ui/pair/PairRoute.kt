package moe.tabidachi.electro.ui.pair

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe
import moe.tabidachi.electro.ui.common.MessageAction

@Serializable
data class PairRoute(val target: Long) : NavKey

context(navController: NavHostController)
fun NavGraphBuilder.pair() = composable<PairRoute> {
    val viewModel: PairViewModel = hiltViewModel()
    val (state, event) = viewModel.observe { }
    PairScreen(
        state = state.value,
        event = {
            when (it) {
                PairContract.Event.NavigateUp -> navController.navigateUp()
                else -> event(it)
            }
        },
        messenger = viewModel.messenger,
        messageManager = viewModel.messageManager,
        action = MessageAction(
            navigateToPair = navController::navigateToPair
        )
    )
}

fun NavHostController.navigateToPair(target: Long) {
    navigate(PairRoute(target))
}

context(backStack: NavBackStack<NavKey>)
fun EntryProviderScope<NavKey>.pair() = entry<PairRoute> {
    val viewModel: PairViewModel = hiltViewModel<PairViewModel, PairContract.Factory>(
        creationCallback = { factory ->
            factory.create(it)
        }
    )
    val (state, event) = viewModel.observe { }
    PairScreen(
        state = state.value,
        event = {
            when (it) {
                PairContract.Event.NavigateUp -> backStack.removeLastOrNull()
                else -> event(it)
            }
        },
        messenger = viewModel.messenger,
        messageManager = viewModel.messageManager,
        action = MessageAction(
            navigateToPair = backStack::navigateToPair
        )
    )
}

fun NavBackStack<NavKey>.navigateToPair(target: Long) {
    add(PairRoute(target))
}
package cn.tabidachi.electro.ui.pair

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import cn.tabidachi.electro.ui.common.MessageAction
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data class PairRoute(val target: Long)

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
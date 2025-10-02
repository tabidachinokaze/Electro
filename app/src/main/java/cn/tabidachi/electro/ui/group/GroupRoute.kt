package cn.tabidachi.electro.ui.group

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import cn.tabidachi.electro.ui.common.MessageAction
import cn.tabidachi.electro.ui.group.GroupContract.Event
import cn.tabidachi.electro.ui.pair.navigateToPair
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data class GroupRoute(val sid: Long)

context(navController: NavHostController)
fun NavGraphBuilder.group() = composable<GroupRoute> {
    val viewModel: GroupViewModel = hiltViewModel()
    val (state, event) = viewModel.observe {
        when (it) {
            GroupContract.Effect.NavigateUp -> navController.navigateUp()
        }
    }

    GroupScreen(
        state = state.value,
        event = {
            when (it) {
                is Event.NavigateToChannelAdmin -> navController.navigateToGroupAdmin()
                is Event.NavigateToChannelDetail -> navController.navigateToGroupDetail()
                is Event.NavigateToChannelEdit -> navController.navigateToGroupEdit()
                is Event.NavigateToChannelInvite -> navController.navigateToGroupInvite()
                is Event.NavigateToPair -> navController.navigateToPair(it.uid)
                Event.NavigateUp -> navController.navigateUp()
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

fun NavHostController.navigateToGroup(sid: Long) {
    navigate(GroupRoute(sid))
}

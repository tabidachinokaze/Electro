package moe.tabidachi.electro.ui.server

import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import moe.tabidachi.electro.ui.server.ServerContract.Action
import moe.tabidachi.electro.ui.server.ServerContract.Event
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data object ServerRoute

context(navController: NavHostController)
fun NavGraphBuilder.server() = composable<ServerRoute> {
    val viewModel: ServerViewModel = hiltViewModel()
    val (state, event) = viewModel.observe { }
    ServerScreen(
        state = state.value,
        action = remember {
            Action(
                showDialog = { event(Event.ShowDialog(it)) },
                hideDialog = { event(Event.HideDialog) },
                onSave = { event(Event.OnSave) },
                onDialogValueChange = { event(Event.OnDialogValueChange(it)) },
                onNavigateUp = navController::navigateUp
            )
        }
    )
}

fun NavHostController.navigateToServer() {
    navigate(ServerRoute)
}
package cn.tabidachi.electro.ui.server

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import cn.tabidachi.electro.ui.ElectroNavigationActions

class ServerCoordinator(
    val viewModel: ServerViewModel, val navigationActions: ElectroNavigationActions
) {
    val state = viewModel.state

    fun doStuff() {
        // TODO Handle UI Action
    }

    fun showDialog(type: ServerDialogType) = viewModel.showDialog(type)

    fun hideDialog() = viewModel.hideDialog()

    fun onSave() = viewModel.onSave()

    fun onDialogValueChange(value: String) = viewModel.onDialogValueChange(value)

    fun onNavigateUp() = navigationActions.navigateUp()
}

@Composable
fun rememberServerCoordinator(
    viewModel: ServerViewModel = hiltViewModel(),
    navigationActions: ElectroNavigationActions = ElectroNavigationActions(rememberNavController())
): ServerCoordinator {
    return remember(viewModel, navigationActions) {
        ServerCoordinator(
            viewModel = viewModel, navigationActions = navigationActions
        )
    }
}
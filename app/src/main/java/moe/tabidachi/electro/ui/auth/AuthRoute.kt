package moe.tabidachi.electro.ui.auth

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import moe.tabidachi.electro.ext.navigateToLocaleSettings
import moe.tabidachi.electro.ui.server.navigateToServer
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data object AuthRoute

context(navController: NavHostController)
fun NavGraphBuilder.auth() = composable<AuthRoute> {
    val context = LocalContext.current
    val viewModel: AuthViewModel = hiltViewModel()
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val (state, event) = viewModel.observe {
        when (it) {
            is AuthContract.Effect.Toast -> scope.launch {
                hostState.showSnackbar(message = it.value, withDismissAction = true)
            }
        }
    }
    AuthScreen(
        state = state.value,
        event = {
            when (it) {
                is AuthContract.Event.NavigateToServer -> navController.navigateToServer()
                is AuthContract.Event.NavigateToLocaleSettings -> context.navigateToLocaleSettings()

                else -> event(it)
            }
        },
        hostState = hostState
    )
}

fun NavHostController.navigateToAuth() {
    navigate(AuthRoute)
}

package moe.tabidachi.electro.ui.auth

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe
import moe.tabidachi.electro.ext.navigateToLocaleSettings
import moe.tabidachi.electro.ui.server.navigateToServer

@Serializable
data object AuthRoute : NavKey

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

context(backStack: NavBackStack<NavKey>)
fun EntryProviderScope<NavKey>.auth() = entry<AuthRoute> {
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
                is AuthContract.Event.NavigateToServer -> backStack.navigateToServer()
                is AuthContract.Event.NavigateToLocaleSettings -> context.navigateToLocaleSettings()

                else -> event(it)
            }
        },
        hostState = hostState
    )
}

fun NavBackStack<NavKey>.navigateToAuth() {
    add(AuthRoute)
}

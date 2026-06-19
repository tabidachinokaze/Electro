package moe.tabidachi.electro.ui.profile

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe
import moe.tabidachi.electro.ui.profile.ProfileContract.Event

@Serializable
data object ProfileRoute : NavKey

context(navController: NavHostController)
fun NavGraphBuilder.profile() = composable<ProfileRoute> {
    val viewModel: ProfileViewModel = hiltViewModel()
    val (state, event) = viewModel.observe {
        when (it) {
            ProfileContract.Effect.NavigateUp -> navController.navigateUp()
        }
    }
    ProfileScreen(
        state = state.value,
        event = {
            when (it) {
                Event.NavigateUp -> navController.navigateUp()
                else -> event(it)
            }
        }
    )
}

fun NavHostController.navigateToProfile() {
    navigate(ProfileRoute)
}

context(backStack: NavBackStack<NavKey>)
fun EntryProviderScope<NavKey>.profile() = entry<ProfileRoute> {
    val viewModel: ProfileViewModel = hiltViewModel()
    val (state, event) = viewModel.observe {
        when (it) {
            ProfileContract.Effect.NavigateUp -> backStack.removeLastOrNull()
        }
    }
    ProfileScreen(
        state = state.value,
        event = {
            when (it) {
                Event.NavigateUp -> backStack.removeLastOrNull()
                else -> event(it)
            }
        }
    )
}

fun NavBackStack<NavKey>.navigateToProfile() {
    add(ProfileRoute)
}


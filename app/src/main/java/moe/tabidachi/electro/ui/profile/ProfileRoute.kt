package moe.tabidachi.electro.ui.profile

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import moe.tabidachi.electro.ui.profile.ProfileContract.Event
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data object ProfileRoute

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

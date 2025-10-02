package moe.tabidachi.electro.ui.contact

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import moe.tabidachi.electro.ui.pair.navigateToPair
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data object ContactRoute

context(navController: NavHostController)
fun NavGraphBuilder.contact() = composable<ContactRoute> {
    val viewModel: ContactViewModel = hiltViewModel()
    val (state, event) = viewModel.observe { }
    ContactScreen(
        state = state.value,
        event = {
            when (it) {
                is ContactContract.Event.NavigateToPair -> navController.navigateToPair(it.uid)
                ContactContract.Event.NavigateUp -> navController.navigateUp()
                else -> event(it)
            }
        },
        messenger = viewModel.messenger
    )
}

fun NavHostController.navigateToContact() {
    navigate(ContactRoute)
}

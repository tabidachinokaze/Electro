package moe.tabidachi.electro.ui.contact

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe
import moe.tabidachi.electro.ui.pair.navigateToPair

@Serializable
data object ContactRoute : NavKey

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

context(backStack: NavBackStack<NavKey>)
fun EntryProviderScope<NavKey>.contact() = entry<ContactRoute> {
    val viewModel: ContactViewModel = hiltViewModel()
    val (state, event) = viewModel.observe { }
    ContactScreen(
        state = state.value,
        event = {
            when (it) {
                is ContactContract.Event.NavigateToPair -> backStack.navigateToPair(it.uid)
                ContactContract.Event.NavigateUp -> backStack.removeLastOrNull()
                else -> event(it)
            }
        },
        messenger = viewModel.messenger
    )
}

fun NavBackStack<NavKey>.navigateToContact() {
    add(ContactRoute)
}

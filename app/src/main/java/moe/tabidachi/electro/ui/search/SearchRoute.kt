package moe.tabidachi.electro.ui.search

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import moe.tabidachi.electro.ui.group.navigateToGroup
import moe.tabidachi.electro.ui.pair.navigateToPair
import moe.tabidachi.electro.ui.search.SearchContract.Event
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data object SearchRoute : NavKey

context(navController: NavHostController)
fun NavGraphBuilder.search() = composable<SearchRoute> {
    val viewModel: SearchViewModel = hiltViewModel()
    val (state, event) = viewModel.observe { }
    SearchScreen(
        state = state.value,
        event = {
            when (it) {
                is Event.NavigateToGroup -> navController.navigateToGroup(it.sid)
                is Event.NavigateToPair -> navController.navigateToPair(it.target)
                Event.NavigateUp -> navController.navigateUp()
                else -> event(it)
            }
        }
    )
}

fun NavHostController.navigateToSearch() {
    navigate(SearchRoute)
}

context(backStack: NavBackStack<NavKey>)
fun EntryProviderScope<NavKey>.search() = entry<SearchRoute> {
    val viewModel: SearchViewModel = hiltViewModel()
    val (state, event) = viewModel.observe { }
    SearchScreen(
        state = state.value,
        event = {
            when (it) {
                is Event.NavigateToGroup -> backStack.navigateToGroup(it.sid)
                is Event.NavigateToPair -> backStack.navigateToPair(it.target)
                Event.NavigateUp -> backStack.removeLastOrNull()
                else -> event(it)
            }
        }
    )
}

fun NavBackStack<NavKey>.navigateToSearch() {
    add(SearchRoute)
}

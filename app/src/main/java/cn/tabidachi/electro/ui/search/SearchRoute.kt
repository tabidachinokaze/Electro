package cn.tabidachi.electro.ui.search

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import cn.tabidachi.electro.ui.group.navigateToGroup
import cn.tabidachi.electro.ui.pair.navigateToPair
import cn.tabidachi.electro.ui.search.SearchContract.Event
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data object SearchRoute

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

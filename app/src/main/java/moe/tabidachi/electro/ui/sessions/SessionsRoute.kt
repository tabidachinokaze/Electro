package moe.tabidachi.electro.ui.sessions

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import moe.tabidachi.electro.ui.auth.navigateToAuth
import moe.tabidachi.electro.ui.channel.navigateToChannelCreate
import moe.tabidachi.electro.ui.channel.navigateToChannel
import moe.tabidachi.electro.ui.contact.navigateToContact
import moe.tabidachi.electro.ui.group.navigateToGroup
import moe.tabidachi.electro.ui.group.navigateToGroupCreate
import moe.tabidachi.electro.ui.pair.navigateToPair
import moe.tabidachi.electro.ui.search.navigateToSearch
import moe.tabidachi.electro.ui.settings.navigateToSettings
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data object SessionsRoute : NavKey

context(navController: NavHostController)
fun NavGraphBuilder.sessions() = composable<SessionsRoute> {
    val viewModel: SessionsViewModel = hiltViewModel()
    val (state, event) = viewModel.observe { }
    SessionsScreen(
        state = state.value,
        event = {
            when (it) {
                SessionsContract.Event.NavigateToAuth -> navController.navigateToAuth()
                is SessionsContract.Event.NavigateToChannel -> navController.navigateToChannel(it.sid)
                SessionsContract.Event.NavigateToContact -> navController.navigateToContact()
                SessionsContract.Event.NavigateToChannelCreate -> navController.navigateToChannelCreate()
                SessionsContract.Event.NavigateToGroupCreate -> navController.navigateToGroupCreate()
                SessionsContract.Event.NavigateToDialogs -> navController.navigateToSessions()
                is SessionsContract.Event.NavigateToGroup -> navController.navigateToGroup(it.sid)
                is SessionsContract.Event.NavigateToPair -> navController.navigateToPair(it.uid)
                SessionsContract.Event.NavigateToSearch -> navController.navigateToSearch()
                SessionsContract.Event.NavigateToSettings -> navController.navigateToSettings()
                SessionsContract.Event.NavigateUp -> navController.navigateUp()
                else -> event(it)
            }
        }
    )
}

fun NavHostController.navigateToSessions() {
    navigate(SessionsRoute)
}

context(backStack: NavBackStack<NavKey>)
fun EntryProviderScope<NavKey>.sessions() = entry<SessionsRoute> {
    val viewModel: SessionsViewModel = hiltViewModel()
    val (state, event) = viewModel.observe { }
    SessionsScreen(
        state = state.value,
        event = {
            when (it) {
                SessionsContract.Event.NavigateToAuth -> backStack.navigateToAuth()
                is SessionsContract.Event.NavigateToChannel -> backStack.navigateToChannel(it.sid)
                SessionsContract.Event.NavigateToContact -> backStack.navigateToContact()
                SessionsContract.Event.NavigateToChannelCreate -> backStack.navigateToChannelCreate()
                SessionsContract.Event.NavigateToGroupCreate -> backStack.navigateToGroupCreate()
                SessionsContract.Event.NavigateToDialogs -> backStack.navigateToSessions()
                is SessionsContract.Event.NavigateToGroup -> backStack.navigateToGroup(it.sid)
                is SessionsContract.Event.NavigateToPair -> backStack.navigateToPair(it.uid)
                SessionsContract.Event.NavigateToSearch -> backStack.navigateToSearch()
                SessionsContract.Event.NavigateToSettings -> backStack.navigateToSettings()
                SessionsContract.Event.NavigateUp -> backStack.removeLastOrNull()
                else -> event(it)
            }
        }
    )
}

fun NavBackStack<NavKey>.navigateToSessions() {
    add(SessionsRoute)
}


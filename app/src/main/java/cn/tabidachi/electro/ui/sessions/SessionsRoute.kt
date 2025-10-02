package cn.tabidachi.electro.ui.sessions

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import cn.tabidachi.electro.ui.auth.navigateToAuth
import cn.tabidachi.electro.ui.channel.navigateToChannelCreate
import cn.tabidachi.electro.ui.channel.navigateToChannel
import cn.tabidachi.electro.ui.contact.navigateToContact
import cn.tabidachi.electro.ui.group.navigateToGroup
import cn.tabidachi.electro.ui.group.navigateToGroupCreate
import cn.tabidachi.electro.ui.pair.navigateToPair
import cn.tabidachi.electro.ui.search.navigateToSearch
import cn.tabidachi.electro.ui.settings.navigateToSettings
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data object SessionsRoute

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

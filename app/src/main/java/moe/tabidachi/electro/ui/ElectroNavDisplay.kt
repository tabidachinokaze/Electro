package moe.tabidachi.electro.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import moe.tabidachi.electro.ui.auth.auth
import moe.tabidachi.electro.ui.channel.ChannelContract
import moe.tabidachi.electro.ui.channel.ChannelRoute
import moe.tabidachi.electro.ui.channel.ChannelViewModel
import moe.tabidachi.electro.ui.channel.channel
import moe.tabidachi.electro.ui.channel.channelAdmin
import moe.tabidachi.electro.ui.channel.channelCreate
import moe.tabidachi.electro.ui.channel.channelDetail
import moe.tabidachi.electro.ui.channel.channelEdit
import moe.tabidachi.electro.ui.channel.channelInvite
import moe.tabidachi.electro.ui.contact.contact
import moe.tabidachi.electro.ui.group.GroupContract
import moe.tabidachi.electro.ui.group.GroupRoute
import moe.tabidachi.electro.ui.group.GroupViewModel
import moe.tabidachi.electro.ui.group.group
import moe.tabidachi.electro.ui.group.groupAdmin
import moe.tabidachi.electro.ui.group.groupCreate
import moe.tabidachi.electro.ui.group.groupDetail
import moe.tabidachi.electro.ui.group.groupEdit
import moe.tabidachi.electro.ui.group.groupInvite
import moe.tabidachi.electro.ui.pair.pair
import moe.tabidachi.electro.ui.profile.profile
import moe.tabidachi.electro.ui.search.search
import moe.tabidachi.electro.ui.server.server
import moe.tabidachi.electro.ui.sessions.sessions
import moe.tabidachi.electro.ui.settings.settings
import moe.tabidachi.electro.ui.splash.splash

@Composable
fun ElectroNavDisplay(
    modifier: Modifier = Modifier,
    startDestination: NavKey,
    backStack: NavBackStack<NavKey> = rememberNavBackStack(startDestination)
) = with(backStack) {
    NavDisplay(
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        }, entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            splash()
            auth()
            settings()
            profile()
            contact()
            sessions()
            pair()
            search()
            groupCreate()
            GroupNavDisplay()
            channelCreate()
            ChannelNavDisplay()
            server()
        }, modifier = modifier
    )
}

@Composable
context(parentBackStack: NavBackStack<NavKey>)
fun EntryProviderScope<NavKey>.GroupNavDisplay() = entry<GroupRoute> {
    val backStack = rememberNavBackStack(it)
    val viewModel: GroupViewModel = hiltViewModel<GroupViewModel, GroupContract.Factory>(
        creationCallback = { factory ->
            factory.create(navKey = it)
        }
    )
    NavDisplay(
        backStack = backStack,
        onBack = {
            //backStack.removeLastOrNull()
        },
        entryProvider = entryProvider {
            with(backStack) {
                group(
                    viewModel = viewModel
                )
                groupAdmin(
                    viewModel = viewModel,
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
                groupDetail(
                    viewModel = viewModel,
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
                groupEdit(
                    viewModel = viewModel,
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
                groupInvite(
                    viewModel = viewModel,
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

@Composable
context(parentBackStack: NavBackStack<NavKey>)
fun EntryProviderScope<NavKey>.ChannelNavDisplay() = entry<ChannelRoute> {
    val backStack = rememberNavBackStack(it)
    val viewModel: ChannelViewModel = hiltViewModel<ChannelViewModel, ChannelContract.Factory>(
        creationCallback = { factory ->
            factory.create(it)
        }
    )
    NavDisplay(
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        entryProvider = entryProvider {
            with(backStack) {
                channel(
                    viewModel = viewModel
                )
                channelAdmin(
                    viewModel = viewModel,
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
                channelDetail(
                    viewModel = viewModel,
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
                channelEdit(
                    viewModel = viewModel,
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
                channelInvite(
                    viewModel = viewModel,
                    onNavigateUp = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

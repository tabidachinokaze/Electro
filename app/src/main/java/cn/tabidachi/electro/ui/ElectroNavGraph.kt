package cn.tabidachi.electro.ui

import android.annotation.SuppressLint
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cn.tabidachi.electro.ElectroViewModel
import cn.tabidachi.electro.ui.auth.AuthScreen
import cn.tabidachi.electro.ui.channel.ChannelAdminScreen
import cn.tabidachi.electro.ui.channel.ChannelCreateScreen
import cn.tabidachi.electro.ui.channel.ChannelDetailScreen
import cn.tabidachi.electro.ui.channel.ChannelEditScreen
import cn.tabidachi.electro.ui.channel.ChannelInviteScreen
import cn.tabidachi.electro.ui.channel.ChannelScreen
import cn.tabidachi.electro.ui.chatgpt.ChatGPTScreen
import cn.tabidachi.electro.ui.contact.ContactScreen
import cn.tabidachi.electro.ui.group.CreateGroupScreen
import cn.tabidachi.electro.ui.group.CroupEditScreen
import cn.tabidachi.electro.ui.group.GroupAdminScreen
import cn.tabidachi.electro.ui.group.GroupDetailScreen
import cn.tabidachi.electro.ui.group.GroupScreen
import cn.tabidachi.electro.ui.group.InviteScreen
import cn.tabidachi.electro.ui.map.DragDropSelectPointScreen
import cn.tabidachi.electro.ui.pair.PairScreen
import cn.tabidachi.electro.ui.profile.ProfileScreen
import cn.tabidachi.electro.ui.search.SearchScreen
import cn.tabidachi.electro.ui.sessions.SessionsScreen
import cn.tabidachi.electro.ui.settings.SettingsScreen

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun ElectroNavGraph(
    modifier: Modifier = Modifier,
    navHostController: NavHostController = rememberNavController(),
    startDestination: String = ElectroDestinations.SPLASH_ROUTE,
    navigationActions: ElectroNavigationActions = remember(navHostController) {
        ElectroNavigationActions(navHostController)
    }
) {
    val electroViewModel: ElectroViewModel = hiltViewModel()
    val viewState by electroViewModel.viewState.collectAsState()
    NavHost(
        navController = navHostController,
        startDestination = viewState.startDestination,
//        startDestination = "map",
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(220, delayMillis = 90)
                    )
        }, exitTransition = {
            fadeOut(animationSpec = tween(90))
        }, popEnterTransition = {
            fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(220, delayMillis = 90)
                    )
        }, popExitTransition = {
            fadeOut(animationSpec = tween(90))
        }
    ) {
        composable("chatgpt") {
            ChatGPTScreen(navHostController = navHostController)
        }
        composable("map") {
            DragDropSelectPointScreen()
        }
        composable(route = ElectroDestinations.SPLASH_ROUTE) {

        }
        composable(route = ElectroDestinations.AUTH_ROUTE) {
            AuthScreen(
                navigationActions = navigationActions,
                navHostController = navHostController
            )
        }
        composable(route = ElectroDestinations.SETTINGS_ROUTE) {
            SettingsScreen(navHostController, navigationActions)
        }
        composable(ElectroDestinations.PROFILE_ROUTE) {
            ProfileScreen(navHostController)
        }
        composable(ElectroDestinations.CONTACT_ROUTE) {
            ContactScreen(navHostController = navHostController, navigationActions)
        }

        composable(ElectroDestinations.DIALOGS_ROUTE) {
            SessionsScreen(navigationActions = navigationActions)
        }
        composable(
            ElectroDestinations.PAIR_ROUTE,
            arguments = listOf(
                navArgument(ElectroDestinationArgs.UID_ARG) {
                    type = NavType.LongType
                },
            )
        ) { entry ->
            val uid = entry.arguments?.getLong(ElectroDestinationArgs.UID_ARG)
            PairScreen(uid!!, navigationActions, navHostController)
        }
        composable(ElectroDestinations.CREATE_GROUP_ROUTE) {
            CreateGroupScreen(navigationActions, navHostController)
        }
        composable(ElectroDestinations.SEARCH_ROUTE) {
            SearchScreen(navigationActions)
        }
        composable(
            ElectroDestinations.GROUP_ROUTE,
            arguments = listOf(
                navArgument(ElectroDestinationArgs.SID_ARG) {
                    type = NavType.LongType
                },
            )
        ) { entry ->
            val sid = entry.arguments?.getLong(ElectroDestinationArgs.SID_ARG)
            sid?.let {
                GroupScreen(sid = sid, navigationActions = navigationActions)
            } ?: navHostController.navigateUp()
        }
        composable(
            ElectroDestinations.GROUP_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(ElectroDestinationArgs.SID_ARG) {
                    type = NavType.LongType
                },
            )
        ) { entry ->
            val sid = entry.arguments?.getLong(ElectroDestinationArgs.SID_ARG)
            val backStackEntry = remember {
                navHostController.getBackStackEntry(ElectroDestinations.GROUP_ROUTE)
            }
            sid?.let {
                GroupDetailScreen(
                    sid = sid,
                    navigationActions = navigationActions,
                    hiltViewModel(backStackEntry)
                )
            } ?: navHostController.navigateUp()
        }
        composable(
            ElectroDestinations.GROUP_EDIT_ROUTE,
            arguments = listOf(
                navArgument(ElectroDestinationArgs.SID_ARG) {
                    type = NavType.LongType
                },
            )
        ) { entry ->
            val sid = entry.arguments?.getLong(ElectroDestinationArgs.SID_ARG)
            val backStackEntry = remember {
                navHostController.getBackStackEntry(ElectroDestinations.GROUP_ROUTE)
            }
            sid?.let {
                CroupEditScreen(
                    sid = sid,
                    navigationActions = navigationActions,
                    hiltViewModel(backStackEntry)
                )
            } ?: navHostController.navigateUp()
        }
        composable(
            ElectroDestinations.INVITE_ROUTE,
            arguments = listOf(
                navArgument(ElectroDestinationArgs.SID_ARG) {
                    type = NavType.LongType
                },
            )
        ) {
            val backStackEntry = remember {
                navHostController.getBackStackEntry(ElectroDestinations.GROUP_ROUTE)
            }
            it.arguments?.getLong(ElectroDestinationArgs.SID_ARG)?.let {
                InviteScreen(sid = it, navigationActions = navigationActions, hiltViewModel(backStackEntry))
            } ?: navHostController.navigateUp()
        }
        composable(
            ElectroDestinations.GROUP_ADMIN_ROUTE,
            arguments = listOf(
                navArgument(ElectroDestinationArgs.SID_ARG) {
                    type = NavType.LongType
                },
            )
        ) { entry ->
            val sid = entry.arguments?.getLong(ElectroDestinationArgs.SID_ARG)
            val backStackEntry = remember {
                navHostController.getBackStackEntry(ElectroDestinations.GROUP_ROUTE)
            }
            sid?.let {
                GroupAdminScreen(
                    sid = sid,
                    navigationActions = navigationActions,
                    hiltViewModel(backStackEntry)
                )
            } ?: navHostController.navigateUp()
        }
        composable(ElectroDestinations.CHANNEL_CREATE_ROUTE) {
            ChannelCreateScreen(
                navigationActions = navigationActions,
                navHostController = navHostController
            )
        }
        composable(
            ElectroDestinations.CHANNEL_ROUTE,
            arguments = listOf(
                navArgument(ElectroDestinationArgs.SID_ARG) {
                    type = NavType.LongType
                },
            )
        ) { entry ->
            val sid = entry.arguments?.getLong(ElectroDestinationArgs.SID_ARG)
            sid?.let {
                ChannelScreen(sid = sid, navigationActions = navigationActions)
            } ?: navHostController.navigateUp()
        }
        composable(
            ElectroDestinations.CHANNEL_ADMIN_ROUTE,
            arguments = listOf(
                navArgument(ElectroDestinationArgs.SID_ARG) {
                    type = NavType.LongType
                },
            )
        ) { entry ->
            val sid = entry.arguments?.getLong(ElectroDestinationArgs.SID_ARG)
            val backStackEntry = remember {
                navHostController.getBackStackEntry(ElectroDestinations.CHANNEL_ROUTE)
            }
            sid?.let {
                ChannelAdminScreen(
                    sid = sid,
                    navigationActions = navigationActions,
                    hiltViewModel(backStackEntry)
                )
            } ?: navHostController.navigateUp()
        }
        composable(
            ElectroDestinations.CHANNEL_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(ElectroDestinationArgs.SID_ARG) {
                    type = NavType.LongType
                },
            )
        ) { entry ->
            val sid = entry.arguments?.getLong(ElectroDestinationArgs.SID_ARG)
            val backStackEntry = remember {
                navHostController.getBackStackEntry(ElectroDestinations.CHANNEL_ROUTE)
            }
            sid?.let {
                ChannelDetailScreen(
                    sid = sid,
                    navigationActions = navigationActions,
                    hiltViewModel(backStackEntry)
                )
            } ?: navHostController.navigateUp()
        }
        composable(
            ElectroDestinations.CHANNEL_INVITE_ROUTE,
            arguments = listOf(
                navArgument(ElectroDestinationArgs.SID_ARG) {
                    type = NavType.LongType
                },
            )
        ) {
            val backStackEntry = remember {
                navHostController.getBackStackEntry(ElectroDestinations.CHANNEL_ROUTE)
            }
            it.arguments?.getLong(ElectroDestinationArgs.SID_ARG)?.let {
                ChannelInviteScreen(sid = it, navigationActions = navigationActions, hiltViewModel(backStackEntry))
            } ?: navHostController.navigateUp()
        }
        composable(
            ElectroDestinations.CHANNEL_EDIT_ROUTE,
            arguments = listOf(
                navArgument(ElectroDestinationArgs.SID_ARG) {
                    type = NavType.LongType
                },
            )
        ) { entry ->
            val sid = entry.arguments?.getLong(ElectroDestinationArgs.SID_ARG)
            val backStackEntry = remember {
                navHostController.getBackStackEntry(ElectroDestinations.CHANNEL_ROUTE)
            }
            sid?.let {
                ChannelEditScreen(
                    sid = sid,
                    navigationActions = navigationActions,
                    hiltViewModel(backStackEntry)
                )
            } ?: navHostController.navigateUp()
        }
    }
}
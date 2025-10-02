package cn.tabidachi.electro.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import cn.tabidachi.electro.ElectroViewModel
import cn.tabidachi.electro.ui.auth.auth
import cn.tabidachi.electro.ui.channel.channelAdmin
import cn.tabidachi.electro.ui.channel.channel
import cn.tabidachi.electro.ui.channel.channelDetail
import cn.tabidachi.electro.ui.channel.channelEdit
import cn.tabidachi.electro.ui.channel.channelInvite
import cn.tabidachi.electro.ui.channel.channelCreate
import cn.tabidachi.electro.ui.contact.contact
import cn.tabidachi.electro.ui.group.group
import cn.tabidachi.electro.ui.group.groupAdmin
import cn.tabidachi.electro.ui.group.groupCreate
import cn.tabidachi.electro.ui.group.groupDetail
import cn.tabidachi.electro.ui.group.groupEdit
import cn.tabidachi.electro.ui.group.groupInvite
import cn.tabidachi.electro.ui.pair.pair
import cn.tabidachi.electro.ui.profile.profile
import cn.tabidachi.electro.ui.search.search
import cn.tabidachi.electro.ui.server.server
import cn.tabidachi.electro.ui.sessions.sessions
import cn.tabidachi.electro.ui.settings.settings
import cn.tabidachi.electro.ui.splash.splash

@Composable
fun ElectroNavGraph(
    modifier: Modifier = Modifier,
    navHostController: NavHostController = rememberNavController(),
) = with(navHostController) {
    val electroViewModel: ElectroViewModel = hiltViewModel()
    val viewState by electroViewModel.viewState.collectAsState()
    NavHost(
        navController = navHostController,
        startDestination = viewState.startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(220, delayMillis = 90)
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(90))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(220, delayMillis = 90)
                    )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(90))
        }
    ) {
        splash()
        auth()
        settings()
        profile()
        contact()
        sessions()
        pair()
        search()
        group()
        groupAdmin()
        groupCreate()
        groupDetail()
        groupEdit()
        groupInvite()
        channel()
        channelAdmin()
        channelCreate()
        channelDetail()
        channelEdit()
        channelInvite()
        server()
    }
}

package moe.tabidachi.electro.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import moe.tabidachi.electro.ui.auth.auth
import moe.tabidachi.electro.ui.channel.channel
import moe.tabidachi.electro.ui.channel.channelAdmin
import moe.tabidachi.electro.ui.channel.channelCreate
import moe.tabidachi.electro.ui.channel.channelDetail
import moe.tabidachi.electro.ui.channel.channelEdit
import moe.tabidachi.electro.ui.channel.channelInvite
import moe.tabidachi.electro.ui.contact.contact
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
fun ElectroNavGraph(
    modifier: Modifier = Modifier,
    navHostController: NavHostController = rememberNavController(),
    startDestination: Any
) = with(navHostController) {
    NavHost(
        navController = navHostController,
        startDestination = startDestination,
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

package cn.tabidachi.electro.ui

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import cn.tabidachi.electro.ui.ElectroDestinationArgs.IS_OFFER_ARG
import cn.tabidachi.electro.ui.ElectroDestinationArgs.SID_ARG
import cn.tabidachi.electro.ui.ElectroDestinationArgs.UID_ARG
import cn.tabidachi.electro.ui.ElectroScreens.AUTH_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.CALL_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.CHANNEL_ADMIN_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.CHANNEL_CREATE_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.CHANNEL_DETAIL_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.CHANNEL_EDIT_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.CHANNEL_INVITE_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.CHANNEL_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.CHATGPT_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.DIALOGS_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.CHAT_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.CONTACT_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.CREATE_GROUP_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.GROUP_ADMIN_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.GROUP_DETAIL_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.GROUP_EDIT_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.GROUP_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.INVITE_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.PAIR_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.MESSAGE_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.PROFILE_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.SEARCH_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.SETTINGS_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.SPLASH_SCREEN
import cn.tabidachi.electro.ui.ElectroScreens.VIDEO_CALL_SCREEN

private object ElectroScreens {
    const val AUTH_SCREEN = "auth"
    const val SPLASH_SCREEN = "splash"
    const val MESSAGE_SCREEN = "message"
    const val SETTINGS_SCREEN = "settings"
    const val PROFILE_SCREEN = "profile"
    const val CONTACT_SCREEN = "contact"
    const val CHAT_SCREEN = "chat"
    const val DIALOGS_SCREEN = "chats"
    const val PAIR_SCREEN = "direct"
    const val CREATE_GROUP_SCREEN = "create_group"
    const val CHATGPT_SCREEN = "chatgpt_screen"
    const val SEARCH_SCREEN = "search"
    const val GROUP_SCREEN = "group"
    const val GROUP_DETAIL_SCREEN = "group_detail"
    const val INVITE_SCREEN = "invite"
    const val VIDEO_CALL_SCREEN = "video_call"
    const val CALL_SCREEN = "call"
    const val GROUP_EDIT_SCREEN = "group_edit"
    const val GROUP_ADMIN_SCREEN = "group_admin"
    const val CHANNEL_SCREEN = "channel"
    const val CHANNEL_CREATE_SCREEN = "channel_create"
    const val CHANNEL_ADMIN_SCREEN = "group_admin"
    const val CHANNEL_DETAIL_SCREEN = "channel_detail"
    const val CHANNEL_INVITE_SCREEN = "channel_invite"
    const val CHANNEL_EDIT_SCREEN = "channel_edit"
}

object ElectroDestinationArgs {
    const val SID_ARG = "sid"
    const val UID_ARG = "uid"
    const val IS_OFFER_ARG = "answer_offer"
}

object ElectroDestinations {
    const val GROUP_EDIT_ROUTE = "$GROUP_EDIT_SCREEN?$SID_ARG={$SID_ARG}"
    const val CALL_ROUTE = "$CALL_SCREEN?$UID_ARG={$UID_ARG}?$IS_OFFER_ARG={$IS_OFFER_ARG}"
    const val VIDEO_CALL_ROUTE = "$VIDEO_CALL_SCREEN?$UID_ARG={$UID_ARG}"
    const val INVITE_ROUTE = "$INVITE_SCREEN?$SID_ARG={$SID_ARG}"
    const val GROUP_DETAIL_ROUTE = "$GROUP_DETAIL_SCREEN?$SID_ARG={$SID_ARG}"
    const val GROUP_ROUTE = "$GROUP_SCREEN?$SID_ARG={$SID_ARG}"
    const val CREATE_GROUP_ROUTE = CREATE_GROUP_SCREEN
    const val PAIR_ROUTE = "$PAIR_SCREEN?$UID_ARG={$UID_ARG}"
    const val DIALOGS_ROUTE = DIALOGS_SCREEN
    const val CONTACT_ROUTE = CONTACT_SCREEN
    const val SETTINGS_ROUTE = SETTINGS_SCREEN
    const val MESSAGE_ROUTE = MESSAGE_SCREEN
    const val SPLASH_ROUTE = SPLASH_SCREEN
    const val AUTH_ROUTE = AUTH_SCREEN
    const val PROFILE_ROUTE = PROFILE_SCREEN
    const val CHATGPT_ROUTE = CHATGPT_SCREEN
    const val SEARCH_ROUTE = SEARCH_SCREEN
    const val GROUP_ADMIN_ROUTE = "$GROUP_ADMIN_SCREEN?$SID_ARG={$SID_ARG}"
    const val CHANNEL_ROUTE = "$CHANNEL_SCREEN?$SID_ARG={$SID_ARG}"
    const val CHANNEL_CREATE_ROUTE = CHANNEL_CREATE_SCREEN
    const val CHANNEL_ADMIN_ROUTE = "$CHANNEL_ADMIN_SCREEN?$SID_ARG={$SID_ARG}"
    const val CHANNEL_DETAIL_ROUTE = "$CHANNEL_DETAIL_SCREEN?$SID_ARG={$SID_ARG}"
    const val CHANNEL_INVITE_ROUTE = "$CHANNEL_INVITE_SCREEN?$SID_ARG={$SID_ARG}"
    const val CHANNEL_EDIT_ROUTE = "$CHANNEL_EDIT_SCREEN?$SID_ARG={$SID_ARG}"
}

class ElectroNavigationActions(
    private val navHostController: NavHostController
) {
    fun navigateUp() {
        navHostController.navigateUp()
    }

    fun navigateToAuth() {
        navHostController.navigate(ElectroDestinations.AUTH_ROUTE) {
            popUpTo(navHostController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToSettings() {
        navHostController.navigate(ElectroDestinations.SETTINGS_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToProfile() {
        navHostController.navigate(ElectroDestinations.PROFILE_ROUTE)
    }

    fun navigateToContact() {
        navHostController.navigate(ElectroDestinations.CONTACT_ROUTE)
    }

    fun navigateToDialogs() {
        navHostController.navigate(ElectroDestinations.DIALOGS_ROUTE) {
            popUpTo(navHostController.graph.findStartDestination().id) {
                saveState = true
                inclusive = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToPair(target: Long) {
        navHostController.navigate("$PAIR_SCREEN?$UID_ARG=$target")
    }

    fun navigateToCreateGroup() {
        navHostController.navigate(CREATE_GROUP_SCREEN)
    }

    fun navigateToSearch() {
        navHostController.navigate(SEARCH_SCREEN)
    }

    fun navigateToGroup(sid: Long) {
        navHostController.navigate("$GROUP_SCREEN?$SID_ARG=$sid")
    }

    fun navigateToGroupDetail(sid: Long) {
        navHostController.navigate("$GROUP_DETAIL_SCREEN?$SID_ARG=$sid")
    }

    fun navigateToGroupEdit(sid: Long) {
        navHostController.navigate("$GROUP_EDIT_SCREEN?$SID_ARG=$sid")
    }

    fun navigateToInvite(sid: Long) {
        navHostController.navigate("$INVITE_SCREEN?$SID_ARG=$sid")
    }

    fun navigateToCall(target: Long, isOffer: Boolean) {
        navHostController.navigate("$CALL_SCREEN?$UID_ARG=$target?$IS_OFFER_ARG=$isOffer") {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToChatGPT() {
        navHostController.navigate("chatgpt")
    }

    fun navigateToGroupAdmin(sid: Long) {
        navHostController.navigate("$GROUP_ADMIN_SCREEN?$SID_ARG=$sid")
    }

    fun navigateToChannel(sid: Long) {
        navHostController.navigate("$CHANNEL_SCREEN?$SID_ARG=$sid")
    }

    fun navigateToCreateChannel() {
        navHostController.navigate(CHANNEL_CREATE_SCREEN)
    }

    fun navigateToChannelAdmin(sid: Long) {
        navHostController.navigate("$CHANNEL_ADMIN_SCREEN?$SID_ARG=$sid")
    }

    fun navigateToChannelDetail(sid: Long) {
        navHostController.navigate("$CHANNEL_DETAIL_SCREEN?$SID_ARG=$sid")
    }

    fun navigateToChannelInvite(sid: Long) {
        navHostController.navigate("$CHANNEL_INVITE_SCREEN?$SID_ARG=$sid")
    }

    fun navigateToChannelEdit(sid: Long) {
        navHostController.navigate("$CHANNEL_EDIT_SCREEN?$SID_ARG=$sid")
    }
}
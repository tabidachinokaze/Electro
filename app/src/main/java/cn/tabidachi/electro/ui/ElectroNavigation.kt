package cn.tabidachi.electro.ui

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController


enum class Screen {
    AUTH,
    SPLASH,
    MESSAGE,
    SETTINGS,
    PROFILE,
    CONTACT,
    CHAT,
    DIALOGS,
    PAIR,
    CREATE_GROUP,
    CHATGPT,
    SEARCH,
    GROUP,
    GROUP_DETAIL,
    INVITE,
    VIDEO_CALL,
    CALL,
    GROUP_EDIT,
    GROUP_ADMIN,
    CHANNEL,
    CHANNEL_CREATE,
    CHANNEL_ADMIN,
    CHANNEL_DETAIL,
    CHANNEL_INVITE,
    CHANNEL_EDIT,
    SERVER
}

enum class Args {
    SID,
    UID,
    IS_OFFER,
}

object ElectroDestinations {
    val GROUP_EDIT_ROUTE = "${Screen.GROUP_EDIT}?${Args.SID}={${Args.SID}}"
    val CALL_ROUTE = "${Screen.CALL}?${Args.UID}={${Args.UID}}?${Args.IS_OFFER}={${Args.IS_OFFER}}"
    val VIDEO_CALL_ROUTE = "${Screen.VIDEO_CALL}?${Args.UID}={${Args.UID}}"
    val INVITE_ROUTE = "${Screen.INVITE}?${Args.SID}={${Args.SID}}"
    val GROUP_DETAIL_ROUTE = "${Screen.GROUP_DETAIL}?${Args.SID}={${Args.SID}}"
    val GROUP_ROUTE = "${Screen.GROUP}?${Args.SID}={${Args.SID}}"
    val CREATE_GROUP_ROUTE = "${Screen.CREATE_GROUP}"
    val PAIR_ROUTE = "${Screen.PAIR}?${Args.UID}={${Args.UID}}"
    val DIALOGS_ROUTE = "${Screen.DIALOGS}"
    val CONTACT_ROUTE = "${Screen.CONTACT}"
    val SETTINGS_ROUTE = "${Screen.SETTINGS}"
    val MESSAGE_ROUTE = "${Screen.MESSAGE}"
    val SPLASH_ROUTE = "${Screen.SPLASH}"
    val AUTH_ROUTE = "${Screen.AUTH}"
    val PROFILE_ROUTE = "${Screen.PROFILE}"
    val CHATGPT_ROUTE = "${Screen.CHATGPT}"
    val SEARCH_ROUTE = "${Screen.SEARCH}"
    val GROUP_ADMIN_ROUTE = "${Screen.GROUP_ADMIN}?${Args.SID}={${Args.SID}}"
    val CHANNEL_ROUTE = "${Screen.CHANNEL}?${Args.SID}={${Args.SID}}"
    val CHANNEL_CREATE_ROUTE = "${Screen.CHANNEL_CREATE}"
    val CHANNEL_ADMIN_ROUTE = "${Screen.CHANNEL_ADMIN}?${Args.SID}={${Args.SID}}"
    val CHANNEL_DETAIL_ROUTE = "${Screen.CHANNEL_DETAIL}?${Args.SID}={${Args.SID}}"
    val CHANNEL_INVITE_ROUTE = "${Screen.CHANNEL_INVITE}?${Args.SID}={${Args.SID}}"
    val CHANNEL_EDIT_ROUTE = "${Screen.CHANNEL_EDIT}?${Args.SID}={${Args.SID}}"
    val SERVER_ROUTE = "${Screen.SERVER}"
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
        navHostController.navigate("${Screen.PAIR}?${Args.UID}=$target")
    }

    fun navigateToCreateGroup() {
        navHostController.navigate(Screen.CREATE_GROUP.name)
    }

    fun navigateToSearch() {
        navHostController.navigate(Screen.SEARCH.name)
    }

    fun navigateToGroup(sid: Long) {
        navHostController.navigate("${Screen.GROUP}?${Args.SID}=$sid")
    }

    fun navigateToGroupDetail(sid: Long) {
        navHostController.navigate("${Screen.GROUP_DETAIL}?${Args.SID}=$sid")
    }

    fun navigateToGroupEdit(sid: Long) {
        navHostController.navigate("${Screen.GROUP_EDIT}?${Args.SID}=$sid")
    }

    fun navigateToInvite(sid: Long) {
        navHostController.navigate("${Screen.INVITE}?${Args.SID}=$sid")
    }

    fun navigateToCall(target: Long, isOffer: Boolean) {
        navHostController.navigate("${Screen.CALL}?${Args.UID}=$target?${Args.IS_OFFER}=$isOffer") {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToChatGPT() {
        navHostController.navigate("chatgpt")
    }

    fun navigateToGroupAdmin(sid: Long) {
        navHostController.navigate("${Screen.GROUP_ADMIN}?${Args.SID}=$sid")
    }

    fun navigateToChannel(sid: Long) {
        navHostController.navigate("${Screen.CHANNEL}?${Args.SID}=$sid")
    }

    fun navigateToCreateChannel() {
        navHostController.navigate(Screen.CHANNEL_CREATE.name)
    }

    fun navigateToChannelAdmin(sid: Long) {
        navHostController.navigate("${Screen.CHANNEL_ADMIN}?${Args.SID}=$sid")
    }

    fun navigateToChannelDetail(sid: Long) {
        navHostController.navigate("${Screen.CHANNEL_DETAIL}?${Args.SID}=$sid")
    }

    fun navigateToChannelInvite(sid: Long) {
        navHostController.navigate("${Screen.CHANNEL_INVITE}?${Args.SID}=$sid")
    }

    fun navigateToChannelEdit(sid: Long) {
        navHostController.navigate("${Screen.CHANNEL_EDIT}?${Args.SID}=$sid")
    }

    fun navigateToServer() {
        navHostController.navigate("${Screen.SERVER}")
    }
}
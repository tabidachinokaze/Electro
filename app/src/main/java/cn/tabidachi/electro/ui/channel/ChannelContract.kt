package cn.tabidachi.electro.ui.channel

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import cn.tabidachi.electro.data.database.entity.Dialog
import cn.tabidachi.electro.data.database.entity.Session
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.model.response.ChannelRole
import cn.tabidachi.electro.model.response.GroupRole


/**
 * UI State that represents ChannelScreen
 **/
data class ChannelState(
    val dialog: Dialog? = null,
    val isExit: Boolean = false,
    val canSendMessage: Boolean = false,
    val users: List<User> = emptyList(),
    val roles: List<ChannelRole> = emptyList(),
    val isAdmin: Boolean = false,
    val owner: Long = 0,
    val filter: String = "",
    val contacts: List<User> = emptyList(),
    val reply: Long? = null,
    val image: Bitmap? = null,
    val title: String = "",
    val isTitleError: Boolean = false,
    val description: String = "",
    val processing: Boolean = false,
    val session: Session? = null,
)

/**
 * Channel Actions emitted from the UI Layer
 * passed to the coordinator to handle
 **/
data class ChannelActions(
    val onClick: () -> Unit = {}
)

/**
 * Compose Utility to retrieve actions from nested components
 **/
val LocalChannelActions = staticCompositionLocalOf<ChannelActions> {
    error("{NAME} Actions Were not provided, make sure ProvideChannelActions is called")
}

@Composable
fun ProvideChannelActions(actions: ChannelActions, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalChannelActions provides actions) {
        content.invoke()
    }
}


package cn.tabidachi.electro.ui.common

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Forward
import androidx.compose.material.icons.rounded.Reply
import androidx.compose.ui.graphics.vector.ImageVector
import cn.tabidachi.electro.R

enum class MessageMenu(val icon: ImageVector, @StringRes val text: Int) {
    Reply(icon = Icons.Rounded.Reply, text = R.string.reply),
    Copy(icon = Icons.Rounded.ContentCopy, text = R.string.copy),
    Forward(icon = Icons.Rounded.Forward, text = R.string.forward),
    Edit(icon = Icons.Rounded.Edit, text = R.string.edit),
    Delete(icon = Icons.Rounded.Delete, text = R.string.delete)
}
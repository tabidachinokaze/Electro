package cn.tabidachi.electro.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.ui.graphics.vector.ImageVector

enum class AttachmentType(val icon: ImageVector) {
    Audio(Icons.Rounded.AudioFile),
    Video(Icons.Rounded.VideoFile),
    Image(Icons.Rounded.Image),
    Location(Icons.Rounded.LocationOn),
    File(Icons.Rounded.Add),
}
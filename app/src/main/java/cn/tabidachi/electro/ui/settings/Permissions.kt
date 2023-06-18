package cn.tabidachi.electro.ui.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ext.applicationSettings
import cn.tabidachi.electro.ui.settings.components.SettingsCategory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
fun LazyListScope.Permissions() {
    item {
        val context = LocalContext.current
        SettingsCategory(stringResource(id = R.string.permissions)) {
            Permissions.values().forEach {
                val permissionState =
                    rememberPermissionState(permission = it.permission)
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = {
                        if (!it) {
                            context.applicationSettings()
                        }
                    }
                )
                PermissionItem(
                    permissions = it,
                    modifier = Modifier.clickable {
                        if (!permissionState.status.isGranted) {
                            launcher.launch(it.permission)
                        }
                    }, hasPermission = permissionState.status.isGranted
                )
            }
        }
    }
}

enum class Permissions(
    val icon: ImageVector,
    @StringRes val title: Int,
    @StringRes val description: Int,
    val permission: String
) {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    POST_NOTIFICATIONS(
        icon = Icons.Rounded.Notifications,
        title = R.string.notification,
        description = R.string.notification_description,
        permission = Manifest.permission.POST_NOTIFICATIONS
    ),
    CAMERA(
        icon = Icons.Rounded.Videocam,
        title = R.string.camera_permission,
        description = R.string.camera_description,
        permission = Manifest.permission.CAMERA
    ),
    RECORD_AUDIO(
        icon = Icons.Rounded.Mic,
        title = R.string.microphone,
        description = R.string.microphone_description,
        permission = Manifest.permission.RECORD_AUDIO
    ),
    LOCATION(
        icon = Icons.Rounded.LocationOn,
        title = R.string.location_fine,
        description = R.string.location_descripiton,
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
}

@Composable
fun PermissionItem(
    permissions: Permissions,
    modifier: Modifier = Modifier,
    hasPermission: Boolean
) {
    ListItem(
        headlineContent = {
            Text(text = stringResource(id = permissions.title))
        }, supportingContent = {
            Text(text = stringResource(id = permissions.description))
        }, leadingContent = {
            Icon(
                imageVector = permissions.icon,
                contentDescription = null,
            )
        }, trailingContent = {
            if (hasPermission) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Cancel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }, modifier = modifier
    )
}
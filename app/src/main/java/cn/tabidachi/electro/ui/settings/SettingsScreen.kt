package cn.tabidachi.electro.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ui.ElectroNavigationActions
import cn.tabidachi.electro.ui.common.ImageTopAppBar
import cn.tabidachi.electro.ui.common.SimpleListItem
import coil3.compose.AsyncImage
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.rememberImagePicker
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navHostController: NavHostController,
    navigationActions: ElectroNavigationActions,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()
    val user = viewState.user
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imageCropper = rememberImageCropper()
    val imagePicker = rememberImagePicker(onImage = { uri ->
        scope.launch {
            when (val result = imageCropper.crop(uri, context)) {
                is CropResult.Success -> {
                    viewModel.updateAvatar(result.bitmap.asAndroidBitmap())
                }

                else -> {

                }
            }
        }
    })
    LaunchedEffect(key1 = Unit, block = {
        viewModel.getUser()
    })

    if (imageCropper.cropState != null) {
        ImageCropperDialog(state = imageCropper.cropState!!)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ImageTopAppBar(
                image = {
                    AsyncImage(
                        model = user.avatar,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                },
                title = {
                    SimpleListItem(
                        headlineContent = {
                            Text(text = user.username)
                        }, supportingContent = {
                            if (user.uid != -1L) {
                                Text(text = "UID: ${user.uid}")
                            }
                        }
                    )
                }, navigationIcon = {
                    IconButton(onClick = {
                        navHostController.navigateUp()
                    }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }, actions = {
                    IconButton(onClick = viewModel::onMenuExpand) {
                        Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = viewState.isMenuExpanded,
                        onDismissRequest = viewModel::onMenuDismiss
                    ) {
                        SettingsDropdownMenuItem.values().forEach {
                            DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = it.stringRes))
                                }, onClick = {
                                    viewModel.onMenuDismiss()
                                    when (it) {
                                        SettingsDropdownMenuItem.AVATAR -> {
                                            imagePicker.pick()
                                        }

                                        SettingsDropdownMenuItem.LOGOUT -> {
                                            viewModel.logout()
                                        }

                                        SettingsDropdownMenuItem.PROFILE -> {
                                            navigationActions.navigateToProfile()
                                        }
                                    }
                                }, leadingIcon = {
                                    Icon(imageVector = it.leadingIcon, contentDescription = null)
                                }
                            )
                        }
                    }
                }, scrollBehavior = scrollBehavior
            )
        }
    ) {
        LazyColumn(
            contentPadding = it,
        ) {
            Account(user)
            item {
                Divider()
            }
            Theme(viewModel)
            item {
                Divider()
            }
            Languages(viewModel)
            item {
                Divider()
            }
            Permissions()
            item {
                Divider()
            }
            About()
        }
    }
}

enum class SettingsDropdownMenuItem(
    @StringRes val stringRes: Int,
    val leadingIcon: ImageVector,
) {
    AVATAR(R.string.set_avatar, Icons.Rounded.PhotoCamera),
    PROFILE(R.string.profile_edit, Icons.Rounded.Edit),
    LOGOUT(R.string.logout, Icons.Rounded.Logout),
}
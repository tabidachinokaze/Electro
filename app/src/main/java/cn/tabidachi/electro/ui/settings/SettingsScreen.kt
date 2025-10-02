package cn.tabidachi.electro.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import cn.tabidachi.electro.R
import cn.tabidachi.electro.ext.navigateToLocaleSettings
import cn.tabidachi.electro.ui.common.ImageTopAppBar
import cn.tabidachi.electro.ui.common.SimpleListItem
import cn.tabidachi.electro.ui.preview.PreviewSurface
import cn.tabidachi.electro.ui.preview.Previews
import cn.tabidachi.electro.ui.profile.navigateToProfile
import cn.tabidachi.electro.ui.settings.SettingsContract.Event
import cn.tabidachi.electro.ui.settings.SettingsContract.State
import coil3.compose.AsyncImage
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.rememberImagePicker
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data object SettingsRoute

context(navController: NavHostController)
fun NavGraphBuilder.settings() = composable<SettingsRoute> {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = hiltViewModel()
    val (state, event) = viewModel.observe { }
    SettingsScreen(
        state = state.value,
        event = {
            when (it) {
                Event.NavigateToProfile -> navController.navigateToProfile()
                Event.NavigateUp -> navController.navigateUp()
                Event.NavigateToLocaleSettings -> context.navigateToLocaleSettings()
                else -> event(it)
            }
        }
    )
}

fun NavHostController.navigateToSettings() {
    navigate(SettingsRoute)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: State,
    event: (Event) -> Unit
) {
    val user = state.user
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imageCropper = rememberImageCropper()
    val imagePicker = rememberImagePicker(onImage = { uri ->
        scope.launch {
            when (val result = imageCropper.crop(uri, context)) {
                is CropResult.Success -> {
                    event(Event.UpdateAvatar(result.bitmap))
                }

                else -> {
                }
            }
        }
    })
    LaunchedEffect(Unit) {
        event(Event.GetUser)
    }
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
                        },
                        supportingContent = {
                            if (user.uid != -1L) {
                                Text(text = "UID: ${user.uid}")
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            event(Event.NavigateUp)
                        }
                    ) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { event(Event.OnMenuExpand) }) {
                        Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = state.isMenuExpanded,
                        onDismissRequest = { event(Event.OnMenuDismiss) }
                    ) {
                        SettingsDropdownMenuItem.entries.forEach {
                            DropdownMenuItem(
                                text = {
                                    Text(text = stringResource(id = it.stringRes))
                                },
                                onClick = {
                                    event(Event.OnMenuDismiss)
                                    when (it) {
                                        SettingsDropdownMenuItem.AVATAR -> {
                                            imagePicker.pick()
                                        }

                                        SettingsDropdownMenuItem.LOGOUT -> {
                                            event(Event.Logout)
                                        }

                                        SettingsDropdownMenuItem.PROFILE -> {
                                            event(Event.NavigateToProfile)
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(imageVector = it.leadingIcon, contentDescription = null)
                                }
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        LazyColumn(
            contentPadding = it,
        ) {
            Account(user)
            item {
                HorizontalDivider()
            }
            Theme(state = state, event = event)
            item {
                HorizontalDivider()
            }
            Languages(state = state, event = event)
            item {
                HorizontalDivider()
            }
            Permissions()
            item {
                HorizontalDivider()
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

@Composable
@Previews
private fun SettingsScreenPreview() {
    PreviewSurface {
        SettingsScreen(
            state = State(),
            event = {}
        )
    }
}

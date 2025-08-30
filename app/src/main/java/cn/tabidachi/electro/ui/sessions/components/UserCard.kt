package cn.tabidachi.electro.ui.sessions.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.ModeNight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import cn.tabidachi.electro.PreferenceConstant
import cn.tabidachi.electro.data.database.entity.User
import cn.tabidachi.electro.ext.dataStore
import cn.tabidachi.electro.ui.theme.DarkLight
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun UserCard(
    user: User,
    onAvatarClick: () -> Unit,
    isExpand: Boolean,
    onExpand: () -> Unit
) {
    val context = LocalContext.current
    var darkLight by remember {
        mutableStateOf(DarkLight.SYSTEM)
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        context.dataStore.data.map {
            it[PreferenceConstant.Key.DARK_LIGHT]
        }.filterNotNull().collect {
            darkLight = DarkLight.valueOf(it)
        }
    }
    Surface(tonalElevation = 8.dp) {
        Column(
            modifier = Modifier.statusBarsPadding()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 16.dp)
                        .size(72.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    shape = CircleShape
                ) {
                    AsyncImage(
                        model = user.avatar,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.clickable(onClick = onAvatarClick)
                    )
                }
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                    AnimatedContent(
                        targetState = darkLight, modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp), label = ""
                    ) {
                        val isNight = when (it) {
                            DarkLight.SYSTEM -> isSystemInDarkTheme()
                            DarkLight.DARK -> true
                            DarkLight.LIGHT -> false
                        }
                        IconButton(onClick = {
                            scope.launch {
                                context.dataStore.edit {
                                    it[PreferenceConstant.Key.DARK_LIGHT] =
                                        if (isNight) DarkLight.LIGHT.name else DarkLight.DARK.name
                                }
                            }
                        }) {
                            Icon(
                                imageVector = if (isNight) Icons.Rounded.LightMode else Icons.Rounded.ModeNight,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null,
                        onClick = onExpand
                    )
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        maxLines = 1
                    )
                }
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                    IconButton(onClick = onExpand, modifier = Modifier.padding()) {
                        AnimatedContent(
                            targetState = if (isExpand) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            label = ""
                        ) {
                            Icon(
                                imageVector = it,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}
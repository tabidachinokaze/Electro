package cn.tabidachi.electro.ui.sessions.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import cn.tabidachi.electro.R
import de.charlex.compose.BottomAppBarSpeedDialFloatingActionButton
import de.charlex.compose.FloatingActionButtonItem
import de.charlex.compose.SpeedDialFloatingActionButtonState
import de.charlex.compose.SubSpeedDialFloatingActionButtons

@Composable
fun SessionsFloatingActionButton(
    buttonState: SpeedDialFloatingActionButtonState,
    onFabItemClicked: (SessionsFloatingActionButtonItem) -> Unit,
) {
    ConstraintLayout {
        val (button, subButton) = createRefs()
        Box(
            modifier = Modifier.constrainAs(subButton) {
                bottom.linkTo(button.top, margin = 8.dp)
                end.linkTo(button.end)
            }
        ) {
            SubSpeedDialFloatingActionButtons(
                state = buttonState,
                items = SessionsFloatingActionButtonItem.values().map {
                    FloatingActionButtonItem(
                        icon = it.icon,
                        label = stringResource(id = it.text),
                        onFabItemClicked = {
                            onFabItemClicked(it)
                        }
                    )
                }
            )
        }
        BottomAppBarSpeedDialFloatingActionButton(
            state = buttonState,
            modifier = Modifier
                .constrainAs(button) {}
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
        }
    }
}

enum class SessionsFloatingActionButtonItem(
    val icon: ImageVector,
    @StringRes val text: Int
) {
    CONTACT(Icons.Rounded.PersonAdd, R.string.contact),
    NEW_GROUP(Icons.Rounded.GroupAdd, R.string.create_new_group),
    NEW_CHANNEL(Icons.Rounded.Podcasts, R.string.create_channel)
}
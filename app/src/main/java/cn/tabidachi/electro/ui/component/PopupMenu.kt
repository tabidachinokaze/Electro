package cn.tabidachi.electro.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.popupMenuAnchor(popupState: PopupState) = pointerInteropFilter {
    IntOffset(it.rawX.toInt(), it.rawY.toInt()).let(popupState::positionChange)
    false
}

class PopupState {
    var offset by mutableStateOf(IntOffset.Zero)
        private set
    var position by mutableStateOf(IntOffset.Zero)
        private set
    var visible by mutableStateOf(false)
        private set

    fun show() {
        visible = true
    }

    fun hide() {
        visible = false
    }

    fun offsetChange(intOffset: IntOffset) {
        offset = intOffset
    }

    fun positionChange(intOffset: IntOffset) {
        position = intOffset
    }
}

@Composable
fun rememberPopupState() = remember {
    PopupState()
}

@Composable
fun PopupMenu(
    state: PopupState = rememberPopupState(),
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (state.visible) Popup(
        onDismissRequest = onDismissRequest,
        offset = state.offset,
        content = {
            Surface(
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .layout { measurable: Measurable, constraints: Constraints ->
                        val placeable: Placeable = measurable.measure(constraints)
                        val addition = if (state.position.x - 96 < placeable.width) {
                            placeable.width + 96
                        } else {
                            -96
                        }
                        state.offsetChange(
                            IntOffset(
                                state.position.x - placeable.width + addition,
                                state.position.y - placeable.height / 2
                            )
                        )
                        layout(
                            width = placeable.width,
                            height = placeable.height
                        ) {
                            placeable.placeRelative(0, 0)
                        }
                    }
                    .width(IntrinsicSize.Min)
            ) {
                Column(content = content)
            }
        }
    )
}
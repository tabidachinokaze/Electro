package cn.tabidachi.electro.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.layoutId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMotionApi::class)
@Composable
fun MotionTopAppBar(
    title: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    progress: Float
) {
    MotionLayout(
        modifier = modifier.windowInsetsPadding(windowInsets),
        start = ConstraintSet {
            val navigationIcon = createRefFor(MotionTopAppBarItem.NAVIGATION_ICON)
            val actions = createRefFor(MotionTopAppBarItem.ACTIONS)
            val icon = createRefFor(MotionTopAppBarItem.ICON)
            val title = createRefFor(MotionTopAppBarItem.TITLE)
            constrain(navigationIcon) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }
            constrain(actions) {
                top.linkTo(parent.top)
                end.linkTo(parent.end)
            }
            constrain(icon) {
                width = Dimension.value(48.dp)
                height = Dimension.value(48.dp)
                top.linkTo(parent.top)
                start.linkTo(navigationIcon.end)
            }
            constrain(title) {
                top.linkTo(parent.top)
                start.linkTo(icon.end)
            }
        }, end = ConstraintSet {
            val navigationIcon = createRefFor(MotionTopAppBarItem.NAVIGATION_ICON)
            val actions = createRefFor(MotionTopAppBarItem.ACTIONS)
            val icon = createRefFor(MotionTopAppBarItem.ICON)
            val title = createRefFor(MotionTopAppBarItem.TITLE)
            constrain(navigationIcon) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }
            constrain(actions) {
                top.linkTo(parent.top)
                end.linkTo(parent.end)
            }
            constrain(icon) {
                width = Dimension.fillToConstraints
                height = Dimension.ratio("1:1")
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
            }
            constrain(title) {
                start.linkTo(parent.start)
                bottom.linkTo(parent.bottom)
            }
        }, progress = progress
    ) {
        Box(modifier = Modifier.layoutId(MotionTopAppBarItem.NAVIGATION_ICON)) {
            navigationIcon()
        }
        Row(modifier = Modifier.layoutId(MotionTopAppBarItem.ACTIONS), content = actions)
        Box(modifier = Modifier.layoutId(MotionTopAppBarItem.ICON)) {
            icon()
        }
        Box(modifier = Modifier.layoutId(MotionTopAppBarItem.TITLE)) {
            title()
        }
    }
}

object MotionTopAppBarItem {
    const val NAVIGATION_ICON = "navigation_icon"
    const val ACTIONS = "actions"
    const val TITLE = "title"
    const val ICON = "icon"
    const val CONTENT = "content"
}
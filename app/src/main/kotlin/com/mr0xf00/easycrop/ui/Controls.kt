package com.mr0xf00.easycrop.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.RotateLeft
import androidx.compose.material.icons.rounded.RotateRight
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.mr0xf00.easycrop.AspectRatio
import com.mr0xf00.easycrop.CropShape
import com.mr0xf00.easycrop.CropState
import com.mr0xf00.easycrop.LocalCropperStyle
import com.mr0xf00.easycrop.flipHorizontal
import com.mr0xf00.easycrop.flipVertical
import com.mr0xf00.easycrop.rotLeft
import com.mr0xf00.easycrop.rotRight
import com.mr0xf00.easycrop.utils.eq0
import com.mr0xf00.easycrop.utils.setAspect

private fun Size.isAspect(aspect: AspectRatio): Boolean {
    return ((width / height) - (aspect.x.toFloat() / aspect.y)).eq0()
}

internal val LocalVerticalControls = staticCompositionLocalOf { false }

@Composable
internal fun CropperControls(
    isVertical: Boolean,
    state: CropState,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(key1 = Unit, block = {
        state.region = state.region.setAspect(AspectRatio(1, 1))
        state.aspectLock = true
    })
    CompositionLocalProvider(LocalVerticalControls provides isVertical) {
        ButtonsBar(modifier = modifier) {
            IconButton(onClick = { state.rotLeft() }) {
                Icon(imageVector = Icons.Rounded.RotateLeft, null)
            }
            IconButton(onClick = { state.rotRight() }) {
                Icon(imageVector = Icons.Rounded.RotateRight, null)
            }
            IconButton(onClick = { state.flipHorizontal() }) {
                Icon(imageVector = Icons.Rounded.Flip, null)
            }
            IconButton(onClick = { state.flipVertical() }) {
                Icon(
                    imageVector = Icons.Rounded.Flip,
                    null,
                    modifier = Modifier.rotate(90f)
                )
            }
            /*
            Box {
                var menu by remember { mutableStateOf(false) }
                IconButton(onClick = { menu = !menu }) {
                    Icon(imageVector = Icons.Rounded.Resize, null)
                }
                if (menu) AspectSelectionMenu(
                    onDismiss = { menu = false },
                    region = state.region,
                    onRegion = { state.region = it },
                    lock = state.aspectLock,
                    onLock = { state.aspectLock = it }
                )
            }
            */
            /*
            LocalCropperStyle.current.shapes?.let { shapes ->
                Box {
                    var menu by remember { mutableStateOf(false) }
                    IconButton(onClick = { menu = !menu }) {
                        Icon(Icons.Default.Star, null)
                    }
                    if (menu) ShapeSelectionMenu(
                        onDismiss = { menu = false },
                        selected = state.shape,
                        onSelect = { state.shape = it },
                        shapes = shapes
                    )
                }
            }
            */
        }
    }
}

@Composable
private fun ButtonsBar(
    modifier: Modifier = Modifier,
    buttons: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = .8f),
    ) {
        if (LocalVerticalControls.current) Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            buttons()
        } else Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            buttons()
        }
    }
}


@Composable
private fun ShapeSelectionMenu(
    onDismiss: () -> Unit,
    shapes: List<CropShape>,
    selected: CropShape,
    onSelect: (CropShape) -> Unit,
) {
    OptionsPopup(onDismiss = onDismiss, optionCount = shapes.size) { i ->
        val shape = shapes[i]
        ShapeItem(shape = shape, selected = selected == shape,
            onSelect = { onSelect(shape) })
    }
}


@Composable
private fun ShapeItem(
    shape: CropShape, selected: Boolean, onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = if (!selected) LocalContentColor.current
//        else MaterialTheme.colors.secondaryVariant
        else MaterialTheme.colorScheme.secondaryContainer
    )
    IconButton(
        modifier = modifier,
        onClick = onSelect
    ) {
        val shapeState by rememberUpdatedState(newValue = shape)
        Box(modifier = Modifier
            .size(20.dp)
            .drawWithCache {
                val path = shapeState.asPath(size.toRect())
                val strokeWidth = 2.dp.toPx()
                onDrawWithContent {
                    drawPath(path = path, color = color, style = Stroke(strokeWidth))
                }
            })
    }
}


@Composable
private fun AspectSelectionMenu(
    onDismiss: () -> Unit,
    region: Rect,
    onRegion: (Rect) -> Unit,
    lock: Boolean,
    onLock: (Boolean) -> Unit
) {
    val aspects = LocalCropperStyle.current.aspects
    OptionsPopup(onDismiss = onDismiss, optionCount = 1 + aspects.size) { i ->
        val unselectedTint = LocalContentColor.current
//        val selectedTint = MaterialTheme.colors.secondaryVariant
        val selectedTint = MaterialTheme.colorScheme.secondaryContainer
        if (i == 0) IconButton(onClick = { onLock(!lock) }) {
            Icon(
                Icons.Default.Lock, null,
                tint = if (lock) selectedTint else unselectedTint
            )
        } else {
            val aspect = aspects[i - 1]
            val isSelected = region.size.isAspect(aspect)
            IconButton(onClick = { onRegion(region.setAspect(aspect)) }) {
                Text(
                    "${aspect.x}:${aspect.y}",
                    color = if (isSelected) selectedTint else unselectedTint
                )
            }
        }
    }
}
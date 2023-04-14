package cn.tabidachi.electro.ui.common

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

abstract class BubbleBasedShape(
    val topStart: CornerSize,
    val topEnd: CornerSize,
    val bottomEnd: CornerSize,
    val bottomStart: CornerSize,
    val horizontalPadding: Dp
) : Shape {
    final override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        var topStart = topStart.toPx(size, density)
        var topEnd = topEnd.toPx(size, density)
        var bottomEnd = bottomEnd.toPx(size, density)
        var bottomStart = bottomStart.toPx(size, density)
        val horizontalPadding = with(density) {
            horizontalPadding.toPx()
        }
        val minDimension = size.minDimension
        if (topStart + bottomStart > minDimension) {
            val scale = minDimension / (topStart + bottomStart)
            topStart *= scale
            bottomStart *= scale
        }
        if (topEnd + bottomEnd > minDimension) {
            val scale = minDimension / (topEnd + bottomEnd)
            topEnd *= scale
            bottomEnd *= scale
        }
        require(topStart >= 0.0f && topEnd >= 0.0f && bottomEnd >= 0.0f && bottomStart >= 0.0f) {
            "Corner size in Px can't be negative(topStart = $topStart, topEnd = $topEnd, " +
                    "bottomEnd = $bottomEnd, bottomStart = $bottomStart)!"
        }
        return createOutline(
            size = size,
            topStart = topStart,
            topEnd = topEnd,
            bottomEnd = bottomEnd,
            bottomStart = bottomStart,
            layoutDirection = layoutDirection,
            horizontalPadding = horizontalPadding
        )
    }

    abstract fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection,
        horizontalPadding: Float
    ): Outline
}

enum class BubbleType {
    Incoming, Outgoing
}

class BubbleShape(
    corner: CornerSize,
    private val bubbleType: BubbleType,
    horizontalPadding: Dp
) : BubbleBasedShape(
    topStart = corner,
    topEnd = corner,
    bottomEnd = corner,
    bottomStart = corner,
    horizontalPadding = horizontalPadding
) {
    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection,
        horizontalPadding: Float
    ): Outline = if (topStart + topEnd + bottomStart + bottomEnd == 0.0f) {
        Outline.Rectangle(size.toRect())
    } else Outline.Generic(
        Path().apply {
            val cornerSize = if (layoutDirection == LayoutDirection.Ltr) topStart else topEnd
            val horizontalLeftStart = 0f + horizontalPadding
            val horizontalLeftEnd = cornerSize * 2 + horizontalPadding
            val horizontalRightStart = size.width - cornerSize * 2 - horizontalPadding
            val horizontalRightEnd = size.width - horizontalPadding
            val verticalTopStart = 0f
            val verticalTopEnd = cornerSize * 2
            val verticalBottomStart = size.height - cornerSize * 2
            val verticalBottomEnd = size.height
            val topStartCorner =
                Rect(horizontalLeftStart, verticalTopStart, horizontalLeftEnd, verticalTopEnd)
            val topEndCorner =
                Rect(horizontalRightStart, verticalTopStart, horizontalRightEnd, verticalTopEnd)
            val bottomEndCorner = with(
                Rect(
                    horizontalRightStart,
                    verticalBottomStart,
                    horizontalRightEnd,
                    verticalBottomEnd
                )
            ) {
                if (bubbleType == BubbleType.Outgoing) {
                    Rect(
                        horizontalRightEnd,
                        size.height - horizontalPadding * 2,
                        horizontalRightEnd + horizontalPadding * 2,
                        size.height
                    )
                } else {
                    this
                }
            }
            val bottomStartCorner = with(
                Rect(
                    horizontalLeftStart,
                    verticalBottomStart,
                    horizontalLeftEnd,
                    verticalBottomEnd
                )
            ) {
                if (bubbleType == BubbleType.Incoming) {
                    Rect(
                        horizontalLeftStart - horizontalPadding * 2,
                        size.height - horizontalPadding * 2,
                        horizontalLeftStart,
                        size.height
                    )
                } else {
                    this
                }
            }
            moveTo(horizontalLeftStart, verticalTopEnd)
            arcTo(topStartCorner, 180f, 90f, forceMoveTo = false)
            arcTo(topEndCorner, -90f, 90f, forceMoveTo = false)
            arcTo(
                bottomEndCorner,
                if (bubbleType == BubbleType.Incoming) 0f else 180f,
                if (bubbleType == BubbleType.Incoming) 90f else -90f,
                forceMoveTo = false
            )
            arcTo(
                bottomStartCorner,
                if (bubbleType == BubbleType.Incoming) 90f else 90f,
                if (bubbleType == BubbleType.Incoming) -90f else 90f,
                forceMoveTo = false
            )
            close()
        }
    )
}
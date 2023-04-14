package cn.tabidachi.electro.icons.rounded

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Icons.Rounded.Resize: ImageVector
    get() {
        if (_resize != null) {
            return _resize!!
        }
        _resize = ImageVector.Builder(name = "Resize", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(3.0f, 13.0f)
                verticalLineTo(11.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(13.0f)
                close()
                moveTo(3.0f, 9.0f)
                verticalLineTo(7.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(9.0f)
                close()
                moveTo(7.0f, 5.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(9.0f)
                verticalLineTo(5.0f)
                close()
                moveTo(11.0f, 21.0f)
                verticalLineTo(19.0f)
                horizontalLineTo(13.0f)
                verticalLineTo(21.0f)
                close()
                moveTo(11.0f, 5.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(13.0f)
                verticalLineTo(5.0f)
                close()
                moveTo(15.0f, 21.0f)
                verticalLineTo(19.0f)
                horizontalLineTo(17.0f)
                verticalLineTo(21.0f)
                close()
                moveTo(19.0f, 17.0f)
                verticalLineTo(15.0f)
                horizontalLineTo(21.0f)
                verticalLineTo(17.0f)
                close()
                moveTo(19.0f, 13.0f)
                verticalLineTo(11.0f)
                horizontalLineTo(21.0f)
                verticalLineTo(13.0f)
                close()
                moveTo(20.0f, 9.0f)
                quadTo(19.575f, 9.0f, 19.288f, 8.712f)
                quadTo(19.0f, 8.425f, 19.0f, 8.0f)
                verticalLineTo(5.0f)
                quadTo(19.0f, 5.0f, 19.0f, 5.0f)
                quadTo(19.0f, 5.0f, 19.0f, 5.0f)
                horizontalLineTo(16.0f)
                quadTo(15.575f, 5.0f, 15.288f, 4.712f)
                quadTo(15.0f, 4.425f, 15.0f, 4.0f)
                quadTo(15.0f, 3.575f, 15.288f, 3.287f)
                quadTo(15.575f, 3.0f, 16.0f, 3.0f)
                horizontalLineTo(19.0f)
                quadTo(19.825f, 3.0f, 20.413f, 3.587f)
                quadTo(21.0f, 4.175f, 21.0f, 5.0f)
                verticalLineTo(8.0f)
                quadTo(21.0f, 8.425f, 20.712f, 8.712f)
                quadTo(20.425f, 9.0f, 20.0f, 9.0f)
                close()
                moveTo(5.0f, 21.0f)
                quadTo(4.175f, 21.0f, 3.587f, 20.413f)
                quadTo(3.0f, 19.825f, 3.0f, 19.0f)
                verticalLineTo(16.0f)
                quadTo(3.0f, 15.575f, 3.288f, 15.287f)
                quadTo(3.575f, 15.0f, 4.0f, 15.0f)
                quadTo(4.425f, 15.0f, 4.713f, 15.287f)
                quadTo(5.0f, 15.575f, 5.0f, 16.0f)
                verticalLineTo(19.0f)
                quadTo(5.0f, 19.0f, 5.0f, 19.0f)
                quadTo(5.0f, 19.0f, 5.0f, 19.0f)
                horizontalLineTo(8.0f)
                quadTo(8.425f, 19.0f, 8.713f, 19.288f)
                quadTo(9.0f, 19.575f, 9.0f, 20.0f)
                quadTo(9.0f, 20.425f, 8.713f, 20.712f)
                quadTo(8.425f, 21.0f, 8.0f, 21.0f)
                close()
                moveTo(19.0f, 21.0f)
                verticalLineTo(19.0f)
                horizontalLineTo(21.0f)
                quadTo(21.0f, 19.825f, 20.413f, 20.413f)
                quadTo(19.825f, 21.0f, 19.0f, 21.0f)
                close()
                moveTo(3.0f, 5.0f)
                quadTo(3.0f, 4.175f, 3.587f, 3.587f)
                quadTo(4.175f, 3.0f, 5.0f, 3.0f)
                verticalLineTo(5.0f)
                close()
            }
        }
        .build()
        return _resize!!
    }

private var _resize: ImageVector? = null

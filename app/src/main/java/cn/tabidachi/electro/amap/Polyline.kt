package cn.tabidachi.electro.amap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions

internal class PolylineNode(
    val polyline: Polyline,
    var onPolylineClick: (Polyline) -> Unit
) : MapNode {
    override fun onRemoved() {
        polyline.remove()
    }
}

/**
 * 地图线段覆盖物。一个线段是多个连贯点的集合线段。
 *
 * @param points 线段的坐标点列表
 * @param color 线段的颜色
 * @param geodesic 线段是否画大地曲线，默认false，不画大地曲线
 * @param visible 线段的可见属性
 * @param isDottedLine 线段是否虚线，默认为false，画实线
 * @param useGradient 线段是否使用渐变色
 * @param lineCustomTexture 线段的纹理图
 * @param lineJoinType Polyline连接处形状
 * @param lineCapType Polyline尾部形状
 * @param width 线段的宽度
 * @param zIndex 显示层级
 * @param onClick polyline点击事件回调
 */
@Composable
@AMapComposable
fun Polyline(
    points: List<LatLng>,
    color: Color = Color.Black,
    geodesic: Boolean = false,
    visible: Boolean = true,
    isDottedLine: Boolean = false,
    useGradient: Boolean = false,
    lineCustomTexture: BitmapDescriptor? = null,
    lineJoinType: PolylineOptions.LineJoinType = PolylineOptions.LineJoinType.LineJoinBevel,
    lineCapType: PolylineOptions.LineCapType = PolylineOptions.LineCapType.LineCapRound,
    width: Float = 10f,
    zIndex: Float = 0f,
    onClick: (Polyline) -> Unit = {}
) {
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<PolylineNode, MapApplier>(
        factory = {
            val polyline = mapApplier?.map?.addPolyline (
                PolylineOptions().apply {
                    addAll(points)
                    color(color.toArgb())
                    geodesic(geodesic)
                    setDottedLine(isDottedLine)
                    lineJoinType(lineJoinType)
                    lineCapType(lineCapType)
                    customTexture = lineCustomTexture
                    useGradient(useGradient)
                    visible(visible)
                    width(width)
                    zIndex(zIndex)
                }) ?: error("Error adding Polyline")
            PolylineNode(polyline, onClick)
        },
        update = {
            update(onClick) { this.onPolylineClick = it }

            set(points) { this.polyline.points = it }
            set(lineJoinType) { this.polyline.options.lineJoinType(it) }
            set(lineCapType) { this.polyline.options.lineCapType(it) }
            set(color) { this.polyline.color = it.toArgb() }
            set(geodesic) { this.polyline.isGeodesic = it }
            set(isDottedLine) { this.polyline.isDottedLine = it }
            set(lineCustomTexture) { this.polyline.setCustomTexture(it) }
            set(visible) { this.polyline.isVisible = it }
            set(width) { this.polyline.width = it }
            set(zIndex) { this.polyline.zIndex = it }
        }
    )
}